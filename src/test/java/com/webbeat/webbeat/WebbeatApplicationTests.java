package com.webbeat.webbeat;

import com.webbeat.webbeat.controller.SchedulerController;
import com.webbeat.webbeat.model.Monitored;
import com.webbeat.webbeat.repository.MonitoredRepository;
import com.webbeat.webbeat.service.SchedulerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
class WebbeatApplicationTests {
    @Autowired
    MonitoredRepository monitoredRepository;
    @Autowired
    SchedulerService schedulerService;


	@Test
	void contextLoads() throws InterruptedException {

	}

}
