package com.jju.passbook.service.impl;

import com.alibaba.fastjson.JSON;
import com.jju.passbook.constant.Constants;
import com.jju.passbook.constant.PassStatus;
import com.jju.passbook.dao.MerchantsDao;
import com.jju.passbook.entity.Merchants;
import com.jju.passbook.mapper.PassRowMapper;
import com.jju.passbook.service.IUserPassService;
import com.jju.passbook.vo.Pass;
import com.jju.passbook.vo.PassInfo;
import com.jju.passbook.vo.PassTemplate;
import com.jju.passbook.vo.Response;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 *  用户优惠券相关功能实现
 */
@Service
@Slf4j
public class UserPassServiceImpl implements IUserPassService {

    private final HbaseTemplate hbaseTemplate;

    private final MerchantsDao merchantsDao;

    @Autowired
    public UserPassServiceImpl(HbaseTemplate hbaseTemplate, MerchantsDao merchantsDao) {
        this.hbaseTemplate = hbaseTemplate;
        this.merchantsDao = merchantsDao;
    }

    @Override
    public Response getUserPassInfo(Long userId) throws Exception {
        return getPassInfoByStatus(userId, PassStatus.UNUSED);
    }

    @Override
    public Response getUserUsedPassInfo(Long userId) throws Exception {
        return getPassInfoByStatus(userId, PassStatus.USED);
    }

    @Override
    public Response getUserAllPassInfo(Long userId) throws Exception {
        return getPassInfoByStatus(userId, PassStatus.ALL);
    }

    @Override
    public Response userUsePass(Pass pass) {
        //根据userId构造行键前缀
        byte [] rowPrefix = Bytes.toBytes(new StringBuilder(String.valueOf(pass.getUserId())).reverse().toString());

        Scan scan = new Scan();
        List<Filter> filters = new ArrayList<>();
        //行过滤器
        filters.add(new PrefixFilter(rowPrefix));
        //保证TemplateId相等
        filters.add(new SingleColumnValueFilter(
                Constants.PassTable.FAMILY_I.getBytes(),
                Constants.PassTable.TEMPLATE_ID.getBytes(),
                CompareFilter.CompareOp.EQUAL,
                Bytes.toBytes(pass.getTemplateId())
        ));
        //保证CON_DATE为空，即没有消费
        filters.add(new SingleColumnValueFilter(
                Constants.PassTable.FAMILY_I.getBytes(),
                Constants.PassTable.CON_DATE.getBytes(),
                CompareFilter.CompareOp.EQUAL,
                Bytes.toBytes("-1")
        ));

        //设置过滤器
        scan.setFilter(new FilterList(filters));

        List<Pass> passes = hbaseTemplate.find(Constants.PassTable.TABLE_NAME, scan, new PassRowMapper());
        if(null == passes || passes.size() != 1){
            log.error("UserUsePass Error：{}", JSON.toJSONString(pass));
            return Response.failure("UserUsePass Error");
        }

        byte [] FAMILY_I = Constants.PassTable.FAMILY_I.getBytes();
        byte [] CON_DATE = Constants.PassTable.CON_DATE.getBytes();

        List<Mutation> datas = new ArrayList<>();
        Put put = new Put(passes.get(0).getRowKey().getBytes());
        put.addColumn(FAMILY_I, CON_DATE, Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(new Date())));
        datas.add(put);

        hbaseTemplate.saveOrUpdates(Constants.PassTable.TABLE_NAME, datas);

        return Response.success();
    }

    /**
     *  根据优惠券状态获取优惠券信息
     * @param userId        用户id
     * @param status        优惠券状态
     * @return              Response
     * @throws Exception
     */
    private Response getPassInfoByStatus(Long userId, PassStatus status) throws Exception{
        //根据userId构造行键前缀
        byte [] rowPrefix = Bytes.toBytes(new StringBuilder(String.valueOf(userId)).reverse().toString());
        CompareFilter.CompareOp compareOp =
                status == PassStatus.UNUSED ?
                        CompareFilter.CompareOp.EQUAL :
                        CompareFilter.CompareOp.NOT_EQUAL;

        Scan scan = new Scan();

        List<Filter> filters = new ArrayList<>();

        //1、行键前缀过滤器，找到特定用户的优惠券
        filters.add(new PrefixFilter(rowPrefix));
        //2、基于列单元值的过滤器，找到未使用的优惠券
        if(status != PassStatus.ALL){
            filters.add(new SingleColumnValueFilter(
                    Constants.PassTable.FAMILY_I.getBytes(),
                    Constants.PassTable.CON_DATE.getBytes(),
                    compareOp,
                    Bytes.toBytes("-1")
            ));
        }

        scan.setFilter(new FilterList(filters));

        List<Pass> passes = hbaseTemplate.find(Constants.PassTable.TABLE_NAME, scan, new PassRowMapper());
        Map<String, PassTemplate> passTemplateMap = buildPassTemplateMap(passes);
        Map<Integer, Merchants> merchantsMap = buildMerchantsMap(new ArrayList<>(passTemplateMap.values()));

        List<PassInfo> result = new ArrayList<>();
        for (Pass pass : passes) {
            PassTemplate passTemplate = passTemplateMap.getOrDefault(pass.getTemplateId(), null);
            if(null == passTemplate){
                log.error("PassTemplate NULL : {}", pass.getTemplateId());
                continue;
            }

            Merchants merchants = merchantsMap.getOrDefault(passTemplate.getId(), null);
            if(null == merchants){
                log.error("Merchants NULL : {}", passTemplate.getId());
                continue;
            }

            result.add(new PassInfo(pass, passTemplate, merchants));
        }

        return new Response(result);
    }

    /**
     *  通过获取的 Passes 对象构造 Map
     * @param passes        passes
     * @return              Map
     * @throws Exception
     */
    private Map<String, PassTemplate> buildPassTemplateMap(List<Pass> passes) throws Exception{
        String [] patterns = new String[] {"yyyy-MM-dd"};

        byte [] FAMILY_B = Bytes.toBytes(Constants.PassTemplateTable.FAMILY_B);
        byte [] ID = Bytes.toBytes(Constants.PassTemplateTable.ID);
        byte [] TITLE = Bytes.toBytes(Constants.PassTemplateTable.TITLE);
        byte [] SUMMARY = Bytes.toBytes(Constants.PassTemplateTable.SUMMARY);
        byte [] DESC = Bytes.toBytes(Constants.PassTemplateTable.DESC);
        byte [] HAS_TOKEN = Bytes.toBytes(Constants.PassTemplateTable.HAS_TOKEN);
        byte [] BACKGROUND = Bytes.toBytes(Constants.PassTemplateTable.BACKGROUND);

        byte [] FAMILY_C = Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C);
        byte [] LIMIT = Bytes.toBytes(Constants.PassTemplateTable.LIMIT);
        byte [] START = Bytes.toBytes(Constants.PassTemplateTable.START);
        byte [] END = Bytes.toBytes(Constants.PassTemplateTable.END);

        List<String> templateIds = passes.stream().map(
                Pass::getTemplateId
        ).collect(Collectors.toList());

        List<Get> templateGets = new ArrayList<>(templateIds.size());
        templateIds.forEach(t -> templateGets.add(new Get(Bytes.toBytes(t))));

        Result [] templateResults = hbaseTemplate.getConnection()
                .getTable(TableName.valueOf(Constants.PassTemplateTable.TABLE_NAME))
                .get(templateGets);

        //构造PassTemplateId -> PassTemplate Object 的 Map, 用于构造 PassInfo
        Map<String, PassTemplate> templateId2Object = new HashMap<>();
        for (Result templateResult : templateResults) {
            PassTemplate passTemplate = new PassTemplate();

            passTemplate.setId(Bytes.toInt(templateResult.getValue(FAMILY_B, ID)));
            passTemplate.setTitle(Bytes.toString(templateResult.getValue(FAMILY_B, TITLE)));
            passTemplate.setSummary(Bytes.toString(templateResult.getValue(FAMILY_B, SUMMARY)));
            passTemplate.setDesc(Bytes.toString(templateResult.getValue(FAMILY_B, DESC)));
            passTemplate.setHasToken(Bytes.toBoolean(templateResult.getValue(FAMILY_B, HAS_TOKEN)));
            passTemplate.setBackground(Bytes.toInt(templateResult.getValue(FAMILY_B, BACKGROUND)));

            passTemplate.setLimit(Bytes.toLong(templateResult.getValue(FAMILY_C, LIMIT)));
            passTemplate.setStart(DateUtils.parseDate(Bytes.toString(templateResult.getValue(FAMILY_C, START)), patterns));
            passTemplate.setEnd(DateUtils.parseDate(Bytes.toString(templateResult.getValue(FAMILY_C, END)), patterns));

            templateId2Object.put(Bytes.toString(templateResult.getRow()), passTemplate);
        }

        return templateId2Object;
    }

    /**
     *  通过获取的 PassTemplate 对象构造 Merchants Map
     * @param passTemplates     PassTemplate
     * @return                  Map
     */
    private Map<Integer, Merchants> buildMerchantsMap(List<PassTemplate> passTemplates){
        Map<Integer, Merchants> merchantsMap = new HashMap<>();
        List<Integer> merchantsIds = passTemplates.stream().map(
                PassTemplate::getId
        ).collect(Collectors.toList());
        List<Merchants> merchants = merchantsDao.findByIdIn(merchantsIds);

        merchants.forEach(m -> merchantsMap.put(m.getId(), m));

        return merchantsMap;
    }

}
