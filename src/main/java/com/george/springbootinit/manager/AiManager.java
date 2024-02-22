package com.george.springbootinit.manager;

import com.george.springbootinit.common.ErrorCode;
import com.george.springbootinit.config.XingHuoAiConfig;
import com.george.springbootinit.exception.ThrowUtils;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.exception.SparkException;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.model.response.SparkTextUsage;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于对接AI
 */
@Service
public class AiManager {

    @Resource
    private YuCongMingClient yuCongMingClient;

    @Resource
    private SparkClient sparkClient;

    /**
     * AI对话
     *
     * @param modelId 模型的id
     * @param message 发送给ai的消息
     * @return ai的回复
     */
    public String doChat(long modelId, String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);

        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        ThrowUtils.throwIf(response == null, ErrorCode.SYSTEM_ERROR);

        return response.getData().getContent();
    }

    //参考https://github.com/briqt/xunfei-spark4j
    public String doChatByXingHuo(String message) {
        // 消息列表，可以在此列表添加历史对话记录
        List<SparkMessage> messages = new ArrayList<>();
        String prompt = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                "分析需求：\n" +
                "{数据分析的需求或者目标}\n" +
                "原始数据：\n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                "【【【【【\n" +
                "{前端 Echarts V5 的 option 配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释,js代码要严格遵守json格式}\n" +
                "【【【【【\n" +
                "{明确的数据分析结论、越详细越好，不要生成多余的注释}";

        String assistantContent = "【【【【【\n" +
                "{\n" +
                "    \"title\": {\n" +
                "        \"text\": \"网站用户增长情况\",\n" +
                "        \"x\": \"center\"\n" +
                "    },\n" +
                "    \"xAxis\": {\n" +
                "        \"data\": [\"1号\", \"2号\", \"3号\"]\n" +
                "    },\n" +
                "    \"yAxis\": {},\n" +
                "    \"series\": [{\n" +
                "        \"name\": \"用户数\",\n" +
                "        \"type\": \"line\",\n" +
                "        \"data\": [10, 20, 30]\n" +
                "    }]\n" +
                "}\n" +
                "【【【【【\n" +
                "根据给定的数据，可以看出网站用户数在不断增长。\n" +
                "1号时有10个用户，2号时增长到20个用户，3号时增长到30个用户。可以明显看出网站的用户数呈现上升的趋势。";

        messages.add(SparkMessage.systemContent(prompt));
        messages.add(SparkMessage.assistantContent(assistantContent));
        messages.add(SparkMessage.userContent(message));
// 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
// 消息列表
                .messages(messages)
// 模型回答的tokens的最大长度,非必传，默认为2048。
// V1.5取值为[1,4096]
// V2.0取值为[1,8192]
// V3.0取值为[1,8192]
                .maxTokens(8192)
// 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.2)
// 指定请求版本，默认使用最新3.5版本
                .apiVersion(SparkApiVersion.V3_5)
                .build();
        String res = "";
        try {
            // 同步调用
            SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
            SparkTextUsage textUsage = chatResponse.getTextUsage();
            res = chatResponse.getContent();
            System.out.println("\n回答：" + chatResponse.getContent());
//            System.out.println("\n提问tokens：" + textUsage.getPromptTokens()
//                    + "，回答tokens：" + textUsage.getCompletionTokens()
//                    + "，总消耗tokens：" + textUsage.getTotalTokens());
        } catch (SparkException e) {
            System.out.println("发生异常了：" + e.getMessage());
        }

        return res;
    }

    public String chatWithXingHuo(String message) {
        // 消息列表，可以在此列表添加历史对话记录
        List<SparkMessage> messages = new ArrayList<>();
        messages.add(SparkMessage.userContent(message));
// 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
// 消息列表
                .messages(messages)
// 模型回答的tokens的最大长度,非必传，默认为2048。
// V1.5取值为[1,4096]
// V2.0取值为[1,8192]
// V3.0取值为[1,8192]
                .maxTokens(8192)
// 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.2)
// 指定请求版本，默认使用最新3.5版本
                .apiVersion(SparkApiVersion.V3_5)
                .build();
        String res = "";
        try {
            // 同步调用
            SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
            SparkTextUsage textUsage = chatResponse.getTextUsage();
            res = chatResponse.getContent();
            System.out.println("\n回答：" + chatResponse.getContent());
            System.out.println("\n提问tokens：" + textUsage.getPromptTokens()
                    + "，回答tokens：" + textUsage.getCompletionTokens()
                    + "，总消耗tokens：" + textUsage.getTotalTokens());
        } catch (SparkException e) {
            System.out.println("发生异常了：" + e.getMessage());
        }

        return res;
    }
}
