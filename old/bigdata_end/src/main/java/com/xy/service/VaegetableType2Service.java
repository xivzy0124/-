package com.xy.service;

import com.xy.mapper.VaegetableType2Mapper;
import com.xy.pojo.VaegetableType2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VaegetableType2Service {
    @Autowired
    private VaegetableType2Mapper vaegetableType2Mapper;

    /**
     * 获取所有蔬菜分类信息
     * @return 蔬菜分类信息列表
     */
    public List<VaegetableType2> getAllVegetableTypes() {
        return vaegetableType2Mapper.selectAll();
    }
} 