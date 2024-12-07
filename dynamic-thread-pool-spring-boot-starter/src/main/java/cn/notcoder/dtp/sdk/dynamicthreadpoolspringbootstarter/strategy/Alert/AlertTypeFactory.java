package cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.strategy.Alert;

import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.config.properties.DynamicThreadPoolAlarmAutoProperties;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.AlertMessageDTO;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.enums.AlertEnum;
import com.taobao.api.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 告警工厂
 * @Author:bread
 * @Date: 2024-12-07 16:45
 */
@Service
public class AlertTypeFactory {

    // 通过 @Autowired 注入配置类
    @Autowired
    private DynamicThreadPoolAlarmAutoProperties dynamicThreadPoolAlarmAutoProperties;

    // 创建一个 map 来存储 alert 类型
    @Resource
    Map<String, AlertType> alertTypeMap = new HashMap<>();

    // 获取 alert 类型
    public List<AlertType> getAlertType() {
        List<AlertType> alertTypeList = new LinkedList<>();
        // 获取 usePlatform 配置项
        List<String> alertName = dynamicThreadPoolAlarmAutoProperties.getUsePlatform();

        for (String alert : alertName) {
            for (AlertEnum alertEnum : AlertEnum.values()) {
                if (alert.equals(alertEnum.getValue())) {
                    alertTypeList.add(alertTypeMap.get(alertEnum.getValue()));
                }
            }
        }
        return alertTypeList;
    }

    // 发送 alert
    public void sendAlert(AlertMessageDTO alertMessageDTO) throws ApiException {
        for (AlertType alertType : getAlertType()) {
            alertType.sendAlert(alertMessageDTO);
        }
    }
}

