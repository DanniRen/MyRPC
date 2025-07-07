package org.example.rpcVersion7.register;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

public class RoundLoadBalance implements LoadBalance {
    private int chosenIndex = -1;
    @Override
    public Instance Select(List<Instance> instanceList) {
        chosenIndex = (chosenIndex + 1) % instanceList.size();
        return instanceList.get(chosenIndex);
    }
}
