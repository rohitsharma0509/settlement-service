package com.scb.settlement.controller;

import com.scb.settlement.constants.Constants;
import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.dto.CreateSettlementRequest;
import com.scb.settlement.model.dto.ReturnFileResult;
import com.scb.settlement.model.dto.RiderPocketSettlementDetails;
import com.scb.settlement.model.dto.RiderSettlementBatchResponse;
import com.scb.settlement.model.enumeration.SettlementBatchStatus;
import com.scb.settlement.service.document.RiderSettlementService;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderSettlementControllerTest {

	private static final String BATCH_ID = "S1000000001";
	private static final String RECON_BATCH_ID = "RECON000000001";

	@InjectMocks
	private RiderSettlementController riderSettlementController;

	@Mock
	private RiderSettlementService riderSettlementService;

	@Test
	public void trigger_batch_thenReturns201() {
		when(riderSettlementService.triggerSettlementBatch(BATCH_ID, Constants.SYSTEM)).thenReturn(new RiderSettlementBatchInfo());
		ResponseEntity<RiderSettlementBatchInfo> fetchedDto = riderSettlementController.triggerSettlementBatch(Constants.SYSTEM, BATCH_ID);
		assertEquals(HttpStatus.CREATED, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());

	}

	@Test
	public void get_batch_info_thenReturns200() {
		when(riderSettlementService.getSettlementStatus(BATCH_ID)).thenReturn(new RiderSettlementBatchInfo());
		ResponseEntity<RiderSettlementBatchInfo> fetchedDto = riderSettlementController.getS1FileBatchStatus(BATCH_ID);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());
	}

	@Test
	public void get_Batch_Details_thenReturns200() {
		Page<RiderSettlementBatchDetails> data=new PageImpl<>(new ArrayList<>());
		when(riderSettlementService.getSettlementBatchDetails(0, 5, BATCH_ID)).thenReturn(data);
		ResponseEntity<Page<RiderSettlementBatchResponse>> fetchedDto = riderSettlementController.getSettlementBatchDetails(BATCH_ID,0, 5);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());

	}

	@Test
	public void get_Batch_Details_By_Status_thenReturns200() {

		Page<RiderSettlementBatchInfo> data=new PageImpl<>(new ArrayList<>());
		when(riderSettlementService.getAllSettlementByStatus(0, 5, SettlementBatchStatus.ALL)).thenReturn(data);
		ResponseEntity<Page<RiderSettlementBatchInfo>> fetchedDto = riderSettlementController.getAllSettlement(SettlementBatchStatus.ALL,0, 5);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());

	}

	@Test
	public void push_reconcile_data_thenReturns200() {
		String pic = "abc.jpg";
		when(riderSettlementService.pushReconcileDetails(RECON_BATCH_ID, pic)).thenReturn(true);
		ResponseEntity<Boolean> fetchedDto = riderSettlementController.pushReconcileDetails(RECON_BATCH_ID, pic);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());

	}

	@Test
	public void testAddSettlementBatch() {
		RiderSettlementBatchInfo info = new RiderSettlementBatchInfo();
		info.setReconcileBatchId(RECON_BATCH_ID);
		when(riderSettlementService.addSettlementBatch(any(CreateSettlementRequest.class))).thenReturn(info);
		CreateSettlementRequest request = CreateSettlementRequest.builder().build();
		ResponseEntity<RiderSettlementBatchInfo> result = riderSettlementController.addSettlementBatch(request);
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(RECON_BATCH_ID, result.getBody().getReconcileBatchId());
	}

	@Test
	public void save_return_file_result_thenReturns200() {
		ReturnFileResult result = ReturnFileResult.builder().build();
		when(riderSettlementService.saveReturnFileResult(result)).thenReturn(true);
		ResponseEntity<Boolean> fetchedDto = riderSettlementController.saveReturnFileResult(result);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());
		assertNotNull(result.toString());

	}

	@Test
	public void fetch_Batch_Details_By_Rider_thenReturns200() {
		String riderId = "RR27411";
		List<RiderSettlementBatchDetails> riderPocketSettlementDetails = new ArrayList<>();
		when(riderSettlementService.findRiderSettlementDetails(riderId)).thenReturn(riderPocketSettlementDetails);
		ResponseEntity<List<RiderPocketSettlementDetails>> fetchedDto = riderSettlementController.getPaymentReconciliation(riderId);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());

	}

	@Test
	public void fetch_Batch_Details_By_Rider_For_Month_thenReturns200() {
		String riderId = "RR27411";
		List<RiderSettlementBatchDetails> riderPocketSettlementDetails = new ArrayList<>();
		when(riderSettlementService.findRiderSettlementDetailsWithinMonths(riderId, 1)).thenReturn(riderPocketSettlementDetails);
		ResponseEntity<List<RiderPocketSettlementDetails>> fetchedDto = riderSettlementController.getPaymentReconciliationWithinMonths(riderId, 1);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());

	}

	@Test
	public void fetch_Batch_Info_By_ReconBatch() {
		String reconBatchId = "RECON0000000001";
		List<RiderSettlementBatchDetails> riderPocketSettlementDetails = new ArrayList<>();
		when(riderSettlementService.getReconcileDetails(reconBatchId)).thenReturn(new RiderSettlementBatchInfo());
		ResponseEntity<RiderSettlementBatchInfo> fetchedDto = riderSettlementController.getReconcileDetails(reconBatchId);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
	}

	@Test
	public void fetch_security_balances_thenReturns200() {
		String riderId = "RR27411";
		List<RiderSettlementBatchDetails> riderPocketSettlementDetails = new ArrayList<>();
		when(riderSettlementService.getRiderPaymentDetails(BATCH_ID)).thenReturn(new ArrayList<>());
		ResponseEntity<List<RiderPaymentDetails>> fetchedDto = riderSettlementController.getRiderPaymentDetails(BATCH_ID);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());

	}

	@Test
	public void fetch_Batch_Details_By_Rider_With_In_Months_thenReturns200() {
		String riderId = "RR27411";
		LocalDateTime startDate = LocalDateTime.now();
		LocalDateTime endDate = LocalDateTime.now();
		List<RiderSettlementBatchDetails> riderPocketSettlementDetails = new ArrayList<>();
		RiderSettlementBatchDetails details = new RiderSettlementBatchDetails();
		details.setBatchRef("S100000000001");
		riderPocketSettlementDetails.add(details);
		when(riderSettlementService.findRiderSettlementDetailsWithinDates(riderId, startDate, endDate)).thenReturn(riderPocketSettlementDetails);
		ResponseEntity<List<RiderPocketSettlementDetails>> fetchedDto = riderSettlementController.getPaymentReconciliationWithinDates(riderId, startDate, endDate);
		assertEquals(HttpStatus.OK, fetchedDto.getStatusCode());
		assertNotNull(fetchedDto.toString());
		assertNotNull(details.toString());

	}

	@Test
	public void shouldGeneratePocketReport() {
		when(riderSettlementService.generatePocketReport("S100000000001")).thenReturn(true);
		ResponseEntity<Boolean> result = riderSettlementController.generatePocketReport("S100000000001");
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody());
	}


	@Test
	public void getSettlementBatchStatusTest_thenReturns200(){
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now().plusDays(1);
		List<RiderSettlementBatchInfo> settlementBatchStatusList = new ArrayList<>();
		when(riderSettlementService.getSettlementStatusByDateIntervals(any(),any())).thenReturn(settlementBatchStatusList);
		ResponseEntity<List<RiderSettlementBatchInfo>> response= riderSettlementController.getSettlementBatchInfo(startDate,endDate);
		assertEquals(HttpStatus.OK,response.getStatusCode());
	}

	@Test
	public void getSettlementBatchStatusTest_thenReturns400(){
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now().plusDays(1);
		List<RiderSettlementBatchInfo> settlementBatchStatusList = new ArrayList<>();
		when(riderSettlementService.getSettlementStatusByDateIntervals(any(),any())).thenReturn(settlementBatchStatusList);
		ResponseEntity<List<RiderSettlementBatchInfo>> response= riderSettlementController.getSettlementBatchInfo(endDate,startDate);
		assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
	}
}