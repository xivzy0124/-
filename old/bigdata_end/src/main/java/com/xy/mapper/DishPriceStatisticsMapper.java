package com.xy.mapper;

import com.xy.pojo.DishPriceStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface DishPriceStatisticsMapper {
    int insert(DishPriceStatistics record);

    List<DishPriceStatistics> selectAll();

    List<DishPriceStatistics> selectByQuart(String name,Integer start,Integer end);
    List<DishPriceStatistics> selectByYear(String name,Integer start,Integer end);
}