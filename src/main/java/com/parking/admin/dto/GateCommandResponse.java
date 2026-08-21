package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * POST .../barrier/open, POST .../lpr/reboot 공통 응답.
 * 근거: design.md [Screen 1] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateCommandResponse {

    private String commandId;

    /** ACCEPTED / FAILED */
    private String commandStatus;

    private LocalDateTime requestedAt;
}
