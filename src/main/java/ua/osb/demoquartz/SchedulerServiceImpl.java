package ua.osb.demoquartz;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {
    private final Scheduler scheduler;
    private final Map<TriggerKey, JobDetail> jobDetailsByTriggerKeysCache;

    @Autowired
    public SchedulerServiceImpl(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.jobDetailsByTriggerKeysCache = new ConcurrentHashMap<>();
    }

    @Override
    public void rescheduleJob(final String triggerName, final String triggerGroup, final int newIntervalInSeconds) {
        final TriggerKey triggerKey = createTriggerKey(triggerName, triggerGroup);
        final TriggerParameters triggerParameters = buildTriggerParameters(triggerKey, newIntervalInSeconds);
        final Trigger newTrigger = buildTrigger(triggerKey, triggerParameters);
        handleScheduling(triggerKey, newTrigger);
    }

    private void handleScheduling(TriggerKey triggerKey, Trigger newTrigger) {
        if (triggerExists(triggerKey)) {
            reschedule(triggerKey, newTrigger);
        } else {
            schedule(newTrigger);
        }
    }

    private TriggerKey createTriggerKey(String triggerName, String triggerGroup) {
        return new TriggerKey(triggerName, triggerGroup);
    }

    private TriggerParameters buildTriggerParameters(TriggerKey triggerKey, final int newIntervalInSeconds) {
        final JobDetail jobDetail = findJobDetail(triggerKey);
        return new TriggerParameters(newIntervalInSeconds, jobDetail);
    }

    private JobDetail findJobDetail(TriggerKey triggerKey) {
        cacheJobDetailsIfNotCached(triggerKey);
        return jobDetailsByTriggerKeysCache.get(triggerKey);
    }

    private Trigger buildTrigger(TriggerKey triggerKey, TriggerParameters parameters) {
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .forJob(parameters.targetJob)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(parameters.repeatInterval)
                        .repeatForever())
                .build();
    }

    private void reschedule(TriggerKey triggerKey, Trigger newTrigger) {
        try {
            scheduler.rescheduleJob(triggerKey, newTrigger);
        } catch (SchedulerException e) {
            log.error("Failed to schedule a job with trigger {}", newTrigger, e);
            throw new RuntimeException("Failed to reschedule");
        }
    }

    private void schedule(Trigger newTrigger) {
        try {
            scheduler.scheduleJob(newTrigger);
        } catch (SchedulerException e) {
            log.error("Failed to schedule a job with trigger {}", newTrigger, e);
            throw new RuntimeException("Fail to schedule");
        }
    }

    private boolean triggerExists(TriggerKey triggerKey) {
        try {
            return scheduler.checkExists(triggerKey);
        } catch (SchedulerException e) {
            log.error("Existence check for trigger <{}> has failed", triggerKey, e);
            return false;
        }
    }

    @Override
    public void stopJob(String triggerName, String triggerGroup) {
        TriggerKey triggerKey = createTriggerKey(triggerName, triggerGroup);
        cacheJobDetailsIfNotCached(triggerKey);
        try {
            scheduler.unscheduleJob(triggerKey);
        } catch (SchedulerException e) {
            log.error("Failed to unschedule job {}", triggerKey, e);
            throw new RuntimeException(e);
        }
    }

    private void cacheJobDetailsIfNotCached(TriggerKey triggerKey) {
        if (!jobDetailsByTriggerKeysCache.containsKey(triggerKey)) {
            JobDetail job = getJobDetailFromScheduler(triggerKey);
            jobDetailsByTriggerKeysCache.putIfAbsent(triggerKey, job);
        }
    }

    private JobDetail getJobDetailFromScheduler(TriggerKey triggerKey) {
        try {
            final Trigger trigger = scheduler.getTrigger(triggerKey);
            final JobKey jobKey = trigger.getJobKey();
            return scheduler.getJobDetail(jobKey);
        } catch (SchedulerException e) {
            log.error("Failed to find JobDetail for trigger key:{}", triggerKey);
            throw new RuntimeException(e);
        }
    }

    @Data
    private static final class TriggerParameters {
        private final int repeatInterval;
        private final JobDetail targetJob;
    }
}
