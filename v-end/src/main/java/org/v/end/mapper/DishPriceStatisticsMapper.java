package org.v.end.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.v.end.model.entity.DishPriceStatistics;
import java.util.List;

@Mapper
public interface DishPriceStatisticsMapper {
    int insert(DishPriceStatistics record);
    List<DishPriceStatistics> selectList(DishPriceStatistics record);
    List<DishPriceStatistics> selectAll();
}