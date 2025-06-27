package de.gedoplan.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.batch.core.*;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.stream.Collectors;

@Configuration
public class BatchBean {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private ItemWriteMetricsListener itemWriteMetricsListener;

    @Autowired
    private RunningBatchListener runningBatchListener;

    @Bean
    public Job syncDummyUser(JobRepository jobRepository, PlatformTransactionManager tx, RestTemplate restTemplate) {
        return new JobBuilder("syncDummyUser", jobRepository)
                .start(loadUsers(jobRepository, tx, restTemplate))
                .listener(runningBatchListener)
                .build();
    }

    @Bean
    public Step loadUsers(JobRepository jobRepository, PlatformTransactionManager tx, RestTemplate restTemplate) {
        return new StepBuilder("loadUsers", jobRepository)
                .<User, User>chunk(5, tx)
                .reader(jsonUserItemReader(restTemplate, null))
                .writer(consoleWriter())
                .listener(itemWriteMetricsListener)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<User> jsonUserItemReader(RestTemplate restTemplate, @Value("#{stepExecution}") StepExecution stepExecution) {
        return () -> {
            long keyToRead = stepExecution.getExecutionContext().containsKey("id") ? stepExecution.getExecutionContext().getLong("id") : 1L;
            try {
                var user = restTemplate.getForEntity("https://dummyjson.com/users/" + keyToRead, User.class).getBody();
                stepExecution.getExecutionContext().put("id", keyToRead + 1);
                return user;
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return null;
                } else {
                    throw e;
                }
            }
        };
    }

    @Bean
    ItemWriter<User> consoleWriter() {
        return (user) -> System.out.println(user.getItems().stream().map(User::id).map(BigInteger::toString).collect(Collectors.joining(",")));
    }

    public record User(BigInteger id, String firstName, String lastName) {
    }

    @Component
    public static class ItemWriteMetricsListener implements StepExecutionListener {

        private final MeterRegistry meterRegistry;

        public ItemWriteMetricsListener(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        @Override
        @AfterStep
        public ExitStatus afterStep(StepExecution stepExecution) {
            long writeCount = stepExecution.getWriteCount();
            meterRegistry.counter("batch.items.written-total." + stepExecution.getJobExecution().getJobInstance().getJobName()).increment(writeCount);
            return stepExecution.getExitStatus();
        }
    }
}
