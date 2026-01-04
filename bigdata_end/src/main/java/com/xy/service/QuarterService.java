package com.xy.service;

import com.xy.mapper.DishPriceStatisticsMapper;
import com.xy.mapper.PriceBreakdownByProvinceMapper;
import com.xy.pojo.DishPriceStatistics;
import com.xy.pojo.PriceBreakdownByProvince;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuarterService {
    @Autowired
    private DishPriceStatisticsMapper dish;
    public List<DishPriceStatistics> selectByQuart(String name, Integer start, Integer end){
        return dish.selectByQuart(name,start,end);
    }
    public List<DishPriceStatistics> selectByYear(String name, Integer start, Integer end){
        System.out.println(start+" "+end);
        return dish.selectByYear(name,start,end);
    }
}
