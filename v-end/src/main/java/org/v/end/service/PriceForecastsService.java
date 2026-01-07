package org.v.end.service;

import org.v.end.model.entity.PriceForecasts;
import java.util.List;

public interface PriceForecastsService {
    int insert(PriceForecasts record);
    List<PriceForecasts> selectList(PriceForecasts record);
    List<PriceForecasts> selectAll();
}
