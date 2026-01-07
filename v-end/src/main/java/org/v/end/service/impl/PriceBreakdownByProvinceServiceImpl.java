package org.v.end.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.v.end.mapper.PriceBreakdownByProvinceMapper;
import org.v.end.model.dto.YearlyTrendDTO;
import org.v.end.model.entity.PriceBreakdownByProvince;
import org.v.end.service.PriceBreakdownByProvinceService;
import java.util.List;

@Service
public class PriceBreakdownByProvinceServiceImpl implements PriceBreakdownByProvinceService {

    @Autowired
    private PriceBreakdownByProvinceMapper priceBreakdownByProvinceMapper;

    @Override
    public int insert(PriceBreakdownByProvince record) {
        return priceBreakdownByProvinceMapper.insert(record);
    }

    @Override
    public List<PriceBreakdownByProvince> selectList(PriceBreakdownByProvince record) {
        return priceBreakdownByProvinceMapper.selectList(record);
    }

    @Override
    public List<PriceBreakdownByProvince> selectAll() {
        return priceBreakdownByProvinceMapper.selectAll();
    }

    @Override
    public List<YearlyTrendDTO> getYearlyTrendByProvince(String provinceName, String varietyName) {
        return priceBreakdownByProvinceMapper.getYearlyTrendByProvince(provinceName, varietyName);
    }
}
