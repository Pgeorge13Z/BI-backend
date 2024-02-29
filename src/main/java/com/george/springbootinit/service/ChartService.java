package com.george.springbootinit.service;

import com.george.springbootinit.model.dto.chart.ChartRetryRequest;
import com.george.springbootinit.model.dto.chart.genChartByAiRequest;
import com.george.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.george.springbootinit.model.vo.BiResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Pgeorge
 * @description 针对表【chart(图表信息表)】的数据库操作Service
 * @createDate 2024-02-19 17:23:23
 */
public interface ChartService extends IService<Chart> {
    /**
     * 定义更新图表失败的工具类函数
     */
    public void handleChartUpdateError(long chartId, String execMessage);

    /**
     * AI生成图表 同步
     *
     * @param multipartFile       用户上传的文件信息
     * @param genChartByAiRequest 用户的需求
     * @param request             http request
     * @return BiResponse 处理后的ai生成内容
     */
    BiResponse genChartByAi(MultipartFile multipartFile, genChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    /**
     * AI生成图表 异步
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    BiResponse genChartByAiAsync(MultipartFile multipartFile, genChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    /**
     * AI生成图表 异步RabbitMQ消息队列
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    BiResponse genChartByAiAsyncMq(MultipartFile multipartFile, genChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    BiResponse retryGenChart(ChartRetryRequest chartRetryRequest);

}
