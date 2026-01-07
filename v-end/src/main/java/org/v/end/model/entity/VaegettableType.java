package org.v.end.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表：vaegettable_type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaegettableType {
    /**
     * 大类
     */
    private String oneLevel;

    /**
     * 小类
     */
    private String twoLevel;

    /**
     * 菜名
     */
    private String varietyname;
}