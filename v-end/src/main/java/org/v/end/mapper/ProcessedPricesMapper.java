package org.v.end.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.v.end.model.entity.ProcessedPrices;
import java.util.List;

@Mapper
public interface ProcessedPricesMapper {
    int insert(ProcessedPrices record);
    List<ProcessedPrices> selectList(ProcessedPrices record);
    List<ProcessedPrices> selectAll();
}