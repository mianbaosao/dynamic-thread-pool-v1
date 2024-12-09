package cn.notcoder.dtp.sdk.dynamicthreadpoolspringbootstarter.utils;

import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class ApplicationUtils {
    public static String getApplicationName(ApplicationContext applicationContext) {
        return applicationContext.getEnvironment().getProperty("spring.application.name");
    }


    public static int getPort(ApplicationContext applicationContext) {
        return Integer.parseInt(applicationContext.getEnvironment().getProperty("server.port"));
    }
}
