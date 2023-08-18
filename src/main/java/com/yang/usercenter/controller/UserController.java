package com.yang.usercenter.controller;
/*
 * Author: 咸余杨
 * */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yang.usercenter.common.BaseResponse;
import com.yang.usercenter.common.ErrorCode;
import com.yang.usercenter.common.ResultUtils;
import com.yang.usercenter.exception.BusinessException;
import com.yang.usercenter.mapper.UserMapper;
import com.yang.usercenter.model.domain.User;
import com.yang.usercenter.model.request.UserLoginRequest;
import com.yang.usercenter.model.request.UserRegisterRequest;
import com.yang.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.yang.usercenter.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000/"},allowCredentials = "true")
public class UserController {

    @Resource
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Integer result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    // 获取当前登录的用户
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        //获取当前用户的登录状态
        User objectUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if(objectUser == null) {
            throw new BusinessException(ErrorCode.NOT_NULL,"用户未登录");
        }
        Long userId = objectUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);

    }

    @GetMapping("/search")
    public BaseResponse<List<User>> selectUsers(String username, HttpServletRequest request) {
        // 鉴权
        // 检验身份是否为管理员
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userLists = userService.list(queryWrapper);
        List<User> safetyUserLists = userLists.stream().map(user -> userService.getSafetyUser(user))
                .collect(Collectors.toList());
        return ResultUtils.success(safetyUserLists);

    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteByUserIds(@RequestBody long userId, HttpServletRequest request) {

        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (userId <= 1) {
            throw new BusinessException(ErrorCode.NOT_NULL);
        }
        boolean b = userService.removeById(userId);
        return ResultUtils.success(b);
    }




    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }



    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request) {
        // 判空
        if(user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录的用户信息
        User currentLoginUser = userService.getCurrentLoginUser(request);
        int result = userService.updateUser(user, currentLoginUser);

        return ResultUtils.success(result);
    }


    /**
     * 推荐用户
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<List<User>> recommendUsers(int pageSize,int pageNum,HttpServletRequest request){
        if(request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.recommendUsers(pageSize,pageNum,request));
    }

    /**
     * 获取和登录兴趣相同的用户
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(int num,HttpServletRequest request) {
        if(num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentLoginUser(request);
        List<User> userList = userService.matchUsers(num,loginUser);
        return ResultUtils.success(userList);
    }

}
