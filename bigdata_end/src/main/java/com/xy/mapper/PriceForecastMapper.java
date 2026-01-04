package com.xy.mapper;

import com.xy.pojo.PriceForecast;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface PriceForecastMapper {
    /**
     * 查询所有价格预测信息
     * @return 价格预测信息列表
     */
    List<PriceForecast> selectAll();
    
    /**
     * 根据省份和产品查询价格预测信息
     * @param province 省份
     * @param productName 产品名称
     * @return 价格预测信息列表
     */
    List<PriceForecast> selectByProvinceAndProduct(@Param("province") String province, 
                                                 @Param("productName") String productName);
    
    /**
     * 根据预测目标日期范围查询价格预测信息
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 价格预测信息列表
     */
    List<PriceForecast> selectByForecastDateRange(@Param("startDate") Date startDate, 
                                                @Param("endDate") Date endDate);
} 