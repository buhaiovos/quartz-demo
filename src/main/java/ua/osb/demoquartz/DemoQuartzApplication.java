package ua.osb.demoquartz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class DemoQuartzApplication implements ApplicationRunner {

    @Autowired
    private HelloMessageQuartzBean helloMessageQuartzBean;

    public static void main(String[] args) {
        SpringApplication.run(DemoQuartzApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Application started!");
        log.info("Starting job");
        helloMessageQuartzBean.scheduleJob();
    }
}
