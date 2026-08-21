package com.parking.admin.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 이름/연락처 마스킹 공용 컴포넌트. 반드시 DTO 직렬화 단계(응답 조립 시점)에서만 사용한다 — 원본을 내려보내고
 * 화면에서 가리는 방식 금지(design.md 비기능요구사항 §2).
 * 근거: design.md [Screen 4] 모듈경계 MaskingSupport, 비기능요구사항 §2("김*수", "010-****-1234" 형식)
 */
@Component
public class MaskingSupport {

    private static final int PHONE_MASK_LENGTH = 4;

    /** "김철수" -> "김*수", "김수" -> "김*", 1글자는 그대로 반환 */
    public String maskName(String name) {
        if (!StringUtils.hasText(name)) {
            return name;
        }
        int length = name.length();
        if (length == 1) {
            return name;
        }
        if (length == 2) {
            return name.charAt(0) + "*";
        }
        StringBuilder masked = new StringBuilder();
        masked.append(name.charAt(0));
        for (int i = 1; i < length - 1; i++) {
            masked.append('*');
        }
        masked.append(name.charAt(length - 1));
        return masked.toString();
    }

    /**
     * "010-1234-5678" -> "010-****-1234" (design.md 예시 형식 그대로 — 중간 블록을 가리고 마지막 블록을 노출).
     * 하이픈이 없는 11자리 연속 숫자도 동일 규칙으로 처리. 그 외 형식은 인식 불가 시 원본 그대로 반환(가정 — Issues 참고).
     */
    public String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return phone;
        }
        String[] parts = phone.split("-");
        if (parts.length == 3) {
            return parts[0] + "-****-" + parts[2];
        }
        if (phone.length() >= 11) {
            String prefix = phone.substring(0, 3);
            String suffix = phone.substring(phone.length() - PHONE_MASK_LENGTH);
            return prefix + "****" + suffix;
        }
        return phone;
    }
}
