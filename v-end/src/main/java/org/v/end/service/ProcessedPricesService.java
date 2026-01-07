package org.v.end.service;

import org.v.end.model.entity.ProcessedPrices;
import java.util.List;

public interface ProcessedPricesService {
    int insert(ProcessedPrices record);
    List<ProcessedPrices> selectList(ProcessedPrices record);
    List<ProcessedPrices> selectAll();
}
