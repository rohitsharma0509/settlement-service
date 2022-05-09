package com.scb.settlement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.settlement.constants.Constants;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.dto.FinalPaymentReconciliationDetails;
import com.scb.settlement.model.enumeration.FinalReconciledStatus;
import com.scb.settlement.model.enumeration.SettlementBatchStatus;
import com.scb.settlement.repository.RiderSettlementBatchDetailsRepository;
import com.scb.settlement.repository.RiderSettlementBatchInfoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataServiceTest {

  @InjectMocks
  private DataProcess process;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private RiderSettlementBatchDetailsRepository batchDetailsRepository;
  @Mock
  private RiderSettlementBatchInfoRepository batchInfoRepository;

  private static RiderSettlementBatchInfo batchInfo;
  private static FinalPaymentReconciliationDetails rDetails;

  @BeforeAll
  public static void setUp() {
    String batchRef = "S1000000001";
    String batchId = "RECON0000000001";
    batchInfo = new RiderSettlementBatchInfo();
    batchInfo.setReconcileBatchId(batchId);
    batchInfo.setBatchStatus(SettlementBatchStatus.READY_FOR_RUN);
    batchInfo.setBatchRef(batchRef);
    batchInfo.setTotalPaymentAmount("1000");
    batchInfo.setTotalNumberOfTransactions("1");

    rDetails = new FinalPaymentReconciliationDetails();
    rDetails.setId("1223");
    rDetails.setAccountNumber("12234656148");
    rDetails.setBatchId("RECON0000000001");
    rDetails.setRaJobNumber("Job123");
    rDetails.setRaOrderNumber("Order132");
    rDetails.setRaPaymentAmount("300.00");
    rDetails.setRaRiderId("RR27441");
    rDetails.setRaRiderName("Pankaj");
    rDetails.setRaOrderStatus("MATCHED");
    rDetails.setRhJobNumber("Job123");
    rDetails.setRhOrderNumber("Order132");
    rDetails.setRhPaymentAmount("300.00");
    rDetails.setRhRiderId("RR27441");
    rDetails.setRhRiderName("Pankaj");
    rDetails.setRhTransactionType("Delivered");
    rDetails.setStatus(FinalReconciledStatus.MATCHED);
  }
  @Test
  public void testConsume() throws Exception {
    String message = "{\"batchId\":\"RECON0000000001\",\"raRiderId\":\"RR27441\"}";
    RiderSettlementBatchDetails details = new RiderSettlementBatchDetails();
    details.setTransferDate(LocalDateTime.now());
    List<RiderSettlementBatchDetails> detailsList = new ArrayList<>();
    detailsList.add(details);
    when(batchInfoRepository.findByReconcileBatchId(rDetails.getBatchId())).thenReturn(Optional.of(batchInfo));
    when(batchDetailsRepository.findAllByCustomerReferenceNumberAndBatchRefAndProcessingStatus(rDetails.getRaRiderId(), batchInfo.getBatchRef(), Constants.SUCCESS)).thenReturn(detailsList);
    when(batchInfoRepository.save(batchInfo)).thenReturn(batchInfo);
    when(batchDetailsRepository.save(new RiderSettlementBatchDetails())).thenReturn(new RiderSettlementBatchDetails());
    when(objectMapper.readValue(message, FinalPaymentReconciliationDetails.class)).thenReturn(rDetails);
    process.processKafkaMessage(message);
    Assertions.assertNotNull(rDetails.toString());
  }

}
