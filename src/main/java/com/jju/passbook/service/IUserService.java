package com.jju.passbook.service;

import com.jju.passbook.vo.Response;
import com.jju.passbook.vo.User;

/**
 *  用户服务
 */
public interface IUserService {

    /**
     *  创建用户
     * @param user      User
     * @return          Response
     * @throws Exception
     */
    Response createUser(User user) throws Exception;

}
