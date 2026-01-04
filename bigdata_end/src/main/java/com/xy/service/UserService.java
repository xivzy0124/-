package com.xy.service;

import com.xy.mapper.CalendarDataMapper;
import com.xy.mapper.ProcessedPricesMapper;
import com.xy.mapper.ScatterDataMapper;
import com.xy.mapper.StatisticsMapper;
import com.xy.pojo.CalendarData;
import com.xy.pojo.ProcessedPrices;
import com.xy.pojo.ScatterData;
import com.xy.pojo.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    CalendarDataMapper calendar;
    @Autowired
    private ScatterDataMapper scatterDataMapper;
    //提供user界面顶部卡片数据
    @Autowired
    private StatisticsMapper statisticsMapper;
    @Autowired
    private ProcessedPricesMapper processedPricesMapper;
    // 根据省份查询数据绘制桑基图
    public List<CalendarData> calendarByProvince(String province){
        return calendar.selectByProvince(province);
    }

    // 获取所有省份
    public List<String> getProvince(){
        return calendar.getProvince();
    }

    // 根据省份和蔬菜名称查询数据
    public List<ScatterData> getScatterDataByProvinceAndVariety(String provinceName, String varietyName) {
        return scatterDataMapper.selectByProvinceAndVariety(provinceName, varietyName);
    }
    //获取固定数据
    public List<Statistics> selectAll() {
        return statisticsMapper.selectAll();
    }
    //新数据，根据菜名查询
    public List<ProcessedPrices> selectProcessedPriceByName(String name) {
        return processedPricesMapper.selectByName(name);
    }
}
