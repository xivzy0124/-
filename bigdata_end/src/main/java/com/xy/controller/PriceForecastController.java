package com.xy.controller;

import com.xy.pojo.PriceForecast;
import com.xy.service.PriceForecastService;
import com.xy.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/price/forecast")
public class PriceForecastController {
    @Autowired
    private PriceForecastService priceForecastService;

    /**
     * 获取所有价格预测信息。
     * 该接口用于获取数据库中所有的价格预测记录，便于前端展示整体预测情况。
     *
     * @return JsonResult
     *         - 成功：data为所有价格预测数据列表
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @GetMapping("/all")
    public JsonResult getAllPriceForecasts() {
        try {
            List<PriceForecast> list = priceForecastService.getAllPriceForecasts();
            if (list.isEmpty()) {
                return new JsonResult("1", "数据为空");
            }
            return new JsonResult(list, "请求成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult("1", "服务器错误");
        }
    }
    
    /**
     * 根据省份和产品名称获取价格预测信息。
     * 该接口用于获取指定省份和产品的价格预测数据，便于前端进行区域和品种的预测对比。
     *
     * @param province 省份名称（String，必填）
     * @param productName 产品名称（String，必填）
     * @return JsonResult
     *         - 成功：data为筛选后的价格预测数据列表
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @GetMapping("/byProvince")
    public JsonResult getPriceForecastsByProvinceAndProduct(
            @RequestParam String province,
            @RequestParam String productName) {
        try {
            List<PriceForecast> list = priceForecastService.getPriceForecastsByProvinceAndProduct(province, productName);
            if (list.isEmpty()) {
                return new JsonResult("1", "数据为空");
            }
            return new JsonResult(list, "请求成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult("1", "服务器错误");
        }
    }
    
    /**
     * 根据预测目标日期范围获取价格预测信息。
     * 该接口用于获取指定日期范围内的价格预测数据，便于前端进行时间区间的预测分析。
     *
     * @param startDate 开始日期（Date，必填，格式yyyy-MM-dd）
     * @param endDate 结束日期（Date，必填，格式yyyy-MM-dd）
     * @return JsonResult
     *         - 成功：data为筛选后的价格预测数据列表
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @GetMapping("/byDateRange")
    public JsonResult getPriceForecastsByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            List<PriceForecast> list = priceForecastService.getPriceForecastsByDateRange(startDate, endDate);
            if (list.isEmpty()) {
                return new JsonResult("1", "数据为空");
            }
            return new JsonResult(list, "请求成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult("1", "服务器错误");
        }
    }
} 