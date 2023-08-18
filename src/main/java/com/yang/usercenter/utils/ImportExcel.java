package com.yang.usercenter.utils;
/*
 * Author: 咸余杨
 * */


import cn.hutool.json.JSON;
import com.alibaba.excel.EasyExcel;

import java.io.File;
import java.util.List;


/**
 * 导入excel数据
 */
public class ImportExcel {


    public static void main(String[] args) {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        String fileName = "D:\\大三\\code\\user-center\\src\\main\\resources\\mapper\\user.xlsx";
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
//        readByListerner(fileName);
        synchronousRead(fileName);
    }

    private static void readByListerner(String fileName) {
        EasyExcel.read(fileName, DemoData.class, new DemoDataListener()).sheet().doRead();
    }

    /**
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */

    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<DemoData> list = EasyExcel.read(fileName).head(DemoData.class).sheet().doReadSync();
        for (DemoData data : list) {
            System.out.println(data);
        }
    }
}

