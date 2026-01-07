package org.v.end.service;

import org.v.end.model.entity.Statistics;
import java.util.List;

public interface StatisticsService {
    int insert(Statistics record);
    List<Statistics> selectList(Statistics record);
    List<Statistics> selectAll();
}
