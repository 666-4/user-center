package com.yang.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.usercenter.model.domain.UserTeam;
import com.yang.usercenter.service.UserTeamService;
import com.yang.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【user_team(队伍关系表)】的数据库操作Service实现
* @createDate 2023-08-11 10:07:34
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




