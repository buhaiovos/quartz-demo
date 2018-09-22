package ua.osb.demoquartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class QuartzBean {
    private final Scheduler scheduler;

    @Autowired
    public QuartzBean(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    void scheduleJob() {
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            log.error("Failed to start scheduler");
        }

        JobDetail job = JobBuilder.newJob(HelloJob.class)
                .withIdentity("job1", "group1")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1", "group1")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(10)
                        .repeatForever())
                .build();

        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error("Failed to schedule job: {}", job);
            throw new RuntimeException();
        }
    }
}
