package com.xy.mapper;

import com.xy.pojo.VaegetableType2;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VaegetableType2Mapper {
    /**
     * 查询所有蔬菜分类信息
     * @return 蔬菜分类信息列表
     */
    List<VaegetableType2> selectAll();
} 