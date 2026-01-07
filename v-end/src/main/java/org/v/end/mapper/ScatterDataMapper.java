package org.v.end.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.v.end.model.entity.ScatterData;
import java.util.List;

@Mapper
public interface ScatterDataMapper {
    int insert(ScatterData record);
    List<ScatterData> selectList(ScatterData record);
    List<ScatterData> selectAll();
}