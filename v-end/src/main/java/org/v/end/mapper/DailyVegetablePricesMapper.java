package org.v.end.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.v.end.model.entity.DailyVegetablePrices;
import java.util.List;

@Mapper
public interface DailyVegetablePricesMapper {
    int insert(DailyVegetablePrices record);
    List<DailyVegetablePrices> selectList(DailyVegetablePrices record);
    List<DailyVegetablePrices> selectAll();
}