package ua.osb.demoquartz;

public interface SchedulerService {
    void rescheduleJob(String triggerName, String triggerGroup, int newIntervalInSeconds);
    void stopJob(String triggerName, String triggerGroup);
}
