package com.xy.controller;

import com.xy.pojo.ProvinceDishPrice;
import com.xy.service.DishPriceService;
import com.xy.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜品价格控制器
 */
@RestController
@RequestMapping("/api/dishprice")
public class DishPriceController {
    @Autowired
    private DishPriceService dishPriceService;
    
    /**
     * 根据菜名和年份获取各省价格信息
     * @param dishName 菜品名称
     * @param year 年份
     * @return 各省菜品价格信息列表
     */
    @GetMapping("/provincePrices")
    public JsonResult getProvincePricesByDishAndYear(
            @RequestParam String dishName,
            @RequestParam Integer year) {
        try {
            List<ProvinceDishPrice> list = dishPriceService.getProvincePricesByDishAndYear(dishName, year);
            if (list == null || list.isEmpty()) {
                return new JsonResult("1", "数据为空");
            }
            return new JsonResult(list, "请求成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult("1", "服务器错误: " + e.getMessage());
        }
    }
} 