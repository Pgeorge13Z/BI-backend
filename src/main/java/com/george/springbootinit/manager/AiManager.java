package com.george.springbootinit.manager;

import com.george.springbootinit.common.ErrorCode;
import com.george.springbootinit.exception.ThrowUtils;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用于对接AI
 */
@Service
public class AiManager {

    @Resource
    private YuCongMingClient yuCongMingClient;

    /**
     * AI对话
     * @param modelId 模型的id
     * @param message  发送给ai的消息
     * @return ai的回复
     */
    public String doChat(long modelId,String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);

        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        ThrowUtils.throwIf(response==null, ErrorCode.SYSTEM_ERROR);

        return response.getData().getContent();
    }
}
