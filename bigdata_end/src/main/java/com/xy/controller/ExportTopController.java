package com.xy.controller;

import com.xy.mapper.ExportTOPMapper;
import com.xy.pojo.DailyVegetablePrices;
import com.xy.pojo.ExportTOP;
import com.xy.service.ExportTopService;
import com.xy.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/export")
public class ExportTopController {
    @Autowired
    ExportTopService e;

    /**
     * 查询指定品种的出口前N名数据。
     * 该接口用于获取某种蔬菜或农产品出口量排名前N的相关数据，便于前端进行排行展示。
     *
     * @param query 请求体，Map结构，必须包含以下字段：
     *              - name (String, 必填)：品种名称
     * @return JsonResult
     *         - 成功：data为出口前N名数据列表
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @PostMapping("/topN")
    public JsonResult topN(@RequestBody Map<String, Object> query){
        List<ExportTOP> list = e.topN(query.get("name").toString());
        if(list!=null)
            return new JsonResult(list);
        return new JsonResult("1","服务器错误");
    }
}
