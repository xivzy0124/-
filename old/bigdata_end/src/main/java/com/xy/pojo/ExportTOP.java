package com.xy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportTOP {
    private Double tradingvolume;

    private Double minimumprice;

    private Double middleprice;

    private Double highestprice;

    private String varietyname;

    private String marketname;
}