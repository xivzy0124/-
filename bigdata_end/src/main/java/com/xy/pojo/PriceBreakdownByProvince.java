package com.xy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceBreakdownByProvince {
    private String provincename;

    private String varietyname;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private BigDecimal avgPrice;

    private BigDecimal tradingvolume;

    private String marketname;
}