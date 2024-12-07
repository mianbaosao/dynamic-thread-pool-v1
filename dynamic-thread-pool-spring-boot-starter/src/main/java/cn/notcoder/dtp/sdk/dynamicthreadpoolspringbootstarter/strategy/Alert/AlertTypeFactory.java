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
    @Autowired
    private DynamicThreadPoolAlarmAutoProperties dynamicThreadPoolAlarmAutoProperties;

    @Resource
    Map<String, AlertType> alertTypeMap = new HashMap<>();

    public List<AlertType> getAlertType() {
        List<AlertType> alertTypeList = new LinkedList<>();
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

    public void sendAlert(AlertMessageDTO alertMessageDTO) throws ApiException {
        for (AlertType alertType : getAlertType()) {
            alertType.sendAlert(alertMessageDTO);
        }
    }
}

