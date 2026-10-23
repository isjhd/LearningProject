package com.isjhd.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.isjhd.reggie.entity.AddressBook;
import com.isjhd.reggie.mapper.AddressBookMapper;
import com.isjhd.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
