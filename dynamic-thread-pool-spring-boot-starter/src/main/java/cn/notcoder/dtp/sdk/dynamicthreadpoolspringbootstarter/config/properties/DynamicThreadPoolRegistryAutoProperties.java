package cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "dynamic-thread-pool.registry")
public class DynamicThreadPoolRegistryAutoProperties {
    //用于存储定时任务的cron表达式，表示每20秒执行一次
    private String reportCron = "0/20 * * * * ?";
}
