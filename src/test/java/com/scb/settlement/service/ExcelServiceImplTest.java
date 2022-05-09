package com.scb.settlement.service;

import com.scb.settlement.client.impl.OperationServiceClient;
import com.scb.settlement.client.impl.PocketServiceClient;
import com.scb.settlement.constants.Constants;
import com.scb.settlement.exception.DataNotFoundException;
import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.model.dto.BatchConfigurationDto;
import com.scb.settlement.model.dto.FinalPaymentReconciliationDetails;
import com.scb.settlement.model.dto.RiderPocketBalanceResponse;
import com.scb.settlement.repository.RiderPaymentDetailsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelServiceImplTest {

    private static final String BATCH_ID = "S10000000001";
    private static final String RIDER_ID = "RR00001";

    @InjectMocks
    private ExcelServiceImpl excelServiceImpl;

    @Mock
    private OperationServiceClient operationServiceClient;

    @Mock
    private PocketServiceClient pocketServiceClient;

    @Mock
    private RiderPaymentDetailsRepository riderPaymentDetailsRepository;

    @Test
    void createSettlementReportTest() throws IOException {
        List<FinalPaymentReconciliationDetails> matchedJobs = getFinalPaymentList();
        BatchConfigurationDto config = getOpsConfig();
        when(operationServiceClient.getBatchConfiguration()).thenReturn(config);
        byte[] bytes = excelServiceImpl.createSettlementReport(matchedJobs);
        Assertions.assertNotNull(bytes);
    }

    @Test
    void createPocketBalanceReportTestForNoDataFound() throws IOException {
        when(operationServiceClient.getBatchConfiguration()).thenReturn(getOpsConfig());
        RiderPocketBalanceResponse pocket = RiderPocketBalanceResponse.builder().pocketBalance(10.0).build();
        when(pocketServiceClient.getPocketDetailByTimeInBatch(any(LocalDateTime.class), anyListOf(String.class))).thenReturn(Arrays.asList(pocket));
        when(riderPaymentDetailsRepository.findByBatchRef(eq(BATCH_ID))).thenReturn(Collections.emptyList());
        List<FinalPaymentReconciliationDetails> matchedJobs = getFinalPaymentList();
        Assertions.assertThrows(DataNotFoundException.class, () -> excelServiceImpl.createPocketBalanceReport(BATCH_ID, matchedJobs));
    }

    @Test
    void createPocketBalanceReportTest() throws IOException {
        when(operationServiceClient.getBatchConfiguration()).thenReturn(getOpsConfig());
        RiderPocketBalanceResponse pocket = RiderPocketBalanceResponse.builder().pocketBalance(10.0).build();
        when(pocketServiceClient.getPocketDetailByTimeInBatch(any(LocalDateTime.class), anyListOf(String.class))).thenReturn(Arrays.asList(pocket));
        when(riderPaymentDetailsRepository.findByBatchRef(eq(BATCH_ID))).thenReturn(getListOfRiderPaymentDetails());
        List<FinalPaymentReconciliationDetails> matchedJobs = getFinalPaymentList();
        byte[] bytes = excelServiceImpl.createPocketBalanceReport(BATCH_ID, matchedJobs);
        Assertions.assertNotNull(bytes);
    }

    private List<FinalPaymentReconciliationDetails> getFinalPaymentList() {
        FinalPaymentReconciliationDetails paymentDetails = new FinalPaymentReconciliationDetails();
        paymentDetails.setRhJobAmount("10.0");
        paymentDetails.setRhMdrValue("10.0");
        paymentDetails.setRhVatOnMdr("10.0");
        paymentDetails.setRhPaymentAmount("10.0");
        paymentDetails.setRaJobAmount("10.0");
        paymentDetails.setMdrValue("10.0");
        paymentDetails.setVatValue("10.0");
        paymentDetails.setRaPaymentAmount("10.0");
        paymentDetails.setRaRiderId(RIDER_ID);
        return Arrays.asList(paymentDetails);
    }

    private BatchConfigurationDto getOpsConfig() {
        return BatchConfigurationDto.builder()
                .mdrPercentage("10.0")
                .vatPercentage("10.0")
                .rhCutOffTime("06:00 PM")
                .build();
    }

    private List<RiderPaymentDetails> getListOfRiderPaymentDetails() {
        return Arrays.asList(RiderPaymentDetails.builder().batchRef(BATCH_ID).riderId(RIDER_ID)
                .processingStatus(Constants.SUCCESS).securityAmountDeducted(10.0).build());
    }
}
