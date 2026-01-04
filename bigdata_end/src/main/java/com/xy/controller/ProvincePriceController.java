package com.xy.controller;

import com.xy.pojo.PriceBreakdownByProvince;
import com.xy.service.ProvincePrice;
import com.xy.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 省级价格控制器
 */
@RestController
@RequestMapping("/provinceAndPrice")
public class ProvincePriceController {
    @Autowired
    private ProvincePrice p;
    
    /**
     * 根据品种名称查询各省份的价格明细。
     * 该接口用于获取指定品种在全国各省的价格明细，便于前端进行区域对比分析。
     *
     * @param query 请求体，Map结构，必须包含以下字段：
     *              - name (String, 必填)：品种名称
     * @return JsonResult
     *         - 成功：data为省级价格明细列表
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @PostMapping("/byName")
    public JsonResult byName(@RequestBody Map<String, Object> query){
        List<PriceBreakdownByProvince> list = p.selectByName(query.get("name").toString());
        if(list==null)
            return new JsonResult("1","服务器错误");
        return new JsonResult(list);
    }
    
    /**
     * 计算指定品种的总交易价值（均价*交易量的总和）。
     * 该接口用于获取某品种在所有省份的总交易价值，便于前端展示品种市场规模。
     *
     * @param query 请求体，Map结构，必须包含以下字段：
     *              - name (String, 必填)：品种名称
     * @return JsonResult
     *         - 成功：data中包含totalValue字段（BigDecimal类型）
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @PostMapping("/totalValue")
    public JsonResult getTotalValue(@RequestBody Map<String, Object> query) {
        BigDecimal totalValue = p.calculateTotalValue(query.get("name").toString());
        if (totalValue == null)
            return new JsonResult("1", "数据不存在或服务器错误");
        Map<String, Object> result = new HashMap<>();
        result.put("totalValue", totalValue);
        return new JsonResult(result);
    }
    
    /**
     * 计算指定品种的总交易量。
     * 该接口用于获取某品种在所有省份的总交易量，便于前端展示品种流通规模。
     *
     * @param query 请求体，Map结构，必须包含以下字段：
     *              - name (String, 必填)：品种名称
     * @return JsonResult
     *         - 成功：data中包含totalVolume字段（BigDecimal类型）
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @PostMapping("/totalVolume")
    public JsonResult getTotalVolume(@RequestBody Map<String, Object> query) {
        BigDecimal totalVolume = p.calculateTotalVolume(query.get("name").toString());
        if (totalVolume == null)
            return new JsonResult("1", "数据不存在或服务器错误");
        Map<String, Object> result = new HashMap<>();
        result.put("totalVolume", totalVolume);
        return new JsonResult(result);
    }
    
    /**
     * 统计指定品种在多少个市场有售。
     * 该接口用于获取某品种在全国范围内的市场覆盖数量，便于前端展示品种流通广度。
     *
     * @param query 请求体，Map结构，必须包含以下字段：
     *              - name (String, 必填)：品种名称
     * @return JsonResult
     *         - 成功：data中包含marketCount字段（Integer类型）
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @PostMapping("/marketCount")
    public JsonResult getMarketCount(@RequestBody Map<String, Object> query) {
        Integer marketCount = p.countMarkets(query.get("name").toString());
        if (marketCount == null)
            return new JsonResult("1", "数据不存在或服务器错误");
        Map<String, Object> result = new HashMap<>();
        result.put("marketCount", marketCount);
        return new JsonResult(result);
    }
}
