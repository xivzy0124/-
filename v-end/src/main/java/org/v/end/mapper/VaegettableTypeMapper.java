package org.v.end.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.v.end.model.entity.VaegettableType;
import java.util.List;

@Mapper
public interface VaegettableTypeMapper {
    int insert(VaegettableType record);
    List<VaegettableType> selectList(VaegettableType record);
    List<VaegettableType> selectAll();
}