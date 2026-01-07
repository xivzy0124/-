package org.v.end.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDTO {
    private String fxDate;
    private String tempMax;
    private String tempMin;
    private String textDay;
}
