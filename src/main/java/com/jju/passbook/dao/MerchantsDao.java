package com.jju.passbook.dao;

import com.jju.passbook.entity.Merchants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 *  Merchants Dao 接口
 */
public interface MerchantsDao extends JpaRepository<Merchants, Integer> {

    /**
     *  根据主键id获取商户对象
     * @param id    商户 id
     * @return      Merchants
     */
    Merchants findById(Integer id);

    /**
     *  根据商户名称获取商户对象
     * @param name  商户名称
     * @return      Merchants
     */
    Merchants findByName(String name);

    /**
     *  根据商户 ids 获取商户对象
     * @param ids   商户Ids
     * @return      List<Merchants>
     */
    List<Merchants> findByIdIn(List<Integer> ids);

}
