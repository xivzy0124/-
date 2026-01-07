package org.v.end.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.v.end.common.Result;
import org.v.end.model.dto.YearlyTrendDTO;
import org.v.end.model.entity.PriceBreakdownByProvince;
import org.v.end.service.PriceBreakdownByProvinceService;

import java.util.List;

@RestController
@RequestMapping("/api/price")
public class PriceBreakdownByProvinceController {

    @Autowired
    private PriceBreakdownByProvinceService priceBreakdownByProvinceService;

    @GetMapping("/yearlyTrend")
    public Result<List<YearlyTrendDTO>> getYearlyTrend(
            @RequestParam String provinceName,
            @RequestParam String varietyName) {
        try {
            List<YearlyTrendDTO> trendData = priceBreakdownByProvinceService.getYearlyTrendByProvince(provinceName, varietyName);
            return Result.success(trendData);
        } catch (Exception e) {
            return Result.error("获取年度趋势数据失败: " + e.getMessage());
        }
    }

    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody PriceBreakdownByProvince record) {
        try {
            int result = priceBreakdownByProvinceService.insert(record);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("插入数据失败: " + e.getMessage());
        }
    }

    @PostMapping("/selectList")
    public Result<List<PriceBreakdownByProvince>> selectList(@RequestBody PriceBreakdownByProvince record) {
        try {
            List<PriceBreakdownByProvince> result = priceBreakdownByProvinceService.selectList(record);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/selectAll")
    public Result<List<PriceBreakdownByProvince>> selectAll() {
        try {
            List<PriceBreakdownByProvince> result = priceBreakdownByProvinceService.selectAll();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询所有数据失败: " + e.getMessage());
        }
    }
}
