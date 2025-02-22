package cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.trigger.listener;

import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.RefreshThreadPoolConfigDTO;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.UpdateThreadPoolConfigDTO;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.entity.ThreadPoolConfigEntity;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.registry.IRegistry;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.service.IDynamicThreadPoolService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.listener.MessageListener;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class ThreadPoolConfigRefreshListener implements MessageListener<RefreshThreadPoolConfigDTO> {

    private IDynamicThreadPoolService dynamicThreadPoolService;

    private IRegistry registry;

    @Override
    public void onMessage(CharSequence channel, RefreshThreadPoolConfigDTO refreshThreadPoolConfigDTO) {
        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();
        registry.reportThreadPool(threadPoolConfigEntities);
        log.info("动态线程池, 上报线程池信息: {}", threadPoolConfigEntities);

        threadPoolConfigEntities.forEach(threadPoolConfigEntity -> {
            UpdateThreadPoolConfigDTO updateThreadPoolConfigDTO = UpdateThreadPoolConfigDTO.buildUpdateThreadPoolConfigDTO(
                    dynamicThreadPoolService.queryThreadPoolByName(threadPoolConfigEntity.getThreadPoolName())
            );
            registry.reportUpdateThreadPoolConfigParameter(updateThreadPoolConfigDTO);
            log.info("动态线程池, 上报配置参数: {}", updateThreadPoolConfigDTO);
        });
    }
}
