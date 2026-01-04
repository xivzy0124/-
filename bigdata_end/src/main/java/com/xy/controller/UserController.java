package com.xy.controller;

import com.xy.pojo.*;
import com.xy.service.UserService;
import com.xy.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService user;
    //桑基图
    /**
     * 根据省份名称查询桑基图所需的日历数据
     * @param query 包含province字段（省份名称）的请求参数
     * @return JsonResult 桑基图日历数据列表，若无数据则返回错误信息
     */
    @PostMapping("/calendar")
    public JsonResult byProvince(@RequestBody Map<String, Object> query){
        List<CalendarData> list = user.calendarByProvince(query.get("province").toString());
        if(list==null)
            return new JsonResult("1","服务器错误");
        return new JsonResult(list);
    }

    /**
     * 获取所有省份
     * @return 所有省份名称user
     */
    @GetMapping("/getProvince")
    public JsonResult GetProvince(){
        List<String> list = user.getProvince();
        if(list==null)
            return new JsonResult("1","服务器错误");
        return new JsonResult(list);
    }

    /**
     * 根据省份名称和市场名称获取散点数据
     *
     * @param query 包含省份名称和市场名称的请求参数
     * @return JsonResult 包含请求结果的 JSON 对象
     */
    @PostMapping("/byScatter")
    public JsonResult byName(@RequestBody Map<String, Object> query) {
        System.out.println(query);
        if (query == null) {
            return new JsonResult("1", "请求参数缺失");
        }
        String provinceName = query.get("provincename") != null ? query.get("provincename").toString() : null;
        String marketName = query.get("marketname") != null ? query.get("marketname").toString() : null;
        if (provinceName == null ) {
            System.out.println("请求provinceName参数缺失");
            return new JsonResult("1", "请求provinceName参数缺失");
        }
        if (marketName == null) {
            System.out.println("请求marketName参数缺失");
            return new JsonResult("1", "请求marketName参数缺失");
        }
        try {
            List<ScatterData> list = user.getScatterDataByProvinceAndVariety(provinceName, marketName);
            if (list.isEmpty()) {
                System.out.println("数据为空");
                return new JsonResult("1", "数据为空");
            }
            return new JsonResult(list, "请求成功");
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return new JsonResult("1", "请求处理出错，请稍后重试");
        }
    }

    /**
     * 提供用户界面卡片数据信息
     * @return JsonResult 包含请求结果的 JSON 对象
     */
    @GetMapping("/getTitle")
    public JsonResult getTitle(){
        List<Statistics> list = user.selectAll();
        if(list==null)
            return new JsonResult("1","服务器错误");
        return new JsonResult(list);
    }

    /**
     * 原接口为DateAndPriceController下的day。数据库为daily_vegetable_prices
     * 现改为新数据，数据库为processed_prices
     * @param query 包含菜名
     * @return JsonResult 包含请求结果的 JSON 对象
     */
    @PostMapping("/day")
    public JsonResult day(@RequestBody Map<String, Object> query){
        List<ProcessedPrices> list = user.selectProcessedPriceByName(query.get("name").toString());
        if(list!=null)
            return new JsonResult(list);
        return new JsonResult("1","服务器错误");
    }

}
