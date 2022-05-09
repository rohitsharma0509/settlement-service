package com.scb.settlement.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class ReturnPaymentResult {
    private String recordType;
    private String paymentCurrency;
    private String paymentAmount;
    private String beneficiaryAccount;
    private String beneficiaryName;
    private String beneficiaryTaxId;
    private String beneficiaryBankName;
    private String beneficiaryBankCode;
    private String beneficiaryBranchName;
    private String beneficiaryBranchCode;
    private String bankReference;
    private String processingStatus;
    private String processingRemarks;
    private String netPaymentAmount;
    private String chequeNumber;
    private String customerReferenceNumber;
    private String whtSerialNumber;
    private String personalId;
    private String chequeNumber1;
}