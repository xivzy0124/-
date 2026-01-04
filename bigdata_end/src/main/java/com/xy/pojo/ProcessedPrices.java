package com.xy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedPrices {
    private Double minimumprice;

    private Double middleprice;

    private Double highestprice;

    private Double tradingvolume;

    private String marketname;

    private String provincename;

    private String varietyname;

    private String reporttime;

    private String dt;
}