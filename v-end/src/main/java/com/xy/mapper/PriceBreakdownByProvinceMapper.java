package com.xy.mapper;

import com.xy.pojo.PriceBreakdownByProvince;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface PriceBreakdownByProvinceMapper {
    /**
     * 插入一条省级价格明细记录
     * @param record 省级价格明细记录
     * @return 受影响的行数
     */
    int insert(PriceBreakdownByProvince record);

    /**
     * 查询所有省级价格明细记录
     * @return 省级价格明细记录列表
     */
    List<PriceBreakdownByProvince> selectAll();

    /**
     * 根据品种名称查询省级价格明细记录
     * @param name 品种名称
     * @return 省级价格明细记录列表
     */
    List<PriceBreakdownByProvince> selectByName(String name);
    
    /**
     * 计算指定品种的总交易价值（均价*交易量的总和）
     * @param name 品种名称
     * @return 总交易价值
     */
    BigDecimal calculateTotalValue(String name);
    
    /**
     * 计算指定品种的总交易量
     * @param name 品种名称
     * @return 总交易量
     */
    BigDecimal calculateTotalVolume(String name);
    
    /**
     * 统计指定品种在多少个市场有售
     * @param name 品种名称
     * @return 市场数量
     */
    Integer countMarkets(String name);

}