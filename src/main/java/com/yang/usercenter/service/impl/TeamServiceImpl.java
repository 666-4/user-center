package com.yang.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yang.usercenter.common.ErrorCode;
import com.yang.usercenter.constant.UserConstant;
import com.yang.usercenter.exception.BusinessException;
import com.yang.usercenter.model.domain.Team;
import com.yang.usercenter.model.domain.User;
import com.yang.usercenter.model.domain.UserTeam;
import com.yang.usercenter.model.dto.TeamQuery;
import com.yang.usercenter.model.enums.TeamStatusEnum;
import com.yang.usercenter.model.request.TeamJoinRequest;
import com.yang.usercenter.model.request.TeamQuitRequest;
import com.yang.usercenter.model.request.TeamUpdateRequest;
import com.yang.usercenter.model.vo.TeamUserVO;
import com.yang.usercenter.model.vo.UserVO;
import com.yang.usercenter.service.TeamService;
import com.yang.usercenter.mapper.TeamMapper;
import com.yang.usercenter.service.UserService;
import com.yang.usercenter.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Lenovo
 * @description 针对表【team(队伍表)】的数据库操作Service实现
 * @createDate 2023-08-11 10:05:01
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {


    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 保存用户信息
     *
     * @param team
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTeam(Team team, User loginUser) {
        // 1、判断请求参数是否为空
        // 2、判断是否登录
        if (team == null || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final Long userId = loginUser.getId();
        // 3、校验用户提交的信息
        // 3.1、队伍人数 > 1 && <= 10
        Integer maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 0 || maxNum >= 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }
        // 3.2 队伍名称 < 20
        String teamName = team.getTeamName();
        if (!StringUtils.isNotBlank(teamName) || teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不符合要求");
        }
        // 3.3、队伍描述 <= 500
        String teamDescription = team.getTeamDescription();
        if (StringUtils.isNotBlank(teamDescription) && teamDescription.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不符合要求");
        }
        // 3.4、teamStatus 是否公开 不公开 默认就是 0
        Integer teamStatus = Optional.ofNullable(team.getTeamStatus()).orElse(0);
        TeamStatusEnum enumByTagValue = TeamStatusEnum.getEnumByTagValue(teamStatus);
        if (enumByTagValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        // 3.5、如果队伍是加密状态，一定要有密码，且密码 <= 32
        String teamPassword = team.getTeamPassword();
        if (TeamStatusEnum.SECRET.equals(enumByTagValue) && (StringUtils.isBlank(teamPassword) || teamPassword.length() > 32)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
        }
        // 3.6、设置的超时时间要 > 当时时间
        if (new Date().after(team.getExpireTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已失效");
        }
        // 3.7、用户最多只能创建 5 个队伍
        // todo 在并发下有问题，可能超过上限
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("userId", userId);
        int count = this.count(teamQueryWrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多只能创建五支队伍");
        }
        // 4、校验完成、插入信息
        team.setUserId(userId);
        boolean result = this.save(team);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        // 5、插入到关系表 user_team
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(team.getId());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return team.getId();
    }

    @Override
    public List<TeamUserVO> getTeamList(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> teamIds = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(teamIds)) {
                queryWrapper.in("id", teamIds);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("teamName", searchText).or().like("teamDescription", searchText));
            }
            String teamName = teamQuery.getTeamName();
            if (StringUtils.isNotBlank(teamName)) {
                queryWrapper.like("teamName", teamName);
            }
            String teamDescription = teamQuery.getTeamDescription();
            if (StringUtils.isNotBlank(teamDescription)) {
                queryWrapper.like("teamDescription", teamDescription);
            }
            Integer maxNum = teamQuery.getMaxNum();
            //查询最大人数相等
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxMum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            //根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            //根据状态来查询
            Integer teamStatus = teamQuery.getTeamStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByTagValue(teamStatus);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("teamStatus", statusEnum.getValue());
        }
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        //不展示已过期的队伍
        //expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));

//       List<Team> teamList = this.list(queryWrapper);
//       if (CollectionUtils.isEmpty(teamList)) {
//           return new ArrayList<>();
//       }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        //关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 已加入队伍的人数
            Long teamId = team.getId();
            if (teamId == null || teamId <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            //脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setUserVO(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        // 1. 判断请求参数是否为空
        if (teamUpdateRequest == null && loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 查询队伍是否存在
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "该队伍不存在！！");
        }
        // 3. 只有管理员或者队伍的创建者可以修改
        if (loginUser.getId() != teamUpdateRequest.getUserId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // todo 4. 如果用户传入的新值和老值一致，就不用 update 了（可自行实现，降低数据库使用次数）
        // todo 5. **如果队伍状态改为加密，必须要有密码**
        TeamStatusEnum enumByTagValue = TeamStatusEnum.getEnumByTagValue(teamUpdateRequest.getTeamStatus());
        if (enumByTagValue == TeamStatusEnum.SECRET) {
            if (StringUtils.isNotBlank(teamUpdateRequest.getTeamPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须设置密码");
            }
        }
        // 6. 更新成功
        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, team);
        boolean result = this.updateById(team);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        //其他人、未满、未过期，允许加入多个队伍，但是要有个上限 P0
        if (teamJoinRequest == null || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 队伍必须存在，只能加入未满、未过期的队伍
        Long id = teamJoinRequest.getTeamId();
        if (id == null && id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该队伍不存在");
        }
        Team team = getTeamById(id);
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该队伍已过期");
        }

        // 3. 禁止加入私有的队伍
        Integer teamStatus = team.getTeamStatus();
        TeamStatusEnum enumByTagValue = TeamStatusEnum.getEnumByTagValue(teamStatus);
        if (enumByTagValue == TeamStatusEnum.PRIVATE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "私密房间不允许加入");
        }
        // 4. 如果加入的队伍是加密的，必须密码匹配才可以
        String teamPassword = teamJoinRequest.getTeamPassword();
        if (enumByTagValue == TeamStatusEnum.SECRET) {
            if (StringUtils.isBlank(teamPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
            }
            if (!teamPassword.equals(team.getTeamPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不匹配");
            }
        }
        // 最多只能加入5支队伍
        RLock lock = redissonClient.getLock(UserConstant.YANG_USER_JOIN_LOCK);
        try {
            while (true) {

                if (lock.tryLock(0, 30000, TimeUnit.MILLISECONDS)) {
                    Long userId = loginUser.getId();
                    QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("userId", userId);
                    int userJoinTeamNumber = userTeamService.count(queryWrapper);
                    if (userJoinTeamNumber > 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多只能创建5支队伍");
                    }
                    // 2. 不能加入自己的队伍，不能重复加入已加入的队伍（幂等性）
                    queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("userId", userId);
                    queryWrapper.eq("teamId", id);
                    int hasUserJoinTeam = userTeamService.count(queryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已经加入该队伍");
                    }
                    //已加入队伍的人数
                    long teamHasJoinNum = countTeamUserByTeamId(id);
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    // 5. 新增队伍 - 用户关联信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(id);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            // 判断是否是当前线程持有的锁，如果是才释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

        }
    }

    /**
     * 退出队伍
     *
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        //队伍只剩下一个人，解散
        if (teamHasJoinNum == 1) {
            //删除队伍
            this.removeById(teamId);
        } else {
            //队伍至少还剩下两人
            //是队长
            if (team.getUserId() == userId) {
                //把队伍转移给最早加入的用户
                //1.查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                //更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        //移除关系
        return userTeamService.remove(queryWrapper);

    }

    /**
     * 根据队伍Id获取队伍信息
     *
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 删除队伍
     *
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验队伍是否存在
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        // 校验当前用户是不是队长
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作");
        }
        // 删除所有加入队伍的关联信息
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(queryWrapper);
        // 删除队伍
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败！");
        }
        // 删除队伍
        return this.removeById(teamId);
    }


    /**
     * 获取某队伍当前人数
     *
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }
}




