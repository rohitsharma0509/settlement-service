package com.scb.settlement.model.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scb.settlement.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document
@CompoundIndexes({
        @CompoundIndex(name = "customerRef_transferDate", def = "{ 'customerReferenceNumber': 1, 'transferDate': 1 }")
})
public class RiderSettlementBatchDetails extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -7148502726642617998L;
    @Id
    private String id;
    @Indexed
    private String batchRef;
    private String recordType;
    private String paymentCurrency;
    private String paymentAmount;
    private String beneficiaryAccount;
    @Indexed
    private String beneficiaryName;
    private String beneficiaryTaxId;
    private String beneficiaryBankName;
    private String beneficiaryBankCode;
    private String beneficiaryBranchName;
    private String beneficiaryBranchCode;
    private String bankReference;
    @Indexed
    private String processingStatus;
    private String processingRemarks;
    private String netPaymentAmount;
    @JsonIgnore
    private double paymentAmountSort;
    private String chequeNumber;
    private String customerReferenceNumber;
    private String whtSerialNumber;
    private String personalId;
    private String chequeNumber1;
    private LocalDateTime transferDate;
    private LocalDateTime postDate;
    @JsonIgnore
    private String dateSearch;
    private String updatedBy;
}