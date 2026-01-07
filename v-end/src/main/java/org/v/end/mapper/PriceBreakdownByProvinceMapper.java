package org.v.end.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.v.end.model.entity.PriceBreakdownByProvince;
import java.util.List;

@Mapper
public interface PriceBreakdownByProvinceMapper {
    int insert(PriceBreakdownByProvince record);
    List<PriceBreakdownByProvince> selectList(PriceBreakdownByProvince record);
    List<PriceBreakdownByProvince> selectAll();
}