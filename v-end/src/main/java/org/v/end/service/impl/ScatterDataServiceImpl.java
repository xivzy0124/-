package org.v.end.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.v.end.mapper.ScatterDataMapper;
import org.v.end.model.entity.ScatterData;
import org.v.end.service.ScatterDataService;
import java.util.List;

@Service
public class ScatterDataServiceImpl implements ScatterDataService {

    @Autowired
    private ScatterDataMapper scatterDataMapper;

    @Override
    public int insert(ScatterData record) {
        return scatterDataMapper.insert(record);
    }

    @Override
    public List<ScatterData> selectList(ScatterData record) {
        return scatterDataMapper.selectList(record);
    }

    @Override
    public List<ScatterData> selectAll() {
        return scatterDataMapper.selectAll();
    }
}
