package com.parking.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @EnableScheduling: DashboardAlertScheduler(Screen 1 알림 주기 평가)용.
 * 근거: design.md [Screen 1] [완료조건] 알림 바
 */
@SpringBootApplication
@EnableScheduling
public class ParkingAdminDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingAdminDashboardApplication.class, args);
	}

}
