package org.v.end.service;

import org.v.end.model.dto.MapDataDTO;

import java.util.List;

public interface MapDataService {
    MapDataDTO getChinaMap();
    MapDataDTO getProvinceMap(String adcode);
    MapDataDTO getCityMap(String adcode);
    List<String> getRegionNames(String adcode);
}
