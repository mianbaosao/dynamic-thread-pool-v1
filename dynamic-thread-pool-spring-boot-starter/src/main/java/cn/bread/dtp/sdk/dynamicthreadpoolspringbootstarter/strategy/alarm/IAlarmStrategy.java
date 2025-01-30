package cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.strategy.alarm;

import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.AlarmMessageDTO;
import com.taobao.api.ApiException;

public interface IAlarmStrategy {
    void send(AlarmMessageDTO message) throws ApiException;
}
