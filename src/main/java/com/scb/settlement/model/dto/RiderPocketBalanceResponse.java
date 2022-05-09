package com.scb.settlement.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RiderPocketBalanceResponse {
    private String riderId;
    private Double pocketBalance;
    private LocalDateTime currentTime;
}
