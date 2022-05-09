package com.scb.settlement.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.settlement.constants.Constants;
import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.utils.CommonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class RiderPaymentDetailsSearchResponse extends RiderSettlementBatchResponse {
    private double pocketBalance;
    private double securityDeducted;
    private double otherPayments;
    private double otherDeduction;
    private double remainingDeductions;
    private double netExcessiveWaitAmount;

    public static List<RiderPaymentDetailsSearchResponse> toSearchResponse(List<RiderPaymentDetails> listOfRiderPaymentDetails) {
        List<RiderPaymentDetailsSearchResponse> listOfPaymentSearchResponse = new ArrayList<>();
        for (RiderPaymentDetails riderPaymentDetails : listOfRiderPaymentDetails) {
            listOfPaymentSearchResponse.add(toSearchResponse(riderPaymentDetails));
        }
        return listOfPaymentSearchResponse;
    }

    public static RiderPaymentDetailsSearchResponse toSearchResponse(RiderPaymentDetails riderPaymentDetails) {
        RiderPaymentDetailsSearchResponse paymentSearchResponse = new RiderPaymentDetailsSearchResponse();
        paymentSearchResponse.setBatchRef(riderPaymentDetails.getBatchRef());
        paymentSearchResponse.setAccountNumber(riderPaymentDetails.getBeneficiaryAccount());
        paymentSearchResponse.setPaymentStatus(riderPaymentDetails.getProcessingStatus());
        paymentSearchResponse.setRemarks(riderPaymentDetails.getProcessingRemarks());
        paymentSearchResponse.setRiderId(riderPaymentDetails.getRiderId());
        paymentSearchResponse.setRiderName(riderPaymentDetails.getBeneficiaryName());
        paymentSearchResponse.setTransferAmount(CommonUtils.round(riderPaymentDetails.getNetPaymentAmount()) + "");
        if (!Constants.RECTIFIED.equalsIgnoreCase(paymentSearchResponse.getPaymentStatus())) {
            paymentSearchResponse.setOtherPayments(Objects.nonNull(riderPaymentDetails.getOtherPayments()) ? CommonUtils.round(riderPaymentDetails.getOtherPayments()) : 0);
            paymentSearchResponse.setOtherDeduction(Objects.nonNull(riderPaymentDetails.getOtherDeductions()) ? CommonUtils.round(riderPaymentDetails.getOtherDeductions()) : 0);
            paymentSearchResponse.setRemainingDeductions(Objects.nonNull(riderPaymentDetails.getRemainingDeductions()) ? CommonUtils.round(riderPaymentDetails.getRemainingDeductions()) : 0);
            paymentSearchResponse.setSecurityDeducted(Objects.nonNull(riderPaymentDetails.getSecurityAmountDeducted()) ? CommonUtils.round(riderPaymentDetails.getSecurityAmountDeducted()) : 0);
            paymentSearchResponse.setPocketBalance(Objects.nonNull(riderPaymentDetails.getPocketBalance()) ? CommonUtils.round(riderPaymentDetails.getPocketBalance()) : 0);
            paymentSearchResponse.setNetExcessiveWaitAmount(Objects.nonNull(riderPaymentDetails.getNetExcessiveWaitTimeAmount()) ? CommonUtils.round(riderPaymentDetails.getNetExcessiveWaitTimeAmount()) : 0);
        }

        if (Objects.nonNull(riderPaymentDetails.getTransferDate()) && !Constants.DEFAULT_DATE.isEqual(riderPaymentDetails.getTransferDate())) {
            paymentSearchResponse.setTransferDate(riderPaymentDetails.getTransferDate());
        }
        return paymentSearchResponse;
    }
}

