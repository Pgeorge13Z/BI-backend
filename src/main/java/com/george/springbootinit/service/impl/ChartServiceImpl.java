package com.george.springbootinit.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.george.springbootinit.bizmq.BiMessageProducer;
import com.george.springbootinit.bizmq.BiMqConstant;
import com.george.springbootinit.common.ErrorCode;
import com.george.springbootinit.constant.ChartConstant;
import com.george.springbootinit.exception.BusinessException;
import com.george.springbootinit.exception.ThrowUtils;
import com.george.springbootinit.manager.AiManager;
import com.george.springbootinit.manager.RedisLimiterManager;
import com.george.springbootinit.model.dto.chart.ChartRetryRequest;
import com.george.springbootinit.model.dto.chart.genChartByAiRequest;
import com.george.springbootinit.model.entity.Chart;
import com.george.springbootinit.model.entity.User;
import com.george.springbootinit.model.vo.BiResponse;
import com.george.springbootinit.service.ChartService;
import com.george.springbootinit.mapper.ChartMapper;
import com.george.springbootinit.service.UserService;
import com.george.springbootinit.utils.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
* @author Pgeorge
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-02-19 17:23:23
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{


    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMessageProducer biMessageProducer;



    @Override
    public BiResponse genChartByAi(MultipartFile multipartFile, genChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        /**
         * 参数检验
         */
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //校验
        //如果分析目标为空，就抛出异常
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR);
        //如果名字长度大于100，抛出异常
        ThrowUtils.throwIf(StringUtils.isNoneBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");

        /**
         * 检验文件
         */
        boolean validFile = isValidFile(multipartFile);
        ThrowUtils.throwIf(!validFile,ErrorCode.PARAMS_ERROR,"上传的文件不合规");

        //获取登录用户，存入数据库的时候需要知道用户id
        User loginUser = userService.getLoginUser(request);

        //限流判断
        redisLimiterManager.doRateLimit("genChartByAI"+loginUser.getId());

        // 调用AI生成图表代码，结论和压缩后的数据
        /** 预设的用户的输入样式(参考)
         分析需求：
         分析网站用户的增长情况
         原始数据：
         日期,用户数
         1号,10
         2号,20
         3号,30
         * */
        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        //如果图表类型不为空，拼接图表类型
        if (StringUtils.isNotBlank(chartType)){
            goal+=",请使用"+chartType;
        }
        userInput.append(goal).append("\n");

        //拼接压缩后的数据
        userInput.append("原始数据：").append("\n");
        String csvResult = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvResult).append("\n");

        //AI处理，拿到返回结果
        //AI模型的ID
//        long biModelId = 1659171950288818178L;
        //String aiResult = aiManager.doChat(biModelId, userInput.toString());
        String aiResult = aiManager.doChatByXingHuo(userInput.toString());

        /**
         预设的输出的样式：
         【【【【【
         {
         title: {
         text: '网站用户增长情况',
         subtext: ''
         },
         tooltip: {
         trigger: 'axis',
         axisPointer: {
         type: 'shadow'
         }
         },
         legend: {
         data: ['用户数']
         },
         xAxis: {
         data: ['1号', '2号', '3号']
         },
         yAxis: {},
         series: [{
         name: '用户数',
         type: 'bar',
         data: [10, 20, 30]
         }]
         }
         【【【【【
         根据数据分析可得，该网站用户数量逐日增长，时间越长，用户数量增长越多。
         */
        //根据中括号拆分
        String[] splits = aiResult.split(ChartConstant.GEN_CONTENT_SPLITS);
        //最前面有个空字符串 + js代码 + 分析结论
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI生成错误");
        }

        String genChart = splits[1].trim();
        String genResult = splits[2].trim();


        //插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setChartData(csvResult);
        chart.setChartType(chartType);
        chart.setGoal(goal);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        boolean save = this.save(chart);
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR,"图表保存失败");

        //构造返回对象
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        
        return biResponse;
    }

    @Override
    public BiResponse genChartByAiAsync(MultipartFile multipartFile, genChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //校验
        //如果分析目标为空，就抛出异常
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR);
        //如果名字长度大于100，抛出异常
        ThrowUtils.throwIf(StringUtils.isNoneBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR);

        /**
         * 检验文件
         */
        boolean validFile = isValidFile(multipartFile);
        ThrowUtils.throwIf(!validFile,ErrorCode.PARAMS_ERROR,"上传的文件不合规");

        //获取登录用户，存入数据库的时候需要知道用户id
        User loginUser = userService.getLoginUser(request);

        //限流判断
        redisLimiterManager.doRateLimit("genChartByAI" + loginUser.getId());

        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        //如果图表类型不为空，拼接图表类型
        if (StringUtils.isNotBlank(chartType)) {
            goal += ",请使用" + chartType;
        }
        userInput.append(goal).append("\n");

        //拼接压缩后的数据
        userInput.append("原始数据：").append("\n");
        String csvResult = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvResult).append("\n");

        //先把图表保存到数据库中
        Chart chart = new Chart();
        chart.setName(name);
        chart.setChartData(csvResult);
        chart.setChartType(chartType);
        chart.setGoal(goal);
        chart.setUserId(loginUser.getId());
        //设置任务状态为排队中，wait
        chart.setStatus("wait");
        boolean preSaveResult = this.save(chart);
        if (!preSaveResult) {
            this.handleChartUpdateError(chart.getId(), "图表预保存失败");
        }

        // 在最终的结果返回之前提交一个任务
        // todo 处理任务队列满了之后，抛异常的情况（因为提交任务报错了，前端会返回异常）
        CompletableFuture.runAsync(() -> {
            //先修改图表状态为“执行中”，等执行完成后再修改为“已完成”
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean updateR = this.updateById(updateChart);
            if (!updateR) {
                this.handleChartUpdateError(chart.getId(), "更新图表《执行中》状态失败");
            }

            // 调用 AI
            //AI处理，拿到返回结果
            //AI模型的ID
//        long biModelId = 1659171950288818178L;
            //String aiResult = aiManager.doChat(biModelId, userInput.toString());
            String aiResult = aiManager.doChatByXingHuo(userInput.toString());


            //根据中括号拆分
            String[] splits = aiResult.split(ChartConstant.GEN_CONTENT_SPLITS);
            //最前面有个空字符串 + js代码 + 分析结论
            if (splits.length < 3) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
            }

            String genChart = splits[1].trim();
            String genResult = splits[2].trim();


            //得到AI结果，再更新一次数据库
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            //设置任务状态为成功，succeed
            updateChartResult.setStatus("succeed");
            boolean UpdateResult = this.updateById(updateChartResult);
            ThrowUtils.throwIf(!UpdateResult, ErrorCode.SYSTEM_ERROR, "图表更新《成功》状态失败");
        }, threadPoolExecutor);

        //构造返回对象
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    @Override
    public BiResponse genChartByAiAsyncMq(MultipartFile multipartFile, genChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //校验
        //如果分析目标为空，就抛出异常
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR);
        //如果名字长度大于100，抛出异常
        ThrowUtils.throwIf(StringUtils.isNoneBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR);

        /**
         * 检验文件
         */
        boolean validFile = isValidFile(multipartFile);
        ThrowUtils.throwIf(!validFile,ErrorCode.PARAMS_ERROR,"上传的文件不合规");

        //获取登录用户，存入数据库的时候需要知道用户id
        User loginUser = userService.getLoginUser(request);

        //限流判断
        redisLimiterManager.doRateLimit("genChartByAI" + loginUser.getId());


        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        //如果图表类型不为空，拼接图表类型
        if (StringUtils.isNotBlank(chartType)) {
            goal += ",请使用" + chartType;
        }
        userInput.append(goal).append("\n");

        //拼接压缩后的数据
        userInput.append("原始数据：").append("\n");
        String csvResult = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvResult).append("\n");

        //先把图表保存到数据库中
        Chart chart = new Chart();
        chart.setName(name);
        chart.setChartData(csvResult);
        chart.setChartType(chartType);
        chart.setGoal(goal);
        chart.setUserId(loginUser.getId());
        //设置任务状态为排队中，wait
        chart.setStatus("wait");
        boolean preSaveResult = this.save(chart);
        if (!preSaveResult) {
            this.handleChartUpdateError(chart.getId(), "图表预保存失败");
        }
        Long newChatId = chart.getId();
        // 在最终结果返回前提交一个任务
        biMessageProducer.sendMessage(BiMqConstant.BI_EXCHANGE_NAME,BiMqConstant.BI_ROUTING_KEY,String.valueOf(newChatId));

        //构造返回对象
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    @Override
    public BiResponse retryGenChart(ChartRetryRequest chartRetryRequest) {
        ThrowUtils.throwIf(chartRetryRequest == null, ErrorCode.PARAMS_ERROR);
        Long chartId = chartRetryRequest.getId();

        // 更新用户状态为等待
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("wait");
        boolean updateResult = this.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表等待状态失败"+chartId);
        }
        biMessageProducer.sendMessage(BiMqConstant.BI_EXCHANGE_NAME,BiMqConstant.BI_ROUTING_KEY,String.valueOf(chartId));
        return new BiResponse(chartId);
    }

    @Override
    public void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage(execMessage);
        boolean updateResult = this.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败"+chartId+","+execMessage);
        }
    }

    public boolean isValidFile(MultipartFile multipartFile) {
        long fileSize = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        ThrowUtils.throwIf(fileSize > ChartConstant.FILE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过 1M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!ChartConstant.VALID_FILE_SUFFIX.contains(suffix), ErrorCode.PARAMS_ERROR, "不支持该类型文件");

        return true;
    }

//    /**
//     * 返回AI生成的图表代码，结论，和压缩后的原始数据
//     * @param chartType
//     * @param goal
//     * @param multipartFile
//     * @return String[0]:图表代码 String[1]:结论 String[2]:压缩后的原始数据
//     */
//    public String[] getAiResult(String chartType,String goal,MultipartFile multipartFile) {
//        /** 预设的用户的输入样式(参考)
//         分析需求：
//         分析网站用户的增长情况
//         原始数据：
//         日期,用户数
//         1号,10
//         2号,20
//         3号,30
//         * */
//        //构造用户输入
//        StringBuilder userInput = new StringBuilder();
//        userInput.append("分析需求：").append("\n");
//
//        //拼接分析目标
//        //如果图表类型不为空，拼接图表类型
//        if (StringUtils.isNotBlank(chartType)){
//            goal+=",请使用"+chartType;
//        }
//        userInput.append(goal).append("\n");
//
//        //拼接压缩后的数据
//        userInput.append("原始数据：").append("\n");
//        String csvResult = ExcelUtils.excelToCsv(multipartFile);
//        userInput.append(csvResult).append("\n");
//
//        //AI处理，拿到返回结果
//        //AI模型的ID
////        long biModelId = 1659171950288818178L;
//        //String aiResult = aiManager.doChat(biModelId, userInput.toString());
//        String aiResult = aiManager.doChatByXingHuo(userInput.toString());
//
//        /**
//         预设的输出的样式：
//         【【【【【
//         {
//         title: {
//         text: '网站用户增长情况',
//         subtext: ''
//         },
//         tooltip: {
//         trigger: 'axis',
//         axisPointer: {
//         type: 'shadow'
//         }
//         },
//         legend: {
//         data: ['用户数']
//         },
//         xAxis: {
//         data: ['1号', '2号', '3号']
//         },
//         yAxis: {},
//         series: [{
//         name: '用户数',
//         type: 'bar',
//         data: [10, 20, 30]
//         }]
//         }
//         【【【【【
//         根据数据分析可得，该网站用户数量逐日增长，时间越长，用户数量增长越多。
//         */
//        //根据中括号拆分
//        String[] splits = aiResult.split("【【【【【");
//        //最前面有个空字符串 + js代码 + 分析结论
//        if (splits.length < 3) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI生成错误");
//        }
//
//        String genChart = splits[1].trim();
//        String genResult = splits[2].trim();
//        return new String[]{genChart,genResult,csvResult};
//    }
}




