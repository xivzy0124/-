package com.xy.service;

import com.xy.mapper.DailyVegetablePricesMapper;
import com.xy.pojo.DailyVegetablePrices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DateAndPriceService {
    @Autowired
    DailyVegetablePricesMapper d;
    public List<DailyVegetablePrices> selectByName(String name){
        return d.selectByName(name);
    }
}
