package com.xy.mapper;

import com.xy.pojo.ProcessedPrices;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProcessedPricesMapper {
    int insert(ProcessedPrices record);

    List<ProcessedPrices> selectAll();

    List<ProcessedPrices> selectByName(String name);
}