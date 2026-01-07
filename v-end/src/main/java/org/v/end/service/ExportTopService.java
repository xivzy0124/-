package org.v.end.service;

import org.v.end.model.entity.ExportTop;
import java.util.List;

public interface ExportTopService {
    int insert(ExportTop record);
    List<ExportTop> selectList(ExportTop record);
    List<ExportTop> selectAll();
}
