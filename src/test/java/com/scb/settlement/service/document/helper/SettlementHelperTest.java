package com.scb.settlement.service.document.helper;

import com.scb.settlement.client.impl.PocketServiceClient;
import com.scb.settlement.constants.Constants;
import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
import com.scb.settlement.repository.RiderPaymentDetailsRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@ExtendWith(MockitoExtension.class)
class SettlementHelperTest {

    private static final String BATCH_ID = "S10000000001";
    private static final String RR0001 = "RR0001";
    private static final String RR0002 = "RR0002";
    private static final String RR0003 = "RR0003";
    private static final String RR0004 = "RR0004";
    private static final int ONE_TIME = 1;

    @InjectMocks
    private SettlementHelper settlementHelper;

    @Mock
    private PocketServiceClient pocketServiceClient;

    @Mock
    public RiderPaymentDetailsRepository riderPaymentDetailsRepository;

    @Test
    void saveRiderDetailsAndDoPocketProcessingTestForEmptySecurityDetails() {
        settlementHelper.saveRiderDetailsAndDoPocketProcessing(null, getBatchDetailsMap(), getRiderIdsForFailedTransaction(), LocalDateTime.now());
        verifyZeroInteractions(pocketServiceClient, riderPaymentDetailsRepository);
    }

    @Test
    void saveRiderDetailsAndDoPocketProcessingTestForNullSecurityDetails() {
        settlementHelper.saveRiderDetailsAndDoPocketProcessing(null, getBatchDetailsMap(), getRiderIdsForFailedTransaction(), LocalDateTime.now());
        verifyZeroInteractions(pocketServiceClient, riderPaymentDetailsRepository);
    }

    @Test
    void saveRiderDetailsAndDoPocketProcessingTestBeforeProcessingReturnFile() {
        List<RiderPaymentDetails> list = new ArrayList<>();
        list.add(getRiderPaymentDetails(RR0003, 20.0, 10.0, 30.0, 70.0, 0));
        settlementHelper.saveRiderDetailsAndDoPocketProcessing(list, null, null, LocalDateTime.now());
        verify(pocketServiceClient, times(ONE_TIME)).updateRiderPocketDetailsInBatch(anyList());
        verify(riderPaymentDetailsRepository, times(ONE_TIME)).saveAll(anyList());
    }

    @Test
    void saveRiderDetailsAndDoPocketProcessingTestWhenProcessingReturnFile() {
        settlementHelper.saveRiderDetailsAndDoPocketProcessing(getRiderPayDetailsList(), getBatchDetailsMap(), getRiderIdsForFailedTransaction(), LocalDateTime.now());
        verify(pocketServiceClient, times(ONE_TIME)).updateRiderPocketDetailsInBatch(anyList());
        verify(riderPaymentDetailsRepository, times(ONE_TIME)).saveAll(anyList());
    }

    private List<RiderPaymentDetails> getRiderPayDetailsList() {
        List<RiderPaymentDetails> list = new ArrayList<>();
        list.add(getRiderPaymentDetails(RR0001, 120.0, 200.0, 100.0, 0.0, 20.0));
        list.add(getRiderPaymentDetails(RR0002, 20.0, 200.0, 100.0, 0.0, 0));
        list.add(getRiderPaymentDetails(RR0003, 20.0, 0.0, 20.0, 80.0, 0));
        list.add(getRiderPaymentDetails(RR0004, 180.0, 100.0, 100.0, 0.0, 80));
        return list;
    }

    private RiderPaymentDetails getRiderPaymentDetails(String riderId, Double pocketBalance, Double incentive
            , Double securityDeducted, Double remainingSecurity, double paymentAmount) {
        return RiderPaymentDetails.builder().riderId(riderId).netPaymentAmount(paymentAmount).pocketBalance(pocketBalance)
                .securityAmountDeducted(securityDeducted).remainingSecurityBalance(remainingSecurity)
                .otherPayments(incentive).netIncentiveAmount(incentive).build();
    }

    private Map<String, RiderSettlementBatchDetails> getBatchDetailsMap() {
        Map<String, RiderSettlementBatchDetails> batchDetailsMap = new HashMap<>();
        batchDetailsMap.put(RR0001, getRiderSettlementBatchDetails(RR0001, Constants.SUCCESS, StringUtils.EMPTY, LocalDateTime.now()));
        batchDetailsMap.put(RR0002, getRiderSettlementBatchDetails(RR0002, Constants.SUCCESS, StringUtils.EMPTY, LocalDateTime.now()));
        batchDetailsMap.put(RR0003, getRiderSettlementBatchDetails(RR0003, Constants.SUCCESS, StringUtils.EMPTY, LocalDateTime.now()));
        batchDetailsMap.put(RR0004, getRiderSettlementBatchDetails(RR0004, Constants.FAILED, "ACCOUNT CLOSED", LocalDateTime.now()));
        return batchDetailsMap;
    }

    private RiderSettlementBatchDetails getRiderSettlementBatchDetails(String riderId, String status, String remarks, LocalDateTime transferDate) {
        RiderSettlementBatchDetails batchDetails = new RiderSettlementBatchDetails();
        batchDetails.setCustomerReferenceNumber(riderId);
        batchDetails.setProcessingStatus(status);
        batchDetails.setProcessingRemarks(remarks);
        batchDetails.setTransferDate(transferDate);
        return batchDetails;
    }

    private Set<String> getRiderIdsForFailedTransaction() {
        Set<String> set = new HashSet<>();
        set.add(RR0004);
        return set;
    }

}
