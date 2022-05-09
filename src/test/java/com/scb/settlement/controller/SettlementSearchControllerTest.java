package com.scb.settlement.controller;

import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.dto.BatchDetailsFilterDto;
import com.scb.settlement.model.dto.BatchInfoFilterDto;
import com.scb.settlement.model.dto.SearchResponseDto;
import com.scb.settlement.service.document.SettlementSearchService;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class SettlementSearchControllerTest {

	@InjectMocks
	private SettlementSearchController settlementSearchController;

	@Mock
	private SettlementSearchService settlementSearchService;

	
	@Test
	public void get_Batch_Info_thenReturns200() {
		
		Page<RiderSettlementBatchInfo> data=new PageImpl<>(new ArrayList<>());
		 Pageable pageable=PageRequest.of(1, 1);

		when(settlementSearchService.getSettlementBatchInfoBySearchTermWithFilterQuery(new ArrayList<>(),pageable)).thenReturn(BatchInfoFilterDto.of(new ArrayList<>(), 0, 0, 0));
		ResponseEntity<BatchInfoFilterDto> fetchedDto = settlementSearchController.getBatchInfoSearchTerm(new ArrayList<>(),pageable);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());
		
	}
	
	@Test
	public void get_Batch_Info_filter_thenReturns200() {
		
		Page<RiderSettlementBatchInfo> data=new PageImpl<>(new ArrayList<>());
		 Pageable pageable=PageRequest.of(1, 1);
		 List<String> filterquery=new ArrayList<>();
		 filterquery.add("batchRef:123");
		when(settlementSearchService.getSettlementBatchInfoBySearchTermWithFilterQuery(filterquery,pageable)).thenReturn(BatchInfoFilterDto.of(new ArrayList<>(), 0, 0, 0));
		ResponseEntity<BatchInfoFilterDto> fetchedDto = settlementSearchController.getBatchInfoSearchTerm(filterquery,pageable);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());
		
	}
	
	@Test
	public void get_Batch_Details_with_filter_thenReturns200() {
		
		 Pageable pageable=PageRequest.of(1, 1);
		 List<String> filterquery=new ArrayList<>();
		 filterquery.add("riderId:123");
		when(settlementSearchService.getSettlementBatchDetailsBySearchTermWithFilterQuery("123",filterquery,pageable)).thenReturn(BatchDetailsFilterDto.of(new ArrayList<>(), 0, 0, 0));
		ResponseEntity<BatchDetailsFilterDto> fetchedDto = settlementSearchController
				.getBatchDetailsSearchTerm("123",filterquery,pageable);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());
		
	}
	
	@Test
	public void get_Batch_Details_thenReturns200() {
		
		 Pageable pageable=PageRequest.of(1, 1);
		 
		when(settlementSearchService.getSettlementBatchDetailsBySearchTermWithFilterQuery("123",new ArrayList<>(),pageable)).thenReturn(BatchDetailsFilterDto.of(new ArrayList<>(), 0, 0, 0));
		ResponseEntity<BatchDetailsFilterDto> fetchedDto = settlementSearchController
				.getBatchDetailsSearchTerm("123",new ArrayList<>(),pageable);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());
		
	}
	
	@Test
	public void get_Rider_Details_thenReturns200() {
		
		 Pageable pageable=PageRequest.of(1, 1);

		when(settlementSearchService.findRiderSettlementDetailsByRiderId(pageable,"123",new ArrayList<>())).thenReturn(SearchResponseDto.of(new ArrayList<>(), 0, 0, 0));
		ResponseEntity<SearchResponseDto> fetchedDto = settlementSearchController.searchRiderPaymentDetails("123",new ArrayList<>(),pageable);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());
		
	}	
	
	
}