package de.gedoplan.metrics;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.UUID;

@Component
public class ScheduleBean {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job syncDummyUser;


    @Scheduled(cron = "0 * * * * *")
    public void sayHelloCron(){
        System.out.println("Hello");
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void startBatch() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        jobLauncher.run(syncDummyUser, new JobParametersBuilder()
                .addJobParameter("id",BigInteger.ZERO, BigInteger.class)
                .addJobParameter("jobUUid", UUID.randomUUID(), UUID.class)
                .toJobParameters());
    }
}
