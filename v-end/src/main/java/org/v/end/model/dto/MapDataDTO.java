package org.v.end.model.dto;

import java.util.List;
import java.util.Map;

public class MapDataDTO {
    private String type;
    private List<MapFeature> features;

    public static class MapFeature {
        private String type;
        private Map<String, Object> properties;
        private Map<String, Object> geometry;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }

        public Map<String, Object> getGeometry() {
            return geometry;
        }

        public void setGeometry(Map<String, Object> geometry) {
            this.geometry = geometry;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<MapFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<MapFeature> features) {
        this.features = features;
    }
}
