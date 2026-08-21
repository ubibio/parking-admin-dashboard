package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * GET /api/revenue/details/{periodKey} 응답의 byPaymentMethod 원소.
 * paymentMethod=CARD면 easyPayProvider=null, EASY_PAY면 KAKAO/NAVER/SAMSUNG로 세분화.
 * 근거: design.md [Screen 3] 입출력계약, [상태값] paymentMethod/easyPayProvider
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenuePaymentMethodItemResponse {

    private String paymentMethod;
    private String easyPayProvider;
    private int settlementCount;
    private BigDecimal amount;
}
