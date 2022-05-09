package com.scb.settlement.service;

import com.scb.settlement.constants.SearchConstants;
import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.dto.*;
import com.scb.settlement.model.enumeration.SettlementBatchStatus;
import com.scb.settlement.repository.RiderPaymentDetailsRepository;
import com.scb.settlement.repository.RiderSettlementBatchDetailsRepository;
import com.scb.settlement.repository.RiderSettlementBatchInfoRepository;
import com.scb.settlement.service.document.SettlementSearchService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class SettlementSearchServiceTest {

	@Mock
	private RiderSettlementBatchInfoRepository riderSettlementBatchInfoRepository;

	@Mock
	private RiderSettlementBatchDetailsRepository riderSettlementBatchDetailsRepository;

	@Mock
	private RiderPaymentDetailsRepository riderPaymentDetailsRepository;
	
	@InjectMocks
	private SettlementSearchService settlementSearchService;

	private static RiderSettlementBatchInfo batchInfo = new RiderSettlementBatchInfo();

	@BeforeAll
	void Setup() {
		batchInfo.setBatchStatus(SettlementBatchStatus.READY_FOR_RUN);
		batchInfo.setReconcileBatchId("RECON0000000001");
	}

	@Test
	public void getAllBatchInfo() {
		Pageable pageable = PageRequest.of(1, 1);
		Page<RiderSettlementBatchInfo> data = new PageImpl<>(new ArrayList<>());
		when(riderSettlementBatchInfoRepository.findAll(pageable)).thenReturn(data);
		BatchInfoFilterDto fetchedDto = settlementSearchService
				.getSettlementBatchInfoBySearchTermWithFilterQuery(new ArrayList<>(), pageable);
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}
	
	

	
	@Test
	public void getFilteredBatchInfoWithFilter() {
		List<Sort.Order> orders=new ArrayList<>();
		orders.add(new Order(Sort.Direction.ASC, "batchRef"));
		Pageable pageable = PageRequest.of(1, 1,Sort.by(orders));
		RiderSettlementBatchInfo riderSettlementBatchInfo=new RiderSettlementBatchInfo();
		riderSettlementBatchInfo.setId("601391073733e18378ad65bd");
		riderSettlementBatchInfo.setReconcileBatchId("RECON0000000001");
		List<RiderSettlementBatchInfo> mappedResults=new ArrayList<>();
		mappedResults.add(riderSettlementBatchInfo);
		Document doc=new Document();
		doc.put("result", mappedResults);
		doc.put("ok", 1.0);
		Page<RiderSettlementBatchInfo> data = new PageImpl<>(mappedResults);

		AggregationResults<RiderSettlementBatchInfo> res=new AggregationResults<>(mappedResults, doc);
		when(riderSettlementBatchInfoRepository.getBatchInfoAndQueryOnFields(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res);
		List<ObjectId> ids = new LinkedList<>();
		ids.add(new ObjectId(riderSettlementBatchInfo.getId()));
		
		when(riderSettlementBatchInfoRepository.findAllById(ids,pageable)).thenReturn(data);
		
		List<String> filterQuery=new ArrayList<>();
		filterQuery.add("batchRef:123");
		filterQuery.add("startTime:05:05");
		BatchInfoFilterDto fetchedDto = settlementSearchService
				.getSettlementBatchInfoBySearchTermWithFilterQuery(filterQuery, pageable);
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}
	
	@Test
	public void getFilteredBatchInfo() {
		
		Pageable pageable = PageRequest.of(1, 1);
		Page<RiderSettlementBatchInfo> data = new PageImpl<>(new ArrayList<>());
		RiderSettlementBatchInfo riderSettlementBatchInfo=new RiderSettlementBatchInfo();
		riderSettlementBatchInfo.setId("601391073733e18378ad65bd");
		List<RiderSettlementBatchInfo> mappedResults=new ArrayList<>();
		mappedResults.add(riderSettlementBatchInfo);
		Document doc=new Document();
		doc.put("result", mappedResults);
		doc.put("ok", 1.0);
		AggregationResults<RiderSettlementBatchInfo> res=new AggregationResults<>(mappedResults, doc);
		when(riderSettlementBatchInfoRepository.getBatchInfoAndQueryOnFields("", "", "", "", "", "", "")).thenReturn(res);
		List<ObjectId> ids = new LinkedList<>();
		ids.add(new ObjectId(riderSettlementBatchInfo.getId()));
		when(riderSettlementBatchInfoRepository.findAllById(ids,pageable)).thenReturn(data);
		
		List<String> filterQuery=new ArrayList<>();
		
		BatchInfoFilterDto fetchedDto = settlementSearchService
				.getSettlementBatchInfoBySearchTermWithFilterQuery(filterQuery, pageable);
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}
	
	
	
	
	@Test
	public void getFilteredBatchDetails() {
		List<Sort.Order> orders=new ArrayList<>();
		orders.add(new Order(Sort.Direction.ASC, "batchRef"));
		Pageable pageable = PageRequest.of(1, 1,Sort.by(orders));
		
		RiderSettlementBatchDetails riderSettlementBatchDetails=new RiderSettlementBatchDetails();
		riderSettlementBatchDetails.setId("601391073733e18378ad65bd");
		List<RiderSettlementBatchDetails> mappedResults=new ArrayList<>();
		mappedResults.add(riderSettlementBatchDetails);
		Document doc=new Document();
		doc.put("result", new ArrayList<>());
		doc.put("ok", 1.0);
		AggregationResults<RiderSettlementBatchDetails> res=new AggregationResults<>(mappedResults, doc);
		Page<RiderSettlementBatchDetails> data = new PageImpl<>(mappedResults);
		when(riderSettlementBatchDetailsRepository.getAllBatchInfoAndQueryOnFields("123", "", "", "", "", "", "","123")).thenReturn(res);
		List<ObjectId> ids = new LinkedList<>();
		ids.add(new ObjectId(riderSettlementBatchDetails.getId()));
		when(riderSettlementBatchDetailsRepository.findAllById(ids,pageable)).thenReturn(data);
		
		List<String> filterQuery=new ArrayList<>();
		filterQuery.add("riderId:123");
		filterQuery.add("batchRef:123");
		BatchDetailsFilterDto fetchedDto = settlementSearchService
				.getSettlementBatchDetailsBySearchTermWithFilterQuery("123",filterQuery, pageable);
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}
	
	@Test
	public void getBatchDetails() {
		List<Sort.Order> orders=new ArrayList<>();
		orders.add(new Order(Sort.Direction.ASC, "batchRef"));
		Pageable pageable = PageRequest.of(1, 1,Sort.by(orders));
		
		RiderSettlementBatchDetails riderSettlementBatchDetails=new RiderSettlementBatchDetails();
		riderSettlementBatchDetails.setId("601391073733e18378ad65bd");
		List<RiderSettlementBatchDetails> mappedResults=new ArrayList<>();
		mappedResults.add(riderSettlementBatchDetails);
		Document doc=new Document();
		doc.put("result", new ArrayList<>());
		doc.put("ok", 1.0);
		Page<RiderSettlementBatchDetails> data = new PageImpl<>(mappedResults);
		List<ObjectId> ids = new LinkedList<>();
		ids.add(new ObjectId(riderSettlementBatchDetails.getId()));
		when(riderSettlementBatchDetailsRepository.findByBatchRef("123", pageable)).thenReturn(data);
		
		List<String> filterQuery=new ArrayList<>();
		
		BatchDetailsFilterDto fetchedDto = settlementSearchService
				.getSettlementBatchDetailsBySearchTermWithFilterQuery("123",filterQuery, pageable);
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}
	
	@Test
	public void getBlankBatchDetails() {
		Pageable pageable = PageRequest.of(1, 1);
		
		RiderSettlementBatchDetails riderSettlementBatchDetails=new RiderSettlementBatchDetails();
		riderSettlementBatchDetails.setId("601391073733e18378ad65bd");
		List<RiderSettlementBatchDetails> mappedResults=new ArrayList<>();
		mappedResults.add(riderSettlementBatchDetails);
		Document doc=new Document();
		doc.put("result", new ArrayList<>());
		doc.put("ok", 1.0);
		AggregationResults<RiderSettlementBatchDetails> res=new AggregationResults<>(mappedResults, doc);
		Page<RiderSettlementBatchDetails> data = new PageImpl<>(new ArrayList<>());
		when(riderSettlementBatchDetailsRepository.getAllBatchInfoAndQueryOnFields("123", "", "", "", "", "", "","123")).thenReturn(res);
		List<ObjectId> ids = new LinkedList<>();
		ids.add(new ObjectId(riderSettlementBatchDetails.getId()));
		when(riderSettlementBatchDetailsRepository.findAllById(ids,pageable)).thenReturn(data);
		
		List<String> filterQuery=new ArrayList<>();
		filterQuery.add("riderId:123");
		filterQuery.add("batchRef:123");
		BatchDetailsFilterDto fetchedDto = settlementSearchService
				.getSettlementBatchDetailsBySearchTermWithFilterQuery("123",filterQuery, pageable);
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}
	

	@Test
	public void getRiderBatchDetails() {
		List<Sort.Order> orders=new ArrayList<>();
		orders.add(new Order(Sort.Direction.ASC, SearchConstants.DETAILS_DATE));
		Pageable pageable = PageRequest.of(1, 1,Sort.by(orders));

		RiderPaymentDetails riderPaymentDetails = RiderPaymentDetails.builder().beneficiaryAccount("123")
				.netPaymentAmount(600.0).pocketBalance(600.0).securityAmountDeducted(20.0).riderId("123").build();

		List<RiderPaymentDetails> mappedResults=new ArrayList<>();
		mappedResults.add(riderPaymentDetails);

		SearchResponseDto res = SearchResponseDto.of(mappedResults, 1, 1, 1);

		when(riderPaymentDetailsRepository.searchRiderPaymentDetails(anyString(), anyString(), anyString(), anyString(), anyString()
				, anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),anyString(), any(Pageable.class))).thenReturn(res);
		
		List<String> filterQuery=new ArrayList<>();
		SearchResponseDto fetchedDto = settlementSearchService
				.findRiderSettlementDetailsByRiderId(pageable,"123",filterQuery );
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}
	
	@Test
	public void getRiderBatchWithFilterDetails() {
		List<Sort.Order> orders=new ArrayList<>();
		orders.add(new Order(Sort.Direction.ASC, SearchConstants.DETAILS_DATE));
		Pageable pageable = PageRequest.of(1, 1,Sort.by(orders));
		
		RiderPaymentDetails riderPaymentDetails = RiderPaymentDetails.builder().beneficiaryAccount("123")
				.netPaymentAmount(600.0).pocketBalance(600.0).securityAmountDeducted(20.0).riderId("123").build();

		List<RiderPaymentDetails> mappedResults=new ArrayList<>();
		mappedResults.add(riderPaymentDetails);

		SearchResponseDto res = SearchResponseDto.of(mappedResults, 1, 1, 1);

		when(riderPaymentDetailsRepository.searchRiderPaymentDetails(anyString(), anyString(), anyString(), anyString(), anyString()
				, anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),anyString(), any(Pageable.class))).thenReturn(res);
		
		List<String> filterQuery=new ArrayList<>();
		filterQuery.add("accountNumber:123");
		SearchResponseDto fetchedDto = settlementSearchService
				.findRiderSettlementDetailsByRiderId(pageable,"123",filterQuery );
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}
	
	
	
}
