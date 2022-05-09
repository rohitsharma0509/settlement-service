package com.scb.settlement.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Getter
@Setter
@ToString
public class SettlementDetails {
    private double totalConsolidatedAmount;
    private int noOfCredits;
    private double totalDebitAmount;
    private String batchReferenceNumber;
    private List<RiderSettlementDetails> riderSettlementDetails;
}
