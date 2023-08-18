package com.yang.usercenter.utils;
/*
 * Author: 咸余杨
 * */


import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImportXingQiuUser {

    public static void main(String[] args) {
        String fileName = "D:\\大三\\code\\user-center\\src\\main\\resources\\prodExcel.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<DemoData> list = EasyExcel.read(fileName).head(DemoData.class).sheet().doReadSync();
        System.out.println("总数：" + list.size());

        Map<String, List<DemoData>> listMap = list.stream().filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                .collect(Collectors.groupingBy(DemoData::getUsername));
        for (List<DemoData> value : listMap.values()) {
            System.out.println(value);
        }
        System.out.println("不重复的昵称数：" + listMap.size());
    }
}
