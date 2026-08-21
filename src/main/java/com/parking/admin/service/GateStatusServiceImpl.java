package com.parking.admin.service;

import com.parking.admin.dto.GateStatusResponse;
import com.parking.admin.entity.Gate;
import com.parking.admin.repository.GateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GateStatusServiceImpl implements GateStatusService {

    private final GateRepository gateRepository;

    @Override
    public List<GateStatusResponse> getGates() {
        return gateRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GateStatusResponse toResponse(Gate gate) {
        return GateStatusResponse.builder()
                .gateId(gate.getGateId())
                .gateName(gate.getGateName())
                .gateType(gate.getGateType())
                .barrierStatus(gate.getBarrierStatus())
                .lprStatus(gate.getLprStatus())
                .payStationType(gate.getPayStationType())
                .payStationStatus(gate.getPayStationStatus())
                .lastVehicleNo(gate.getLastVehicleNo())
                .lastEventAt(gate.getLastEventAt())
                .build();
    }
}
