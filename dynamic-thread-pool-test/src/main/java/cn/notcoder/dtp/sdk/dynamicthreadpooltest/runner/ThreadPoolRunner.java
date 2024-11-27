package cn.notcoder.dtp.sdk.dynamicthreadpooltest.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ThreadPoolRunner implements ApplicationRunner {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor01;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 创建一个线程
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    Random random = new Random();
                    int startDuration = random.nextInt(5) + 1;
                    // 生成一个1到10之间的随机数作为运行时间
                    int runDuration = random.nextInt(10) + 1;
                    // 提交一个任务到线程池
                    threadPoolExecutor01.submit(() -> {
                        try {
                            // 让线程休眠启动时间
                            TimeUnit.SECONDS.sleep(startDuration);
                            // 输出启动时间
                            System.out.printf("启动花费时间: %ds\n", startDuration);

                            // 让线程休眠运行时间
                            TimeUnit.SECONDS.sleep(runDuration);
                            // 输出运行时间
                            System.out.printf("运行花费时间: %ds\n", runDuration);
                        } catch (InterruptedException e) {
                            // 抛出运行时异常
                            throw new RuntimeException(e);
                        }
                    });
                    // 让线程休眠1到10秒
                    Thread.sleep((random.nextInt(10) + 1) * 1000);
                }
            } catch (Exception e) {}
        });
        // 启动线程
        t.start();
    }
}
