package org.v.end.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.v.end.mapper.VaegettableTypeMapper;
import org.v.end.model.entity.VaegettableType;
import org.v.end.service.VaegettableTypeService;
import java.util.List;

@Service
public class VaegettableTypeServiceImpl implements VaegettableTypeService {

    @Autowired
    private VaegettableTypeMapper vaegettableTypeMapper;

    @Override
    public int insert(VaegettableType record) {
        return vaegettableTypeMapper.insert(record);
    }

    @Override
    public List<VaegettableType> selectList(VaegettableType record) {
        return vaegettableTypeMapper.selectList(record);
    }

    @Override
    public List<VaegettableType> selectAll() {
        return vaegettableTypeMapper.selectAll();
    }
}
