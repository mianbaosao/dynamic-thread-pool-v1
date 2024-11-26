package cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto;

import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.entity.ThreadPoolConfigEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateThreadPoolConfigDTO {
    // 应用程序名称
    private String applicationName;
    // 线程池名称
    private String threadPoolName;
    // 核心线程数
    private Integer corePoolSize;
    // 最大线程数
    private Integer maximumPoolSize;
    // 队列容量
    private Integer queueCapacity;


    public static UpdateThreadPoolConfigDTO buildUpdateThreadPoolConfigDTO(
            ThreadPoolConfigEntity threadPoolConfigEntity ) {
        return new UpdateThreadPoolConfigDTO(
                threadPoolConfigEntity.getApplicationName(),
                threadPoolConfigEntity.getThreadPoolName(),
                threadPoolConfigEntity.getCorePoolSize(),
                threadPoolConfigEntity.getMaximumPoolSize(),
                threadPoolConfigEntity.getQueueSize() + threadPoolConfigEntity.getRemainingCapacity()
        );
    }
}
