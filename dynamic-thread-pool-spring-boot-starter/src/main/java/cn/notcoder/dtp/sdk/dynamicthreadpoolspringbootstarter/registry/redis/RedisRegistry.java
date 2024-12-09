package cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.registry.redis;

import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.AlertMessageDTO;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.UpdateThreadPoolConfigDTO;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.entity.ThreadPoolConfigEntity;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.enums.RegistryEnum;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.registry.IRegistry;

import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.service.IAlertService;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.service.impl.AlertServiceImpl;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.utils.ApplicationUtils;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.utils.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 实现注册中心
 * 完成THREAD_POOL_CONFIG_LIST_KEY的配置
 */

@Slf4j
@AllArgsConstructor
public class RedisRegistry implements IRegistry {

    private RedissonClient redissonClient;

    //private IAlarmService alarmService;

    private IAlertService alertService;

    @Override
    public void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolConfigEntityList) {

        // 如果传入的线程池配置列表为空，则直接返回
        if (threadPoolConfigEntityList == null || threadPoolConfigEntityList.isEmpty()) {
            return;
        }
        // 如果可以发送线程池危险警报，则发送警报
        if (AlertServiceImpl.canSendForThreadPoolDanger()) {
            alertService.sendIfThreadPoolHasDanger(threadPoolConfigEntityList);
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
            alertService.send(AlertMessageDTO
                            .buildAlertMessageDTO("上报线程池列表出错")
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

    private void reportThreadPoolRealProcess(List<ThreadPoolConfigEntity> threadPoolConfigEntityList, RList<ThreadPoolConfigEntity> list) {
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

        // 创建一个新的列表来存储有效的线程池配置
        List<ThreadPoolConfigEntity> validThreadPools = new ArrayList<>();

        // 更新线程池配置，并删除未运行的配置
        for (int i = 0; i < threadPoolConfigEntityList.size(); i++, index++) {
            ThreadPoolConfigEntity newPoolConfig = threadPoolConfigEntityList.get(i);
            boolean isRunning = isApplicationRunning(newPoolConfig.getPort()); // 检查应用是否运行

            // 如果应用正在运行，则加入有效列表
            if (isRunning) {
                validThreadPools.add(newPoolConfig);
            } else {
                // 输出未运行的配置，并跳过
                log.info("应用在端口 " + newPoolConfig.getPort() + " 上没有运行，已删除该配置");
            }

            // 如果 Redis 中的线程池列表已经遍历完，直接添加新的配置
            if (index >= listSize) {
                list.add(newPoolConfig);
                continue;
            }

            ThreadPoolConfigEntity originalPoolConfig = list.get(index);
            // 如果是不同的应用，插入到列表中
            if (!Objects.equals(originalPoolConfig.getApplicationName(), applicationName)) {
                list.add(index + 1, newPoolConfig);
                continue;
            }

            // 如果当前应用的某个线程池发生了修改
            if (!Objects.equals(originalPoolConfig.toString(), newPoolConfig.toString())) {
                // 从列表中删除指定索引的元素
                list.fastRemove(index);
                list.add(index, newPoolConfig);
            }
        }

        // 如果线程池已不存在，但缓存中仍然存在配置，则删除
        while (index < listSize) {
            ThreadPoolConfigEntity originalPoolConfig = list.get(index);
            if (!Objects.equals(originalPoolConfig.getApplicationName(), applicationName)) {
                break;
            }
            list.fastRemove(index);
            index++;
        }

        // 更新 Redis 中的线程池配置列表为有效配置列表
        list.clear();
        list.addAll(validThreadPools); // 重新填充有效的线程池配置列表

        // 排序，确保同一应用的线程池配置在一起
        list.sort(Comparator.comparing(ThreadPoolConfigEntity::getApplicationName));

        // 获取所有有效的端口列表
        List<Integer> ports = new ArrayList<>();
        for (ThreadPoolConfigEntity threadPoolConfigEntity : validThreadPools) {
            ports.add(threadPoolConfigEntity.getPort());
        }

        // 如果需要做其他操作，比如报告这些端口，可以在此处继续处理
    }

    // 检查指定端口的应用是否正在运行
    public static boolean isApplicationRunning(int port) {
        String urlString = "http://localhost:" + port;  // 假设服务在 localhost 上运行
        try {
            // 创建 URL 对象
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");  // 使用 GET 请求
            connection.setConnectTimeout(2000);  // 设置连接超时为 2 秒
            connection.setReadTimeout(2000);     // 设置读取超时为 2 秒

            // 获取响应码
            int responseCode = connection.getResponseCode();

            // 如果响应码是 200，表示应用正在运行
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            // 捕获异常表示连接不上，可能是应用未运行
            return false;
        }
    }


}
