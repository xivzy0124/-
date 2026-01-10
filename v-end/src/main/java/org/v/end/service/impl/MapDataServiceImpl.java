package org.v.end.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.v.end.model.dto.MapDataDTO;
import org.v.end.service.MapDataService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MapDataServiceImpl implements MapDataService {

    private static final String DATAV_API_URL = "https://geo.datav.aliyun.com/areas_v3/bound";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MapDataServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public MapDataDTO getChinaMap() {
        return getMapData("100000_full");
    }

    @Override
    public MapDataDTO getProvinceMap(String adcode) {
        return getMapData(adcode + "_full");
    }

    @Override
    public MapDataDTO getCityMap(String adcode) {
        return getMapData(adcode + "_full");
    }

    @Override
    public List<String> getRegionNames(String adcode) {
        try {
            MapDataDTO mapData = getMapData(adcode + "_full");
            if (mapData != null && mapData.getFeatures() != null) {
                List<String> regionNames = new ArrayList<>();
                for (MapDataDTO.MapFeature feature : mapData.getFeatures()) {
                    if (feature.getProperties() != null) {
                        String name = (String) feature.getProperties().get("name");
                        if (name != null) {
                            regionNames.add(name);
                        }
                    }
                }
                return regionNames;
            }
        } catch (Exception e) {
            System.err.println("获取区域名称失败: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private MapDataDTO getMapData(String fileName) {
        try {
            String url = DATAV_API_URL + "/" + fileName + ".json";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "Mozilla/5.0");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseGeoJson(response.getBody());
            } else {
                System.err.println("请求失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("获取地图数据失败: " + e.getMessage());
        }
        return null;
    }

    private MapDataDTO parseGeoJson(String geoJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(geoJson);
            
            MapDataDTO mapDataDTO = new MapDataDTO();
            mapDataDTO.setType(rootNode.path("type").asText());
            
            JsonNode featuresNode = rootNode.path("features");
            List<MapDataDTO.MapFeature> features = new ArrayList<>();
            
            // 如果 features 为空或不是数组，直接返回空列表
            if (featuresNode.isMissingNode() || !featuresNode.isArray()) {
                mapDataDTO.setFeatures(features);
                return mapDataDTO;
            }

            for (JsonNode featureNode : featuresNode) {
                MapDataDTO.MapFeature feature = new MapDataDTO.MapFeature();
                feature.setType(featureNode.path("type").asText());
                
                // 1. 解析 properties (保持你原有的逻辑，用 convertValue 更简单)
                JsonNode propertiesNode = featureNode.path("properties");
                Map<String, Object> properties = objectMapper.convertValue(propertiesNode, Map.class);
                feature.setProperties(properties);
                
                // 2. 解析 geometry
                JsonNode geometryNode = featureNode.path("geometry");
                Map<String, Object> geometry = new HashMap<>();
                geometry.put("type", geometryNode.path("type").asText());
                
                // --- 核心修改点开始 ---
                // 以前的手动循环删掉，直接用 convertValue 转为 List
                JsonNode coordinatesNode = geometryNode.path("coordinates");
                if (!coordinatesNode.isMissingNode()) {
                    // 自动处理任意层级的 List<List<...>>
                    Object coordinates = objectMapper.convertValue(coordinatesNode, List.class);
                    geometry.put("coordinates", coordinates);
                }
                // --- 核心修改点结束 ---
                
                feature.setGeometry(geometry);
                features.add(feature);
            }
            
            mapDataDTO.setFeatures(features);
            return mapDataDTO;
            
        } catch (Exception e) {
            System.err.println("解析GeoJSON失败: " + e.getMessage());
            e.printStackTrace(); // 建议打印堆栈以便调试
            return null;
        }
    }
}
