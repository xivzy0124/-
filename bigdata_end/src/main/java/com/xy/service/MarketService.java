package com.xy.service;

import com.xy.mapper.ScatterDataMapper;
import com.xy.pojo.ScatterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketService {
    @Autowired
    ScatterDataMapper scatter;

    public List<ScatterData> scatterByProvince(String provincename,String greens){
        return scatter.selectByProvince(provincename,greens);
    }
}
