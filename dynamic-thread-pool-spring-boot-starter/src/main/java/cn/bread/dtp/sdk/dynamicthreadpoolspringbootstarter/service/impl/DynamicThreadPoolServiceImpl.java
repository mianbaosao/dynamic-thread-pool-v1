package cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.service.impl;

import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.AlertMessageDTO;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.UpdateThreadPoolConfigDTO;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.entity.ThreadPoolConfigEntity;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.hook.ResizableCapacityLinkedBlockingQueue;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.service.IAlertService;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.service.IDynamicThreadPoolService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态线程池服务
 */

@Slf4j
@AllArgsConstructor
public class DynamicThreadPoolServiceImpl implements IDynamicThreadPoolService {

    private String applicationName;

    private int port;

    //其中的键是线程池的名称（beanName）threadPoolExecutor01、threadPoolExecutor02
    private Map<String, ThreadPoolExecutor> threadPoolExecutorMap;

    private IAlertService alertService;

    @Override
    public List<ThreadPoolConfigEntity> queryThreadPoolList() {
        ArrayList<ThreadPoolConfigEntity> threadPools = new ArrayList<>();

        threadPoolExecutorMap.forEach((beanName, executor) -> threadPools.add(
                ThreadPoolConfigEntity.buildThreadPoolConfigEntity(
                        applicationName,
                        port,
                        beanName,
                        executor
                )
        ));
        for (ThreadPoolConfigEntity threadPoolConfig : threadPools) {
            // 假设 ThreadPoolConfigEntity 类有 getter 方法来获取配置信息
            System.out.println(threadPoolConfig.getThreadPoolName()+" "+"配置如下:");
            System.out.println("Application Name: " + threadPoolConfig.getApplicationName());
            System.out.println("Port: " + threadPoolConfig.getPort());
            System.out.println("Thread Pool Name: " + threadPoolConfig.getThreadPoolName());
            System.out.println("Core Pool Size: " + threadPoolConfig.getCorePoolSize());
            System.out.println("Max Pool Size: " + threadPoolConfig.getMaximumPoolSize());
            System.out.println("Queue Size: " + threadPoolConfig.getQueueSize());
            System.out.println("---------------------------------");
        }
        return threadPools;
    }

    @Override
    public ThreadPoolConfigEntity queryThreadPoolByName(String threadPoolName) {
        return ThreadPoolConfigEntity.buildThreadPoolConfigEntity(
                applicationName,
                port,
                threadPoolName,
                threadPoolExecutorMap.get(threadPoolName)
        );
    }

    @Override
    public Boolean updateThreadPoolConfig(UpdateThreadPoolConfigDTO updateThreadPoolConfigDTO) {
        if (updateThreadPoolConfigDTO == null) {
            return false;
        }

        if (!Objects.equals(updateThreadPoolConfigDTO.getApplicationName(), applicationName)) {
            return false;
        }

        String threadPoolName = updateThreadPoolConfigDTO.getThreadPoolName();
        if (threadPoolName == null) {
            return false;
        }

        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolName);
        if (threadPoolExecutor == null) {
            return false;
        }

        Integer corePoolSize = updateThreadPoolConfigDTO.getCorePoolSize();
        Integer maximumPoolSize = updateThreadPoolConfigDTO.getMaximumPoolSize();
        // CorePoolSize 小于等于 MaximumPoolSize, 否则发出告警
        if (maximumPoolSize < corePoolSize) {
            alertService.send(
                    AlertMessageDTO
                            .buildAlertMessageDTO("变更配置出错")
                            .appendParameter("错误原因", "最大线程数小于核心线程数")
                            .appendParameter("最大线程数", maximumPoolSize)
                            .appendParameter("核心线程数", corePoolSize)
            );
            log.error("动态线程池, 变更配置时出错(最大线程数小于核心线程数): {}", updateThreadPoolConfigDTO);
            return false;
        }

        // 变更时注意设置值的顺序, 始终满足CorePoolSize 小于等于 MaximumPoolSize
        if (corePoolSize < threadPoolExecutor.getMaximumPoolSize()) {
            threadPoolExecutor.setCorePoolSize(updateThreadPoolConfigDTO.getCorePoolSize());
            threadPoolExecutor.setMaximumPoolSize(updateThreadPoolConfigDTO.getMaximumPoolSize());
        } else {
            threadPoolExecutor.setMaximumPoolSize(updateThreadPoolConfigDTO.getMaximumPoolSize());
            threadPoolExecutor.setCorePoolSize(updateThreadPoolConfigDTO.getCorePoolSize());
        }

        // 变更阻塞队列的大小
        ResizableCapacityLinkedBlockingQueue queue =
                (ResizableCapacityLinkedBlockingQueue) threadPoolExecutor.getQueue();
        queue.setCapacity(updateThreadPoolConfigDTO.getQueueCapacity());

        // 可再次推送告警
        AlarmServiceImpl.setCanSendForThreadPoolDanger(true);
        return true;
    }
}
