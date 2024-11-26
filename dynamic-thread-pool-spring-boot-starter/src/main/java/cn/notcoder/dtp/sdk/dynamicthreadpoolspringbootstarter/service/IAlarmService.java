package cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.service;

import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.AlarmMessageDTO;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.entity.ThreadPoolConfigEntity;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 告警服务类
 */
public interface IAlarmService {


    void send(AlarmMessageDTO message);

    void sendIfThreadPoolHasDanger(List<ThreadPoolConfigEntity> pools);
}
