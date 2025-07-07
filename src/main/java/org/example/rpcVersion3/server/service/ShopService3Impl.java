package org.example.rpcVersion3.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion3.common.Shop;
import org.example.rpcVersion3.server.mapper.ShopMapper3;
import org.springframework.stereotype.Service;

@Service
public class ShopService3Impl extends ServiceImpl<ShopMapper3, Shop> implements ShopService3 {
    public ShopService3Impl(ShopMapper3 shopMapper3) {
        this.baseMapper = shopMapper3;
    }
}
