package org.example.rpcVersion5.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion5.common.Shop;
import org.example.rpcVersion5.server.mapper.ShopMapper5;
import org.springframework.stereotype.Service;

@Service
public class ShopService5Impl extends ServiceImpl<ShopMapper5, Shop> implements ShopService5 {
    public ShopService5Impl(ShopMapper5 shopMapper5) {
        this.baseMapper = shopMapper5;
    }
}
