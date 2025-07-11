package de.gedoplan.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class RunningBatchListener implements JobExecutionListener {

    public static final String BATCH_RUNNING = "batch.running";

    private final AtomicLong running = new AtomicLong();

    public RunningBatchListener(MeterRegistry meterRegistry) {
        Gauge.builder(BATCH_RUNNING, this.running, AtomicLong::doubleValue)
                .strongReference(true)
                .register(meterRegistry);
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        this.running.incrementAndGet();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        this.running.decrementAndGet();
    }
}
