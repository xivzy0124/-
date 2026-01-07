package com.xy.service;

import com.xy.mapper.DishPriceByProvinceMapper;
import com.xy.pojo.ProvinceDishPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishPriceService {
    @Autowired
    private DishPriceByProvinceMapper dishPriceByProvinceMapper;
    
    /**
     * 根据菜名和年份获取各省价格信息
     * @param dishName 菜品名称
     * @param year 年份
     * @return 各省菜品价格信息列表
     */
    public List<ProvinceDishPrice> getProvincePricesByDishAndYear(String dishName, Integer year) {
        return dishPriceByProvinceMapper.getProvincePricesByDishAndYear(dishName, year);
    }
} 