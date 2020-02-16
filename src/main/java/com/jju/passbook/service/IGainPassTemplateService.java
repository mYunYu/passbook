package com.jju.passbook.service;

import com.jju.passbook.vo.GainPassTemplateRequest;
import com.jju.passbook.vo.Response;

/**
 *  用户领取优惠券功能实现
 */
public interface IGainPassTemplateService {

    /**
     *  用户领取优惠券
     * @param request           GainPassTemplateRequest
     * @return                  Response
     * @throws Exception
     */
    Response gainPassTemplate(GainPassTemplateRequest request) throws Exception;

}
