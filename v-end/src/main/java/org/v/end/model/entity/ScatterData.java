package org.v.end.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表：scatter_data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScatterData {
    private String provincename;
    private String marketname;
    private String varietyname;
    private Double volume;
    private Double priceVariance;
    private Double averagePrice;
}