package com.parking.admin.service;

import com.parking.admin.dto.RevenueDetailByPeriodResponse;
import com.parking.admin.dto.RevenueDetailItemResponse;
import com.parking.admin.dto.RevenueDetailsResponse;
import com.parking.admin.dto.RevenuePaymentMethodItemResponse;
import com.parking.admin.dto.RevenueSummaryResponse;
import com.parking.admin.entity.ParkingRecord;
import com.parking.admin.repository.ParkingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * 근거: design.md [Screen 3] 모듈경계 RevenueStatisticsService, 입출력계약, backend-schema.md §4.
 *
 * MyBatis 미사용(가정 — Issues 참고): design.md는 "다중 GROUP BY 집계는 RevenueQueryMapper(MyBatis)" 를
 * 명시하나, 이 target_repo에는 MyBatis 의존성이 없고(build.gradle) Screen 1(6-0)의 트래픽 집계도
 * ParkingRecordRepository로 조회한 리스트를 서비스 계층에서 Java로 그룹핑하는 동일 관례를 이미 쓰고 있다
 * (ParkingEventLogServiceImpl 참고). 새 라이브러리(mybatis-spring-boot-starter) 임의 추가를 피하기 위해
 * 같은 방식(JPA 조회 + 서비스 계층 그룹핑)을 그대로 따랐다.
 *
 * 집계 기준 시각은 exitAt(정산 확정 시점)이다 — entryCount만 entryAt 기준으로 별도 집계한다.
 * unpaidAmount/refundAmount 정의(가정 — Issues 참고):
 *  - unpaidAmount = settlementStatus=UNSETTLED(출차했으나 미정산) 건의 (feeAmount - discountAmount) 합
 *  - refundAmount = 0 고정 — PARKING_RECORD에 결제취소/환불 이력 컬럼이 없어 데이터 없음(추측 금지, §Issues)
 */
@Service
@RequiredArgsConstructor
public class RevenueStatisticsServiceImpl implements RevenueStatisticsService {

    private static final String PERIOD_DAILY = "DAILY";
    private static final String PERIOD_MONTHLY = "MONTHLY";
    private static final String PERIOD_YEARLY = "YEARLY";

    private static final String SETTLEMENT_UNSETTLED = "UNSETTLED";
    private static final String PAYMENT_CARD = "CARD";
    private static final String PAYMENT_EASY_PAY = "EASY_PAY";

    private final ParkingRecordRepository parkingRecordRepository;

    @Override
    public RevenueSummaryResponse getSummary(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime start = requireStart(fromDate);
        LocalDateTime end = requireEnd(toDate);

        BigDecimal totalFeeAmount = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        BigDecimal netPaidAmount = BigDecimal.ZERO;
        BigDecimal unpaidAmount = BigDecimal.ZERO;

        for (ParkingRecord record : parkingRecordRepository.findByExitAtBetween(start, end)) {
            BigDecimal fee = nvl(record.getFeeAmount());
            BigDecimal discount = nvl(record.getDiscountAmount());
            totalFeeAmount = totalFeeAmount.add(fee);
            totalDiscountAmount = totalDiscountAmount.add(discount);
            BigDecimal net = fee.subtract(discount);
            if (SETTLEMENT_UNSETTLED.equals(record.getSettlementStatus())) {
                unpaidAmount = unpaidAmount.add(net);
            } else {
                netPaidAmount = netPaidAmount.add(net);
            }
        }

        return RevenueSummaryResponse.builder()
                .totalFeeAmount(totalFeeAmount)
                .totalDiscountAmount(totalDiscountAmount)
                .netPaidAmount(netPaidAmount)
                .unpaidAmount(unpaidAmount)
                .refundAmount(BigDecimal.ZERO)
                .build();
    }

    @Override
    public RevenueDetailsResponse getDetails(String periodType, LocalDate fromDate, LocalDate toDate) {
        String period = requirePeriodType(periodType);
        LocalDateTime start = requireStart(fromDate);
        LocalDateTime end = requireEnd(toDate);

        Map<String, Integer> entryCountByPeriod = new LinkedHashMap<>();
        for (ParkingRecord record : parkingRecordRepository.findByEntryAtBetween(start, end)) {
            String key = resolvePeriodKey(period, record.getEntryAt());
            entryCountByPeriod.merge(key, 1, Integer::sum);
        }

        Map<String, ExitAggregate> exitAggregateByPeriod = new LinkedHashMap<>();
        for (ParkingRecord record : parkingRecordRepository.findByExitAtBetween(start, end)) {
            String key = resolvePeriodKey(period, record.getExitAt());
            exitAggregateByPeriod.computeIfAbsent(key, k -> new ExitAggregate()).accumulate(record);
        }

        TreeSet<String> periodKeys = new TreeSet<>();
        periodKeys.addAll(entryCountByPeriod.keySet());
        periodKeys.addAll(exitAggregateByPeriod.keySet());

        List<RevenueDetailItemResponse> items = new ArrayList<>();
        for (String key : periodKeys) {
            ExitAggregate exitAggregate = exitAggregateByPeriod.getOrDefault(key, new ExitAggregate());
            items.add(RevenueDetailItemResponse.builder()
                    .periodKey(key)
                    .entryCount(entryCountByPeriod.getOrDefault(key, 0))
                    .exitCount(exitAggregate.exitCount)
                    .settlementCount(exitAggregate.settlementCount)
                    .baseFeeAmount(exitAggregate.baseFeeAmount)
                    .discountAmount(exitAggregate.discountAmount)
                    .cardAmount(exitAggregate.cardAmount)
                    .easyPayAmount(exitAggregate.easyPayAmount)
                    .build());
        }

        return RevenueDetailsResponse.builder().items(items).build();
    }

    @Override
    public RevenueDetailByPeriodResponse getDetailByPeriod(String periodKey) {
        String period = inferPeriodType(periodKey);
        LocalDateTime[] range = resolveRange(period, periodKey);

        Map<String, PaymentMethodAggregate> aggregateByKey = new LinkedHashMap<>();
        for (ParkingRecord record : parkingRecordRepository.findByExitAtBetween(range[0], range[1])) {
            if (record.getPaymentMethod() == null) {
                continue;
            }
            String key = record.getPaymentMethod() + "|" + (record.getEasyPayProvider() == null ? "" : record.getEasyPayProvider());
            aggregateByKey.computeIfAbsent(key, k -> new PaymentMethodAggregate(record.getPaymentMethod(), record.getEasyPayProvider()))
                    .accumulate(record);
        }

        List<RevenuePaymentMethodItemResponse> byPaymentMethod = new ArrayList<>();
        for (PaymentMethodAggregate aggregate : aggregateByKey.values()) {
            byPaymentMethod.add(RevenuePaymentMethodItemResponse.builder()
                    .paymentMethod(aggregate.paymentMethod)
                    .easyPayProvider(aggregate.easyPayProvider)
                    .settlementCount(aggregate.count)
                    .amount(aggregate.amount)
                    .build());
        }

        return RevenueDetailByPeriodResponse.builder()
                .periodKey(periodKey)
                .byPaymentMethod(byPaymentMethod)
                .build();
    }

    /** periodType별 GROUP BY 키 산출 — 이 서비스에만 존재(design.md [Screen 3] 모듈경계 제약) */
    private String resolvePeriodKey(String periodType, LocalDateTime at) {
        switch (periodType) {
            case PERIOD_YEARLY:
                return String.format("%04d", at.getYear());
            case PERIOD_MONTHLY:
                return String.format("%04d-%02d", at.getYear(), at.getMonthValue());
            case PERIOD_DAILY:
            default:
                return at.toLocalDate().toString();
        }
    }

    /** periodKey 형식(yyyy / yyyy-MM / yyyy-MM-dd)으로 periodType을 역산한다(가정 — Issues 참고: 계약에 별도 periodType 파라미터 없음) */
    private String inferPeriodType(String periodKey) {
        if (periodKey == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "periodKey는 필수입니다.");
        }
        if (periodKey.length() == 4) {
            return PERIOD_YEARLY;
        }
        if (periodKey.length() == 7) {
            return PERIOD_MONTHLY;
        }
        return PERIOD_DAILY;
    }

    private LocalDateTime[] resolveRange(String periodType, String periodKey) {
        try {
            switch (periodType) {
                case PERIOD_YEARLY: {
                    LocalDate start = LocalDate.of(Integer.parseInt(periodKey), 1, 1);
                    return new LocalDateTime[] {start.atStartOfDay(), start.plusYears(1).atStartOfDay()};
                }
                case PERIOD_MONTHLY: {
                    YearMonth yearMonth = YearMonth.parse(periodKey);
                    LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
                    return new LocalDateTime[] {start, start.plusMonths(1)};
                }
                case PERIOD_DAILY:
                default: {
                    LocalDate date = LocalDate.parse(periodKey);
                    LocalDateTime start = date.atStartOfDay();
                    return new LocalDateTime[] {start, start.plusDays(1)};
                }
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "periodKey 형식이 올바르지 않습니다: " + periodKey);
        }
    }

    private String requirePeriodType(String periodType) {
        if (!PERIOD_DAILY.equals(periodType) && !PERIOD_MONTHLY.equals(periodType) && !PERIOD_YEARLY.equals(periodType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "periodType은 DAILY/MONTHLY/YEARLY 중 하나여야 합니다.");
        }
        return periodType;
    }

    private LocalDateTime requireStart(LocalDate fromDate) {
        if (fromDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate는 필수입니다.");
        }
        return fromDate.atStartOfDay();
    }

    private LocalDateTime requireEnd(LocalDate toDate) {
        if (toDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toDate는 필수입니다.");
        }
        return toDate.plusDays(1).atStartOfDay();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /** 기간 버킷 1개의 출차(exitAt) 기준 집계 누적기 */
    private static final class ExitAggregate {
        private int exitCount;
        private int settlementCount;
        private BigDecimal baseFeeAmount = BigDecimal.ZERO;
        private BigDecimal discountAmount = BigDecimal.ZERO;
        private BigDecimal cardAmount = BigDecimal.ZERO;
        private BigDecimal easyPayAmount = BigDecimal.ZERO;

        void accumulate(ParkingRecord record) {
            exitCount++;
            BigDecimal fee = record.getFeeAmount() != null ? record.getFeeAmount() : BigDecimal.ZERO;
            BigDecimal discount = record.getDiscountAmount() != null ? record.getDiscountAmount() : BigDecimal.ZERO;
            baseFeeAmount = baseFeeAmount.add(fee);
            discountAmount = discountAmount.add(discount);

            if (!SETTLEMENT_UNSETTLED.equals(record.getSettlementStatus())) {
                settlementCount++;
                BigDecimal net = fee.subtract(discount);
                if (PAYMENT_CARD.equals(record.getPaymentMethod())) {
                    cardAmount = cardAmount.add(net);
                } else if (PAYMENT_EASY_PAY.equals(record.getPaymentMethod())) {
                    easyPayAmount = easyPayAmount.add(net);
                }
            }
        }
    }

    /** periodKey 1건의 (paymentMethod, easyPayProvider) 단위 집계 누적기 */
    private static final class PaymentMethodAggregate {
        private final String paymentMethod;
        private final String easyPayProvider;
        private int count;
        private BigDecimal amount = BigDecimal.ZERO;

        PaymentMethodAggregate(String paymentMethod, String easyPayProvider) {
            this.paymentMethod = paymentMethod;
            this.easyPayProvider = easyPayProvider;
        }

        void accumulate(ParkingRecord record) {
            if (SETTLEMENT_UNSETTLED.equals(record.getSettlementStatus())) {
                return;
            }
            count++;
            BigDecimal fee = record.getFeeAmount() != null ? record.getFeeAmount() : BigDecimal.ZERO;
            BigDecimal discount = record.getDiscountAmount() != null ? record.getDiscountAmount() : BigDecimal.ZERO;
            amount = amount.add(fee.subtract(discount));
        }
    }
}
