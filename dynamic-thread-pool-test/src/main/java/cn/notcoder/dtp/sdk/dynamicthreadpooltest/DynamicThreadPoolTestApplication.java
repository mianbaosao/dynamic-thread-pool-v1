package cn.notcoder.dtp.sdk.dynamicthreadpooltest;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class DynamicThreadPoolTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(DynamicThreadPoolTestApplication.class, args);
    }
}