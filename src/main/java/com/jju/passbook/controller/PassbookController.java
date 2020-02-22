package com.jju.passbook.controller;

import com.jju.passbook.log.LogConstants;
import com.jju.passbook.log.LogGenerator;
import com.jju.passbook.service.IFeedbackService;
import com.jju.passbook.service.IGainPassTemplateService;
import com.jju.passbook.service.IInventoryService;
import com.jju.passbook.service.IUserPassService;
import com.jju.passbook.vo.Feedback;
import com.jju.passbook.vo.GainPassTemplateRequest;
import com.jju.passbook.vo.Pass;
import com.jju.passbook.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 *  Passbook Rest Controller
 */
@Slf4j
@RestController
@RequestMapping("/passbook")
public class PassbookController {

    //用户优惠券服务
    private final IUserPassService userPassService;

    //优惠券库存服务
    private final IInventoryService inventoryService;

    //领取优惠券服务
    private final IGainPassTemplateService gainPassTemplateService;

    //反馈服务
    private final IFeedbackService feedbackService;

    private final HttpServletRequest httpServletRequest;

    @Autowired
    public PassbookController(IUserPassService userPassService, IInventoryService inventoryService, IGainPassTemplateService gainPassTemplateService, IFeedbackService feedbackService, HttpServletRequest httpServletRequest) {
        this.userPassService = userPassService;
        this.inventoryService = inventoryService;
        this.gainPassTemplateService = gainPassTemplateService;
        this.feedbackService = feedbackService;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     *  获取用户个人的优惠券信息
     * @param userId        用户id
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/userPassInfo")
    Response userPassInfo(Long userId) throws Exception {
        LogGenerator.genLog(
                httpServletRequest,
                userId, LogConstants.ActionName.USER_PASS_INFO,
                null
        );
        return userPassService.getUserPassInfo(userId);
    }

    /**
     *  获取用户使用了的优惠券信息
     * @param userId        用户id
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/userUsedPassInfo")
    Response userUsedPassInfo(Long userId) throws Exception {
        LogGenerator.genLog(
                httpServletRequest,
                userId, LogConstants.ActionName.USER_USED_PASS_INFO,
                null
        );
        return userPassService.getUserUsedPassInfo(userId);
    }

    /**
     *  用户使用优惠券
     * @param pass
     * @return
     */
    @ResponseBody
    @PostMapping("/userUsePass")
    Response userUsePass(Pass pass){
        LogGenerator.genLog(
                httpServletRequest,
                pass.getUserId(), LogConstants.ActionName.USER_USE_PASS,
                pass
        );
        return userPassService.userUsePass(pass);
    }

    /**
     *  获取库存信息
     * @param userId    用户id
     * @return
     */
    @ResponseBody
    @GetMapping("/inventoryInfo")
    Response inventoryInfo(Long userId) throws Exception {
        LogGenerator.genLog(
                httpServletRequest,
                userId, LogConstants.ActionName.INVENTORY_INFO,
                null
        );
        return inventoryService.getInventoryInfo(userId);
    }

    /**
     *  用户领取优惠券
     * @param request
     * @return
     */
    @ResponseBody
    @PostMapping("/gainPassTemplate")
    Response gainPassTemplate(@RequestBody GainPassTemplateRequest request) throws Exception {
        LogGenerator.genLog(
                httpServletRequest,
                request.getUserId(), LogConstants.ActionName.GAIN_PASS_TEMPLATE,
                request
        );
        return gainPassTemplateService.gainPassTemplate(request);
    }

    /**
     *  用户创建评论
     * @param feedback
     * @return
     */
    @ResponseBody
    @PostMapping("/createFeedback")
    Response createFeedback(Feedback feedback){
        LogGenerator.genLog(
                httpServletRequest,
                feedback.getUserId(), LogConstants.ActionName.GAIN_PASS_TEMPLATE,
                feedback
        );
        return feedbackService.createFeedback(feedback);
    }

    /**
     *  用户获取评论信息
     * @param userId        用户id
     * @return
     */
    @ResponseBody
    @GetMapping("/getFeedback")
    Response getFeedback(Long userId){
        LogGenerator.genLog(
                httpServletRequest,
                userId, LogConstants.ActionName.GAIN_PASS_TEMPLATE,
                null
        );
        return feedbackService.getFeedback(userId);
    }

    /**
     *  异常演示接口
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/exception")
    Response exception() throws Exception {
        throw new Exception("Welcome To JJU");
    }

}
