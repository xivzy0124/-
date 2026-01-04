package com.xy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 按日查询蔬菜价格
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DailyVegetablePrices {
    private String dt;//每天日期
    private String name;//菜名
    private BigDecimal price;//价格
}