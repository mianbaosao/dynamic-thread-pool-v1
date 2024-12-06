package cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.registry.redis;

import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.AlarmMessageDTO;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.UpdateThreadPoolConfigDTO;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.entity.ThreadPoolConfigEntity;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.enums.RegistryEnum;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.registry.IRegistry;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.service.IAlarmService;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.service.impl.AlarmServiceImpl;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.utils.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Redis 实现注册中心
 * 完成THREAD_POOL_CONFIG_LIST_KEY的配置
 */

@Slf4j
@AllArgsConstructor
public class RedisRegistry implements IRegistry {

    private RedissonClient redissonClient;

    private IAlarmService alarmService;
    @Override
    public void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolConfigEntityList) {
        // 如果传入的线程池配置列表为空，则直接返回
        if (threadPoolConfigEntityList == null || threadPoolConfigEntityList.isEmpty()) {
            return;
        }
        // 如果可以发送线程池危险警报，则发送警报
        if (AlarmServiceImpl.canSendForThreadPoolDanger()) {
            alarmService.sendIfThreadPoolHasDanger(threadPoolConfigEntityList);
        }
        // 获取Redis中的线程池配置列表
        RList<ThreadPoolConfigEntity> list = RedisUtils.getPoolConfigRList(redissonClient);
        // 如果Redis中的线程池配置列表为空，则将传入的线程池配置列表添加到Redis中
        if (list.isEmpty()) {
            list.addAll(threadPoolConfigEntityList);
            return;
        }

        // 获取Redis锁
        RLock lock = redissonClient.getLock(RegistryEnum.REPORT_THREAD_POOL_CONFIG_LIST_REDIS_LOCK_KEY.getKey());

        try {
            // 尝试获取Redis锁，如果获取成功，则执行上报线程池配置列表的真正处理过程
            boolean canHasLock = lock.tryLock(3000, 3000, TimeUnit.MILLISECONDS);

            if (canHasLock) {
                reportThreadPoolRealProcess(threadPoolConfigEntityList, list);
            }
        } catch (Exception e) {
            // 发送警报
            alarmService.send(
                    AlarmMessageDTO
                            .buildAlarmMessageDTO("上报线程池列表出错")
                            .appendParameter("错误原因", e.toString())
            );
            // 记录错误日志
            log.error("动态线程池, 上报线程池列表时出现错误: {}", e.toString());
        } finally {
            // 释放Redis锁
            lock.unlock();
        }
    }

    @Override
    public void reportUpdateThreadPoolConfigParameter(UpdateThreadPoolConfigDTO updateThreadPoolConfigDTO) {
        // 将更新线程池配置参数保存到Redis中
        RedisUtils.setUpdateThreadPoolConfigDTO(
                redissonClient,
                updateThreadPoolConfigDTO
        );

    }

    private void reportThreadPoolRealProcess(List<ThreadPoolConfigEntity> threadPoolConfigEntityList,
            RList<ThreadPoolConfigEntity> list ) {
        // 初始化索引
        int index = 0;
        // 获取传入的线程池配置列表中的第一个线程池配置的应用名称
        String applicationName = threadPoolConfigEntityList.get(0).getApplicationName();

        // 获取Redis中的线程池配置列表的大小
        int listSize = list.size();
        // 获取本应用线程池的开始索引
        for (int i = 0; i < listSize; i++) {
            ThreadPoolConfigEntity originalPoolConfig = list.get(i);
            // 当前应用位置
            if (Objects.equals(originalPoolConfig.getApplicationName(), applicationName)) {
                index = i;
                break;
            }
        }

        // 更新线程池
        for (int i = 0; i < threadPoolConfigEntityList.size(); i++, index++) {
            ThreadPoolConfigEntity newPoolConfig = threadPoolConfigEntityList.get(i);
            // list已遍历完
            if (index >= listSize) {
                list.add(newPoolConfig);
                continue;
            }

            ThreadPoolConfigEntity originalPoolConfig = list.get(index);
            // list已遍历到下一个应用
            if (!Objects.equals(originalPoolConfig.getApplicationName(), applicationName)) {
                list.add(index + 1, newPoolConfig);
                continue;
            }

            // 当前应用某线程池发生了修改
            if (!Objects.equals(originalPoolConfig.toString(), newPoolConfig.toString())) {
                list.fastRemove(index);
                list.add(index, newPoolConfig);
            }
        }

        // 线程池已不存在，但缓存还在, 从缓存中删除
        while (index < listSize) {
            ThreadPoolConfigEntity originalPoolConfig = list.get(index);
            // 遍历到下一个应用
            if (!Objects.equals(originalPoolConfig.getApplicationName(), applicationName)) {
                break;
            }
            list.fastRemove(index);
            index++;
        }
    }
}
