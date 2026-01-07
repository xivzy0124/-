package com.xy.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class WeatherController {

    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);

    private static final String API_KEY = "a3c62f00504f4bf1a612017e11451795";
    private static final String BASE_URL = "https://kw3v59cm5k.re.qweatherapi.com/v7/weather";

    private final RestTemplate restTemplate;

    public WeatherController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/7d")
    public ResponseEntity<String> get7DayWeather(@RequestParam String location) {
        String url = BASE_URL + "/7d?location=" + location + "&lang=zh&unit=m";

        logger.info("=== 天气API调用开始 ===");
        logger.info("接收到的location参数: {}", location);
        logger.info("构建的完整URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-QW-Api-Key", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 明确要求不压缩
        headers.set("Accept-Encoding", "identity");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            logger.info("准备调用天气API...");

            // 使用byte数组接收响应，手动处理可能的gzip压缩
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, byte[].class);

            logger.info("API响应状态: {}", response.getStatusCode());

            byte[] responseBody = response.getBody();
            String result;

            if (responseBody != null) {
                // 检查是否是gzip压缩数据（gzip魔数：1F 8B）
                if (responseBody.length >= 2 &&
                        responseBody[0] == (byte) 0x1F &&
                        responseBody[1] == (byte) 0x8B) {

                    logger.info("检测到gzip压缩数据，进行解压缩...");
                    try {
                        // 手动解压缩gzip数据
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
                        logger.info("gzip解压缩成功，解压后长度: {}", result.length());

                    } catch (Exception e) {
                        logger.error("gzip解压缩失败: {}", e.getMessage());
                        // 如果解压缩失败，尝试直接转换为字符串
                        result = new String(responseBody, "UTF-8");
                    }
                } else {
                    // 不是gzip数据，直接转换
                    result = new String(responseBody, "UTF-8");
                }

                logger.info("处理后的响应内容前200字符: {}",
                        result.length() > 200 ? result.substring(0, 200) + "..." : result);

            } else {
                logger.warn("API响应体为空");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"code\":\"500\",\"msg\":\"天气数据为空\"}");
            }

            logger.info("=== 天气API调用成功 ===");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("=== 天气API调用失败 ===");
            logger.error("异常信息: {}", e.getMessage());
            logger.error("异常堆栈: ", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"code\":\"500\",\"msg\":\"天气数据获取失败: " + e.getMessage() + "\"}");
        }
    }
}