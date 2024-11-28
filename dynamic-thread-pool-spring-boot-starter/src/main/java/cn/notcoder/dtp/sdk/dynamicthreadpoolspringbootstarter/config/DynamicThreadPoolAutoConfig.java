package cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.config;

import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.config.properties.DynamicThreadPoolRegistryRedisAutoProperties;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.RefreshThreadPoolConfigDTO;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.UpdateThreadPoolConfigDTO;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.enums.RegistryEnum;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.registry.IRegistry;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.registry.redis.RedisRegistry;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.service.IAlarmService;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.service.IDynamicThreadPoolService;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.service.impl.AlarmServiceImpl;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.service.impl.DynamicThreadPoolServiceImpl;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.trigger.job.ThreadPoolDataReportJob;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.trigger.listener.ThreadPoolConfigAdjustListener;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.trigger.listener.ThreadPoolConfigRefreshListener;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.utils.ApplicationUtils;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.utils.RedisUtils;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusProperties;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 自动配置入口
 * 这个类起到了整合和管理作用，将线程池 SDK 的相关配置和功能模块统一注册到 Spring 容器中
 */
@Slf4j
@Configuration
//将配置文件中与 DynamicThreadPoolRegistryRedisAutoProperties 类中字段对应的值加载到类的属性中。
@EnableConfigurationProperties(DynamicThreadPoolRegistryRedisAutoProperties.class)
@EnableScheduling
// 导入自动配置类
@ImportAutoConfiguration({DynamicThreadPoolWebAutoConfig.class, DynamicThreadPoolAlarmAutoConfig.class})
public class DynamicThreadPoolAutoConfig {
    //RedissonClient的配置
    @Bean
    public RedissonClient redissonClient(DynamicThreadPoolRegistryRedisAutoProperties properties) {
        Config config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE);
        config.useSingleServer()
                .setAddress(String.format("redis://%s:%d", properties.getHost(), properties.getPort()))
                .setPassword(properties.getPassword())
                .setDatabase(properties.getDatabase())
                .setConnectionPoolSize(properties.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(properties.getConnectionMinimumIdleSize())
                .setIdleConnectionTimeout(properties.getIdleConnectionTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setKeepAlive(properties.getKeepAlive());
        return Redisson.create(config);
    }

    @Bean
    public IRegistry redisRegistry(RedissonClient redissonClient, IAlarmService alarmService) {
        return new RedisRegistry(redissonClient, alarmService);
    }

    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(
            IDynamicThreadPoolService dynamicThreadPoolService,
            IRegistry redisRegistry) {
        return new ThreadPoolDataReportJob(dynamicThreadPoolService, redisRegistry);
    }


    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(
            IDynamicThreadPoolService dynamicThreadPoolService,
            IRegistry redisRegistry) {
        return new ThreadPoolConfigAdjustListener(dynamicThreadPoolService, redisRegistry);
    }


    @Bean
    public RTopic dynamicThreadPoolAdjustRedisTopic(
            ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener,
            RedissonClient redissonClient) {
        RTopic topic = redissonClient.getTopic(RegistryEnum.DYNAMIC_THREAD_POOL_ADJUST_REDIS_TOPIC_KEY.getKey());
        topic.addListener(UpdateThreadPoolConfigDTO.class, threadPoolConfigAdjustListener);
        return topic;
    }

    @Bean
    public ThreadPoolConfigRefreshListener threadPoolConfigRefreshListener(
            IDynamicThreadPoolService dynamicThreadPoolService,
            IRegistry redisRegistry) {
        return new ThreadPoolConfigRefreshListener(dynamicThreadPoolService, redisRegistry);
    }

    @Bean
    public RTopic dynamicThreadPoolRefreshRedisTopic(
            ThreadPoolConfigRefreshListener threadPoolConfigRefreshListener,
            RedissonClient redissonClient) {
        RTopic topic = redissonClient.getTopic(RegistryEnum.DYNAMIC_THREAD_POOL_REFRESH_REDIS_TOPIC_KEY.getKey());
        topic.addListener(RefreshThreadPoolConfigDTO.class, threadPoolConfigRefreshListener);
        return topic;
    }

    @Bean
    public DynamicThreadPoolServiceImpl dynamicThreadPoolService(
            ApplicationContext applicationContext,
            Map<String, ThreadPoolExecutor> threadPoolExecutorMap,
            RedissonClient redissonClient,
            IAlarmService alarmService
    ) {
        String applicationName = ApplicationUtils.getApplicationName(applicationContext);
        if (StringUtils.isBlank(applicationName)) {
            log.warn("动态线程池启动提示。SpringBoot 应用未配置应用名(spring.application.name)");
        }

        // 创建Bean
        DynamicThreadPoolServiceImpl dynamicThreadPoolService = new DynamicThreadPoolServiceImpl(
                applicationName,
                threadPoolExecutorMap,
                alarmService
        );

        // 获取缓存的配置信息，配置线程池
        threadPoolExecutorMap.forEach((poolName, executor) -> {
            UpdateThreadPoolConfigDTO updateThreadPoolConfigDTO = RedisUtils.getUpdateThreadPoolConfigDTO(
                    redissonClient,
                    applicationName,
                    poolName
            );
            if (updateThreadPoolConfigDTO == null) {
                return;
            }


            dynamicThreadPoolService.updateThreadPoolConfig(
                    updateThreadPoolConfigDTO
            );

        });

        return dynamicThreadPoolService;
    }

    @Bean
    public PrometheusConfigRunner prometheusConfigRunner(
            ApplicationContext applicationContext,  // Spring 应用上下文，提供对 Spring 容器的访问
            WebEndpointProperties webEndpointProperties,  // 管理 Web 端点属性
            PrometheusProperties prometheusProperties  // 管理 Prometheus 相关配置属性
    ) {
        // 配置 Web 端点暴露的内容，包括 "health" 和 "prometheus"
        // "health" 用于提供健康检查信息
        // "prometheus" 用于 Prometheus 监控数据的暴露
        webEndpointProperties.getExposure().setInclude(
                new HashSet<>(Arrays.asList(
                        "health",        // 健康检查端点
                        "prometheus"     // Prometheus 数据暴露端点
                ))
        );
        // 启用 Prometheus 功能，确保 Prometheus 数据被正确暴露
        prometheusProperties.setEnabled(true);
        // 创建并返回 PrometheusConfigRunner 对象，用于运行 Prometheus 的相关配置逻辑
        return new PrometheusConfigRunner(
                applicationContext // 将 Spring 上下文传递给 PrometheusConfigRunner
        );
    }


    @Bean
    public MeterFilter customMeterFilter() {
        return new MeterFilter() {
            @Override
            public MeterFilterReply accept(Meter.Id id) {
                if (id.getName().contains("thread_pool")) {
                    return MeterFilterReply.ACCEPT;
                }
                return MeterFilterReply.DENY;
            }
        };
    }
}
