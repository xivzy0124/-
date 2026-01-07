package com.xy.mapper;

import com.xy.pojo.CalendarData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CalendarDataMapper {
    int insert(CalendarData record);

    List<CalendarData> selectAll();
    List<CalendarData> selectByProvince(String province);

    List<String> getProvince();
}