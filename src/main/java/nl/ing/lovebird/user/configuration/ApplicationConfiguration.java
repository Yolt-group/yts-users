package nl.ing.lovebird.user.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Clock;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableCaching
@Slf4j
public class ApplicationConfiguration {

    public static final String ASYNC_BATCH_EXECUTOR = "asyncBatchExecutor";
    public static final String ASYNC_PG_MIGRATION_EXECUTOR = "asyncPgMigrationExecutor";
    public static final String ASYNC_EULA_BATCH_EXECUTOR = "asyncEulaBatchExecutor";
    public static final String ASYNC_POPULATE_USER_DELETE_EVENTS_EXECUTOR = "asyncPopulateUserDeleteEventsExecutor";

    @Bean(ASYNC_BATCH_EXECUTOR)
    public Executor asyncBatchExecutor(TaskExecutorBuilder builder) {
        return createNewExecutor(builder);
    }

    @Bean(ASYNC_EULA_BATCH_EXECUTOR)
    public Executor asyncEulaBatchExecutor(TaskExecutorBuilder builder) {
        return createNewExecutor(builder);
    }

    @Bean(ASYNC_POPULATE_USER_DELETE_EVENTS_EXECUTOR)
    public Executor asyncPopulateUserDeleteEventsExecutor(TaskExecutorBuilder builder) {
        return builder
                .corePoolSize(1)
                .maxPoolSize(1)
                .queueCapacity(1)
                .threadNamePrefix(ASYNC_POPULATE_USER_DELETE_EVENTS_EXECUTOR + "-")
                .build();
    }

    @Bean(ASYNC_PG_MIGRATION_EXECUTOR)
    public Executor asyncPgMigrationExecutor(TaskExecutorBuilder builder) {
        return createNewExecutor(builder);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    private Executor createNewExecutor(TaskExecutorBuilder builder) {
        return builder
                .corePoolSize(1)
                .maxPoolSize(2)
                .queueCapacity(0)
                .build();
    }
}
