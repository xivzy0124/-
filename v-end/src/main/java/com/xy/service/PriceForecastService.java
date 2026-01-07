package com.xy.service;

import com.xy.mapper.PriceForecastMapper;
import com.xy.pojo.PriceForecast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PriceForecastService {
    @Autowired
    private PriceForecastMapper priceForecastMapper;

    /**
     * 获取所有价格预测信息
     * @return 价格预测信息列表
     */
    public List<PriceForecast> getAllPriceForecasts() {
        return priceForecastMapper.selectAll();
    }
    
    /**
     * 根据省份和产品获取价格预测信息
     * @param province 省份
     * @param productName 产品名称
     * @return 价格预测信息列表
     */
    public List<PriceForecast> getPriceForecastsByProvinceAndProduct(String province, String productName) {
        return priceForecastMapper.selectByProvinceAndProduct(province, productName);
    }
    
    /**
     * 根据预测目标日期范围获取价格预测信息
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 价格预测信息列表
     */
    public List<PriceForecast> getPriceForecastsByDateRange(Date startDate, Date endDate) {
        return priceForecastMapper.selectByForecastDateRange(startDate, endDate);
    }
} 