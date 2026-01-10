package org.v.end.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.v.end.common.Result;
import org.v.end.model.dto.MapDataDTO;
import org.v.end.service.MapDataService;

import java.util.List;

@RestController
@RequestMapping("/api/map")
public class MapDataController {

    @Autowired
    private MapDataService mapDataService;

    @GetMapping("/china")
    public Result<MapDataDTO> getChinaMap() {
        try {
            MapDataDTO mapData = mapDataService.getChinaMap();
            if (mapData != null) {
                return Result.success(mapData);
            } else {
                return Result.error("获取中国地图数据失败");
            }
        } catch (Exception e) {
            return Result.error("获取中国地图数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/province/{adcode}")
    public Result<MapDataDTO> getProvinceMap(@PathVariable String adcode) {
        try {
            MapDataDTO mapData = mapDataService.getProvinceMap(adcode);
            if (mapData != null) {
                return Result.success(mapData);
            } else {
                return Result.error("获取省份地图数据失败");
            }
        } catch (Exception e) {
            return Result.error("获取省份地图数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/city/{adcode}")
    public Result<MapDataDTO> getCityMap(@PathVariable String adcode) {
        try {
            MapDataDTO mapData = mapDataService.getCityMap(adcode);
            if (mapData != null) {
                return Result.success(mapData);
            } else {
                return Result.error("获取城市地图数据失败");
            }
        } catch (Exception e) {
            return Result.error("获取城市地图数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/regions/{adcode}")
    public Result<List<String>> getRegionNames(@PathVariable String adcode) {
        try {
            List<String> regionNames = mapDataService.getRegionNames(adcode);
            return Result.success(regionNames);
        } catch (Exception e) {
            return Result.error("获取区域名称失败: " + e.getMessage());
        }
    }
}
