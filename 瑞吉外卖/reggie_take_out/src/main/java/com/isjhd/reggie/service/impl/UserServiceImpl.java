package com.isjhd.reggie.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isjhd.reggie.entity.User;
import com.isjhd.reggie.mapper.UserMapper;
import com.isjhd.reggie.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
