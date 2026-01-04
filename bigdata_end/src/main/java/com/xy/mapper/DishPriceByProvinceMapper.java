package com.xy.mapper;

import com.xy.pojo.ProvinceDishPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishPriceByProvinceMapper {
    /**
     * 根据菜名和年份获取各省价格信息
     * @param dishName 菜品名称
     * @param year 年份
     * @return 各省菜品价格信息列表
     */
    List<ProvinceDishPrice> getProvincePricesByDishAndYear(@Param("dishName") String dishName, @Param("year") Integer year);
} 