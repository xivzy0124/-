package com.xy.service;

import com.xy.mapper.PriceBreakdownByProvinceMapper;
import com.xy.pojo.PriceBreakdownByProvince;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 省级价格服务类
 */
@Service
public class ProvincePrice {
    @Autowired
    private PriceBreakdownByProvinceMapper pm;
    
    /**
     * 根据品种名称查询省级价格明细记录
     * @param name 品种名称
     * @return 省级价格明细记录列表
     */
    public List<PriceBreakdownByProvince> selectByName(String name){
        return pm.selectByName(name);
    }
    
    /**
     * 计算指定品种的总交易价值（均价*交易量的总和）
     * @param name 品种名称
     * @return 总交易价值
     */
    public BigDecimal calculateTotalValue(String name) {
        return pm.calculateTotalValue(name);
    }
    
    /**
     * 计算指定品种的总交易量
     * @param name 品种名称
     * @return 总交易量
     */
    public BigDecimal calculateTotalVolume(String name) {
        return pm.calculateTotalVolume(name);
    }
    
    /**
     * 统计指定品种在多少个市场有售
     * @param name 品种名称
     * @return 市场数量
     */
    public Integer countMarkets(String name) {
        return pm.countMarkets(name);
    }
}





