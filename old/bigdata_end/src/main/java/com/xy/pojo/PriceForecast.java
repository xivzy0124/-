package com.xy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 价格预测信息
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PriceForecast {
    private Date generateDate;    // 预测生成日期
    private Date forecastDate;    // 预测目标日期
    private String province;      // 省份名称
    private String productName;   // 产品名称
    private BigDecimal predictedPrice; // 预测价格（元/斤）
    private String algorithm;     // 预测算法
} 