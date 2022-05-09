package com.scb.settlement.client.impl;

import com.scb.settlement.client.ReconciliationDataFetchFeignClient;
import com.scb.settlement.exception.ExternalServiceInvocationException;
import com.scb.settlement.model.CustomPageImpl;
import com.scb.settlement.model.dto.FinalPaymentReconciliationDetails;
import com.scb.settlement.model.dto.RiderPaymentDto;
import com.scb.settlement.model.dto.RiderSettlementDetails;
import com.scb.settlement.model.enumeration.FinalReconciledStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceClientTest {

    private static final String RECON_BATCH_ID = "RECON00000001";
    private static final String RECON_BATCH_ID2 = "RECON00000002";
    private static final String RIDER_ID = "RR00001";
    private static final String RIDER_ID2 = "RR00002";
    private static final int PAGE_NO = 0;
    private static final int PAGE_SIZE = 10000;
    private static final String POCKET_BALANCE = "100.0";

    @InjectMocks
    private ReconciliationServiceClient reconciliationServiceClient;

    @Mock
    private ReconciliationDataFetchFeignClient reconciliationFeignClient;

    @Test
    void throwExceptionGetFinalReconciliationDetailsByReconBatchId() {
        when(reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID),
                ArgumentMatchers.eq(FinalReconciledStatus.MATCHED.name()), eq(PAGE_NO), eq(PAGE_SIZE))).thenThrow(new NullPointerException());
        assertThrows(ExternalServiceInvocationException.class, () -> reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(RECON_BATCH_ID));
    }

    @Test
    void shouldGetFinalReconciliationDetailsByReconBatchId() {
        CustomPageImpl<FinalPaymentReconciliationDetails> page = getPage(1, 2l, getFinalPaymentList());
        when(reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID),
                ArgumentMatchers.eq(FinalReconciledStatus.MATCHED.name()), eq(PAGE_NO), eq(PAGE_SIZE))).thenReturn(page);
        List<FinalPaymentReconciliationDetails> result = reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(RECON_BATCH_ID);
        assertEquals(2, result.size());
    }

    @Test
    void throwExceptionGetRiderEwtPaymentDetails() {
        when(reconciliationFeignClient.getRiderEwtPaymentDetails(eq(RECON_BATCH_ID))).thenThrow(new NullPointerException());
        assertThrows(ExternalServiceInvocationException.class, () -> reconciliationServiceClient.getRiderEwtPaymentDetails(RECON_BATCH_ID));
    }

    @Test
    void getRidersMatchedJobsTest() {
        CustomPageImpl<FinalPaymentReconciliationDetails> page = getPage(1, 2l, getFinalPaymentList());
        when(reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(eq(RECON_BATCH_ID),
                ArgumentMatchers.eq(FinalReconciledStatus.MATCHED.name()), eq(PAGE_NO), eq(PAGE_SIZE))).thenReturn(page);
        Map<String, RiderSettlementDetails> result = reconciliationServiceClient.getRiderConsolidatedPaymentDetails(Arrays.asList(RECON_BATCH_ID));
        assertEquals(200.0, result.get(RIDER_ID).getTotalCreditAmount());
    }

    @Test
    void getRidersMatchedJobsTestForMultipleBatches() {
        RiderPaymentDto paymentDto = RiderPaymentDto.builder().riderId(RIDER_ID).totalEwtAmount(10.0).build();
        when(reconciliationFeignClient.getRiderEwtPaymentDetails(anyString())).thenReturn(Arrays.asList(paymentDto));
        CustomPageImpl<FinalPaymentReconciliationDetails> page = getPage(1, 2l, getFinalPaymentList());
        when(reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(anyString(),
                ArgumentMatchers.eq(FinalReconciledStatus.MATCHED.name()), eq(PAGE_NO), eq(PAGE_SIZE))).thenReturn(page);
        Map<String, RiderSettlementDetails> result = reconciliationServiceClient.getRiderConsolidatedPaymentDetails(Arrays.asList(RECON_BATCH_ID, RECON_BATCH_ID2));
        assertEquals(400.0, result.get(RIDER_ID).getTotalCreditAmount());
        assertEquals(20.0, result.get(RIDER_ID).getTotalEwtAmount());
    }

    @Test
    void getRidersMatchedJobsTestWhenRiderIsEligibleForEwtButDoNotHaveMatchedJob() {
        RiderPaymentDto paymentDto = RiderPaymentDto.builder().riderId(RIDER_ID2).totalEwtAmount(10.0).build();
        when(reconciliationFeignClient.getRiderEwtPaymentDetails(anyString())).thenReturn(Arrays.asList(paymentDto, paymentDto));
        CustomPageImpl<FinalPaymentReconciliationDetails> page = getPage(1, 2l, getFinalPaymentList());
        when(reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(anyString(),
                ArgumentMatchers.eq(FinalReconciledStatus.MATCHED.name()), eq(PAGE_NO), eq(PAGE_SIZE))).thenReturn(page);
        Map<String, RiderSettlementDetails> result = reconciliationServiceClient.getRiderConsolidatedPaymentDetails(Arrays.asList(RECON_BATCH_ID));
        assertEquals(2, result.size());
    }

    private List<FinalPaymentReconciliationDetails> getFinalPaymentList() {
        List<FinalPaymentReconciliationDetails> finalPayments = new ArrayList<>();
        finalPayments.add(getRiderPaymentDetails());
        finalPayments.add(getRiderPaymentDetails());
        return finalPayments;
    }

    private CustomPageImpl<FinalPaymentReconciliationDetails> getPage(int totalPages, long totalElements, List<FinalPaymentReconciliationDetails> finalPayments) {
        CustomPageImpl<FinalPaymentReconciliationDetails> page = new CustomPageImpl<>();
        page.setTotalPages(totalPages);
        page.setTotalElements(totalElements);
        page.setContent(finalPayments);
        return page;
    }

    private FinalPaymentReconciliationDetails getRiderPaymentDetails() {
        FinalPaymentReconciliationDetails finalPaymentDetails = new FinalPaymentReconciliationDetails();
        finalPaymentDetails.setRaRiderId(RIDER_ID);
        finalPaymentDetails.setRaPaymentAmount(POCKET_BALANCE);
        return finalPaymentDetails;
    }
}
