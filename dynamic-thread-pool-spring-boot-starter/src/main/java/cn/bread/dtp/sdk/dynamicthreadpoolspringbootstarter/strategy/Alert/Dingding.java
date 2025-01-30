package cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.strategy.Alert;

import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.config.properties.DynamicThreadPoolAlarmAutoProperties;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.AlertMessageDTO;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.enums.AlertEnum;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @Description: 钉钉告警发布
 * @Author:bread
 * @Date: 2024-12-07 16:46
 */
@Component("DingDing")
public class Dingding implements AlertType{


    @Resource
    private DynamicThreadPoolAlarmAutoProperties config;

    @Override
    public String getStrategyName() {
        return AlertEnum.DING_DING.getValue();
    }

    @Override
    public void sendAlert(AlertMessageDTO message) throws ApiException {

        String token = config.getAccessToken().getDingDing();

        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send");
        OapiRobotSendRequest req = new OapiRobotSendRequest();

        //定义文本内容
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent(buildMessageContent(message));

        //定义 @ 对象
        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setIsAtAll(true);

        //设置消息类型
        req.setMsgtype("text");
        req.setText(text);
        req.setAt(at);
        OapiRobotSendResponse rsp = client.execute(req, token);

        if (rsp.isSuccess()) {
            return;
        }

        throw new ApiException(rsp.getErrcode().toString(), rsp.getErrmsg());
    }

    public String buildMessageContent(AlertMessageDTO message) {
        StringBuilder content = new StringBuilder();
        HashMap<String, String> parameters = message.getParameters();
        String remarks = message.getRemarks();

        content.append("【监控告警】 ").append(message.getMessage()).append("\n");
        parameters.forEach((k, v) -> {
            content.append(" ").append(k).append(": ").append(v).append("\n");
        });
        if (remarks != null) {
            content.append(" ").append(remarks).append("\n");
        }
        return content.toString();
    }
}
