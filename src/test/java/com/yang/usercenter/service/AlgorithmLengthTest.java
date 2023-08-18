package com.yang.usercenter.service;
/*
 * Author: 咸余杨
 * */


import com.yang.usercenter.utils.AlgorithmLength;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AlgorithmLengthTest {

    @Test
    public void test1() {
        List<String> tag1 = Arrays.asList("java","大一","自动化","男");
        List<String> tag2 = Arrays.asList("java","大二","计算机","男");
        List<String> tag3 = Arrays.asList("java","大二","计算机","女");
        int result1 = AlgorithmLength.minDistance(tag1, tag2);
        int result2 = AlgorithmLength.minDistance(tag2, tag3);
        System.out.println(result1);
        System.out.println(result2);
    }
}
