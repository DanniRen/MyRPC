package org.example.rpcVersion2.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion2.common.Shop;
import org.example.rpcVersion2.server.mapper.ShopMapper2;
import org.springframework.stereotype.Service;

@Service
public class ShopService2Impl extends ServiceImpl<ShopMapper2, Shop> implements ShopService2{
    public ShopService2Impl(ShopMapper2 shopMapper2) {
        this.baseMapper = shopMapper2;
    }
}
