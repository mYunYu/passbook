package com.jju.passbook.service.impl;

import com.jju.passbook.constant.Constants;
import com.jju.passbook.dao.MerchantsDao;
import com.jju.passbook.entity.Merchants;
import com.jju.passbook.mapper.PassTemplateRowMapper;
import com.jju.passbook.service.IInventoryService;
import com.jju.passbook.service.IUserPassService;
import com.jju.passbook.utils.RowKeyGenUtil;
import com.jju.passbook.vo.*;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.LongComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 *  获取库存信息，只返回用户没有领取的
 */
@Service
@Slf4j
public class InventoryServiceImpl implements IInventoryService {

    private final HbaseTemplate hbaseTemplate;

    private final MerchantsDao merchantsDao;

    private final IUserPassService userPassService;

    @Autowired
    public InventoryServiceImpl(HbaseTemplate hbaseTemplate, MerchantsDao merchantsDao, IUserPassService userPassService) {
        this.hbaseTemplate = hbaseTemplate;
        this.merchantsDao = merchantsDao;
        this.userPassService = userPassService;
    }


    @Override
    @SuppressWarnings("unchecked")
    public Response getInventoryInfo(Long userId) throws Exception {
        Response allUserPass = userPassService.getUserAllPassInfo(userId);
        List<PassInfo> passInfos = (List<PassInfo>) allUserPass.getData();

        List<PassTemplate> excludeObject = passInfos.stream().map(
                PassInfo::getPassTemplate
        ).collect(Collectors.toList());
        List<String> excludeIds = new ArrayList<>();

        excludeObject.forEach(e -> excludeIds.add(RowKeyGenUtil.genPassTemplateRowKey(e)));

        return new Response(new InventoryResponse(userId, buildPassTemplateInfo(getAvailablePassTemplate(excludeIds))));
    }

    /**
     *  获取系统中可用的优惠券
     * @param excludeIds    需要排除的优惠券 ids
     * @return              PassTemplate
     */
    private List<PassTemplate> getAvailablePassTemplate(List<String> excludeIds){

        //设置过滤器，只要通过一个过滤器即可
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);

        //LIMIT>0
        filterList.addFilter(
                new SingleColumnValueFilter(
                        Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C),
                        Bytes.toBytes(Constants.PassTemplateTable.LIMIT),
                        CompareFilter.CompareOp.GREATER,
                        new LongComparator(0L)
                )
        );

        //LIMIT=-1
        filterList.addFilter(
                new SingleColumnValueFilter(
                        Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C),
                        Bytes.toBytes(Constants.PassTemplateTable.LIMIT),
                        CompareFilter.CompareOp.EQUAL,
                        Bytes.toBytes("-1")
                )
        );

        Scan scan = new Scan();
        scan.setFilter(new FilterList(filterList));

        List<PassTemplate> validTemplates = hbaseTemplate.find(
                Constants.PassTemplateTable.TABLE_NAME,
                scan,
                new PassTemplateRowMapper()
        );
        List<PassTemplate> availablePassTemplates = new ArrayList<>();

        Date cur = new Date();

        for (PassTemplate validTemplate : validTemplates) {
            //是否包含rowkey
            if(excludeIds.contains(RowKeyGenUtil.genPassTemplateRowKey(validTemplate))){
                continue;
            }

            //是否过期
            if(cur.getTime() >= validTemplate.getStart().getTime()
                    && cur.getTime() <= validTemplate.getEnd().getTime()){
                availablePassTemplates.add(validTemplate);
            }
        }

        return availablePassTemplates;
    }

    /**
     *  构造优惠券的信息
     * @param passTemplates     PassTemplateInfo
     * @return                  passTemplates
     */
    private List<PassTemplateInfo> buildPassTemplateInfo(List<PassTemplate> passTemplates){
        Map<Integer, Merchants> merchantsMap = new HashMap<>();
        List<Integer> merchantsIds = passTemplates.stream().map(
                PassTemplate::getId
        ).collect(Collectors.toList());
        List<Merchants> merchants = merchantsDao.findByIdIn(merchantsIds);
        merchants.forEach(m -> merchantsMap.put(m.getId(), m));

        List<PassTemplateInfo> result = new ArrayList<>(passTemplates.size());
        for (PassTemplate passTemplate : passTemplates) {
            Merchants mc = merchantsMap.getOrDefault(passTemplate.getId(), null);
            if(null == mc){
                log.error("Merchants Error：{}", passTemplate.getId());
                continue;
            }

            result.add(new PassTemplateInfo(passTemplate, mc));
        }

        return result;
    }

}
