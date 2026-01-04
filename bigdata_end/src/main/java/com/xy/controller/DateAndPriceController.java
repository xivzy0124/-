package com.xy.controller;

import com.xy.pojo.DailyVegetablePrices;
import com.xy.service.DateAndPriceService;
import com.xy.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dateAndPrice")
public class DateAndPriceController {
    @Autowired
    DateAndPriceService dp;

    /**
     * （已弃用）根据菜名查询每日价格数据，提供面积图数据。
     * 该接口用于获取某种蔬菜的历史每日价格，主要用于前端面积图展示。
     *
     * @param query 请求体，Map结构，必须包含以下字段：
     *              - name (String, 必填)：蔬菜名称
     * @return JsonResult
     *         - 成功：data为每日价格数据列表
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @PostMapping("/day")
    public JsonResult day(@RequestBody Map<String, Object> query){
        List<DailyVegetablePrices> list = dp.selectByName(query.get("name").toString());
        if(list!=null)
            return new JsonResult(list);
        return new JsonResult("1","服务器错误");
    }
}
