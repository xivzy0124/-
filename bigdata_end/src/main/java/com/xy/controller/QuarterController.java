package com.xy.controller;

import com.xy.pojo.DishPriceStatistics;
import com.xy.service.QuarterService;
import com.xy.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quarter")
public class QuarterController {
    @Autowired
    private QuarterService q;

    /**
     * 按季度查询指定品种的价格统计信息。
     * 该接口用于获取某品种在指定季度范围内的价格统计数据，便于前端进行季度对比分析。
     *
     * @param query 请求体，Map结构，必须包含以下字段：
     *              - name (String, 必填)：品种名称
     *              - start (Integer, 必填)：起始季度（如1表示第一季度）
     *              - end (Integer, 必填)：结束季度
     * @return JsonResult
     *         - 成功：data为季度价格统计信息列表
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @PostMapping("/byQuart")
    public JsonResult byQuart(@RequestBody Map<String, Object> query){
        List<DishPriceStatistics> list = q.selectByQuart(
                query.get("name").toString(),
                Integer.parseInt(query.get("start").toString()),
                Integer.parseInt(query.get("end").toString())
        );
        if(list==null)
            return new JsonResult("1","服务器错误");
        return new JsonResult(list);
    }

    /**
     * 按年份查询指定品种的价格统计信息。
     * 该接口用于获取某品种在指定年份范围内的价格统计数据，便于前端进行年度对比分析。
     *
     * @param query 请求体，Map结构，必须包含以下字段：
     *              - name (String, 必填)：品种名称
     *              - start (Integer, 必填)：起始年份
     *              - end (Integer, 必填)：结束年份
     * @return JsonResult
     *         - 成功：data为年度价格统计信息列表
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @PostMapping("/byYear")
    public JsonResult byYear(@RequestBody Map<String, Object> query){
        List<DishPriceStatistics> list = q.selectByYear(
                query.get("name").toString(),
                Integer.parseInt(query.get("start").toString()),
                Integer.parseInt(query.get("end").toString())
        );
        if(list==null)
            return new JsonResult("1","服务器错误");
        return new JsonResult(list);
    }
}
