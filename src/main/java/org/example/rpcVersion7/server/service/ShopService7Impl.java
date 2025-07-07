package org.example.rpcVersion7.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion7.common.Shop;
import org.example.rpcVersion7.server.mapper.ShopMapper7;
import org.springframework.stereotype.Service;

@Service
public class ShopService7Impl extends ServiceImpl<ShopMapper7, Shop> implements ShopService7 {
    public ShopService7Impl(ShopMapper7 shopMapper7) {
        this.baseMapper = shopMapper7;
    }
}
