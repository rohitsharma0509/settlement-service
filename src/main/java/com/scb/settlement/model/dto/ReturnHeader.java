package com.scb.settlement.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class ReturnHeader {
    private String recordType;
    private String creationDate;
    private String creationTime;
    private String fileReference;
    private String companyId;
    private String paymentType;
    private String channelId;
    private String batchReferenceNumber;
    private String valueDate;
}