# rpc简介
本质：客户端像调用本地方法一样调用服务端的服务

要调用远程服务，要知道服务的地址，因此需要有注册中心来进行服务注册和服务发现
然后客户端在调用远程服务时需要传入相应的参数，并且由于是远程调用，因此涉及在网络中进行传输，需要对请求体进行序列化，并且按照相应的协议编码在网络中进行传输，这里面涉及到网络编程的内容

网络编程涉及： 
1. 服务端实例化一个ServerSocket对象，并且运行在指定端口上
2. 服务端调用ServerSocket.accept()函数监听端口
3. 客户端实例化一个Socket对象，并且连接到服务端的地址
4. 根据oos和ois进行相应的数据传输

服务端收到客户端传来的数据后，首先进行反序列化，转换为相应的结构体，然后调用本地方法传参得到输出结果，将结果进行序列化，并按照协议编码后再进行网络传输，返回给客户端，整个rpc调用就完成了。

在服务调用的时候可以实现负载均衡
# 0.最简单的rpc调用
在server端有一个用户服务，可以根据id调取数据库中的用户信息，然后client端就调用这个服务进行查找用户的操作
## Server
### UserService
使用mybatis-plus实现UserService，里面有很多方法可以调用，这个版本只调用一个geyById()的方法，根据id获取user信息
```java
public class RPCServer0 {

    @Resource
    private UserService0 userService ;

    public User getUserById(long id){
        return userService.getById(id);
    }

    public void start(){
        try {
            ServerSocket serverSocket = new ServerSocket(8899);
            System.out.println("服务端启动了！运行在: " + serverSocket.getLocalSocketAddress());
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("接收到了来自客户端：" + socket.getRemoteSocketAddress() + "的请求！");
                new Thread(() -> {
                    try {
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                        Long id = ois.readLong();
                        User userById = getUserById(id);
                        oos.writeObject(userById);
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("从IO数据流中读取数据错误");
                    }
                }).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器启动失败");
        }
    }
}
```
UserService的具体实现
```java
public class UserService0Impl extends ServiceImpl<UserMapper0, User> implements UserService0 {
    private final UserMapper0 userMapper0;
    public UserService0Impl(UserMapper0 userMapper0) {
        this.userMapper0 = userMapper0;
    }
}

```
## Client
客户端实例化一个Socket对象，并且连接到服务器，然后传递id得到返回的User信息。
```java
public class RPCClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 8899);

            System.out.println("客户端运行在: " + socket.getLocalSocketAddress());

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeLong(1L);
            oos.flush();

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            User user = (User) ois.readObject();
            System.out.println("服务端返回的user信息：" + user);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("客户端启动失败！");
        }

    }
}
```
## 总结
最简单的rpc服务，有如下问题亟待解决：
- 需要统一发送和返回消息的格式，支持传递多种参数和返回多种类型值
- 解决调用多种方法的问题

# 1.版本1
## 本版本要解决的问题
- 如何对同一个接口下的多个方法进行调用
- 对不同方法如何传递不同参数
- 如何获得不同方法返回的不同类型值
- 
## 更新内容

1. 定义统一的请求体格式
```java
public class RPCRequest {
    /*
    包含了请求的接口、方法、参数和参数类型
     */
    private String interfaceName;
    private String methodName;
    private Object[] params;
    private Class<?>[] paramTypes;
}
```
2. 定义统一的返回体格式
```java
public class RPCResponse implements Serializable {
    /*
    包含一些状态信息和数据
     */
    int code;
    String message;

    Object data;

    public static RPCResponse success(Object data) {
        return RPCResponse.builder().code(200).message("success").data(data).build();
    }
    public static RPCResponse fail() {
        return RPCResponse.builder().code(500).message("fail").build();
    }
}
```
3. 使用动态代理封装不同方法的request
要向服务器发送request，需要获得要调用的接口、方法、参数和参数类型进行request的封装，因此需要使用动态代理的方式在调用某个具体的方法时，获取到这些信息并进行相应request的封装

client代理的具体实现如下所示：

```java
@AllArgsConstructor
public class ClientProxy implements InvocationHandler {
    private String host;
    private int port;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RPCRequest request = RPCRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RPCResponse response = IOClient.sendRequest(host, port, request);
        return response.getData();
    }

    <T>T createProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
    }
}
```

解耦一下client，将原来发送请求的过程使用IOClient来进行：

```java
public class IOClient {
    public static RPCResponse sendRequest(String host, int port, RPCRequest request) {

        try {
            Socket socket = new Socket(host, port);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(request);
            oos.flush();

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            RPCResponse response = (RPCResponse) ois.readObject();
            System.out.println("服务端返回的信息：" + response);
            return response;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
```
## 总结
- 该版本的代码定义了更通用的消息格式，可以调用不同的方法并且传入不同参数，得到不同类型的返回值
- 使用了动态代理进行不同方法request的封装
- 对客户端代码进行解耦，可以传入host和port实现对不同服务器的连接

# 2.版本2
## 本版本要解决的问题
- 服务端提供了多个service，如何调用不同service接口的方法
- 对服务端代码进行解耦
## 准备工作
新建一个shopService

```java
@Service
public class ShopService2Impl extends ServiceImpl<ShopMapper2, Shop> implements ShopService2{
    public ShopService2Impl(ShopMapper2 shopMapper2) {
        this.baseMapper = shopMapper2;
    }
}
```
## 更新内容

1. 将服务端处理客户端请求的代码利用线程类进行解耦

首先接收客户端的请求，接着运行反射调用服务端方法获取返回值
```java
public class WorkThread implements Runnable {

    private Socket socket;
    private ServiceProvider serviceProvider;

    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            RPCRequest request = (RPCRequest) ois.readObject();
            System.out.println("读取到来自客户端的请求信息：" + request);
            RPCResponse response = getResponse(request);

            oos.writeObject(response);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("从IO数据流中读取数据错误");
        }
    }

    public RPCResponse getResponse(RPCRequest request) {
        try {
            Object service = serviceProvider.getService(request.getInterfaceName());
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            Object response = method.invoke(service, request.getParams());
            return RPCResponse.success(response);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("方法执行不成功！");
            return RPCResponse.fail();
        }
    }
}
```
2. 服务端定义一个ServiceProvider类进行接口和实现类的映射
```java
public class ServiceProvider {
    /*
    包含接口和实现类的映射关系
     */
    private Map<String, Object> interfaceServices;

    public ServiceProvider() {
        this.interfaceServices = new HashMap<>();
    }

    public void registerService(Object service) {
        Class<?> targetClass = AopUtils.getTargetClass(service);
        Class<?>[] interfaces = targetClass.getInterfaces();

        for (Class<?> interfaceClass : interfaces) {
            this.interfaceServices.put(interfaceClass.getName(), service);
        }
    }

    public Object getService(String interfaceName) {
        return this.interfaceServices.get(interfaceName);
    }
}
```
这里要注意进行服务和接口映射的时候，不能直接通过`service.getClass().getInterfaces();`获得服务实现的接口类，因为service是一个被spring代理的bean对象，传递过来的是代理类，
因此需要通过`Class<?> targetClass = AopUtils.getTargetClass(service);`获得目标类，然后再获得接口。

3. 启动服务端的代码更新如下：
```java
public class ThreadPoolRPCServer implements RPCServer2{
    private final ThreadPoolExecutor THREAD_POOL_EXECUTOR;
    private ServiceProvider serviceProvider;

    @Resource
    private UserService2 userService2;

    @Resource
    private ShopService2 shopService2;


   public ThreadPoolRPCServer() {
    THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(), 1000, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy()
    );

    this.serviceProvider = new ServiceProvider();
   }



    @Override
    public void start(int port){
        serviceProvider.registerService(userService2);
        serviceProvider.registerService(shopService2);
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("服务端启动了！运行在: " + serverSocket.getLocalSocketAddress());
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("接收到了来自客户端：" + socket.getRemoteSocketAddress() + "的请求！");
                THREAD_POOL_EXECUTOR.execute(new WorkThread(socket, serviceProvider));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器启动失败");
        }
    }

    @Override
    public void stop() {

    }
}
```
利用线程池技术在监听到客户端请求的时候开启一个独立线程去处理，这里需要注意的是，serviceProvider这个map的赋值过程只能在start函数中去做，
ThreadPoolRPCServer的构造函数中只能进行初始化，因为对serviceProvider的赋值需要用到userService和shopService，这两个bean利用`@Resource`注解进行依赖注入，
而spring的依赖注入只能发生在实例化对象中，如果在构造函数中进行`serviceProvider.registerService(userService2)`的注册，会产生空指针报错的情况，因为此时
userService2还没有被依赖注入。

4. 当客户端调用方法时，需要将方法对应的接口类、方法和参数及参数类型封装在RPCRequest中，这时需要根据被调用的方法，找到其对应的接口。
此时需要注意的是，我们调用的方法都是mybatis-plus下IRepository中的方法，在重写代理类的invoke方法时，不能直接通过`method.getDeclaringClass().getName()`获得接口名字
需要先由proxy获得目标类UserService2，得到目标类实现的接口UserService2，然后再寻找一下该接口中是不是有被调用的方法，如果有，则该接口名称即为请求体中的接口名称。
```java
@Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?>[] interfaces = AopUtils.getTargetClass(proxy).getInterfaces();
        String interfaceName = "";
        for (Class<?> iface : interfaces) {
            try {
                Method m = iface.getMethod(method.getName(), method.getParameterTypes());
                interfaceName = iface.getName();
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodException(interfaceName + "." + method.getName());
            }
        }
        RPCRequest request = RPCRequest.builder()
                .interfaceName(interfaceName)
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RPCResponse response = IOClient.sendRequest(host, port, request);
        return response.getData();
    }
```
## 总结
此版本的RPC，重构了服务端的代码，并添加了线程池以提升性能；通过代理实现了调用不同服务的方法。
但有一个痛点是，服务端采用传统的BIO方式进行传输，网络传输性能低下

# 3.版本3
## 要解决的问题

- 使用NIO方式进行网络传输
- 实现长连接和长度前置协议

## 更新
### 服务端
```java
public void start(int port) throws IOException, ClassNotFoundException {
    serviceProvider.registerService(userService3);
    serviceProvider.registerService(shopService3);

    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    SocketAddress socketAddress = new InetSocketAddress(port);
    serverSocketChannel.socket().bind(socketAddress);

    Selector selector = Selector.open();
    serverSocketChannel.configureBlocking(false);
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

    while (true) {
        // 判断是否有事件发生
        if (selector.select(1000) == 0) {
            System.out.println(Thread.currentThread().getName() + "等待客户端连接ing");
            continue;
        }
        // 如果有事件发生，则获取到所有的selectionKey
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            // 当发生的事件是连接事件，则获取连接的客户端channel，并且注册到selector中
            if(key.isAcceptable()) {
                // 获取关联的socketChannel
                SocketChannel clientChannel = serverSocketChannel.accept();
                // 这里注意调用register之前，必须将socketChannel设置为非阻塞状态
                clientChannel.configureBlocking(false);

                // 注册读事件，并绑定一个AtomicBoolean标志，初始为false
                AtomicBoolean processing = new AtomicBoolean(false);
                clientChannel.register(selector, SelectionKey.OP_READ, processing);
                System.out.println(Thread.currentThread().getName() + "客户端连接：" + clientChannel.getRemoteAddress());

            } else if (key.isReadable()) {
                // 当通道内有数据可读时，先检查一下是否有线程已经在处理这个通道了
                AtomicBoolean processing = (AtomicBoolean) key.attachment();
                // processing为false时，新建一个线程去处理
                if(processing.compareAndSet(false, true)) {
                    // 当发生的事件为读取数据事件时，就反向获取关联的socketChannel
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    THREAD_POOL_EXECUTOR.execute(() -> {
                        System.out.println(Thread.currentThread().getName() + "start to process clientChannel");
                        try {
                            new WorkThread(clientChannel, serviceProvider).run();
                            key.cancel();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            processing.set(false);
                        }
                    });
                }
                else {
                    System.out.println(Thread.currentThread().getName() + "clientChannel is processing");
                }
            }
            iterator.remove();
        }

    }
}


```

服务端使用selector监听事件，当有客户端进行连接时，会触发isAcceptable事件，在其中new一个clientChannel来连接客户端对应的socketChannel，
当客户端往clientChannel中发送请求时，会触发isReadable事件，表示通道可读，此时会在线程池中新建一个线程来处理客户端的该次请求。
这里面要注意的是当新创建的线程还没有处理完通道中的数据时，此时仍会触发可读事件，所以就会出现多线程同时处理一个通道的问题。因此需要有一个processing状态来避免重复提交多个线程，
这里用AtomicBoolean来实现。

这里有一个坑，按理说处理请求的线程完成后，就不会再触发可读事件了，但其实在客户端进行`socketChannel.close()`之后也会触发一次可读事件，此时在服务端进行
`clientChannel.read()`函数，会返回-1，表示客户端主动关闭连接了，但是这还是会额外建立一个线程，因此需要在WorkThread执行完后，使用`key.cancel()`来取消selector对当前key的监听，这样就可以避免
在关闭的时候还创建一个额外线程。

### 客户端
```java
public static RPCResponse sendRequest(String host, int port, RPCRequest request) {

        try {
            // 连接服务端，new一个socketChannel
            SocketChannel socketChannel = SocketChannel.open();
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socketChannel.connect(socketAddress);

            // 将request请求体序列化
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(request);
            oos.flush();

            // 发送请求体长度 + 具体数据
            byte[] writeByteArray = baos.toByteArray();
            ByteBuffer writeBuffer = ByteBuffer.allocate(4 + writeByteArray.length);
            writeBuffer.putInt(writeByteArray.length);
            writeBuffer.put(writeByteArray);
            writeBuffer.flip();

            int totalWritten = 0;
            while (writeBuffer.hasRemaining()) {
                int written = socketChannel.write(writeBuffer);
                System.out.println("本次写入字节数：" + written);
                if (written <= 0) {
                    System.out.println("写入被阻塞或连接关闭");
                    break;
                }
                totalWritten += written;
            }
            System.out.println("写入完成，总字节数：" + totalWritten);

            // 接收响应体长度 + 具体数据
            // 先读取响应体长度
            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            while (lengthBuffer.hasRemaining()) {
                int read = socketChannel.read(lengthBuffer);
                if (read == -1) {
                    throw new IOException("Server Socket closed");
                }
            }
            lengthBuffer.flip();
            int length = lengthBuffer.getInt();
            if (length <= 0) {
                throw new IOException("Invalid response length: " + length);
            }

            ByteBuffer readBuffer = ByteBuffer.allocate(length);
            while (readBuffer.hasRemaining()) {
                int read = socketChannel.read(readBuffer);
                if (read == -1) {
                    throw new IOException("Server Socket closed");
                }
            }
            readBuffer.flip();
            ByteArrayInputStream bais = new ByteArrayInputStream(readBuffer.array());
            ObjectInputStream ois = new ObjectInputStream(bais);
            RPCResponse response = (RPCResponse) ois.readObject();
            System.out.println("服务端返回的信息：" + response);

            socketChannel.close();
            return response;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
```

采取消息长度前置协议，先发消息的长度，然后再发送消息体，接收消息的时候也要分两个buffer去接收。

# 4.版本4
- 使用netty高性能网络框架进行传输

## 本节要解决的问题
- 使用netty来提升网络传输的性能，netty是一个java nio的高性能框架，封装了nio底层的通信逻辑

## 更新内容
1. pom.xml文件中引入netty
```xml
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
        <version>4.1.51.Final</version>
    </dependency>
```
2. 重构客户端代码
无论使用bio、nio还是netty方式进行网络传输，RPCClient都需要实现sendrequest函数，并且在代理类中调用这个sendrequest函数去传递请求参数，
因此需要定义一个接口：RPCClient，实现sendrequest方法，就可以使用不同方式进行网络传输。

NettyRPCClient类实现：
```java
public class NettyRPCClient implements RPCClient{

    private static final Bootstrap bootstrap;
    private static final NioEventLoopGroup eventLoopGroup;

    private String host;
    private int port;

    public NettyRPCClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    static {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }

    @Override
    public RPCResponse sendRequest(RPCRequest request) throws InterruptedException {
        try {
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            Channel channel = channelFuture.channel();
            Promise<RPCResponse> promise = new DefaultPromise<>(channel.eventLoop());

            channel.pipeline().addLast(new NettyClientHandler(promise));
            channel.writeAndFlush(request).addListener(future -> {
                if(!future.isSuccess()) {
                    promise.setFailure(future.cause());
                }
            });

            RPCResponse response = promise.await(5, TimeUnit.SECONDS) ? promise.getNow() : null;
            System.out.println("收到来自服务端返回的相应结果：" + response);
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close(){
        eventLoopGroup.shutdownGracefully();
    }
}
```
由于netty是异步传输的通信方式，当向channel中写入request时，函数不会等待写入完毕就立即返回，因此这里添加了一个监听器，来监听写操作的完成结果。
这里采用了promise来接收由服务端响应的结果。
promise表示一个异步操作的可写式未来结果。它的核心作用是在异步操作完成后存储结果（成功或失败），并通知所有关注该结果的代码。
当客户端发送请求后不会阻塞在sendrequest函数中等待response的返回，因此可以通过在客户端的handler中利用promise来接收服务端的response结果，
然后可以通过promise的超时等待机制，在5秒内收到响应后就立即返回response。

NettyClientInitializer类实现：
```java
public class NettyClientInitializer extends ChannelInitializer<NioSocketChannel> {

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4));
        pipeline.addLast(new ObjectEncoder());

        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
            @Override
            public Class<?> resolve(String className) throws ClassNotFoundException {
                return Class.forName(className);
            }
        }));
    }
}
```
initializer类主要用于配置处理器链，通过基于长度字段的协议解析器，解决了TCP的粘包/拆包问题。 整体数据流向如下：
- 出站方向（发送数据）：
  - 业务逻辑：生成RPCResponse对象。
  - ObjectEncoder：将对象序列化为字节数组。
  - LengthFieldPrepender：在字节数组前添加 4 字节的长度字段。
  - 网络发送：完整数据包通过 Socket 发送。
- 入站方向（接收数据）：
  - 网络接收：收到 TCP 数据包（可能包含多个粘包或不完整的拆包）。
  - LengthFieldBasedFrameDecoder：根据长度字段解析出完整消息，跳过长度字段。
  - ObjectDecoder：将字节数组反序列化为RPCResponse对象。
  - 业务逻辑：处理接收到的对象。

NettyClientHandler类实现：
```java
public class NettyClientHandler extends SimpleChannelInboundHandler<RPCResponse> {

    private final Promise<RPCResponse> promise;

    public NettyClientHandler(Promise<RPCResponse> promise){
        this.promise = promise;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponse rpcResponse) throws Exception {
        promise.setSuccess(rpcResponse);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        promise.setFailure(cause);
        cause.printStackTrace();
        ctx.close();
    }
}
```
重写channelRead0方法自定义handler函数，并且在收到服务端返回的结果后通过promise传递给客户端。

3. 重构服务端代码

NettyRPCServer类中start函数的实现：
```java
@Override
public void start(int port) throws IOException, ClassNotFoundException {
    serviceProvider.registerService(userService4);
    serviceProvider.registerService(shopService4);

    NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new NettyServerInitializer(serviceProvider));
        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
        System.out.println("Netty服务端启动了");
        // 死循环监听，一直到通道关闭连接才结束
        channelFuture.channel().closeFuture().sync();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
```
NettyRPCHandler类实现：
```java
@AllArgsConstructor
public class NettyServerHandler extends SimpleChannelInboundHandler<RPCRequest> {
    private ServiceProvider serviceProvider;


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private RPCResponse getResponse(RPCRequest request) {
        try {
            Object service = serviceProvider.getService(request.getInterfaceName());
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            Object response = method.invoke(service, request.getParams());
            return RPCResponse.success(response);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println(Thread.currentThread().getName() + "方法执行不成功！");
            return RPCResponse.fail();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCRequest rpcRequest) throws Exception {
        System.out.println("收到来自客户端的消息： " + rpcRequest);

        try {
            RPCResponse response = getResponse(rpcRequest);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

#:5.版本5
## 要解决的问题
- 实现自定义的消息序列化机制

## 升级思路
之前的版本中使用的都是java自带的序列化机制，并且在服务端进行编码解码时，用的也是netty自带的入站和出站编解码器，消息格式为**消息长度 | 序列化后的数据**。
接下来要自己设置消息协议和格式并编写自定义的编解码器。

自定义的消息格式如表所示

| 消息类型（2Byte）| 序列化方式（2Byte）| 消息长度（4Byte）| 序列化后的数据|
| :---------------- | ---------------- | ---------------- |
| messageType（RPCRequest和RPCResponse）| json or java | 消息长度length| 用不同序列化器序列化后的数据|

## 更新内容
定义一个序列化器，里面有serialize和deserialize两个函数，并且实现jdk原生序列化机制和json序列化机制；由于json对象会丢失
Serializer类
```java
public interface Serializer {
    // 主要有两个功能：
    // 一是将对象序列化成字节数组
    byte[] serialize(Object obj) throws IOException;
    // 二是将字节数组反序列化成消息
    Object deserialize(byte[] bytes, int messageType);

    // 返回使用的序列化器
    int getType();

    static Serializer getSerializerByCode(int code){
        switch(code){
            case 0:
                return new ObjectSerializer();
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }
}
```
通过code来选择使用的序列化器

ObjectSerializer类
```java
public class ObjectSerializer implements Serializer{
    @Override
    public byte[] serialize(Object obj){
        byte[] bytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            bytes = baos.toByteArray();
            oos.close();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            obj = ois.readObject();
            ois.close();
            bais.close();
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return obj;
    }

    @Override
    public int getType() {
        return 0;
    }
}
```

JsonSerializer类
```java
public class JsonSerializer implements Serializer{
    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes = JSONObject.toJSONBytes(obj);
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        switch(messageType){
            case 0:
                RPCRequest request = JSON.parseObject(bytes, RPCRequest.class);
                if(request.getParams() == null) return request;

                Object[] objects = new Object[request.getParams().length];
                for(int i = 0; i < objects.length; i ++){
                    // 获得方法的参数类型
                    Class<?> paramType = request.getParamTypes()[i];
                    // 检验实际传过来的参数和方法定义的参数类型是否兼容
                    if(!paramType.isAssignableFrom(request.getParams()[i].getClass())){
                        // 不兼容的话进行类型转换
                        objects[i] = JSONObject.toJavaObject((JSONObject) request.getParams()[i], paramType);
                    }
                    else {
                        objects[i] = request.getParams()[i];
                    }
                }
                request.setParams(objects);
                obj = request;
                break;
            case 1:
                RPCResponse response = JSON.parseObject(bytes, RPCResponse.class);
                Class<?> dataType = response.getDataType();
                if(!dataType.isAssignableFrom(response.getData().getClass())){
                    response.setData(JSONObject.toJavaObject((JSONObject)response.getData(), dataType));
                }
                obj = response;
                break;
            default:
                System.out.println("暂不支持这种消息类型");
                throw new RuntimeException();
        }
        return obj;
    }

    @Override
    public int getType() {
        return 1;
    }
}
```

入站编码器类MyDecoder类，主要是将byte字节流转换为对应的消息类型
```java
@AllArgsConstructor
public class MyDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        short messageType = byteBuf.readShort();
        if(messageType != MessageType.REQUEST.getCode() &&
        messageType != MessageType.RESPONSE.getCode()){
            System.out.println("暂时不支持这种消息类型的解码");
            return;
        }

        short serializerType = byteBuf.readShort();
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        if(serializer == null) {
            System.out.println("不存在对应的序列化器");
        }
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        Object deserialize = serializer.deserialize(bytes, messageType);
        list.add(deserialize);
    }
}
```
出站编码器类MyEncoder
```java
public class MyEncoder extends MessageToByteEncoder {
    private Serializer serializer;
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {
        // 写入消息类型
        if(msg instanceof RPCRequest){
            byteBuf.writeShort(MessageType.REQUEST.getCode());
        } else if (msg instanceof RPCResponse) {
            byteBuf.writeShort(MessageType.RESPONSE.getCode());
        }
        // 写入序列化方式
        byteBuf.writeShort(serializer.getType());

        byte[] serialize = serializer.serialize(msg);
        // 写入消息体长度
        byteBuf.writeInt(serialize.length);
        // 写入序列化的数据内容
        byteBuf.writeBytes(serialize);
    }
}
```

最后，将netty服务端和客户端使用自定义的编解码器进行channel pipeline的初始化

netty服务端：
```java
public class NettyServerInitializer extends ChannelInitializer<NioSocketChannel> {
    private ServiceProvider serviceProvider;
    @Override
    protected void initChannel(NioSocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new MyEncoder(new ObjectSerializer()));
        pipeline.addLast(new NettyServerHandler(serviceProvider));
    }
}
```

netty客户端：
```java
public class NettyClientInitializer extends ChannelInitializer<NioSocketChannel> {

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new MyEncoder(new ObjectSerializer()));
    }
}
```

## 总结
在此版本中，设计了自定义的消息格式，并且实现了自定义消息编解码器，支持java原生序列化机制和json序列化机制

# 6.版本6

## 要解决的问题
服务端与客户端通信必须要事先知道host和port，那么如果服务端挂了或者改换地址后，客户端无法及时更新连接地址，导致网络通信的扩展性降低

## 升级思路
使用nacos作为注册中心，服务端在注册中心注册自己的服务，客户端调用服务时直接去注册中心根据服务名找到对应的服务地址

下载Nacos，并根据官网的命令启动起来Nacos服务:https://nacos.io/docs/latest/quickstart/quick-start/?spm=5238cd80.6ed7f605.0.0.1881196bQzMNjp

需要注意的是Nacos运行需要JDK 17+，所以我将项目jdk版本升级到了jdk 21，由此引发了一系列依赖不匹配的问题
请自行解决

java引入Nacos依赖

```xml
<dependency>
  <groupId>com.alibaba.nacos</groupId>
  <artifactId>nacos-client</artifactId>
  <version>3.0.2</version>
</dependency>
```

## 更新内容
1. 引入Nacos作为注册中心

定义了一个服务注册的接口，可以方便接入不同的注册中心。

ServiceRegister的接口：
```java
public interface ServiceRegister {
    void register(String serviceName, InetSocketAddress serverAddr);
    InetSocketAddress serviceDiscovery(String serviceName);
}
```

nacos服务注册的接口实现类：
```java
public class NacosServiceRegister implements ServiceRegister{
    NamingService naming;

    public NacosServiceRegister(){
        try {
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, "127.0.0.1:8848");
            properties.put(PropertyKeyConst.USERNAME, "你的用户名");  // 用户名
            properties.put(PropertyKeyConst.PASSWORD, "你的密码");  // 密码
            this.naming = NacosFactory.createNamingService(properties);
        } catch (NacosException e) {
            e.printStackTrace();
            System.out.println("连接Nacos失败！");
        }
    }

    @Override
    public void register(String serviceName, InetSocketAddress serverAddr) {
        Instance instance = new Instance();
        instance.setIp(serverAddr.getHostName());
        instance.setPort(serverAddr.getPort());
        instance.setClusterName("DEFAULT");
        try {
            naming.registerInstance(serviceName, instance);
        } catch (NacosException e) {
            e.printStackTrace();
            System.out.println("注册服务: " + serviceName + "失败!");
        }
    }

    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            Instance instance = naming.selectOneHealthyInstance(serviceName);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            e.printStackTrace();
            System.out.println("没有" + serviceName + "服务的实例！");
        }
        return null;
    }
}
```

2. 服务端将服务注册到Nacos中

服务端有一个serviceProvider用来映射接口名和方法名，因此需要在映射表里将每个服务注册到nacos中，因此服务端主要是对
ServiceProvider进行改造

将服务端的host和ip告知注册中心

```java
ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1", 9988);
```

ServiceProvider类的更新内容如下：
```java
public class ServiceProvider {
    /*
    包含接口和实现类的映射关系
     */
    private Map<String, Object> interfaceServices;
    private ServiceRegister serviceRegister;

    private String host;

    private int port;

    public ServiceProvider(String host, int port) {
        this.interfaceServices = new HashMap<>();
        this.serviceRegister = new NacosServiceRegister();
        this.host = host;
        this.port = port;
    }

    public void registerService(Object service) {
        Class<?> targetClass = AopUtils.getTargetClass(service);
        Class<?>[] interfaces = targetClass.getInterfaces();
        for (Class<?> interfaceClass : interfaces) {
            this.interfaceServices.put(interfaceClass.getName(), service);
            serviceRegister.register(interfaceClass.getName(), new InetSocketAddress(host, port));
        }
    }

    public Object getService(String interfaceName) {
        return this.interfaceServices.get(interfaceName);
    }
}
```

3. 客户端从Nacos中发现服务进行调用

客户端不需要知道服务端的host和ip
```java
NettyRPCClient nettyRPCClient = new NettyRPCClient();
```

客户端在连接服务端时只需要从nacos中找当前调用的服务，以获得地址和端口号即可，对于Client的代码改造如下：
```java
public class NettyRPCClient implements RPCClient {

    private static final Bootstrap bootstrap;
    private static final NioEventLoopGroup eventLoopGroup;

    private NacosServiceRegister nacos;

    public NettyRPCClient() {
        this.nacos = new NacosServiceRegister();
    }

    static {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }

    @Override
    public RPCResponse sendRequest(RPCRequest request) throws InterruptedException {
        // 从nacos中找当前要调用的服务的地址
        InetSocketAddress address = nacos.serviceDiscovery(request.getInterfaceName());
        String host = address.getHostName();
        int port = address.getPort();
        try {
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            Channel channel = channelFuture.channel();
            Promise<RPCResponse> promise = new DefaultPromise<>(channel.eventLoop());

            channel.pipeline().addLast(new NettyClientHandler(promise));
            channel.writeAndFlush(request).addListener(future -> {
                if(!future.isSuccess()) {
                    promise.setFailure(future.cause());
                }
            });

            RPCResponse response = promise.await(5, TimeUnit.SECONDS) ? promise.getNow() : null;
            System.out.println("收到来自服务端返回的相应结果：" + response);
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close(){
        eventLoopGroup.shutdownGracefully();
    }
}
```

# 7.版本7

## 要解决的问题
- 实现服务端的负载均衡

## 更新内容




