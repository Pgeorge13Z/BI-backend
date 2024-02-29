package com.george.springbootinit.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @author Shier
 * CreateTime 2023/5/21 17:38
 */
public interface ChartConstant {
    /**
     * AI生成的内容分隔符
     */
    String GEN_CONTENT_SPLITS = "【【【【【";

    /**
     * 提取生成的图表的Echarts配置的正则
     */
    String GEN_CHART_REGEX = "\\{(?>[^{}]*(?:\\{[^{}]*}[^{}]*)*)}";


    /**
     * 图表上传文件大小 1M
     */
    long FILE_MAX_SIZE = 2 * 1024 * 1024L;

    /**
     * 图表上传文件后缀白名单
     */
    List<String>  VALID_FILE_SUFFIX= Arrays.asList("xlsx","csv","xls","json");


}
