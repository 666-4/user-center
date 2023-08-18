package com.yang.usercenter.service;

import com.yang.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yang.usercenter.model.domain.User;
import com.yang.usercenter.model.dto.TeamQuery;
import com.yang.usercenter.model.request.TeamJoinRequest;
import com.yang.usercenter.model.request.TeamQuitRequest;
import com.yang.usercenter.model.request.TeamUpdateRequest;
import com.yang.usercenter.model.vo.TeamUserVO;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2023-08-11 10:05:01
*/
public interface TeamService extends IService<Team> {

    /**
     * 保存用户信息
     * @param team
     * @param loginUser
     * @return
     */
    Long addTeam(Team team, User loginUser);


    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> getTeamList(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新用户
     * @param team
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest team, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean deleteTeam(TeamQuitRequest teamQuitRequest, User loginUser);
}
