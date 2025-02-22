package cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.config;


import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.config.properties.DynamicThreadPoolWebAutoProperties;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.web.config.FrontendConfig;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.web.controller.AuthController;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.web.controller.SettingsController;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.web.controller.ThreadPoolController;
import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.web.exception.DynamicThreadPoolWebGlobalExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

// 当dynamic-thread-pool.web.enabled的值为true时，该注解才会生效
@ConditionalOnProperty(prefix = "dynamic-thread-pool.web", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(DynamicThreadPoolWebAutoProperties.class)
public class DynamicThreadPoolWebAutoConfig {

    @Bean
    public FrontendConfig dynamicThreadPoolStaticConfig(
            DynamicThreadPoolWebAutoProperties dynamicThreadPoolWebAutoProperties
    ) {
        return new FrontendConfig(dynamicThreadPoolWebAutoProperties);
    }

    @Bean
    public AuthController dynamicThreadPoolAuthController() {
        return new AuthController();
    }

    @Bean
    public ThreadPoolController dynamicThreadPoolThreadPoolController() {
        return new ThreadPoolController();
    }

    @Bean
    public SettingsController dynamicThreadPoolSettingsController() {
        return new SettingsController();
    }

    @Bean
    public DynamicThreadPoolWebGlobalExceptionHandler dynamicThreadPoolWebGlobalExceptionHandler() {
        return new DynamicThreadPoolWebGlobalExceptionHandler();
    }
}
