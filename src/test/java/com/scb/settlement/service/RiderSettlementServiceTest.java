package com.scb.settlement.service;

import com.scb.settlement.client.PocketServiceFeignClient;
import com.scb.settlement.client.impl.PocketServiceClient;
import com.scb.settlement.constants.Constants;
import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.dto.*;
import com.scb.settlement.model.enumeration.SettlementBatchStatus;
import com.scb.settlement.repository.RiderPaymentDetailsRepository;
import com.scb.settlement.repository.RiderSettlementBatchDetailsRepository;
import com.scb.settlement.repository.RiderSettlementBatchInfoRepository;
import com.scb.settlement.service.document.RiderSettlementService;
import com.scb.settlement.service.document.helper.RiderSettlementAsyncService;
import com.scb.settlement.service.document.helper.SettlementHelper;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderSettlementServiceTest {

	@Mock
	private RiderSettlementBatchInfoRepository batchInfoRepository;

	@Mock
	private RiderSettlementBatchDetailsRepository batchDetailsRepository;

	@Mock
	private PocketServiceClient pocketServiceClient;

	@Mock
	private PocketServiceFeignClient pocketServiceFeignClient;

	@Mock
	private RiderSettlementAsyncService riderSettlementAsyncService;

	@Mock
	private RiderPaymentDetailsRepository riderPaymentDetailsRepository;

	@Mock
	private SettlementHelper settlementHelper;

	@Mock
	private ReportGenerator reportGenerator;

	@InjectMocks
	private RiderSettlementService riderSettlementService;

	@Mock
	private RiderSettlementBatchInfoRepository riderSettlementBatchInfoRepository;

	private static final RiderSettlementBatchInfo batchInfo = new RiderSettlementBatchInfo();
	private static final String BATCH_REF = "S1000000001";
	private static final String RIDER_ID = "RR00001";
	private static final String RECON_BATCH_ID = "RECON0000000001";
	private static final String CREATION_DATE = "20210202";
	private static final String CREATION_TIME = "100302";

	@BeforeAll
	static void Setup(){
	}

	@Test
	public void trigger_batch_thenReturns201() {
		batchInfo.setBatchRef(BATCH_REF);
		batchInfo.setBatchStatus(SettlementBatchStatus.READY_FOR_RUN);
		batchInfo.setReconcileBatchId(RECON_BATCH_ID);
		when(batchInfoRepository.findByBatchRef(BATCH_REF)).thenReturn(Optional.of(batchInfo));
		when(batchInfoRepository.save(batchInfo)).thenReturn(batchInfo);
		doNothing().when(riderSettlementAsyncService).triggerSettlementBatch(batchInfo);
		RiderSettlementBatchInfo fetchedDto = riderSettlementService.triggerSettlementBatch(BATCH_REF, Constants.SYSTEM);
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}

	@Test
	public void getSettlementStatus() {
		when(batchInfoRepository.findByBatchRef(BATCH_REF)).thenReturn(Optional.of(batchInfo));
		riderSettlementService.getSettlementStatus(BATCH_REF);
	}

	@Test
	public void saveSettlementStatus() {
		when(batchInfoRepository.findByBatchRef(BATCH_REF)).thenReturn(Optional.of(batchInfo));
		when(batchInfoRepository.save(batchInfo)).thenReturn(batchInfo);
		riderSettlementService.saveSettlementStatus(BATCH_REF, SettlementBatchStatus.SUCCESS);
	}

	@Test
	public void getAllSettlementByStatus() {
		Pageable paging = PageRequest.of(0, 5);
		List<RiderSettlementBatchInfo> batchInfos = new ArrayList<>();
		when(batchInfoRepository.findByBatchStatus(SettlementBatchStatus.READY_FOR_RUN, paging)).thenReturn(new PageImpl<>(batchInfos));
		Page<RiderSettlementBatchInfo> batchInfo = riderSettlementService.getAllSettlementByStatus(0, 5, SettlementBatchStatus.READY_FOR_RUN);
		assertNotNull(batchInfo);
	}

	@Test
	public void getAllSettlementByStatus_ALL() {
		Pageable paging = PageRequest.of(0, 5);
		List<RiderSettlementBatchInfo> batchInfos = new ArrayList<>();
		when(batchInfoRepository.findAll(paging)).thenReturn(new PageImpl<>(batchInfos));
		Page<RiderSettlementBatchInfo> batchInfo = riderSettlementService.getAllSettlementByStatus(0, 5, SettlementBatchStatus.ALL);
		assertNotNull(batchInfo);
	}

	@Test
	public void pushReconcileDetails() {
		String pic = "abc.jpg";
		when(batchInfoRepository.save(batchInfo)).thenReturn(batchInfo);
		Boolean bool = riderSettlementService.pushReconcileDetails(RECON_BATCH_ID, pic);
		assertTrue(bool);
	}

	@Test
	public void addSettlementBatch() {
		RiderSettlementBatchInfo riderSettlementBatchInfo = new RiderSettlementBatchInfo();
		riderSettlementBatchInfo.setBatchRef(BATCH_REF);
		when(batchInfoRepository.save(any(RiderSettlementBatchInfo.class))).thenReturn(riderSettlementBatchInfo);
		RiderSettlementBatchInfo result = riderSettlementService.addSettlementBatch(CreateSettlementRequest.builder().build());
		assertEquals(BATCH_REF, result.getBatchRef());
	}

	@Test
	public void save_return_file_result_thenReturns200() {
		batchInfo.setBatchRef(BATCH_REF);
		when(batchInfoRepository.findByBatchRef(BATCH_REF)).thenReturn(Optional.of(batchInfo));
		when(batchInfoRepository.save(batchInfo)).thenReturn(batchInfo);
		doNothing().when(riderSettlementAsyncService).triggerSettlementBatch(batchInfo);
		RiderSettlementBatchInfo fetchedDto = riderSettlementService.triggerSettlementBatch(BATCH_REF, Constants.SYSTEM);
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}

	@Test
	public void getSettlementBatchDetails() {
		Pageable paging = PageRequest.of(0, 5);
		List<RiderSettlementBatchDetails> batchDetails = new ArrayList<>();
		when(batchDetailsRepository.findByBatchRef(BATCH_REF, paging)).thenReturn(new PageImpl<>(batchDetails));
		Page<RiderSettlementBatchDetails> fetchedDto = riderSettlementService.getSettlementBatchDetails(0, 5, BATCH_REF);
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}

	@Test
	public void findRiderSettlementDetails() {
		List<RiderSettlementBatchDetails> batchDetails = new ArrayList<>();
		when(batchDetailsRepository.findAllByCustomerReferenceNumberAndProcessingStatusNot(RIDER_ID, Constants.RUNNING)).thenReturn(batchDetails);
		List<RiderSettlementBatchDetails> fetchedDto = riderSettlementService.findRiderSettlementDetails(RIDER_ID);
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}

	@Test
	public void findRiderSettlementDetailsWithinMonths() {
		List<RiderSettlementBatchDetails> batchDetails = new ArrayList<>();
		when(batchDetailsRepository.findByCustomerReferenceNumberAndTransferDateAfterAndProcessingStatusNot(RIDER_ID, LocalDateTime.now(), Constants.RUNNING)).thenReturn(batchDetails);
		List<RiderSettlementBatchDetails> fetchedDto = riderSettlementService.findRiderSettlementDetailsWithinMonths(RIDER_ID, 1);
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}

	@Test
	public void findRiderSettlementDetailsWithinDates() {
		List<RiderSettlementBatchDetails> batchDetails = new ArrayList<>();
		when(batchDetailsRepository.findAllByCustomerReferenceNumberAndTransferDateBetweenAndProcessingStatusNot(RIDER_ID, LocalDateTime.now(), LocalDateTime.now(), Constants.RUNNING)).thenReturn(batchDetails);
		List<RiderSettlementBatchDetails> fetchedDto = riderSettlementService.findRiderSettlementDetailsWithinDates(RIDER_ID, LocalDateTime.now(), LocalDateTime.now());
		assertNotNull(fetchedDto);
		assertNotNull(fetchedDto.toString());
	}

	@Test
	public void saveReturnFileResultWhenBatchNotExist() {
		when(batchInfoRepository.findByBatchRef(eq(BATCH_REF))).thenReturn(Optional.empty());
		ReturnFileResult returnFileResult = ReturnFileResult.builder()
				.header(ReturnHeader.builder().batchReferenceNumber(BATCH_REF).creationDate(CREATION_DATE)
						.creationTime(CREATION_TIME).valueDate(CREATION_DATE).build())
				.paymentResults(Arrays.asList(ReturnPaymentResult.builder().customerReferenceNumber(RIDER_ID).build()))
				.build();
		boolean result = riderSettlementService.saveReturnFileResult(returnFileResult);
		assertFalse(result);
	}

	@Test
	public void saveReturnFileResultWhenBatchAlreadyProcessed() {
		batchInfo.setBatchStatus(SettlementBatchStatus.SUCCESS);
		when(batchInfoRepository.findByBatchRef(eq(BATCH_REF))).thenReturn(Optional.of(batchInfo));
		ReturnFileResult returnFileResult = ReturnFileResult.builder()
				.header(ReturnHeader.builder().batchReferenceNumber(BATCH_REF).creationDate(CREATION_DATE)
						.creationTime(CREATION_TIME).valueDate(CREATION_DATE).build())
				.paymentResults(Arrays.asList(ReturnPaymentResult.builder().customerReferenceNumber(RIDER_ID).build()))
				.build();
		boolean result = riderSettlementService.saveReturnFileResult(returnFileResult);
		assertTrue(result);
	}

	@Test
	public void saveReturnFileResultWhenExceptionOccurs() {
		when(batchInfoRepository.findByBatchRef(eq(BATCH_REF))).thenReturn(Optional.empty());
		ReturnFileResult returnFileResult = ReturnFileResult.builder()
				.header(ReturnHeader.builder().batchReferenceNumber(BATCH_REF).build())
				.build();
		boolean result = riderSettlementService.saveReturnFileResult(returnFileResult);
		assertFalse(result);
	}

	@Test
	public void saveReturnFileResultWhenSettlementDetailsNotAvailable() {
		batchInfo.setBatchStatus(SettlementBatchStatus.UPLOADED);
		when(batchInfoRepository.findByBatchRef(eq(BATCH_REF))).thenReturn(Optional.of(batchInfo));
		ReturnFileResult returnFileResult = ReturnFileResult.builder()
				.header(ReturnHeader.builder().batchReferenceNumber(BATCH_REF).creationDate(CREATION_DATE)
						.creationTime(CREATION_TIME).valueDate(CREATION_DATE).build())
				.paymentResults(Arrays.asList(ReturnPaymentResult.builder().customerReferenceNumber(RIDER_ID).build()))
				.build();
		boolean result = riderSettlementService.saveReturnFileResult(returnFileResult);
		assertFalse(result);
	}

	@Test
	public void saveReturnFileResultWhenRiderNotExist() {
		batchInfo.setBatchStatus(SettlementBatchStatus.UPLOADED);
		when(batchInfoRepository.findByBatchRef(eq(BATCH_REF))).thenReturn(Optional.of(batchInfo));
		RiderSettlementBatchDetails settlementDetails = new RiderSettlementBatchDetails();
		when(batchDetailsRepository.findByCustomerReferenceNumberInAndBatchRefAndProcessingStatus(any(), eq(BATCH_REF),
				eq(Constants.RUNNING))).thenReturn(Arrays.asList(settlementDetails));
		ReturnFileResult returnFileResult = ReturnFileResult.builder()
				.header(ReturnHeader.builder().batchReferenceNumber(BATCH_REF).creationDate(CREATION_DATE)
						.creationTime(CREATION_TIME).valueDate(CREATION_DATE).build())
				.paymentResults(Arrays.asList(ReturnPaymentResult.builder().customerReferenceNumber(RIDER_ID).build()))
				.build();
		boolean result = riderSettlementService.saveReturnFileResult(returnFileResult);
		assertFalse(result);
	}


	@Test
	public void saveFileResult() {
		ReturnHeader header = ReturnHeader.builder().build();
		header.setBatchReferenceNumber(BATCH_REF);
		header.setCreationDate("20210202");
		header.setCreationTime("100302");
		header.setValueDate("20210202");
		List<ReturnPaymentResult> paymentResults = new ArrayList<>();
		ReturnPaymentResult paymentResult = ReturnPaymentResult.builder()
				.customerReferenceNumber(RIDER_ID)
				.netPaymentAmount("300.00")
				.processingStatus("S").build();
		paymentResults.add(paymentResult);
		ReturnTrailer trailer = ReturnTrailer.builder().build();
		ReturnFileResult result = ReturnFileResult.builder()
				.header(header)
				.paymentResults(paymentResults)
				.trailer(trailer).build();
		RiderSettlementBatchDetails batchDetails = new RiderSettlementBatchDetails();
		batchDetails.setCustomerReferenceNumber(RIDER_ID);
		List<RiderSettlementBatchDetails> batchDetailList = new ArrayList<>();
		batchDetailList.add(batchDetails);
		batchInfo.setBatchStatus(SettlementBatchStatus.READY_FOR_RUN);
		batchInfo.setReconcileBatchId(RECON_BATCH_ID);
		when(batchInfoRepository.findByBatchRef(eq(BATCH_REF))).thenReturn(Optional.of(batchInfo));
		when(batchDetailsRepository.findByCustomerReferenceNumberInAndBatchRefAndProcessingStatus(Arrays.asList(paymentResult.getCustomerReferenceNumber()),
				header.getBatchReferenceNumber(), Constants.RUNNING)).thenReturn(Arrays.asList(batchDetails));
		when(batchDetailsRepository.saveAll(any(ArrayList.class))).thenReturn(batchDetailList);
		when(batchInfoRepository.save(batchInfo)).thenReturn(batchInfo);
		RiderCredit riderCredit = new RiderCredit(RIDER_ID, "600.00");
		List<RiderCredit> riderCredits = new ArrayList<>();
		riderCredits.add(riderCredit);
		List<RiderPaymentDetails> listOfRiderPaymentDetails = new ArrayList<>();
		listOfRiderPaymentDetails.add(getRiderPaymentDetails(RIDER_ID, 24.0, 66.0, 10.0));
		listOfRiderPaymentDetails.add(getRiderPaymentDetails("RR24112", 24.0, 66.0, 10.0));
		listOfRiderPaymentDetails.add(getRiderPaymentDetails("RR24113", 24.0, 0.0, 10.0));
		listOfRiderPaymentDetails.add(getRiderPaymentDetails("RR24114", 24.0, null, 10.0));
		when(riderPaymentDetailsRepository.findByBatchRef(BATCH_REF)).thenReturn(listOfRiderPaymentDetails);
		when(pocketServiceFeignClient.updateRiderNetPocketBalance(riderCredits)).thenReturn(true);
		when(pocketServiceClient.updateIncentivesInBatch(anyList())).thenReturn(Arrays.asList(true));
		Boolean isSuccess = riderSettlementService.saveReturnFileResult(result);
		verify(settlementHelper, times(1)).saveRiderDetailsAndDoPocketProcessing(any(), any(), any(), any());
		assertNotNull(isSuccess);
		assertTrue(isSuccess);
		assertNotNull(header.toString());
		assertNotNull(trailer.toString());
		assertNotNull(paymentResult.toString());
		assertNotNull(riderCredit.toString());
	}

	@Test
	public void getReconcileDetails() {
		when(batchInfoRepository.findByReconcileBatchId(RECON_BATCH_ID)).thenReturn(Optional.of(new RiderSettlementBatchInfo()));
		RiderSettlementBatchInfo fetchedDto = riderSettlementService.getReconcileDetails(RECON_BATCH_ID);
		assertNotNull(fetchedDto);
	}

	@Test
	public void getRiderPaymentDetailsTest() {
		RiderPaymentDetails paymentDetails = getRiderPaymentDetails(RIDER_ID, 24.0, 66.0, 10.0);
		when(riderPaymentDetailsRepository.findByBatchRef(eq(BATCH_REF))).thenReturn(Arrays.asList(paymentDetails));
		List<RiderPaymentDetails> fetchedDto = riderSettlementService.getRiderPaymentDetails(BATCH_REF);
		assertNotNull(fetchedDto);
	}

	@Test
	public void shouldNotGeneratePocketReportWhenBatchNotExists() {
		String batchRef = "S1000000001";
		when(batchInfoRepository.findByBatchRef(batchRef)).thenReturn(Optional.empty());
		boolean result = riderSettlementService.generatePocketReport(batchRef);
		assertFalse(result);
	}

	@Test
	public void shouldNotGeneratePocketReportWhenBatchExists() {
		String batchRef = "S1000000001";
		RiderSettlementBatchInfo riderSettlementBatchInfo = new RiderSettlementBatchInfo();
		riderSettlementBatchInfo.setBatchStatus(SettlementBatchStatus.SUCCESS);
		when(batchInfoRepository.findByBatchRef(batchRef)).thenReturn(Optional.of(riderSettlementBatchInfo));
		boolean result = riderSettlementService.generatePocketReport(batchRef);
		verify(reportGenerator, times(1)).generatePocketReport(any(RiderSettlementBatchInfo.class));
		assertTrue(result);
	}

	private RiderPaymentDetails getRiderPaymentDetails(String riderId, Double remainingSecurityBal,
												Double securityDeducted, Double incentive) {
		return RiderPaymentDetails.builder().batchRef(BATCH_REF).riderId(riderId).remainingSecurityBalance(remainingSecurityBal)
				.securityAmountDeducted(securityDeducted).netIncentiveAmount(incentive).build();
	}


	@Test
	public void getSettlementStatusByDateIntervalsTest_1(){
		List<RiderSettlementBatchInfo> riderSettlementBatchInfoList =new ArrayList<>();
		riderSettlementBatchInfoList.add(batchInfo);
		when(riderSettlementBatchInfoRepository.findByCreatedDateBetween(any(),any())).thenReturn(Optional.of(riderSettlementBatchInfoList));
		List<RiderSettlementBatchInfo> result = riderSettlementService.getSettlementStatusByDateIntervals(LocalDate.now(),LocalDate.now().plusDays(1));
		assertEquals(1,result.size());
	}

	@Test
	public void getSettlementStatusByDateIntervalsTest_2(){
		List<RiderSettlementBatchInfo> riderSettlementBatchInfoList =new ArrayList<>();
		when(riderSettlementBatchInfoRepository.findByCreatedDateBetween(any(),any())).thenReturn(Optional.of(riderSettlementBatchInfoList));
		List<RiderSettlementBatchInfo> result = riderSettlementService.getSettlementStatusByDateIntervals(LocalDate.now(),LocalDate.now().plusDays(1));
		assertEquals(0,result.size());
	}

}
