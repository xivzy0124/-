package com.xy.mapper;

import com.xy.pojo.ExportTOP;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExportTOPMapper {
    int insert(ExportTOP record);

    List<ExportTOP> selectAll();

    List<ExportTOP> topN(String name);
}