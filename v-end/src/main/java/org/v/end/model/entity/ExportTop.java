package org.v.end.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表：export_top
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportTop {
    private String varietyname;
    private String marketname;
    // 数据库字段本身是驼峰或混合命名，建议保持一致或通过ResultMap映射
    private Double tradingVolume;
    private Double minimumPrice;
    private Double middlePrice;
    private Double highestPrice;
}