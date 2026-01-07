package org.v.end.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 对应表：daily_vegetable_prices
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyVegetablePrices {
    private Date dt;
    private Integer weekOfMonth;
    private String name;
    private BigDecimal price;
}