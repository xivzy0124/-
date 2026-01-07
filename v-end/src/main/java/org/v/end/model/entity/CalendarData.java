package org.v.end.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表：calendar_data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarData {
    private String provincename;
    private String oneLevel;
    private String twoLevel;
    private String varietyname;
    private Double totalExportVolume;
}