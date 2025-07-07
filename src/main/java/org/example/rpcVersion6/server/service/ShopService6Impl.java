package org.example.rpcVersion6.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion6.common.Shop;
import org.example.rpcVersion6.server.mapper.ShopMapper6;
import org.springframework.stereotype.Service;

@Service
public class ShopService6Impl extends ServiceImpl<ShopMapper6, Shop> implements ShopService6 {
    public ShopService6Impl(ShopMapper6 shopMapper6) {
        this.baseMapper = shopMapper6;
    }
}
