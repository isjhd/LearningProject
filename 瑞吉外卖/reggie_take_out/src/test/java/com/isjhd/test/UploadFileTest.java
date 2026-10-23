package com.isjhd.test;

import org.junit.jupiter.api.Test;

import java.io.File;

public class UploadFileTest {

    @Test
    public void test1() {
        String fileName = "error.txt";
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        //创建一个目录对象
        File dir = new File("D:\\project\\take-out\\img\\");
        //判断当前目录是否存在
        if (!dir.exists()) {
            //目录不存在，需要创建
            dir.mkdirs();
        }

        System.out.println(dir);
    }

}
