package org.v.end.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.v.end.mapper.ProcessedPricesMapper;
import org.v.end.model.entity.ProcessedPrices;
import org.v.end.service.ProcessedPricesService;
import java.util.List;

@Service
public class ProcessedPricesServiceImpl implements ProcessedPricesService {

    @Autowired
    private ProcessedPricesMapper processedPricesMapper;

    @Override
    public int insert(ProcessedPrices record) {
        return processedPricesMapper.insert(record);
    }

    @Override
    public List<ProcessedPrices> selectList(ProcessedPrices record) {
        return processedPricesMapper.selectList(record);
    }

    @Override
    public List<ProcessedPrices> selectAll() {
        return processedPricesMapper.selectAll();
    }
}
