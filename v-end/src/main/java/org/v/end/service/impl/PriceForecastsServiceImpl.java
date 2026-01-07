package org.v.end.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.v.end.mapper.PriceForecastsMapper;
import org.v.end.model.entity.PriceForecasts;
import org.v.end.service.PriceForecastsService;
import java.util.List;

@Service
public class PriceForecastsServiceImpl implements PriceForecastsService {

    @Autowired
    private PriceForecastsMapper priceForecastsMapper;

    @Override
    public int insert(PriceForecasts record) {
        return priceForecastsMapper.insert(record);
    }

    @Override
    public List<PriceForecasts> selectList(PriceForecasts record) {
        return priceForecastsMapper.selectList(record);
    }

    @Override
    public List<PriceForecasts> selectAll() {
        return priceForecastsMapper.selectAll();
    }
}
