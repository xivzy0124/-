package org.v.end.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.v.end.mapper.StatisticsMapper;
import org.v.end.model.entity.Statistics;
import org.v.end.service.StatisticsService;
import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Override
    public int insert(Statistics record) {
        return statisticsMapper.insert(record);
    }

    @Override
    public List<Statistics> selectList(Statistics record) {
        return statisticsMapper.selectList(record);
    }

    @Override
    public List<Statistics> selectAll() {
        return statisticsMapper.selectAll();
    }
}
