package com.webbeat.webbeat;

import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.repository.LogRepository;
import com.webbeat.webbeat.repository.MonitoredRepository;
import com.webbeat.webbeat.scheduler.SchedulerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebbeatApplicationTests {
    @Autowired
    LogRepository logsrepository;
    @Autowired
    SchedulerService schedulerService;


	@Test
	void contextLoads() throws InterruptedException {
        schedulerService.allApis("692c6ec9b6b08f269960eea5");
        schedulerService.startScheduler("692c702fb6b08f269960eea6", 1);
        schedulerService.startScheduler("692c7181b6b08f269960eea7", 1);
        Thread.sleep(10000);
        schedulerService.stopMonitoring("692c702fb6b08f269960eea6");
        schedulerService.stopMonitoring("692c7181b6b08f269960eea7");
        Thread.sleep(3000);
        System.out.println(logsrepository.findTopByOwnerIdAndMonitoredIdOrderByTimestampDesc("692c6ec9b6b08f269960eea5","692c702fb6b08f269960eea6"));

	}

}
