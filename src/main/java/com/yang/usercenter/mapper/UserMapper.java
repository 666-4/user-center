package com.yang.usercenter.mapper;

import com.yang.usercenter.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Lenovo
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2023-07-28 16:56:26
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




