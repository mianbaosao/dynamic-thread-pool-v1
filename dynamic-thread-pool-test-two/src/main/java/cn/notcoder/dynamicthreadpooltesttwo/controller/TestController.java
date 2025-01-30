package cn.bread.dynamicthreadpooltesttwo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController
public class TestController {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor02;

    @GetMapping("/dos")
    public void submitLogsOfThreads() {
        new Thread(() -> {
            for (int i = 0; i < 50; i++) {  // 提交大量任务
                threadPoolExecutor02.submit(() -> {
                    try {
                        // 每个任务模拟执行 2 分钟，确保任务处于运行状态
                        TimeUnit.SECONDS.sleep(60 * 2);
                    } catch (Exception ignored) {
                    }
                });
            }
        }).start();
    }
}
