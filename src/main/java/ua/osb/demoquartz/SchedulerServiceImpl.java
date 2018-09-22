package ua.osb.demoquartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {

    private final Scheduler scheduler;

    @Autowired
    public SchedulerServiceImpl(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void rescheduleJob(final String triggerName, final String triggerGroup, final int newIntervalInSeconds) {
        TriggerKey triggerKey = createTriggerKey(triggerName, triggerGroup);
        try {

            Trigger newTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerName, triggerGroup)
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(newIntervalInSeconds)
                            .repeatForever())
                    .build();
            scheduler.rescheduleJob(triggerKey, newTrigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopJob(String triggerName, String triggerGroup) {
        TriggerKey triggerKey = createTriggerKey(triggerName, triggerGroup);
        try {
            scheduler.unscheduleJob(triggerKey);
        } catch (SchedulerException e) {
            log.error("Failed to unschedule job {}", triggerKey, e);
        }
    }

    private TriggerKey createTriggerKey(String triggerName, String triggerGroup) {
        return new TriggerKey(triggerName, triggerGroup);
    }
}
