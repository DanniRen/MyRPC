package org.example.rpcVersion4.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion4.common.Shop;
import org.example.rpcVersion4.server.mapper.ShopMapper4;
import org.springframework.stereotype.Service;

@Service
public class ShopService4Impl extends ServiceImpl<ShopMapper4, Shop> implements ShopService4 {
    public ShopService4Impl(ShopMapper4 shopMapper4) {
        this.baseMapper = shopMapper4;
    }
}
