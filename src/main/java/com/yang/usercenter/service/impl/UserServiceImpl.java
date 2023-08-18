package com.yang.usercenter.service.impl;

import cn.hutool.core.codec.Base64;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yang.usercenter.common.ErrorCode;
import com.yang.usercenter.common.ResultUtils;
import com.yang.usercenter.constant.UserConstant;
import com.yang.usercenter.exception.BusinessException;
import com.yang.usercenter.mapper.UserMapper;
import com.yang.usercenter.model.domain.User;
import com.yang.usercenter.service.UserService;
import com.yang.usercenter.utils.AlgorithmLength;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.FilterableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yang.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.yang.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author Lenovo
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2023-07-28 16:56:26
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;
    //2.密码加密
    private static final String SLAT = "yang";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //1、注册逻辑校验
        // 非空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数为空");
        }
        // 账号不小于 4 位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        // 密码不小于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        // 星球编号长度不能大于5
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账号不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已存在");
        }
        // 星球编号不能重复
        QueryWrapper<User> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("planetCode", planetCode);
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该星球编号已存在");
        }
        String encodePassword = Base64.encode(SLAT + userPassword);

        //3.向数据库插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encodePassword);
        user.setPlanetCode(planetCode);
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.判断逻辑
        // 非空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数为空");
        }
        // 账户不小于 4 位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        // 密码不小于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }

        String encodePassword = Base64.encode(SLAT + userPassword);

        //3.查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encodePassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login fail,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "该账号不存在！");
        }
        // 脱敏
        User safetyUser = getSafetyUser(user);
        // 记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    @Override
    public User getSafetyUser(User user) {
        if (user == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setProfile(user.getProfile());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setTags(user.getTags());
        safetyUser.setPlanetCode(user.getPlanetCode());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setCreateTime(user.getCreateTime());
        return safetyUser;
    }

    @Override
    public Integer userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户   内存查询
     *
     * @param tagNameList 标签json列表
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = this.list();
        // 过滤
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            if (tagsStr == null) {
                return false;
            }
            // 使用Gson将json字符串转化为String
            Gson gson = new Gson();
            Set<String> tagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            for (String tagName : tagNameList) {
                if (!tagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 获取当前登录的用户信息
     *
     * @param request
     * @return
     */
    @Override
    public User getCurrentLoginUser(HttpServletRequest request) {
        // 判空
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据session获取用户信息
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_NULL);
        }
        return loginUser;
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @param userLogin
     * @return
     */
    @Override
    public int updateUser(User user, User userLogin) {
        // 判空
        Long userId = user.getId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果用户没有传递任何要更新的值，就直接报错，不用执行更新

        // 校验权限
        // 管理员可以更新任意信息
        // 用户只能更新自己的信息
        if (!isAdmin(userLogin) && !userId.equals(userLogin.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 返回数据
        User oldUser = this.getById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.NO_UPDATE, "更新数据失败！");
        }
        return result;
    }

    /**
     * 判断是否是管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 鉴权
        // 检验身份是否为管理员
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null || user.getUserRole() != ADMIN_ROLE) {
            return false;
        }
        return true;
    }

    /**
     * 判断该用户是否是管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 推荐用户查询
     *
     * @param pageSize
     * @param pageNum
     * @param request
     * @return
     */
    @Override
    public List<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断缓存中是否存在，存在就直接查询缓存返回
        User userLogin = getCurrentLoginUser(request);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String redisRecommendId = UserConstant.YANG_USER_RECOMMEND + userLogin.getId();
        List<User> userList = (List<User>) valueOperations.get(redisRecommendId);
        if (userList != null) {
            return userList;
        }
        // 缓存不存在，则查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userPage = new Page<>(pageNum, pageSize);
        Page<User> pageCurrent = page(userPage, queryWrapper);
        List<User> records = pageCurrent.getRecords();
        List<User> recommendUserList = records.stream().map(this::getSafetyUser).collect(Collectors.toList());
        // 存入缓存
        try {
            valueOperations.set(redisRecommendId, recommendUserList, 1800, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);

        }
        return recommendUserList;

    }

    /**
     * 获取和自己标签相似的用户
     *
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(int num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 =》 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算出所有和当前用户的相识度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者当前用户为自己，无需比较
            if (StringUtils.isBlank(userTags) || loginUser.getId().equals(user.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmLength.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
            // 按编辑距离由大到小排序
            List<Pair<User, Long>> topUserPairList = list.stream()
                    .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                    .limit(num).collect(Collectors.toList());
            // 原本顺序的 userId 列表
            List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.in("id", userIdList);

            // 1, 3, 2
            // User1、User2、User3
            // 1 => User1, 2 => User2, 3 => User3

            Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                    .map(user1 -> getSafetyUser(user1))
                    .collect(Collectors.groupingBy(User::getId));
            List<User> finalUserList = new ArrayList<>();
            for (Long userId : userIdList) {
                finalUserList.add(userIdUserListMap.get(userId).get(0));
            }
            return finalUserList;

    }

        /**
         * 根据SQL查询
         *
         * @param tagNameLists
         * @return
         */
        @Deprecated
        private List<User> searcherUserByTagsSQL (List < String > tagNameLists) {
            // 判空
            if (tagNameLists == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            // 根据SQL查询
            // 根据标签查询所对应的用户
            // like '' and like ''.....
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            for (String tagNameList : tagNameLists) {
                queryWrapper.like("tags", tagNameList);
            }
            List<User> userList = userMapper.selectList(queryWrapper);
            List<User> safetyUsers = userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
            return safetyUsers;
        }
}




