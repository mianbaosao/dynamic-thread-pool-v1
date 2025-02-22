package cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.service;

import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.AlarmMessageDTO;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * 告警服务类
 */
public interface IAlarmService {


    void send(AlarmMessageDTO message);

    void sendIfThreadPoolHasDanger(List<ThreadPoolConfigEntity> pools);
}
