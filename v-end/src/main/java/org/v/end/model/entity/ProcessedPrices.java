package org.v.end.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表：processed_prices
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedPrices {
    private String marketname;
    private String provincename;
    private Double minimumprice;
    private Double middleprice;
    private Double highestprice;
    private Double tradingvolume;
    private String varietyname;
    // 数据库中定义为 text
    private String reporttime;
    // 数据库中定义为 text
    private String dt;
}