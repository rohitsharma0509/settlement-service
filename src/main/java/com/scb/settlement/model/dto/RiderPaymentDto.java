package com.scb.settlement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
public class RiderPaymentDto {
    private String riderId;
    private String riderName;
    private String accountNumber;
    private Double totalEwtAmount;
}
