package org.v.end.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.v.end.model.entity.Statistics;
import java.util.List;

@Mapper
public interface StatisticsMapper {
    int insert(Statistics record);
    List<Statistics> selectList(Statistics record);
    List<Statistics> selectAll();
}