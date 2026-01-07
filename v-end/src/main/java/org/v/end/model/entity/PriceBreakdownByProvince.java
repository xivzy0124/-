package org.v.end.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表：price_breakdown_by_province
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceBreakdownByProvince {
    private String provincename;
    private String varietyname;
    private Double minPrice;
    private Double maxPrice;
    private Double avgPrice;
    private Double tradingvolume;
    private String marketname;
}