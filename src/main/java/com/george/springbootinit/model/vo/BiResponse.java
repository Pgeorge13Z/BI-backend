package com.george.springbootinit.model.vo;

import com.george.springbootinit.common.ErrorCode;
import com.george.springbootinit.exception.ThrowUtils;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor
@Data
public class BiResponse {
    private String genChart;

    private String genResult;

    private Long chartId;

    /**
     * 这里可以校验 AI 生成的内容
     */
    public BiResponse(Long chartId, String genChart, String genResult) {
        ThrowUtils.throwIf(StringUtils.isAnyBlank(genChart, genResult) || (chartId != null && chartId <= 0), ErrorCode.PARAMS_ERROR);
        this.chartId = chartId;
        this.genChart = genChart;
        this.genResult = genResult;
    }

    public BiResponse(Long chartId) {
        this.chartId = chartId;
    }
}


