package bangbang.gourmet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "crawlTaskExecutor")
    public Executor crawlTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);        // 기본적으로 유지할 스레드 수
        executor.setMaxPoolSize(5);         // 최대 스레드 수
        executor.setQueueCapacity(10);      // 대기 큐 크기
        executor.setThreadNamePrefix("CrawlThread-");
        executor.setWaitForTasksToCompleteOnShutdown(false); // 서버 꺼질 때 작업 안 기다리고 즉시 종료
        executor.setAwaitTerminationSeconds(5);              // 최대 5초까지만 기다려줌
        executor.initialize();
        return executor;
    }
}
