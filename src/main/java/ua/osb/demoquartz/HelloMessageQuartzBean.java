package ua.osb.demoquartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class HelloMessageQuartzBean {
    private static final int DEFAULT_INTERVAL_SEC = 10;
    private final Scheduler scheduler;

    @Value("${quartz.hello-message.job.name}")
    private String jobName;
    @Value("${quartz.hello-message.trigger.name}")
    private String triggerName;
    @Value("${quartz.hello-message.trigger.group}")
    private String triggerGroup;

    @Autowired
    public HelloMessageQuartzBean(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    void scheduleJob() {
        startScheduler();

        JobDetail job = buildJob();

        addJobToSchedulerForReuse(job);

        Trigger trigger = buildTriggerWithRepeatInterval(job);

        scheduleJob(trigger);
    }

    private Trigger buildTriggerWithRepeatInterval(JobDetail job) {
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerName, triggerGroup)
                .forJob(job)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(HelloMessageQuartzBean.DEFAULT_INTERVAL_SEC)
                        .repeatForever())
                .build();
    }

    private JobDetail buildJob() {
        return JobBuilder.newJob(HelloJob.class)
                .withIdentity(jobName)
                .storeDurably()
                .build();
    }

    private void addJobToSchedulerForReuse(JobDetail job) {
        try {
            scheduler.addJob(job, false);
        } catch (SchedulerException e) {
            log.error("Failed to save add job to scheduler for reuse", e);
        }
    }

    private void startScheduler() {
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            log.error("Failed to start scheduler");
        }
    }

    private void scheduleJob(Trigger trigger) {
        try {
            scheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            log.error("Failed to schedule job", e);
            throw new RuntimeException();
        }
    }
}
