package org.v.end.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 对应表：price_forecasts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceForecasts {
    /**
     * 预测生成日期
     */
    private Date generateDate;

    /**
     * 预测目标日期
     */
    private Date forecastDate;

    /**
     * 省份名称
     */
    private String province;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 预测价格（元/斤）
     */
    private BigDecimal predictedPrice;

    /**
     * 预测算法
     */
    private String algorithm;
}