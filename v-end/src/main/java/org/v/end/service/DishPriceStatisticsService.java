package org.v.end.service;

import org.v.end.model.entity.DishPriceStatistics;
import java.util.List;

public interface DishPriceStatisticsService {
    int insert(DishPriceStatistics record);
    List<DishPriceStatistics> selectList(DishPriceStatistics record);
    List<DishPriceStatistics> selectAll();
}
