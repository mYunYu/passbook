package com.jju.passbook.service;

import com.jju.passbook.vo.Pass;
import com.jju.passbook.vo.Response;

/**
 *  获取用户优惠券信息
 */
public interface IUserPassService {

    /**
     *  获取用户未过期的优惠券信息，即我的优惠券功能实现
     * @param userId        用户id
     * @return              Response
     * @throws Exception
     */
    Response getUserPassInfo(Long userId) throws Exception;

    /**
     *  获取用户已经消费的优惠券，即已使用的优惠券功能实现
     * @param userId        用户id
     * @return              Response
     * @throws Exception
     */
    Response getUserUsedPassInfo(Long userId) throws Exception;

    /**
     *  获取用户所有的优惠券
     * @param userId        用户id
     * @return              Response
     * @throws Exception
     */
    Response getUserAllPassInfo(Long userId) throws Exception;

    /**
     *  用户使用优惠券
     * @param pass          Pass
     * @return              Response
     */
    Response userUsePass(Pass pass);

}
