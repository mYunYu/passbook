package com.jju.passbook.service;

import com.jju.passbook.vo.Feedback;
import com.jju.passbook.vo.Response;

/**
 *  评论服务
 */
public interface IFeedbackService {

    /**
     *  创建评论
     * @param feedback      Feedback
     * @return              Response
     */
    Response createFeedback(Feedback feedback);

    /**
     *  获取用户评论
     * @param userId        用户id
     * @return              Response
     */
    Response getFeedback(Long userId);

}
