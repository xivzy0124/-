package com.xy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScatterData {
    private String provincename;

    private String varietyname;

    private String marketname;

    private BigDecimal averagePrice;

    private BigDecimal priceVariance;
}