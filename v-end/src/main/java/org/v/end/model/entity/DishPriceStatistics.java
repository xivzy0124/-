package org.v.end.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表：dish_price_statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishPriceStatistics {
    private Integer year;
    private Integer quarter;
    private String dishName;
    private Double avgMaxPrice;
    private Double avgMinPrice;
    private Double priceDifference;
    private Double averagePrice;
    private String provincename;
}