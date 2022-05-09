package com.scb.settlement.model.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scb.settlement.model.BaseEntity;
import com.scb.settlement.model.enumeration.SettlementBatchStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Document
@ToString
public class RiderSettlementBatchInfo extends BaseEntity implements Serializable {
  private static final long serialVersionUID = -204417442725444689L;
  @Id
  private String id;
  @Indexed(unique = true)
  private String batchRef;
  @Indexed(unique = true)
  private String reconcileBatchId;
  private String fileName;
  private String pointxReconBatchId;
  private String pointxReconFileName;
  private LocalDate dateOfRun;
  private LocalTime startTime;
  private LocalTime endTime;
  private SettlementBatchStatus batchStatus;
  private String s1FilePathUrl;
  private String failureReason;
  private String totalNumberOfTransactions;
  private String totalPaymentAmount;
  private Double totalSecurityAmountDeducted;
  @JsonIgnore
  private String dateSearch; // Do not remove..For search filter
  @JsonIgnore
  private String startTimeSearch; // Do not remove..For search filter
  @JsonIgnore
  private String endTimeSearch; // Do not remove..For search filter
  private String updatedBy;
  }
