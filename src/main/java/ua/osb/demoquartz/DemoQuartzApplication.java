package ua.osb.demoquartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class DemoQuartzApplication implements ApplicationRunner {

    @Autowired
    private QuartzBean quartzBean;
    @Autowired
    private Scheduler scheduler;

    public static void main(String[] args) {
        SpringApplication.run(DemoQuartzApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Application started!");
        log.info("Starting job");
        quartzBean.scheduleJob();
    }
}
