package com.xy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarData {
    private String provincename;

    private String oneLevel;

    private String twoLevel;

    private String varietyname;

    private BigDecimal totalExportVolume;
}