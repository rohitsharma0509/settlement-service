package com.scb.settlement.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class ReturnTrailer {
    private String recordType;
    private String totalNumberOfTransactions;
    private String totalPaymentAmount;
}