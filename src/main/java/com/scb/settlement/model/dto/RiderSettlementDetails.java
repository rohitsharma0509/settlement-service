package com.scb.settlement.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class RiderSettlementDetails {
    private String riderId;
    private String riderAccountNumber;
    private double totalCreditAmount;
    private String riderName;
    private Double totalDeductions;
    private Double totalEwtAmount;
}
