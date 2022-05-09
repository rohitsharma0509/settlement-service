package com.scb.settlement.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.scb.settlement.model.enumeration.FinalReconciledStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class FinalPaymentReconciliationDetails {
    private String id;
    private String batchId;
    private String accountNumber;
    private String rhRiderId;
    private String raRiderId;
    private String rhTransactionType;
    private String raOrderStatus;
    private FinalReconciledStatus status;
    private String rhRiderName;
    private String raRiderName;
    private String rhJobNumber;
    private String raJobNumber;
    private String rhOrderNumber;
    private String raOrderNumber;
    private String rhPaymentAmount;
    private String raPaymentAmount;
    private String rhJobAmount;
    private String raJobAmount;
    private String mdrValue;
    private String vatValue;
    private String rhChargeNo;
    private String rhOrderTime;
    private String rhOrderCompletedTime;
    private String rhPaymentMethod;
    private String rhMdrValue;
    private String rhVatOnMdr;
    private String rhSettlementTime;
    private String orderDateTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime jobStartDateTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime jobEndDateTime;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate createdDate;
    private Double otherDeductions;
    private Double ewtAmount;
    private String updatedBy;
}