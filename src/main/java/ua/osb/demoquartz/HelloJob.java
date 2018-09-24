package ua.osb.demoquartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
public class HelloJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        final String helloMessage = System.getenv("hello.message");
        log.info(helloMessage);
        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            log.warn("Failed to sleep for 10 seconds");
        }
    }
}
