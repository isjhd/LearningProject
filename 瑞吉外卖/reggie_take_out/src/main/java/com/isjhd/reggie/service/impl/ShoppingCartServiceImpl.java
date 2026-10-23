package com.isjhd.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isjhd.reggie.entity.ShoppingCart;
import com.isjhd.reggie.mapper.ShoppingCartMapper;
import com.isjhd.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
