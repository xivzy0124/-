package com.xy.service;

import com.xy.mapper.ExportTOPMapper;
import com.xy.pojo.DailyVegetablePrices;
import com.xy.pojo.ExportTOP;
import com.xy.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Service
public class ExportTopService {
    @Autowired
    ExportTOPMapper e;
    public List<ExportTOP> topN(String name){
        return e.topN(name);
    }
}
