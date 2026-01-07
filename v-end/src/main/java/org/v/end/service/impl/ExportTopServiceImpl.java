package org.v.end.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.v.end.mapper.ExportTopMapper;
import org.v.end.model.entity.ExportTop;
import org.v.end.service.ExportTopService;
import java.util.List;

@Service
public class ExportTopServiceImpl implements ExportTopService {

    @Autowired
    private ExportTopMapper exportTopMapper;

    @Override
    public int insert(ExportTop record) {
        return exportTopMapper.insert(record);
    }

    @Override
    public List<ExportTop> selectList(ExportTop record) {
        return exportTopMapper.selectList(record);
    }

    @Override
    public List<ExportTop> selectAll() {
        return exportTopMapper.selectAll();
    }
}
