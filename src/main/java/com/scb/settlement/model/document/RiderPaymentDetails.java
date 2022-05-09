package com.scb.settlement.model.document;

import com.scb.settlement.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@CompoundIndex(name = "batchRef_riderId", def = "{ 'batchRef': 1, 'riderId': 1 }", unique = true)
@Document("riderPaymentDetails")
@Getter
@Setter
@ToString
public class RiderPaymentDetails extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -7632092199998673581L;

    @Id
    private String id;

    @Indexed(name = "batchRef")
    private String batchRef;

    @Indexed(name = "riderId")
    private String riderId;

    private Double remainingSecurityBalance;

    private Double securityAmountDeducted;

    private String beneficiaryAccount;

    private Double netPaymentAmount;

    private String dateSearch;

    private LocalDateTime transferDate;

    private Double pocketBalance;

    private String processingStatus;

    private String processingRemarks;

    private Double otherPayments;

    private Double otherDeductions;

    private Double remainingDeductions;

    private Double securityRecovered;

    private String beneficiaryName;

    private String netPaymentAmountSearch;

    private String pocketBalanceSearch;

    private String securityAmountDeductedSearch;

    private String otherPaymentsSearch;

    private String otherDeductionsSearch;

    private Double netIncentiveAmount;

    private Double netExcessiveWaitTimeAmount;

    private String netExcessiveWaitTimeAmountSearch;
}
