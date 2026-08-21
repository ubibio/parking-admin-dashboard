package com.parking.admin.config;

import com.parking.admin.entity.Gate;
import com.parking.admin.entity.ParkingRecord;
import com.parking.admin.entity.SeasonPass;
import com.parking.admin.entity.SeasonPassAllowedGate;
import com.parking.admin.entity.SeasonPassAllowedGateId;
import com.parking.admin.entity.Store;
import com.parking.admin.entity.StoreDiscountCoupon;
import com.parking.admin.repository.GateRepository;
import com.parking.admin.repository.ParkingRecordRepository;
import com.parking.admin.repository.SeasonPassAllowedGateRepository;
import com.parking.admin.repository.SeasonPassRepository;
import com.parking.admin.repository.StoreDiscountCouponRepository;
import com.parking.admin.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 개발용(H2) 데모 데이터 시드 — Gate/Store/SeasonPass/ParkingRecord/StoreDiscountCoupon.
 * 각 리포지토리별 count()==0일 때만 시드하여 재기동에 안전하다(DevDataSeeder의 AdminUser 시드 패턴 참고).
 * 이름/전화번호는 데모용 가상값이며 로그에는 건수만 남기고 개인정보는 남기지 않는다(평문 개인정보 로깅 금지 관례 유지).
 * 근거: brief Objective "4개 화면이 빈 화면이 아니라 실제 데이터로 렌더링"
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    private final GateRepository gateRepository;
    private final StoreRepository storeRepository;
    private final SeasonPassRepository seasonPassRepository;
    private final SeasonPassAllowedGateRepository seasonPassAllowedGateRepository;
    private final ParkingRecordRepository parkingRecordRepository;
    private final StoreDiscountCouponRepository storeDiscountCouponRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<Gate> gates = seedGates();
        List<Store> stores = seedStores();
        List<SeasonPass> seasonPasses = seedSeasonPasses(gates);
        List<ParkingRecord> records = seedParkingRecords(gates, seasonPasses);
        seedStoreDiscountCoupons(stores, records);

        log.info("[DemoDataSeeder :: run] :: 데모 데이터 시드 확인 완료 => gate={}, store={}, seasonPass={}, parkingRecord={}, storeDiscountCoupon={}",
                gateRepository.count(), storeRepository.count(), seasonPassRepository.count(),
                parkingRecordRepository.count(), storeDiscountCouponRepository.count());
    }

    private List<Gate> seedGates() {
        if (gateRepository.count() > 0) {
            return gateRepository.findAll();
        }
        Gate entryFront = Gate.builder()
                .gateName("정문입구").gateType("ENTRY")
                .barrierStatus("CLOSED").lprStatus("NORMAL")
                .build();
        Gate exitFront = Gate.builder()
                .gateName("정문출구").gateType("EXIT")
                .barrierStatus("CLOSED").lprStatus("NORMAL")
                .payStationType("EXIT_PAY").payStationStatus("NORMAL")
                .build();
        Gate entryBack = Gate.builder()
                .gateName("후문입구").gateType("ENTRY")
                .barrierStatus("CLOSED").lprStatus("NORMAL")
                .build();
        Gate exitBack = Gate.builder()
                .gateName("후문출구").gateType("EXIT")
                .barrierStatus("CLOSED").lprStatus("NORMAL")
                .payStationType("EXIT_PAY").payStationStatus("PAPER_LOW")
                .build();
        return gateRepository.saveAll(List.of(entryFront, exitFront, entryBack, exitBack));
    }

    private List<Store> seedStores() {
        if (storeRepository.count() > 0) {
            return storeRepository.findAll();
        }
        Store cafe = Store.builder().storeName("1층 카페").build();
        Store mart = Store.builder().storeName("2층 편의점").build();
        return storeRepository.saveAll(List.of(cafe, mart));
    }

    private List<SeasonPass> seedSeasonPasses(List<Gate> gates) {
        if (seasonPassRepository.count() > 0) {
            return seasonPassRepository.findAll();
        }
        LocalDate today = LocalDate.now();
        SeasonPass active = SeasonPass.builder()
                .vehicleNo("12가3456").ownerName("김철수").phone("010-1234-5678")
                .passType("MONTHLY")
                .validFrom(today.minusDays(10)).validTo(today.plusDays(20))
                .paymentStatus("PAID").passStatus("ACTIVE")
                .build();
        SeasonPass expiringSoon = SeasonPass.builder()
                .vehicleNo("34나5678").ownerName("이영희").phone("010-2345-6789")
                .passType("SEMI_ANNUAL")
                .validFrom(today.minusDays(150)).validTo(today.plusDays(3))
                .paymentStatus("PAID").passStatus("EXPIRING_SOON")
                .build();
        SeasonPass resident = SeasonPass.builder()
                .vehicleNo("56다7890").ownerName("박민수").phone("010-3456-7890")
                .passType("RESIDENT_EMPLOYEE")
                .validFrom(today.minusDays(30)).validTo(today.plusDays(60))
                .paymentStatus("FREE_APPROVED").passStatus("ACTIVE")
                .build();
        List<SeasonPass> saved = seasonPassRepository.saveAll(List.of(active, expiringSoon, resident));

        if (seasonPassAllowedGateRepository.count() == 0 && gates.size() >= 2) {
            SeasonPass restricted = saved.get(1);
            SeasonPassAllowedGate allowFront1 = SeasonPassAllowedGate.builder()
                    .id(new SeasonPassAllowedGateId(restricted.getPassId(), gates.get(0).getGateId()))
                    .build();
            SeasonPassAllowedGate allowFront2 = SeasonPassAllowedGate.builder()
                    .id(new SeasonPassAllowedGateId(restricted.getPassId(), gates.get(1).getGateId()))
                    .build();
            seasonPassAllowedGateRepository.saveAll(List.of(allowFront1, allowFront2));
        }
        return saved;
    }

    private List<ParkingRecord> seedParkingRecords(List<Gate> gates, List<SeasonPass> seasonPasses) {
        if (parkingRecordRepository.count() > 0) {
            return parkingRecordRepository.findAll();
        }
        if (gates.size() < 4) {
            log.warn("[DemoDataSeeder :: seedParkingRecords] :: 게이트 수 부족으로 ParkingRecord 시드를 건너뜀 => gateCount={}", gates.size());
            return List.of();
        }
        Long entryFrontId = gates.get(0).getGateId();
        Long exitFrontId = gates.get(1).getGateId();
        Long entryBackId = gates.get(2).getGateId();
        Long exitBackId = gates.get(3).getGateId();
        Long activePassId = seasonPasses.isEmpty() ? null : seasonPasses.get(0).getPassId();

        LocalDateTime now = LocalDateTime.now();

        ParkingRecord r1 = ParkingRecord.builder()
                .entryGateId(entryFrontId).exitGateId(exitFrontId)
                .vehicleNo("11가1111").vehicleNoSuffix("1111").vehicleType("GENERAL")
                .entryAt(now.minusHours(3)).exitAt(now.minusHours(2))
                .recordStatus("NORMAL").settlementStatus("SETTLED")
                .feeAmount(new BigDecimal(3000)).discountAmount(BigDecimal.ZERO)
                .paymentMethod("CARD")
                .build();

        ParkingRecord r2 = ParkingRecord.builder()
                .entryGateId(entryBackId).exitGateId(exitBackId)
                .vehicleNo("22나2222").vehicleNoSuffix("2222").vehicleType("COMPACT")
                .entryAt(now.minusHours(4)).exitAt(now.minusHours(3).minusMinutes(10))
                .recordStatus("NORMAL").settlementStatus("SETTLED")
                .feeAmount(new BigDecimal(2000)).discountAmount(new BigDecimal(1000))
                .paymentMethod("EASY_PAY").easyPayProvider("KAKAO")
                .build();

        ParkingRecord r3 = ParkingRecord.builder()
                .entryGateId(entryFrontId).exitGateId(exitFrontId)
                .vehicleNo("33다3333").vehicleNoSuffix("3333").vehicleType("EV")
                .entryAt(now.minusHours(5)).exitAt(now.minusHours(4).minusMinutes(20))
                .recordStatus("NORMAL").settlementStatus("SETTLED")
                .feeAmount(new BigDecimal(5000)).discountAmount(BigDecimal.ZERO)
                .paymentMethod("EASY_PAY").easyPayProvider("NAVER")
                .build();

        ParkingRecord r4 = ParkingRecord.builder()
                .entryGateId(entryFrontId).exitGateId(exitFrontId)
                .vehicleNo("12가3456").vehicleNoSuffix("3456").vehicleType("GENERAL")
                .seasonPassId(activePassId)
                .entryAt(now.minusHours(6)).exitAt(now.minusHours(5).minusMinutes(40))
                .recordStatus("SEASON_PASS").settlementStatus("SETTLED")
                .feeAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .build();

        ParkingRecord r5 = ParkingRecord.builder()
                .entryGateId(entryBackId).exitGateId(exitBackId)
                .vehicleNo("44라4444").vehicleNoSuffix("4444").vehicleType("GENERAL")
                .entryAt(now.minusHours(7)).exitAt(now.minusHours(6).minusMinutes(30))
                .recordStatus("NORMAL").settlementStatus("SETTLED")
                .feeAmount(new BigDecimal(4000)).discountAmount(new BigDecimal(2000))
                .paymentMethod("CARD")
                .build();

        ParkingRecord r6 = ParkingRecord.builder()
                .entryGateId(entryFrontId).exitGateId(exitFrontId)
                .vehicleNo("55마5555").vehicleNoSuffix("5555").vehicleType("GENERAL")
                .entryAt(now.minusHours(8)).exitAt(now.minusHours(7).minusMinutes(15))
                .recordStatus("NORMAL").settlementStatus("SETTLED")
                .feeAmount(new BigDecimal(3000)).discountAmount(BigDecimal.ZERO)
                .paymentMethod("CARD")
                .build();

        ParkingRecord r7 = ParkingRecord.builder()
                .entryGateId(entryFrontId)
                .vehicleType("GENERAL")
                .entryAt(now.minusMinutes(5))
                .recordStatus("UNRECOGNIZED").settlementStatus("UNSETTLED")
                .build();

        ParkingRecord r8 = ParkingRecord.builder()
                .entryGateId(entryBackId)
                .vehicleNo("66바6666").vehicleNoSuffix("6666").vehicleType("COMPACT")
                .entryAt(now.minusMinutes(20))
                .recordStatus("NORMAL").settlementStatus("UNSETTLED")
                .build();

        return parkingRecordRepository.saveAll(List.of(r1, r2, r3, r4, r5, r6, r7, r8));
    }

    private void seedStoreDiscountCoupons(List<Store> stores, List<ParkingRecord> records) {
        if (storeDiscountCouponRepository.count() > 0) {
            return;
        }
        if (stores.size() < 2 || records.size() < 5) {
            log.warn("[DemoDataSeeder :: seedStoreDiscountCoupons] :: 선행 데이터 부족으로 StoreDiscountCoupon 시드를 건너뜀 => storeCount={}, recordCount={}",
                    stores.size(), records.size());
            return;
        }
        Long cafeId = stores.get(0).getStoreId();
        Long martId = stores.get(1).getStoreId();
        LocalDateTime now = LocalDateTime.now();
        ParkingRecord usedRecordCafe = records.get(1);
        ParkingRecord usedRecordMart = records.get(4);

        StoreDiscountCoupon issuedCafe = StoreDiscountCoupon.builder()
                .storeId(cafeId).couponStatus("ISSUED")
                .issuedAt(now.minusHours(1))
                .build();

        StoreDiscountCoupon usedCafe = StoreDiscountCoupon.builder()
                .storeId(cafeId).recordId(usedRecordCafe.getRecordId())
                .couponStatus("USED").discountAmount(usedRecordCafe.getDiscountAmount())
                .issuedAt(now.minusHours(4)).usedAt(usedRecordCafe.getExitAt())
                .build();

        StoreDiscountCoupon usedMart = StoreDiscountCoupon.builder()
                .storeId(martId).recordId(usedRecordMart.getRecordId())
                .couponStatus("USED").discountAmount(usedRecordMart.getDiscountAmount())
                .issuedAt(now.minusHours(7)).usedAt(usedRecordMart.getExitAt())
                .build();

        StoreDiscountCoupon issuedMart = StoreDiscountCoupon.builder()
                .storeId(martId).couponStatus("ISSUED")
                .issuedAt(now.minusMinutes(30))
                .build();

        storeDiscountCouponRepository.saveAll(List.of(issuedCafe, usedCafe, usedMart, issuedMart));
    }
}
