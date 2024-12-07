package cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.strategy.Alert;

import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.AlertMessageDTO;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.enums.AlertEnum;
import com.taobao.api.ApiException;

/**
 * @Description: 工厂类型
 * @Author:bread
 * @Date: 2024-12-07 16:46
 */
public interface AlertType {
    String getStrategyName();

    void sendAlert(AlertMessageDTO message) throws ApiException;

}
