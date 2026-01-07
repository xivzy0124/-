package org.v.end.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.v.end.common.Result;
import org.v.end.model.dto.WeatherDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private static final String API_HOST = "https://kw3v59cm5k.re.qweatherapi.com";
    private static final String API_TOKEN = "a3c62f00504f4bf1a612017e11451795";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/7day")
    public Result<List<WeatherDTO>> get7DayWeather(@RequestParam String location) {
        String url = API_HOST + "/v7/weather/7d?location=" + location + "&key=" + API_TOKEN;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            byte[] responseBody = response.getBody();
            String result;

            if (responseBody != null) {
                if (responseBody.length >= 2 &&
                        responseBody[0] == (byte) 0x1F &&
                        responseBody[1] == (byte) 0x8B) {
                    try {
                        ByteArrayInputStream bis = new ByteArrayInputStream(responseBody);
                        GZIPInputStream gis = new GZIPInputStream(bis);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();

                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = gis.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }

                        result = bos.toString("UTF-8");
                        gis.close();
                        bis.close();
                    } catch (Exception e) {
                        result = new String(responseBody, "UTF-8");
                    }
                } else {
                    result = new String(responseBody, "UTF-8");
                }

                JsonNode rootNode = objectMapper.readTree(result);
                JsonNode dailyArray = rootNode.path("daily");

                List<WeatherDTO> weatherList = new ArrayList<>();
                for (JsonNode dayNode : dailyArray) {
                    WeatherDTO dto = new WeatherDTO();
                    dto.setFxDate(dayNode.path("fxDate").asText());
                    dto.setTempMax(dayNode.path("tempMax").asText());
                    dto.setTempMin(dayNode.path("tempMin").asText());
                    dto.setTextDay(dayNode.path("textDay").asText());
                    weatherList.add(dto);
                }

                return Result.success(weatherList);
            } else {
                return Result.error("天气数据为空");
            }

        } catch (Exception e) {
            return Result.error("天气数据获取失败: " + e.getMessage());
        }
    }
}
