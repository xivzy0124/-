package org.v.end.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.v.end.model.entity.ExportTop;
import java.util.List;

@Mapper
public interface ExportTopMapper {
    int insert(ExportTop record);
    List<ExportTop> selectList(ExportTop record);
    List<ExportTop> selectAll();
}