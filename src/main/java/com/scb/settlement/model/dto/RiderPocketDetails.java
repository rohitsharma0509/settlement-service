package com.scb.settlement.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class RiderPocketDetails {
    private String riderId;

    private Double securityBalance;

    private Double taxPercentage;

    private Double remainingSecurityBalance;

    private Double pocketBalance;

    private double netIncentiveAmount;
    
    private double netExcessiveWaitAmount;
}
