package org.v.end.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.v.end.common.Result;
import org.v.end.model.entity.VaegettableType;
import org.v.end.service.VaegettableTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/vaegettableType")
public class VaegettableTypeController {

    @Autowired
    private VaegettableTypeService vaegettableTypeService;

    @GetMapping("/selectAll")
    public Result<List<VaegettableType>> selectAll() {
        try {
            List<VaegettableType> result = vaegettableTypeService.selectAll();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询所有数据失败: " + e.getMessage());
        }
    }
}
