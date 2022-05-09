package com.scb.settlement.client.impl;

import com.scb.settlement.client.PocketServiceFeignClient;
import com.scb.settlement.exception.ExternalServiceInvocationException;
import com.scb.settlement.model.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PocketServiceClientTest {

    private static final String RIDER_ID = "RR0001";
    private static final double BALANCE = 10.0;

    @InjectMocks
    private PocketServiceClient pocketServiceClient;

    @Mock
    private PocketServiceFeignClient pocketServiceFeignClient;

    @Test
    void throwExceptionGetPocketDetailsByTimeWhenSomethingWentWrong() {
        when(pocketServiceFeignClient.getPocketDetailsByTime(any(LocalDateTime.class), any(RiderPocketBalanceRequest.class))).thenThrow(new NullPointerException());
        RiderPocketBalanceRequest request = RiderPocketBalanceRequest.builder().build();
        assertThrows(ExternalServiceInvocationException.class, () -> pocketServiceClient.getPocketDetailsByTime(LocalDateTime.now(), request));
    }

    @Test
    void shouldGetPocketDetailsByTime() {
        RiderPocketBalanceResponse response = RiderPocketBalanceResponse.builder().pocketBalance(BALANCE).build();
        when(pocketServiceFeignClient.getPocketDetailsByTime(any(LocalDateTime.class), any(RiderPocketBalanceRequest.class))).thenReturn(Arrays.asList(response));
        List<RiderPocketBalanceResponse> result = pocketServiceClient.getPocketDetailByTimeInBatch(LocalDateTime.now(), Arrays.asList(RIDER_ID));
        assertEquals(BALANCE, result.get(0).getPocketBalance());
    }

    @Test
    void shouldUpdateRiderDetailsInBatch() {
        when(pocketServiceFeignClient.updateRiderPocketDetails(anyList())).thenReturn(Boolean.TRUE);
        RiderPocketModificationDto request = new RiderPocketModificationDto();
        List<Boolean> result = pocketServiceClient.updateRiderPocketDetailsInBatch(Arrays.asList(request));
        assertNotNull(result.get(0));
    }

    @Test
    void shouldNotUpdateRiderDetailsInBatchWhenExceptionOccurs() {
        when(pocketServiceFeignClient.updateRiderPocketDetails(anyList())).thenThrow(new NullPointerException());
        RiderPocketModificationDto  request = new RiderPocketModificationDto();
        List<Boolean> result = pocketServiceClient.updateRiderPocketDetailsInBatch(Arrays.asList(request));
        assertEquals(0, result.size());
    }

    @Test
    void shouldUpdatePocketBalanceInBatch() {
        when(pocketServiceFeignClient.updateRiderNetPocketBalance(anyList())).thenReturn(Boolean.TRUE);
        RiderCredit request = new RiderCredit(RIDER_ID, String.valueOf(BALANCE));
        List<Boolean> result = pocketServiceClient.updatePocketBalanceInBatch(Arrays.asList(request));
        assertTrue(result.get(0));
    }

    @Test
    void shouldNotUpdatePocketBalanceInBatchWhenExceptionOccurs() {
        when(pocketServiceFeignClient.updateRiderNetPocketBalance(anyList())).thenThrow(new NullPointerException());
        RiderCredit request = new RiderCredit(RIDER_ID, String.valueOf(BALANCE));
        List<Boolean> result = pocketServiceClient.updatePocketBalanceInBatch(Arrays.asList(request));
        assertEquals(0, result.size());
    }

    @Test
    void shouldUpdateIncentivesInBatch() {
        when(pocketServiceFeignClient.updateIncentiveAmount(anyList())).thenReturn(Boolean.TRUE);
        RiderIncentive request = RiderIncentive.builder().riderId(RIDER_ID).incentiveAmount(BALANCE).build();
        List<Boolean> result = pocketServiceClient.updateIncentivesInBatch(Arrays.asList(request));
        assertTrue(result.get(0));
    }

    @Test
    void shouldNotUpdateIncentivesInBatchWhenExceptionOccurs() {
        when(pocketServiceFeignClient.updateIncentiveAmount(anyList())).thenThrow(new NullPointerException());
        RiderIncentive request = RiderIncentive.builder().riderId(RIDER_ID).incentiveAmount(BALANCE).build();
        List<Boolean> result = pocketServiceClient.updateIncentivesInBatch(Arrays.asList(request));
        assertEquals(0, result.size());
    }

    @Test
    void shouldUpdateRiderPocketDetailsInBatch() {
        when(pocketServiceFeignClient.updateRiderPocketDetails(anyList())).thenReturn(Boolean.TRUE);
        RiderPocketModificationDto request = RiderPocketModificationDto.builder().riderId(RIDER_ID).pocketBalance(BALANCE).build();
        List<Boolean> result = pocketServiceClient.updateRiderPocketDetailsInBatch(Arrays.asList(request));
        assertTrue(result.get(0));
    }

    @Test
    void shouldNotUpdateRiderPocketDetailsInBatchWhenExceptionOccurs() {
        when(pocketServiceFeignClient.updateRiderPocketDetails(anyList())).thenThrow(new NullPointerException());
        RiderPocketModificationDto request = RiderPocketModificationDto.builder().riderId(RIDER_ID).pocketBalance(BALANCE).build();
        List<Boolean> result = pocketServiceClient.updateRiderPocketDetailsInBatch(Arrays.asList(request));
        assertEquals(0, result.size());
    }

    @Test
    void throwExceptionGetPocketDetailsByRiderIdsWhenSomethingWentWrong() {
        when(pocketServiceFeignClient.getRidersWithSecurityBalanceGreaterThanZero(anyList())).thenThrow(new NullPointerException());
        List<String> riderIds = Arrays.asList(RIDER_ID);
        assertThrows(ExternalServiceInvocationException.class, () -> pocketServiceClient.getPocketDetailsByRiderIdsInBatch(riderIds));
    }

    @Test
    void shouldGetPocketDetailsByRiderIds() {
        RiderPocketDetails response = RiderPocketDetails.builder().pocketBalance(BALANCE).build();
        when(pocketServiceFeignClient.getRidersWithSecurityBalanceGreaterThanZero(anyList())).thenReturn(Arrays.asList(response));
        List<RiderPocketDetails> result = pocketServiceClient.getPocketDetailsByRiderIdsInBatch(Arrays.asList(RIDER_ID));
        assertEquals(BALANCE, result.get(0).getPocketBalance());
    }

}
