package org.v.end.service;

import org.v.end.model.entity.VaegettableType;
import java.util.List;

public interface VaegettableTypeService {
    int insert(VaegettableType record);
    List<VaegettableType> selectList(VaegettableType record);
    List<VaegettableType> selectAll();
}
