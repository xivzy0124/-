package com.xy.controller;


import com.xy.pojo.ExportTOP;
import com.xy.pojo.ScatterData;
import com.xy.service.MarketService;
import com.xy.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/market")
public class MarketController {
    @Autowired
    MarketService market;
    /**
     * 查询指定省份和蔬菜品种的异常散点数据。
     * 该接口用于获取某省份某蔬菜品种在各市场的异常价格点，便于前端进行异常检测和可视化。
     *
     * @param query 请求体，Map结构，必须包含以下字段：
     *              - province (String, 必填)：省份名称
     *              - greens (String, 必填)：蔬菜品种名称
     * @return JsonResult
     *         - 成功：data为异常散点数据列表
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @PostMapping("/anomaly")
    public JsonResult topN(@RequestBody Map<String, Object> query){
        List<ScatterData> list = market.scatterByProvince(query.get("province").toString(),query.get("greens").toString());
        if(list!=null)
            return new JsonResult(list);
        return new JsonResult("1","服务器错误");
    }
}
