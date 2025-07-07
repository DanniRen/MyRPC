import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.*;
import java.util.Arrays;
import java.util.List;

// 测试对象定义
@AllArgsConstructor
@Data
@NoArgsConstructor
class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int age;
    private List<String> hobbies;
    // 构造函数、getter/setter 省略
}

// 性能测试代码
public class SerializationBenchmark {
    public static void main(String[] args) throws Exception {
        User user = new User("张三", 25, Arrays.asList("阅读", "编程", "跑步"));
        int loopCount = 100000; // 循环10万次

        // JDK 序列化性能测试
        long jdkStart = System.nanoTime();
        for (int i = 0; i < loopCount; i++) {
            byte[] data = jdkSerialize(user);
            User result = (User) jdkDeserialize(data);
        }
        long jdkTime = System.nanoTime() - jdkStart;

        // Protostuff 序列化性能测试（需引入依赖）
        // <dependency>
        //     <groupId>com.dyuproject.protostuff</groupId>
        //     <artifactId>protostuff-core</artifactId>
        //     <version>1.1.3</version>
        // </dependency>
        // <dependency>
        //     <groupId>com.dyuproject.protostuff</groupId>
        //     <artifactId>protostuff-runtime</artifactId>
        //     <version>1.1.3</version>
        // </dependency>

        long protoStart = System.nanoTime();
        for (int i = 0; i < loopCount; i++) {
            byte[] data = protostuffSerialize(user);
            User result = protostuffDeserialize(data, User.class);
        }
        long protoTime = System.nanoTime() - protoStart;

        System.out.println("JDK 序列化耗时: " + jdkTime / 1000000 + " ms");
        System.out.println("Protostuff 序列化耗时: " + protoTime / 1000000 + " ms");
        System.out.println("性能差距: " + (double) jdkTime / protoTime + " 倍");
    }

    // JDK 序列化/反序列化工具方法
    private static byte[] jdkSerialize(Object obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        }
    }

    private static Object jdkDeserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }
    }

    // Protostuff 序列化/反序列化工具方法（简化实现）
    private static byte[] protostuffSerialize(Object obj) {
        Schema schema = RuntimeSchema.getSchema(obj.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    private static <T> T protostuffDeserialize(byte[] data, Class<T> clazz) {
        T obj;
        try {
            obj = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Schema schema = RuntimeSchema.getSchema(clazz);
        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }
}