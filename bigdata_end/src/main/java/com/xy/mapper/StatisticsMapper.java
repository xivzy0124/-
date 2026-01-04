package com.xy.mapper;

import com.xy.pojo.Statistics;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StatisticsMapper {
    int insert(Statistics record);

    List<Statistics> selectAll();
}