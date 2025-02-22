package cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.service.impl;

import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.config.properties.DynamicThreadPoolAlarmAutoProperties;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.AlarmMessageDTO;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.model.entity.ThreadPoolConfigEntity;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.service.IAlarmService;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.strategy.alarm.AbstractAlarmStrategy;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.trigger.job.AlarmCanSendStateChangeJob;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;import java.util.Objects;
@Slf4j
@EnableAsync
public class AlarmServiceImpl implements IAlarmService {

    private static Boolean canSendForThreadPoolDanger = true;

    @Resource
    private DynamicThreadPoolAlarmAutoProperties config;

    @Override
    @Async
    public void send(AlarmMessageDTO message) {
        log.info("告警推送: {}", message);

        Boolean enable = config.getEnabled();
        if (!enable) {
            log.info("告警推送未开启");
            return;
        }

        List<AbstractAlarmStrategy> strategies = AbstractAlarmStrategy.selectStrategy(config.getUsePlatform());

        strategies.forEach(strategy -> {
            try {
                strategy.send(message);
            } catch (ApiException e) {
                log.error("推送告警时发生错误: {} | {}", e.getErrCode(), e.getErrMsg());
            }
        });
    }

    @Override
    @Async
    public void sendIfThreadPoolHasDanger(List<ThreadPoolConfigEntity> pools) {
        List<ThreadPoolConfigEntity> dangerPools = new ArrayList<>();
        for (ThreadPoolConfigEntity pool : pools) {
            try {
                // 活跃线程数达到最大或者阻塞队列已满
                if (Objects.equals(pool.getActiveThreadCount(), pool.getMaximumPoolSize())
                        && pool.getRemainingCapacity () == 0) {
                    dangerPools.add(pool);
                }
            } catch (Exception e) {
                // 捕获线程池异常并打印堆栈
                log.error("线程池异常: {}", pool.getThreadPoolName(), e);

                // 生成并发送告警信息
                AlarmMessageDTO alarmMessageDTO = AlarmMessageDTO
                        .buildAlarmMessageDTO("线程池发生异常")
                        .appendParameter("线程池名称", pool.getThreadPoolName())
                        .appendParameter("异常信息", e.getMessage())
                        .appendParameter("堆栈信息", Arrays.toString(e.getStackTrace()));

                send(alarmMessageDTO);
                continue; // 继续处理其他线程池
            }
        }

        if (dangerPools.isEmpty()) {
            return;
        }

        AlarmMessageDTO alarmMessageDTO = AlarmMessageDTO
                .buildAlarmMessageDTO("超出线程池处理能力")
                .appendParameter("告警线程池数", dangerPools.size());
        dangerPools.forEach(pool -> alarmMessageDTO
                .appendParameter("======", "======")
                .appendParameter("应用名称", pool.getApplicationName())
                .appendParameter("线程池名称", pool.getThreadPoolName())
                .appendParameter("池中线程数", pool.getPoolSize())
                .appendParameter("核心线程数", pool.getCorePoolSize())
                .appendParameter("最大线程数", pool.getMaximumPoolSize())
                .appendParameter("活跃线程数", pool.getActiveThreadCount())
                .appendParameter("队列类型", pool.getQueueType())
                .appendParameter("队列中任务数", pool.getQueueSize())
                .appendParameter("队列剩余容量", pool.getRemainingCapacity())
        );
        send(alarmMessageDTO);

        AlarmServiceImpl.canSendForThreadPoolDanger = false;

        // 启动一个定时任务, 10分钟后自动将 canSendForThreadPoolDanger 改为true
        AlarmCanSendStateChangeJob job = new AlarmCanSendStateChangeJob(
                () -> AlarmServiceImpl.setCanSendForThreadPoolDanger(true)
        );
        job.run(60 * 10);
    }

    public static synchronized Boolean canSendForThreadPoolDanger() {
        return canSendForThreadPoolDanger;
    }

    public static synchronized void setCanSendForThreadPoolDanger(Boolean canSendForThreadPoolDanger) {
        AlarmServiceImpl.canSendForThreadPoolDanger = canSendForThreadPoolDanger;
    }
}
