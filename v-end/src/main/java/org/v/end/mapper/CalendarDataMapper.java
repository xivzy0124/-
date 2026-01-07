package org.v.end.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.v.end.model.entity.CalendarData;
import java.util.List;

@Mapper
public interface CalendarDataMapper {
    int insert(CalendarData record);
    List<CalendarData> selectList(CalendarData record);
    List<CalendarData> selectAll();
}