package org.example.rpcVersion7.register;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance {

    @Override
    public Instance Select(List<Instance> instanceList) {
        Random random = new Random();
        int index = random.nextInt(instanceList.size());
        System.out.println("随机负载均衡选择了第" + index + "个服务实例!");
        return instanceList.get(index);
    }
}
