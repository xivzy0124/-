package org.v.end.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.v.end.model.entity.PriceForecasts;
import java.util.List;

@Mapper
public interface PriceForecastsMapper {
    int insert(PriceForecasts record);
    List<PriceForecasts> selectList(PriceForecasts record);
    List<PriceForecasts> selectAll();
}