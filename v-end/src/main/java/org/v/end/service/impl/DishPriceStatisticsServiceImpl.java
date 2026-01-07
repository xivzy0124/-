package org.v.end.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.v.end.mapper.DishPriceStatisticsMapper;
import org.v.end.model.entity.DishPriceStatistics;
import org.v.end.service.DishPriceStatisticsService;
import java.util.List;

@Service
public class DishPriceStatisticsServiceImpl implements DishPriceStatisticsService {

    @Autowired
    private DishPriceStatisticsMapper dishPriceStatisticsMapper;

    @Override
    public int insert(DishPriceStatistics record) {
        return dishPriceStatisticsMapper.insert(record);
    }

    @Override
    public List<DishPriceStatistics> selectList(DishPriceStatistics record) {
        return dishPriceStatisticsMapper.selectList(record);
    }

    @Override
    public List<DishPriceStatistics> selectAll() {
        return dishPriceStatisticsMapper.selectAll();
    }
}
