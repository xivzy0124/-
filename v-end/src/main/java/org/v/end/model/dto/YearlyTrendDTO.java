package org.v.end.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YearlyTrendDTO {
    private Integer year;
    private Double averagePrice;
    private Double avgMaxPrice;
}
