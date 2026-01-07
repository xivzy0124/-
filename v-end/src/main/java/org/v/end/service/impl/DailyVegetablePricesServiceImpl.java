package org.v.end.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.v.end.mapper.DailyVegetablePricesMapper;
import org.v.end.model.entity.DailyVegetablePrices;
import org.v.end.service.DailyVegetablePricesService;
import java.util.List;

@Service
public class DailyVegetablePricesServiceImpl implements DailyVegetablePricesService {

    @Autowired
    private DailyVegetablePricesMapper dailyVegetablePricesMapper;

    @Override
    public int insert(DailyVegetablePrices record) {
        return dailyVegetablePricesMapper.insert(record);
    }

    @Override
    public List<DailyVegetablePrices> selectList(DailyVegetablePrices record) {
        return dailyVegetablePricesMapper.selectList(record);
    }

    @Override
    public List<DailyVegetablePrices> selectAll() {
        return dailyVegetablePricesMapper.selectAll();
    }
}
