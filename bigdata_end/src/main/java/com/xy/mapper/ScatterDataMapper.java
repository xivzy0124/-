package com.xy.mapper;

import com.xy.pojo.ScatterData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScatterDataMapper {
    int insert(ScatterData record);

    List<ScatterData> selectAll();
    List<ScatterData> selectByProvince(String provincename,String greens);

    /**
     * 根据省份名称和蔬菜名称查询 ScatterData 数据
     * @param provinceName 省份名称
     * @param varietyName 蔬菜名称
     * @return 符合条件的 ScatterData 对象列表
     */
    List<ScatterData> selectByProvinceAndVariety(String provinceName, String varietyName);
}