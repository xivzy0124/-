package com.xy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceDishPrice {
    // 省份名称
    private String provinceName;
    
    // 菜品名称
    private String dishName;
    
    // 年份
    private Integer year;
    
    // 最高价格
    private BigDecimal maxPrice;
    
    // 最低价格
    private BigDecimal minPrice;
    
    // 平均价格
    private BigDecimal avgPrice;
} 