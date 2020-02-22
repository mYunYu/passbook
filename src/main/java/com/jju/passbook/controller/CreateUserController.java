package com.jju.passbook.controller;

import com.jju.passbook.log.LogConstants;
import com.jju.passbook.log.LogGenerator;
import com.jju.passbook.service.IUserService;
import com.jju.passbook.vo.Response;
import com.jju.passbook.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 *  创建用户服务
 */
@Slf4j
@RestController
@RequestMapping("/passbook")
public class CreateUserController {

    //创建用户服务
    private final IUserService userService;

    private final HttpServletRequest httpServletRequest;

    @Autowired
    public CreateUserController(IUserService userService, HttpServletRequest httpServletRequest) {
        this.userService = userService;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     *  创建用户
     * @param user
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/createUser")
    Response createUser(@RequestBody User user) throws Exception {
        LogGenerator.genLog(
                httpServletRequest,
                -1L, LogConstants.ActionName.CREATE_USER,
                user
        );
        return userService.createUser(user);
    }



}
