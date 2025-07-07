package org.example.rpcVersion7.register;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

public interface LoadBalance {
    Instance Select(List<Instance> instanceList);
}
