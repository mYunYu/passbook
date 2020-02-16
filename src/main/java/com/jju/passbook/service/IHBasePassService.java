package com.jju.passbook.service;

import com.jju.passbook.vo.PassTemplate;

/**
 *  Pass HBase 服务
 */
public interface IHBasePassService {

    /**
     *  将 PassTemplate 写入 HBase
     * @param passTemplate
     * @return  true/false
     */
    boolean dropPassTemplateToHBase(PassTemplate passTemplate);

}
