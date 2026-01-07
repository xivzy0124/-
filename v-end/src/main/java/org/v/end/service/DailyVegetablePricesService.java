package org.v.end.service;

import org.v.end.model.entity.DailyVegetablePrices;
import java.util.List;

public interface DailyVegetablePricesService {
    int insert(DailyVegetablePrices record);
    List<DailyVegetablePrices> selectList(DailyVegetablePrices record);
    List<DailyVegetablePrices> selectAll();
}
