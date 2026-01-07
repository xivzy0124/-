package org.v.end.service;

import org.v.end.model.entity.CalendarData;
import java.util.List;

public interface CalendarDataService {
    int insert(CalendarData record);
    List<CalendarData> selectList(CalendarData record);
    List<CalendarData> selectAll();
}
