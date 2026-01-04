package com.xy.mapper;

import com.xy.pojo.DailyVegetablePrices;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface DailyVegetablePricesMapper {
    int insert(DailyVegetablePrices record);

    List<DailyVegetablePrices> selectAll();
    List<DailyVegetablePrices> selectByName(String name);
}