package cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.service;

import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.AlertMessageDTO;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @Description: 告警
 * @Author:bread
 * @Date: 2024-12-07 17:20
 */
public interface IAlertService {
    void send(AlertMessageDTO message);

    void sendIfThreadPoolHasDanger(List<ThreadPoolConfigEntity> pools);
}
