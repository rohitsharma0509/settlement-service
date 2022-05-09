package com.scb.settlement.repository.impl;

import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.model.dto.RiderPaymentDetailsSearchResponse;
import com.scb.settlement.model.dto.SearchResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderPaymentDetailsCustomRepositoryImplTest {

    private static final String RIDER_ID = "RR00001";
    private static final String TEST = "TEST";

    @InjectMocks
    private RiderPaymentDetailsCustomRepositoryImpl riderPaymentDetailsCustomRepositoryImpl;

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void searchRiderPaymentDetailsWhenZeroCountWithMatchingCriteria() {
        when(mongoTemplate.count(any(Query.class), eq(RiderPaymentDetails.class))).thenReturn(0l);
        SearchResponseDto result = riderPaymentDetailsCustomRepositoryImpl.searchRiderPaymentDetails(RIDER_ID, TEST, TEST
                , TEST, TEST, TEST, TEST, TEST, TEST, TEST, TEST, TEST, Pageable.unpaged());
        Assertions.assertTrue(CollectionUtils.isEmpty(result.getBatchInfo()));
    }

    @Test
    void searchRiderPaymentDetailsWhenNoRecordsForPage() {
        when(mongoTemplate.count(any(Query.class), eq(RiderPaymentDetails.class))).thenReturn(1l);
        when(mongoTemplate.find(any(Query.class), eq(RiderPaymentDetails.class))).thenReturn(Collections.emptyList());
        SearchResponseDto result = riderPaymentDetailsCustomRepositoryImpl.searchRiderPaymentDetails(RIDER_ID, TEST, TEST
                , TEST, TEST, TEST, TEST, TEST, TEST, TEST, TEST, TEST, Pageable.unpaged());
        Assertions.assertTrue(CollectionUtils.isEmpty(result.getBatchInfo()));
    }

    @Test
    void searchRiderPaymentDetailsWhenFoundRecordsWithMatchingCriteria() {
        RiderPaymentDetails riderPaymentDetails = RiderPaymentDetails.builder().riderId(RIDER_ID).build();
        when(mongoTemplate.count(any(Query.class), eq(RiderPaymentDetails.class))).thenReturn(1l);
        when(mongoTemplate.find(any(Query.class), eq(RiderPaymentDetails.class))).thenReturn(Arrays.asList(riderPaymentDetails));
        Pageable pageable = PageRequest.of(1, 50);
        SearchResponseDto result = riderPaymentDetailsCustomRepositoryImpl.searchRiderPaymentDetails(RIDER_ID, TEST, TEST
                , TEST, TEST, TEST, TEST, TEST, TEST, TEST, TEST, TEST, pageable);
        Assertions.assertEquals(RIDER_ID, ((RiderPaymentDetailsSearchResponse) result.getBatchInfo().get(0)).getRiderId());
    }
}
