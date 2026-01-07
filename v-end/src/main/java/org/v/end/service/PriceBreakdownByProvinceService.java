package org.v.end.service;

import org.v.end.model.dto.YearlyTrendDTO;
import org.v.end.model.entity.PriceBreakdownByProvince;
import java.util.List;

public interface PriceBreakdownByProvinceService {
    int insert(PriceBreakdownByProvince record);
    List<PriceBreakdownByProvince> selectList(PriceBreakdownByProvince record);
    List<PriceBreakdownByProvince> selectAll();
    List<YearlyTrendDTO> getYearlyTrendByProvince(String provinceName, String varietyName);
}
