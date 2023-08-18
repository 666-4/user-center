package com.yang.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yang.usercenter.common.BaseResponse;
import com.yang.usercenter.common.ErrorCode;
import com.yang.usercenter.common.ResultUtils;
import com.yang.usercenter.exception.BusinessException;
import com.yang.usercenter.model.domain.Team;
import com.yang.usercenter.model.domain.User;
import com.yang.usercenter.model.domain.UserTeam;
import com.yang.usercenter.model.dto.TeamQuery;
import com.yang.usercenter.model.request.TeamAddRequest;
import com.yang.usercenter.model.request.TeamJoinRequest;
import com.yang.usercenter.model.request.TeamQuitRequest;
import com.yang.usercenter.model.request.TeamUpdateRequest;
import com.yang.usercenter.model.vo.TeamUserVO;
import com.yang.usercenter.service.TeamService;
import com.yang.usercenter.service.UserService;
import com.yang.usercenter.service.UserTeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import sun.util.resources.cldr.gv.LocaleNames_gv;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Result;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 队伍表(Team)表控制层
 *
 * @author 咸余羊
 * @since 2023-08-11 10:12:50
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:3000/"},allowCredentials = "true")
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;


    /**
     * 保存队伍信息
     * @param teamAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if(teamAddRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        Long result = teamService.addTeam(team, loginUser);
        if(result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存数据失败！");
        }
        return ResultUtils.success(result);
    }

    /**
     * 更新用户信息
     * @param teamUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if(teamUpdateRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if(!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新数据失败！");
        }
        return ResultUtils.success(true);
    }

    /**
     * 全量查询队伍信息
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> getTeamList(TeamQuery teamQuery,HttpServletRequest request) {
        if(teamQuery == null && request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamUserVOList = teamService.getTeamList(teamQuery,isAdmin);
        // 查询加入用户加入的队伍Id
        List<Long> teamIdList = teamUserVOList.stream().map(teamUserVO -> teamUserVO.getId()).collect(Collectors.toList());
        // 判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getCurrentLoginUser(request);
            userTeamQueryWrapper.eq("userId",loginUser.getId());
            userTeamQueryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入队伍Id的集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamUserVOList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
           throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 3、查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamUserVOList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamUserVOList);
    }

    /**
     * 获取我创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> getMyCreateListTeam(TeamQuery teamQuery,HttpServletRequest request) {
        if(teamQuery == null && request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamUserVOList = teamService.getTeamList(teamQuery,true);
        return ResultUtils.success(teamUserVOList);
    }

    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> getMyJoinListTeam(TeamQuery teamQuery,HttpServletRequest request) {
        if(teamQuery == null && request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getCurrentLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 取出不重复的队伍 id
        // teamId userId
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.getTeamList(teamQuery, true);
        return ResultUtils.success(teamList);
    }

    /**
     * 分页查询队伍信息
     * @param teamQuery
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Team>> getTeamListPage(@RequestBody TeamQuery teamQuery) {
        if(teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(team,teamQuery);
        int pageSize = teamQuery.getPageSize();
        int pageNum = teamQuery.getPageNum();
        Page<Team> teamPage = new Page<>(pageNum,pageSize);
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(teamPage, teamQueryWrapper);
        return ResultUtils.success(resultPage);
    }

    /**
     * 根据Id 删除队伍信息
     * @param id
     * @return
     */
    @GetMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteByTeamId(@PathVariable("id") Long id) {
        if(id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.removeById(id);
        if(!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除数据失败！");
        }
        return ResultUtils.success(true);
    }

    /**
     * 根据队伍id查询
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeamByTeamId(long id) {
        if(id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if(team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request) {
        if(teamJoinRequest == null && request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
        if(!result) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(result);
    }

    /**
     * 用户解散队伍
     * @param teamQuitRequest
     * @param request
     * @return
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if(teamQuitRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest,loginUser);
        if(!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody TeamQuitRequest teamQuitRequest,HttpServletRequest request) {
        if(teamQuitRequest == null || request == null ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentLoginUser(request);
        boolean result = teamService.deleteTeam(teamQuitRequest,loginUser);
        if(!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(true);
    }


}

