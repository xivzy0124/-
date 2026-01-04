// PricePredictController.java
package com.xy.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class PricePredictController {

    private static final Logger logger = LoggerFactory.getLogger(PricePredictController.class);

    // 从CSV文件加载的历史价格数据
    private static final Map<String, List<VegetablePriceData>> vegetablePriceHistory = new HashMap<>();
    private static final Map<String, List<String>> cityVegetables = new HashMap<>();

    // 初始化CSV数据
    static {
        loadPriceDataFromCSV();
    }

    /**
     * 从CSV文件加载价格数据
     */
    private static void loadPriceDataFromCSV() {
        try {
            // 尝试从不同路径加载CSV文件
            String[] possiblePaths = {
                    "D:/pytest/hefeng/merged_market_data_with_weather.csv",
                    "classpath:data/merged_market_data_with_weather.csv",
                    "./data/merged_market_data_with_weather.csv"
            };

            String csvFilePath = null;
            for (String path : possiblePaths) {
                if (path.startsWith("classpath:")) {
                    Resource resource = new ClassPathResource(path.substring(10));
                    if (resource.exists()) {
                        csvFilePath = resource.getFile().getAbsolutePath();
                        break;
                    }
                } else {
                    File file = new File(path);
                    if (file.exists()) {
                        csvFilePath = path;
                        break;
                    }
                }
            }

            if (csvFilePath == null) {
                logger.warn("未找到价格数据CSV文件，使用模拟数据");
                initializeMockData();
                return;
            }

            logger.info("加载价格数据文件: {}", csvFilePath);
            List<String> lines = Files.readAllLines(Paths.get(csvFilePath));

            // 跳过标题行
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().isEmpty()) continue;

                try {
                    String[] fields = parseCSVLine(line);
                    if (fields.length >= 16) {
                        VegetablePriceData data = new VegetablePriceData();
                        data.setAreaName(fields[0]);
                        data.setMeteringUnit(fields[1]);
                        data.setMinimumPrice(parseDoubleSafe(fields[2]));
                        data.setMiddlePrice(parseDoubleSafe(fields[3]));
                        data.setHighestPrice(parseDoubleSafe(fields[4]));
                        data.setVarietyName(fields[5]);
                        data.setUnitType(fields[6]);
                        data.setReportTime(fields[7]);
                        data.setMarketName(fields[8]);
                        data.setProvinceName(fields[9]);
                        data.setTempHigh(fields[10]);
                        data.setTempLow(fields[11]);
                        data.setWeather(fields[12]);
                        data.setWind(fields[13]);
                        data.setSourceFile(fields[14]);
                        data.setDataDate(fields[15]);

                        // 按蔬菜品种组织数据
                        String vegetable = data.getVarietyName();
                        if (!vegetablePriceHistory.containsKey(vegetable)) {
                            vegetablePriceHistory.put(vegetable, new ArrayList<>());
                        }
                        vegetablePriceHistory.get(vegetable).add(data);

                        // 记录城市可用的蔬菜品种
                        String city = data.getAreaName();
                        if (!cityVegetables.containsKey(city)) {
                            cityVegetables.put(city, new ArrayList<>());
                        }
                        if (!cityVegetables.get(city).contains(vegetable)) {
                            cityVegetables.get(city).add(vegetable);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("解析CSV行失败: {}, 错误: {}", line, e.getMessage());
                }
            }

            logger.info("成功加载 {} 种蔬菜的价格数据", vegetablePriceHistory.size());
            logger.info("覆盖 {} 个城市", cityVegetables.size());

        } catch (Exception e) {
            logger.error("加载价格数据失败: {}", e.getMessage());
            initializeMockData();
        }
    }

    /**
     * 解析CSV行，处理包含逗号的情况
     */
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString().trim());

        return fields.toArray(new String[0]);
    }

    private static double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 初始化模拟数据（备用）
     */
    private static void initializeMockData() {
        Map<String, List<Double>> mockPrices = new HashMap<>();
        mockPrices.put("大白菜", Arrays.asList(1.8, 1.9, 1.7, 1.8, 1.6, 1.9, 2.0));
        mockPrices.put("西红柿", Arrays.asList(2.3, 2.4, 2.2, 2.5, 2.3, 2.6, 2.4));
        mockPrices.put("土豆", Arrays.asList(2.5, 2.4, 2.6, 2.3, 2.5, 2.4, 2.6));
        mockPrices.put("黄瓜", Arrays.asList(3.8, 3.9, 3.7, 4.0, 3.8, 3.9, 4.1));
        mockPrices.put("茄子", Arrays.asList(3.5, 3.6, 3.4, 3.7, 3.5, 3.8, 3.6));
        mockPrices.put("青椒", Arrays.asList(4.5, 4.6, 4.4, 4.7, 4.5, 4.8, 4.6));
        mockPrices.put("空心菜", Arrays.asList(3.5, 3.6, 3.4, 3.7, 3.5, 3.8, 3.6));

        for (Map.Entry<String, List<Double>> entry : mockPrices.entrySet()) {
            String vegetable = entry.getKey();
            List<Double> prices = entry.getValue();

            for (int i = 0; i < prices.size(); i++) {
                VegetablePriceData data = new VegetablePriceData();
                data.setAreaName("北京市");
                data.setVarietyName(vegetable);
                data.setMiddlePrice(prices.get(i));
                data.setReportTime(LocalDate.now().minusDays(prices.size() - i - 1).toString());
                data.setWeather("晴");
                data.setTempHigh("25");
                data.setTempLow("15");

                if (!vegetablePriceHistory.containsKey(vegetable)) {
                    vegetablePriceHistory.put(vegetable, new ArrayList<>());
                }
                vegetablePriceHistory.get(vegetable).add(data);
            }

            cityVegetables.put("北京市", Arrays.asList("大白菜", "西红柿", "土豆", "黄瓜", "茄子", "青椒", "空心菜"));
        }
    }

    /**
     * 获取各市各类食品价格数据
     */
    @GetMapping("/cityFoodPrices")
    public ResponseEntity<Map<String, Object>> getCityFoodPrices(
            @RequestParam(required = false) String vegetable,
            @RequestParam(required = false) String date) {

        try {
            // 如果没有指定日期，使用最新日期
            String targetDate = (date != null) ? date : "2025-08-25";

            logger.info("获取各市食品价格数据, 蔬菜: {}, 日期: {}", vegetable, targetDate);

            // 从内存数据中过滤指定日期和蔬菜的数据
            List<Map<String, Object>> result = new ArrayList<>();

            for (Map.Entry<String, List<VegetablePriceData>> entry : vegetablePriceHistory.entrySet()) {
                String vegName = entry.getKey();

                // 如果指定了蔬菜，只返回该蔬菜的数据
                if (vegetable != null && !vegetable.equals(vegName)) {
                    continue;
                }

                for (VegetablePriceData data : entry.getValue()) {
                    if (targetDate.equals(data.getReportTime())) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("provinceName", data.getProvinceName());
                        item.put("areaName", data.getAreaName());
                        item.put("varietyName", data.getVarietyName());
                        item.put("middlePrice", data.getMiddlePrice());
                        item.put("meteringUnit", data.getMeteringUnit());
                        item.put("reportTime", data.getReportTime());
                        result.add(item);
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("code", "200");
            response.put("data", result);
            response.put("total", result.size());
            response.put("date", targetDate);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取各市食品价格失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResult("获取数据失败"));
        }
    }

    /**
     * 获取各省份各类食品的市均价
     */
    @GetMapping("/provinceAvgFoodPrices")
    public ResponseEntity<Map<String, Object>> getProvinceAvgFoodPrices(
            @RequestParam(required = false) String vegetable,
            @RequestParam(required = false) String date) {

        try {
            String targetDate = (date != null) ? date : "2025-08-25";

            logger.info("获取各省份食品市均价, 蔬菜: {}, 日期: {}", vegetable, targetDate);

            // 计算各省份的平均价格
            Map<String, Map<String, Object>> provinceData = new HashMap<>();

            for (Map.Entry<String, List<VegetablePriceData>> entry : vegetablePriceHistory.entrySet()) {
                String vegName = entry.getKey();

                if (vegetable != null && !vegetable.equals(vegName)) {
                    continue;
                }

                for (VegetablePriceData data : entry.getValue()) {
                    if (targetDate.equals(data.getReportTime())) {
                        String province = data.getProvinceName();
                        String veg = data.getVarietyName();

                        String key = province + "_" + veg;
                        if (!provinceData.containsKey(key)) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("provinceName", province);
                            item.put("varietyName", veg);
                            item.put("totalPrice", 0.0);
                            item.put("cityCount", 0);
                            item.put("meteringUnit", data.getMeteringUnit());
                            provinceData.put(key, item);
                        }

                        Map<String, Object> item = provinceData.get(key);
                        double totalPrice = (double) item.get("totalPrice") + data.getMiddlePrice();
                        int cityCount = (int) item.get("cityCount") + 1;

                        item.put("totalPrice", totalPrice);
                        item.put("cityCount", cityCount);
                        item.put("avgPrice", totalPrice / cityCount);
                    }
                }
            }

            // 转换为列表
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> item : provinceData.values()) {
                result.add(item);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("code", "200");
            response.put("data", result);
            response.put("total", result.size());
            response.put("date", targetDate);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取各省份食品市均价失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResult("获取数据失败"));
        }
    }

    /**
     * 获取可用的蔬菜列表（从实际数据中提取）
     */
    @GetMapping("/availableVegetables")
    public ResponseEntity<Map<String, Object>> getAvailableVegetables() {
        try {
            Set<String> vegetables = new HashSet<>();
            for (String vegetable : vegetablePriceHistory.keySet()) {
                vegetables.add(vegetable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("code", "200");
            response.put("data", new ArrayList<>(vegetables));
            response.put("total", vegetables.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取蔬菜列表失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResult("获取蔬菜列表失败"));
        }
    }

    /**
     * 调用本地DeepSeek模型进行价格预测
     */
    @PostMapping("/predictPrice")
    public ResponseEntity<Map<String, Object>> predictVegetablePrice(
            @RequestBody PredictionRequest request) {

        logger.info("=== 蔬菜价格预测开始 ===");
        logger.info("请求参数: {}", request);

        try {
            // 验证请求参数
            if (request.getVegetable() == null || request.getVegetable().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResult("蔬菜名称不能为空"));
            }
            if (request.getLocation() == null || request.getLocation().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResult("地点不能为空"));
            }

            // 构建预测输入
            String prompt = buildPredictionPrompt(request);

            // 调用本地DeepSeek模型
            String predictedPrice = callLocalDeepSeekModel(prompt);

            // 解析预测结果
            double price = parsePredictedPrice(predictedPrice, request.getVegetable(), request.getLocation());

            Map<String, Object> result = new HashMap<>();
            result.put("code", "200");
            result.put("vegetable", request.getVegetable());
            result.put("location", request.getLocation());
            result.put("predictedPrice", price);
            result.put("weatherCondition", request.getWeatherCondition());
            result.put("temperature", request.getTemperature());
            result.put("unit", "元/斤");

            logger.info("预测结果: {} {}元/斤", request.getVegetable(), price);
            logger.info("=== 蔬菜价格预测完成 ===");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("价格预测失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResult("价格预测失败: " + e.getMessage()));
        }
    }

    /**
     * 获取全国各省市预测价格
     */
    @GetMapping("/predictPrice/national")
    public ResponseEntity<Map<String, Object>> getNationalPredictedPrices(
            @RequestParam String vegetable) {

        logger.info("获取全国{}预测价格", vegetable);

        try {
            Map<String, Object> result = new HashMap<>();
            result.put("code", "200");
            result.put("vegetable", vegetable);
            result.put("data", generateNationalPredictedData(vegetable));
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("获取全国预测价格失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResult("获取预测价格失败"));
        }
    }

    /**
     * 获取城市可用的蔬菜列表
     */
    @GetMapping("/predictPrice/vegetables")
    public ResponseEntity<Map<String, Object>> getAvailableVegetables(
            @RequestParam String city) {

        logger.info("获取{}可用的蔬菜列表", city);

        try {
            List<String> vegetables = cityVegetables.getOrDefault(city, new ArrayList<>());
            // 如果没有该城市的数据，返回常用蔬菜列表
            if (vegetables.isEmpty()) {
                vegetables = Arrays.asList("大白菜", "西红柿", "土豆", "黄瓜", "茄子", "青椒", "空心菜");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("code", "200");
            result.put("city", city);
            result.put("vegetables", vegetables);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("获取蔬菜列表失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResult("获取蔬菜列表失败"));
        }
    }

    private String buildPredictionPrompt(PredictionRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个蔬菜价格预测专家。请根据以下信息预测").append(request.getVegetable()).append("明天的价格：\n\n");
        prompt.append("地点：").append(request.getLocation()).append("\n");
        prompt.append("天气状况：").append(request.getWeatherCondition()).append("\n");
        prompt.append("温度：").append(request.getTemperature()).append("°C\n");

        // 添加历史价格数据
        List<VegetablePriceData> historyData = getHistoricalData(request.getVegetable(), request.getLocation());
        if (!historyData.isEmpty()) {
            prompt.append("历史价格数据：\n");
            for (int i = 0; i < Math.min(historyData.size(), 7); i++) {
                VegetablePriceData data = historyData.get(i);
                prompt.append("- ").append(data.getReportTime())
                        .append(": ").append(String.format("%.2f", data.getMiddlePrice()))
                        .append("元/斤 (天气:").append(data.getWeather())
                        .append(", 温度:").append(data.getTempHigh()).append("/").append(data.getTempLow())
                        .append(")\n");
            }
        } else {
            prompt.append("历史价格：暂无该地区的历史数据\n");
        }

        prompt.append("\n请综合考虑天气因素、历史价格趋势和市场供需关系，预测明天的价格。");
        prompt.append("请只返回一个数字，表示预测的价格（单位：元/斤），不要包含任何其他文字。");

        return prompt.toString();
    }

    /**
     * 获取指定蔬菜和地点的历史数据
     */
    private List<VegetablePriceData> getHistoricalData(String vegetable, String location) {
        List<VegetablePriceData> result = new ArrayList<>();
        List<VegetablePriceData> allData = vegetablePriceHistory.get(vegetable);

        if (allData != null) {
            for (VegetablePriceData data : allData) {
                if (data.getAreaName().contains(location) || location.contains(data.getAreaName())) {
                    result.add(data);
                }
            }
        }

        // 按日期排序（最近的在前）
        result.sort((a, b) -> b.getReportTime().compareTo(a.getReportTime()));
        return result;
    }

    private String callLocalDeepSeekModel(String prompt) throws IOException, InterruptedException {
        String pythonScript = "D:/pytest/hefeng/DeepSeek-R1/predict.py";
        File scriptFile = new File(pythonScript);

        if (!scriptFile.exists()) {
            createPythonScript(pythonScript);
        }

        ProcessBuilder pb = new ProcessBuilder("python", pythonScript, prompt);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 读取模型输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("模型调用失败，退出码: " + exitCode);
        }

        return output.toString();
    }

    private void createPythonScript(String scriptPath) throws IOException {
        String pythonCode =
                "import sys\n" +
                        "import torch\n" +
                        "from transformers import AutoTokenizer, AutoModelForCausalLM\n" +
                        "import re\n" +
                        "\n" +
                        "def load_model():\n" +
                        "    model_path = \"D:/pytest/hefeng/DeepSeek-R1/DeepSeek-R1\"\n" +
                        "    tokenizer = AutoTokenizer.from_pretrained(model_path)\n" +
                        "    model = AutoModelForCausalLM.from_pretrained(\n" +
                        "        model_path,\n" +
                        "        torch_dtype=torch.float16,\n" +
                        "        device_map=\"auto\"\n" +
                        "    )\n" +
                        "    return tokenizer, model\n" +
                        "\n" +
                        "def predict_price(prompt):\n" +
                        "    try:\n" +
                        "        tokenizer, model = load_model()\n" +
                        "        \n" +
                        "        # 构建更明确的指令\n" +
                        "        full_prompt = f\"\"\"{prompt}\n" +
                        "\n" +
                        "请只返回一个数字，不要任何解释。\"\"\"\n" +
                        "        \n" +
                        "        inputs = tokenizer(full_prompt, return_tensors=\"pt\", max_length=1024, truncation=True)\n" +
                        "        with torch.no_grad():\n" +
                        "            outputs = model.generate(\n" +
                        "                inputs.input_ids,\n" +
                        "                max_new_tokens=50,\n" +
                        "                do_sample=True,\n" +
                        "                temperature=0.3,\n" +
                        "                pad_token_id=tokenizer.eos_token_id,\n" +
                        "                repetition_penalty=1.1\n" +
                        "            )\n" +
                        "        \n" +
                        "        response = tokenizer.decode(outputs[0], skip_special_tokens=True)\n" +
                        "        \n" +
                        "        # 提取价格数字 - 更精确的匹配\n" +
                        "        price_pattern = r'[\\d]+\\.?[\\d]*'\n" +
                        "        prices = re.findall(price_pattern, response)\n" +
                        "        \n" +
                        "        if prices:\n" +
                        "            # 取最后一个数字（通常是最相关的）\n" +
                        "            return prices[-1]\n" +
                        "        else:\n" +
                        "            # 基于规则的回退预测\n" +
                        "            return fallback_prediction(prompt)\n" +
                        "            \n" +
                        "    except Exception as e:\n" +
                        "        print(f\"模型调用错误: {e}\", file=sys.stderr)\n" +
                        "        return fallback_prediction(prompt)\n" +
                        "\n" +
                        "def fallback_prediction(prompt):\n" +
                        "    \"\"\"基于规则的备选预测\"\"\"\n" +
                        "    # 根据天气状况调整价格\n" +
                        "    base_prices = {\n" +
                        "        '大白菜': 2.0, '西红柿': 2.8, '土豆': 2.6, '黄瓜': 3.5, \n" +
                        "        '茄子': 3.2, '青椒': 4.2, '空心菜': 3.5\n" +
                        "    }\n" +
                        "    \n" +
                        "    # 获取蔬菜名称\n" +
                        "    vegetable = None\n" +
                        "    for veg in base_prices:\n" +
                        "        if veg in prompt:\n" +
                        "            vegetable = veg\n" +
                        "            break\n" +
                        "    \n" +
                        "    base_price = base_prices.get(vegetable, 3.0)\n" +
                        "    \n" +
                        "    # 天气影响因子\n" +
                        "    weather_factor = 1.0\n" +
                        "    if '雨' in prompt or '雪' in prompt:\n" +
                        "        weather_factor = 1.2  # 恶劣天气价格上涨\n" +
                        "    elif '晴' in prompt and '高温' in prompt:\n" +
                        "        weather_factor = 1.1  # 高温晴天价格略涨\n" +
                        "    elif '晴' in prompt:\n" +
                        "        weather_factor = 0.95  # 适宜天气价格略降\n" +
                        "    \n" +
                        "    predicted_price = base_price * weather_factor\n" +
                        "    return f\"{predicted_price:.2f}\"\n" +
                        "\n" +
                        "if __name__ == \"__main__\":\n" +
                        "    prompt = sys.argv[1]\n" +
                        "    result = predict_price(prompt)\n" +
                        "    print(result)\n";

        try (FileWriter writer = new FileWriter(scriptPath)) {
            writer.write(pythonCode);
        }
    }

    private double parsePredictedPrice(String predictedPrice, String vegetable, String location) {
        try {
            double price = Double.parseDouble(predictedPrice.trim());

            // 价格合理性检查
            if (price <= 0 || price > 100) {
                logger.warn("预测价格不合理: {}, 使用备选方案", price);
                return getReasonablePrice(vegetable, location);
            }

            return price;
        } catch (NumberFormatException e) {
            logger.warn("无法解析预测价格: {}, 使用备选方案", predictedPrice);
            return getReasonablePrice(vegetable, location);
        }
    }

    private double getReasonablePrice(String vegetable, String location) {
        // 基于历史数据的合理价格计算
        List<VegetablePriceData> history = getHistoricalData(vegetable, location);
        if (!history.isEmpty()) {
            double sum = 0;
            int count = 0;
            for (int i = 0; i < Math.min(history.size(), 5); i++) {
                sum += history.get(i).getMiddlePrice();
                count++;
            }
            return sum / count;
        }

        // 默认价格
        Map<String, Double> defaultPrices = new HashMap<>();
        defaultPrices.put("大白菜", 2.0);
        defaultPrices.put("西红柿", 2.8);
        defaultPrices.put("土豆", 2.6);
        defaultPrices.put("黄瓜", 3.5);
        defaultPrices.put("茄子", 3.2);
        defaultPrices.put("青椒", 4.2);
        defaultPrices.put("空心菜", 3.5);

        return defaultPrices.getOrDefault(vegetable, 3.0);
    }

    private List<Map<String, Object>> generateNationalPredictedData(String vegetable) {
        List<Map<String, Object>> data = new ArrayList<>();

        // 使用CSV数据中的城市
        Set<String> processedCities = new HashSet<>();
        List<VegetablePriceData> vegetableData = vegetablePriceHistory.get(vegetable);

        if (vegetableData != null) {
            // 按城市分组，计算平均价格
            Map<String, List<Double>> cityPrices = new HashMap<>();
            for (VegetablePriceData item : vegetableData) {
                String city = item.getAreaName();
                if (!cityPrices.containsKey(city)) {
                    cityPrices.put(city, new ArrayList<>());
                }
                cityPrices.get(city).add(item.getMiddlePrice());
            }

            // 生成预测数据
            Random random = new Random();
            for (Map.Entry<String, List<Double>> entry : cityPrices.entrySet()) {
                String city = entry.getKey();
                List<Double> prices = entry.getValue();

                // 计算历史平均价格
                double avgPrice = prices.stream().mapToDouble(Double::doubleValue).average().orElse(3.0);

                // 添加一些随机波动
                double variation = (random.nextDouble() - 0.5) * 0.5; // ±25%的波动
                double predictedPrice = avgPrice * (1 + variation);

                Map<String, Object> item = new HashMap<>();
                item.put("name", city);
                item.put("value", Double.parseDouble(String.format("%.2f", predictedPrice)));
                data.add(item);

                processedCities.add(city);
            }
        }

        // 如果没有足够数据，添加主要城市
        String[] majorCities = {"北京市", "上海市", "广州市", "深圳市", "杭州市", "南京市", "武汉市", "成都市", "西安市", "重庆市"};
        for (String city : majorCities) {
            if (!processedCities.contains(city)) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", city);
                item.put("value", Double.parseDouble(String.format("%.2f", getReasonablePrice(vegetable, city))));
                data.add(item);
            }
        }

        return data;
    }

    private Map<String, Object> createErrorResult(String message) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("code", "500");
        errorResult.put("msg", message);
        return errorResult;
    }

    // 请求参数类
    public static class PredictionRequest {
        private String vegetable;
        private String location;
        private String weatherCondition;
        private String temperature;

        // getters and setters
        public String getVegetable() { return vegetable; }
        public void setVegetable(String vegetable) { this.vegetable = vegetable; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getWeatherCondition() { return weatherCondition; }
        public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
        public String getTemperature() { return temperature; }
        public void setTemperature(String temperature) { this.temperature = temperature; }

        @Override
        public String toString() {
            return "PredictionRequest{" +
                    "vegetable='" + vegetable + '\'' +
                    ", location='" + location + '\'' +
                    ", weatherCondition='" + weatherCondition + '\'' +
                    ", temperature='" + temperature + '\'' +
                    '}';
        }
    }

    // 蔬菜价格数据类
    public static class VegetablePriceData {
        private String areaName;
        private String meteringUnit;
        private double minimumPrice;
        private double middlePrice;
        private double highestPrice;
        private String varietyName;
        private String unitType;
        private String reportTime;
        private String marketName;
        private String provinceName;
        private String tempHigh;
        private String tempLow;
        private String weather;
        private String wind;
        private String sourceFile;
        private String dataDate;

        // getters and setters
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public String getMeteringUnit() { return meteringUnit; }
        public void setMeteringUnit(String meteringUnit) { this.meteringUnit = meteringUnit; }
        public double getMinimumPrice() { return minimumPrice; }
        public void setMinimumPrice(double minimumPrice) { this.minimumPrice = minimumPrice; }
        public double getMiddlePrice() { return middlePrice; }
        public void setMiddlePrice(double middlePrice) { this.middlePrice = middlePrice; }
        public double getHighestPrice() { return highestPrice; }
        public void setHighestPrice(double highestPrice) { this.highestPrice = highestPrice; }
        public String getVarietyName() { return varietyName; }
        public void setVarietyName(String varietyName) { this.varietyName = varietyName; }
        public String getUnitType() { return unitType; }
        public void setUnitType(String unitType) { this.unitType = unitType; }
        public String getReportTime() { return reportTime; }
        public void setReportTime(String reportTime) { this.reportTime = reportTime; }
        public String getMarketName() { return marketName; }
        public void setMarketName(String marketName) { this.marketName = marketName; }
        public String getProvinceName() { return provinceName; }
        public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
        public String getTempHigh() { return tempHigh; }
        public void setTempHigh(String tempHigh) { this.tempHigh = tempHigh; }
        public String getTempLow() { return tempLow; }
        public void setTempLow(String tempLow) { this.tempLow = tempLow; }
        public String getWeather() { return weather; }
        public void setWeather(String weather) { this.weather = weather; }
        public String getWind() { return wind; }
        public void setWind(String wind) { this.wind = wind; }
        public String getSourceFile() { return sourceFile; }
        public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }
        public String getDataDate() { return dataDate; }
        public void setDataDate(String dataDate) { this.dataDate = dataDate; }

        @Override
        public String toString() {
            return "VegetablePriceData{" +
                    "areaName='" + areaName + '\'' +
                    ", varietyName='" + varietyName + '\'' +
                    ", middlePrice=" + middlePrice +
                    ", reportTime='" + reportTime + '\'' +
                    ", weather='" + weather + '\'' +
                    '}';
        }
    }
}