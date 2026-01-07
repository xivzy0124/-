package org.v.end.service;

import org.v.end.model.entity.ScatterData;
import java.util.List;

public interface ScatterDataService {
    int insert(ScatterData record);
    List<ScatterData> selectList(ScatterData record);
    List<ScatterData> selectAll();
}
