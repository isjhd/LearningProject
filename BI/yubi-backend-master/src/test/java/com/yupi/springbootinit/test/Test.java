package com.yupi.springbootinit.test;

import com.yupi.springbootinit.model.enums.UserRoleEnum;

public class Test {
    public static void main(String[] args) {
        UserRoleEnum user = UserRoleEnum.getEnumByValue("用户");
        System.out.println(user);
    }
}
