package com.java.test;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StorePerformanceTestApplication {
	static int performance_count = 0;

	public static void main(String[] args) throws IOException {
		SpringApplication.run(StorePerformanceTestApplication.class, args);
		CreateIndex.createindex();

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {

					performance_count += 1;
					PerformanceTest.performanceTest(performance_count);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(runnable, 0, 1000, TimeUnit.MILLISECONDS);

		if (performance_count == 10) {
			service.isShutdown();
		}
	}
}
