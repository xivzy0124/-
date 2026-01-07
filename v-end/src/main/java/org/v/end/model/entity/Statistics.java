package org.v.end.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表：statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statistics {
    private Long total;
    private Long varietynameCt;
    private Long provincenameCt;
    private Long areacodeCt;
}