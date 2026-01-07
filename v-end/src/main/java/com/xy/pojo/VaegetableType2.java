package com.xy.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 蔬菜分类信息 (vaegettable_type_2表)
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VaegetableType2 {
    private String oneLevel;   // 大类
    private String twoLevel;   // 小类
    private String varietyname; // 菜名
} 