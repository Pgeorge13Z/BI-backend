package com.george.springbootinit.mapper;

import com.george.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
* @author Pgeorge
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2024-02-19 17:23:23
* @Entity com.george.springbootinit.model.entity.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {

    /**
     * 动态的创建数据库
     * @param creatTableSQL
     */
    void createTable(final String creatTableSQL);

    /**
     * 向动态创建的数据库之中插入数据
     *
     * @param insertCVSData
     * @return
     */
    void insertValue(final String insertCVSData);

    /*
     * 方法的返回类型是 List<Map<String, Object>>,
     * 表示返回的是一个由多个 map 组成的集合,每个map代表了一行查询结果，
     * 并将其封装成了一组键值对形式的对象。其中,String类型代表了键的类型为字符串，
     * Object 类型代表了值的类型为任意对象,使得这个方法可以适应不同类型的数据查询。
     *
     */
    /**
     * 查询保存数据表的信息
     *
     * @param tableName
     * @return
     */
    List<Map<String, Object>> queryChartData(final Long tableName);
}




