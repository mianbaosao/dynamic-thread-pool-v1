package cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.trigger.job;

import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.config.properties.DynamicThreadPoolRegistryAutoProperties;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.UpdateThreadPoolConfigDTO;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.entity.ThreadPoolConfigEntity;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.registry.IRegistry;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.service.IDynamicThreadPoolService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 周期性地查询当前所有线程池的配置信息并且上报给注册中心
 */
@Slf4j
@AllArgsConstructor
@EnableConfigurationProperties(DynamicThreadPoolRegistryAutoProperties.class)
public class ThreadPoolDataReportJob {

    private IDynamicThreadPoolService dynamicThreadPoolService;

    private IRegistry registry;

    @Scheduled(cron = "${dynamic-thread-pool.registry.report-cron}")
    public void reportThreadPoolData() {
        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();
        registry.reportThreadPool(threadPoolConfigEntities);

        log.info("【reportThreadPoolData】, 上报线程池信息: {}", threadPoolConfigEntities);

        // 遍历每个线程池信息, 上报配置信息
        threadPoolConfigEntities.forEach(threadPoolConfigEntity -> {
            UpdateThreadPoolConfigDTO updateThreadPoolConfigDTO = UpdateThreadPoolConfigDTO
                    .buildUpdateThreadPoolConfigDTO(threadPoolConfigEntity);

            registry.reportUpdateThreadPoolConfigParameter(updateThreadPoolConfigDTO);

            log.info("[更新完成]动态线程池, 上报线程池配置信息: {}", updateThreadPoolConfigDTO);
        });
    }
}
