package org.v.end.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.v.end.mapper.CalendarDataMapper;
import org.v.end.model.entity.CalendarData;
import org.v.end.service.CalendarDataService;
import java.util.List;

@Service
public class CalendarDataServiceImpl implements CalendarDataService {

    @Autowired
    private CalendarDataMapper calendarDataMapper;

    @Override
    public int insert(CalendarData record) {
        return calendarDataMapper.insert(record);
    }

    @Override
    public List<CalendarData> selectList(CalendarData record) {
        return calendarDataMapper.selectList(record);
    }

    @Override
    public List<CalendarData> selectAll() {
        return calendarDataMapper.selectAll();
    }
}
