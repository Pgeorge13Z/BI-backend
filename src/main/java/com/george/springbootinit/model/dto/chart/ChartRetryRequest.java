package com.george.springbootinit.model.dto.chart;

import com.george.springbootinit.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建请求
 *


 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartRetryRequest implements Serializable {
    /**
     * chartId
     */
    private Long id;



    private static final long serialVersionUID = 1L;

}