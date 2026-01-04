package com.xy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishPriceStatistics {
    private Integer year;

    private Integer quarter;

    private String dishName;

    private BigDecimal avgMaxPrice;

    private BigDecimal avgMinPrice;

    private BigDecimal priceDifference;

    private BigDecimal averagePrice;
}