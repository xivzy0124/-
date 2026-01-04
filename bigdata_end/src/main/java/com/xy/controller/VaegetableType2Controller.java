package com.xy.controller;

import com.xy.pojo.VaegetableType2;
import com.xy.service.VaegetableType2Service;
import com.xy.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vegetable/type2")
public class VaegetableType2Controller {
    @Autowired
    private VaegetableType2Service vaegetableType2Service;

    /**
     * 获取所有蔬菜分类信息。
     * 该接口用于获取数据库中所有二级蔬菜分类，便于前端进行分类展示和选择。
     *
     * @return JsonResult
     *         - 成功：data为所有蔬菜分类数据列表
     *         - 失败：code为"1"，msg为错误信息
     * @throws 服务器异常时返回错误信息
     */
    @GetMapping("/all")
    public JsonResult getAllVegetableTypes() {
        try {
            List<VaegetableType2> list = vaegetableType2Service.getAllVegetableTypes();
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