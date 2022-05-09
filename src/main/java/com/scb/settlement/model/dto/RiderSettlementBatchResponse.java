package com.scb.settlement.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.settlement.constants.Constants;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@JsonInclude(JsonInclude.Include. NON_NULL)
@ToString
public class RiderSettlementBatchResponse {
  private String batchRef;
  private String riderId;
  private String riderName;
  private String accountNumber;
  private String transferAmount;
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDateTime transferDate;
  private String paymentStatus;
  private String remarks;

  public static List<RiderSettlementBatchResponse> of(List<RiderSettlementBatchDetails> riderSettlementDetails) {
    ArrayList<RiderSettlementBatchResponse> riderSettlementBatchResponses = new ArrayList<>();
    for(RiderSettlementBatchDetails details : riderSettlementDetails){
      RiderSettlementBatchResponse response = new RiderSettlementBatchResponse();
      response.setAccountNumber(details.getBeneficiaryAccount());
      response.setBatchRef(details.getBatchRef());
      response.setPaymentStatus(details.getProcessingStatus());
      response.setRemarks(details.getProcessingRemarks());
      response.setRiderId(details.getCustomerReferenceNumber());
      response.setRiderName(details.getBeneficiaryName());
      response.setTransferAmount(details.getNetPaymentAmount());
      if(Objects.nonNull(details.getTransferDate()) && !Constants.DEFAULT_DATE.isEqual(details.getTransferDate())) {
        response.setTransferDate(details.getTransferDate());
      }
      riderSettlementBatchResponses.add(response);
    }
    return riderSettlementBatchResponses;
  }
}
