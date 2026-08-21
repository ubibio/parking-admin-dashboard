package com.parking.admin.service;

import com.parking.admin.dto.DashboardKpiResponse;
import com.parking.admin.repository.ParkingRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * totalSpaceCount는 설정값(parking.total-space-count) 임시 처리 — 실제 면수 관리 체계는 명세 범위 밖.
 * occupancyLevel 임계값(90%/98%)은 design.md [상태값] 고정값 그대로 사용.
 * 근거: design.md [Screen 1] [완료조건]/[상태값], backend-schema.md §2
 */
@Service
public class DashboardKpiServiceImpl implements DashboardKpiService {

    private static final double WARN_THRESHOLD = 90.0;
    private static final double FULL_THRESHOLD = 98.0;

    private final ParkingRecordRepository parkingRecordRepository;
    private final int totalSpaceCount;

    public DashboardKpiServiceImpl(ParkingRecordRepository parkingRecordRepository,
                                    @Value("${parking.total-space-count:100}") int totalSpaceCount) {
        this.parkingRecordRepository = parkingRecordRepository;
        this.totalSpaceCount = totalSpaceCount;
    }

    @Override
    public DashboardKpiResponse getKpi() {
        long occupiedCount = parkingRecordRepository.countByExitAtIsNull();
        double occupancyRate = calculateOccupancyRate(occupiedCount);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = startOfDay.plusDays(1);

        long todayEntryCount = parkingRecordRepository.countByEntryAtBetween(startOfDay, startOfNextDay);
        long todayExitCount = parkingRecordRepository.countByExitAtBetween(startOfDay, startOfNextDay);
        BigDecimal todayRevenueAmount = parkingRecordRepository.sumFeeAmountByExitAtBetween(startOfDay, startOfNextDay);

        return DashboardKpiResponse.builder()
                .totalSpaceCount(totalSpaceCount)
                .occupiedCount((int) occupiedCount)
                .occupancyRate(occupancyRate)
                .occupancyLevel(resolveOccupancyLevel(occupancyRate))
                .todayEntryCount((int) todayEntryCount)
                .todayExitCount((int) todayExitCount)
                .todayRevenueAmount(todayRevenueAmount)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private double calculateOccupancyRate(long occupiedCount) {
        if (totalSpaceCount <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(occupiedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalSpaceCount), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String resolveOccupancyLevel(double occupancyRate) {
        if (occupancyRate >= FULL_THRESHOLD) {
            return "FULL";
        }
        if (occupancyRate >= WARN_THRESHOLD) {
            return "WARN";
        }
        return "NORMAL";
    }
}
