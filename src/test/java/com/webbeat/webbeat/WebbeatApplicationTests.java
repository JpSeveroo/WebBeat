package com.webbeat.webbeat;

import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.repository.MonitoredRepository;
import com.webbeat.webbeat.scheduler.SchedulerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebbeatApplicationTests {
    @Autowired
    MonitoredRepository monitoredRepository;
    @Autowired
    SchedulerService schedulerService;


	@Test
	void contextLoads() throws InterruptedException {
        schedulerService.allApis("692c6ec9b6b08f269960eea5");
        schedulerService.startScheduler("6928c90fef163892b02ae0f1", 1);
        Thread.sleep(3000);
        //schedulerService.stopMonitoring("6928c90fef163892b02ae0f1");
	}

}
