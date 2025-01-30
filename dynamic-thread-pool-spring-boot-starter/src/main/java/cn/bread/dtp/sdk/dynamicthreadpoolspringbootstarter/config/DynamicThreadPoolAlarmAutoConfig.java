package cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.config;

import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.config.properties.DynamicThreadPoolAlarmAutoProperties;

import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.service.IAlertService;

import cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.service.impl.AlertServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

//定义 Spring 的配置类  将此时这个类里面的@bean的类放到容器里
@Configuration
//当配置属性 dynamic-thread-pool.alarm.enabled 的值为 true 时，才会加载被注解的 Bean 或配置类
@ConditionalOnProperty(prefix = "dynamic-thread-pool.alarm", name = "enabled", havingValue = "true")
//其属性与 application.yml 或 application.properties 中的相关配置自动绑定
@EnableConfigurationProperties(DynamicThreadPoolAlarmAutoProperties.class)
@ComponentScan("cn.bread.dtp.sdk.dynamicthreadpoolspringbootstarter.strategy")
public class DynamicThreadPoolAlarmAutoConfig {

   /* @Bean
    public IAlarmService alarmService() {
        return new AlarmServiceImpl();
    }*/

    @Bean
    public IAlertService alertService() {return new AlertServiceImpl();}

}
