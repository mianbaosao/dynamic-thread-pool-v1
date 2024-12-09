package cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.trigger.listener;

import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.dto.UpdateThreadPoolConfigDTO;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.model.entity.ThreadPoolConfigEntity;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.registry.IRegistry;
import cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.service.IDynamicThreadPoolService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.listener.MessageListener;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class ThreadPoolConfigAdjustListener implements MessageListener<UpdateThreadPoolConfigDTO> {

    private IDynamicThreadPoolService dynamicThreadPoolService;

    private IRegistry registry;


    /**
     * 处理接收到的消息
     * @param charSequence 消息的内容
     * @param updateThreadPoolConfigDTO 包含线程池配置变更的DTO
     */
    @Override
    public void onMessage(CharSequence charSequence, UpdateThreadPoolConfigDTO updateThreadPoolConfigDTO) {
        // 1. 更新线程池配置
        Boolean success = dynamicThreadPoolService.updateThreadPoolConfig(updateThreadPoolConfigDTO);
        if (!success) {
            log.warn("动态线程池, 配置变更结果: {}, 配置参数: {}", success, updateThreadPoolConfigDTO);
            return;
        }
        log.info("动态线程池, 配置变更结果: {}, 配置参数: {}", success, updateThreadPoolConfigDTO);

        // 2. 获取当前线程池配置列表
        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();

        // 3. 上报线程池配置信息到注册中心
        registry.reportThreadPool(threadPoolConfigEntities);
        log.info("动态线程池, 上报线程池信息: {}", threadPoolConfigEntities);

        // 4. 根据线程池名称查询当前配置
        UpdateThreadPoolConfigDTO updateThreadPoolConfigDTOCurrent = UpdateThreadPoolConfigDTO.buildUpdateThreadPoolConfigDTO(
                dynamicThreadPoolService.queryThreadPoolByName(
                        updateThreadPoolConfigDTO.getThreadPoolName()
                )
        );

        // 5. 上报配置参数变更到注册中心
        registry.reportUpdateThreadPoolConfigParameter(updateThreadPoolConfigDTO);
        log.info("动态线程池, 上报配置参数: {}", updateThreadPoolConfigDTOCurrent);
    }
}
