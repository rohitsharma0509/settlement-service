package com.scb.settlement.service;

import com.scb.settlement.client.SettlementFileGenerationFeignClient;
import com.scb.settlement.client.TaxServiceFeignClient;
import com.scb.settlement.client.impl.PocketServiceClient;
import com.scb.settlement.client.impl.ReconciliationServiceClient;
import com.scb.settlement.exception.ExternalServiceInvocationException;
import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.dto.GLInvoice;
import com.scb.settlement.model.dto.RiderPocketDetails;
import com.scb.settlement.model.dto.RiderSettlementDetails;
import com.scb.settlement.model.dto.SettlementDetails;
import com.scb.settlement.model.dto.SftpRequest;
import com.scb.settlement.model.enumeration.SettlementBatchStatus;
import com.scb.settlement.repository.RiderPaymentDetailsRepository;
import com.scb.settlement.repository.RiderSettlementBatchDetailsRepository;
import com.scb.settlement.repository.RiderSettlementBatchInfoRepository;
import com.scb.settlement.service.document.helper.RiderSettlementAsyncServiceImpl;
import com.scb.settlement.service.document.helper.SettlementHelper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderSettlementAsyncServiceTest {
	private static final String SETTLEMENT_BATCH_ID = "S1000000001";
	private static final String RECON_BATCH_ID = "RECON0000000001";
	private static final String RIDER1 = "RR000001";
	private static final String RIDER2 = "RR000002";
	private static final String RIDER_NAME = "test";
	private static final String FILE_NAME = "s1-file.gpg";
	private static final int ONE_TIME = 1;

	@Mock
	private RiderSettlementBatchInfoRepository batchInfoRepository;

	@Mock
	private ReconciliationServiceClient reconciliationServiceClient;

	@Mock
	private PocketServiceClient pocketServiceClient;

	@Mock
	private TaxServiceFeignClient taxServiceFeignClient;

	@Mock
	private SettlementFileGenerationFeignClient settlementFileGenerationFeignClient;

	@Mock
	private RiderSettlementBatchDetailsRepository batchDetailsRepository;

	@Mock
	private RiderPaymentDetailsRepository riderPaymentDetailsRepository;

	@Mock
	private SettlementHelper settlementHelper;

	@InjectMocks
	private RiderSettlementAsyncServiceImpl riderSettlementAsyncService;

	private static RiderSettlementBatchInfo batchInfo;

	@BeforeAll
	static void setUp() {
		batchInfo = new RiderSettlementBatchInfo();
		batchInfo.setReconcileBatchId(RECON_BATCH_ID);
		batchInfo.setBatchStatus(SettlementBatchStatus.READY_FOR_RUN);
		batchInfo.setBatchRef(SETTLEMENT_BATCH_ID);
	}

	@Test
	void triggerBatchWhenExceptionOccurred() {
		when(reconciliationServiceClient.getRiderConsolidatedPaymentDetails(anyList())).thenThrow(new NullPointerException());
		when(batchInfoRepository.findByBatchRef(eq(SETTLEMENT_BATCH_ID))).thenReturn(Optional.of(batchInfo));
		riderSettlementAsyncService.triggerSettlementBatch(batchInfo);
		verify(batchInfoRepository, times(ONE_TIME)).save(any(RiderSettlementBatchInfo.class));
	}

	@Test
	void triggerBatchWhenReconServiceNotWorking() {
		when(reconciliationServiceClient.getRiderConsolidatedPaymentDetails(anyList())).thenThrow(new ExternalServiceInvocationException("Recon failed"));
		when(batchInfoRepository.findByBatchRef(eq(SETTLEMENT_BATCH_ID))).thenReturn(Optional.of(batchInfo));
		riderSettlementAsyncService.triggerSettlementBatch(batchInfo);
		verify(batchInfoRepository, times(ONE_TIME)).save(any(RiderSettlementBatchInfo.class));
	}

	@Test
	void triggerBatchWhenPocketServiceNotWorking() {
		Map<String, RiderSettlementDetails> riderJobDetails = new HashMap<>();
		riderJobDetails.put(RIDER1, getRiderSettlementDetails(RIDER1, 100.0, 50.0));
		when(reconciliationServiceClient.getRiderConsolidatedPaymentDetails(anyList())).thenReturn(riderJobDetails);
		when(pocketServiceClient.getPocketDetailsByRiderIdsInBatch(anyList())).thenThrow(new NullPointerException());
		when(batchInfoRepository.save(any(RiderSettlementBatchInfo.class))).thenReturn(batchInfo);
		when(batchInfoRepository.findByBatchRef(eq(SETTLEMENT_BATCH_ID))).thenReturn(Optional.of(batchInfo));
		riderSettlementAsyncService.triggerSettlementBatch(batchInfo);
		verify(batchInfoRepository, times(ONE_TIME)).save(any(RiderSettlementBatchInfo.class));
	}

	@Test
	void triggerBatchWhenRidersNotEligibleForPaymentAndNoNeedToDeductSecurity() {
		Map<String, RiderSettlementDetails> riderJobDetails = new HashMap<>();
		riderJobDetails.put(RIDER1, getRiderSettlementDetails(RIDER1, 100.0, 100.0));
		when(reconciliationServiceClient.getRiderConsolidatedPaymentDetails(anyList())).thenReturn(riderJobDetails);
		List<RiderPocketDetails> riderPocketDetails = new ArrayList<>();
		riderPocketDetails.add(getRiderPocketDetails(RIDER1, 100.0, 0.0, 100.0, 0.0));
		when(pocketServiceClient.getPocketDetailsByRiderIdsInBatch(anyList())).thenReturn(riderPocketDetails);
		List<RiderPaymentDetails> listOfRiderPaymentDetails = new ArrayList<>();
		listOfRiderPaymentDetails.add(getRiderPaymentDetails(RIDER1, 100.0, 0.0, 0.0, 0.0, 0.0, 100.0, 0.0));
		when(riderPaymentDetailsRepository.saveAll(anyList())).thenReturn(listOfRiderPaymentDetails);
		when(taxServiceFeignClient.sendBatchesForGL(any(GLInvoice.class))).thenThrow(new NullPointerException());
		when(batchInfoRepository.save(any(RiderSettlementBatchInfo.class))).thenReturn(batchInfo);
		riderSettlementAsyncService.triggerSettlementBatch(batchInfo);
		verify(settlementHelper, times(ONE_TIME)).saveRiderDetailsAndDoPocketProcessing(anyList(), any(), any(), any(LocalDateTime.class));
	}

	@Test
	void triggerBatchWhenRidersNotEligibleForPaymentWithDeductionsAdjustedFromSecurityIsGreaterThanPaidSecurity() {
		Map<String, RiderSettlementDetails> riderJobDetails = new HashMap<>();
		riderJobDetails.put(RIDER1, getRiderSettlementDetails(RIDER1, 100.0, 150.0));
		when(reconciliationServiceClient.getRiderConsolidatedPaymentDetails(anyList())).thenReturn(riderJobDetails);
		List<RiderPocketDetails> riderPocketDetails = new ArrayList<>();
		riderPocketDetails.add(getRiderPocketDetails(RIDER1, 100.0, 0.0, 100.0, 60.0));
		when(pocketServiceClient.getPocketDetailsByRiderIdsInBatch(anyList())).thenReturn(riderPocketDetails);
		List<RiderPaymentDetails> listOfRiderPaymentDetails = new ArrayList<>();
		listOfRiderPaymentDetails.add(getRiderPaymentDetails(RIDER1, 100.0, 0.0, 0.0, 60.0, 0.0, 150.0, 10.0));
		when(riderPaymentDetailsRepository.saveAll(anyList())).thenReturn(listOfRiderPaymentDetails);
		when(taxServiceFeignClient.sendBatchesForGL(any(GLInvoice.class))).thenThrow(new NullPointerException());
		when(batchInfoRepository.save(any(RiderSettlementBatchInfo.class))).thenReturn(batchInfo);
		riderSettlementAsyncService.triggerSettlementBatch(batchInfo);
		verify(settlementHelper, times(ONE_TIME)).saveRiderDetailsAndDoPocketProcessing(anyList(), any(), any(), any(LocalDateTime.class));
	}

	@Test
	void triggerBatchWhenRidersNotEligibleForPaymentButSecurityNeedsToAdjustedAndGlFailed() {
		Map<String, RiderSettlementDetails> riderJobDetails = new HashMap<>();
		riderJobDetails.put(RIDER1, getRiderSettlementDetails(RIDER1, 100.0, 50.0));
		riderJobDetails.put(RIDER2, getRiderSettlementDetails(RIDER2, 100.0, 150.0));
		when(reconciliationServiceClient.getRiderConsolidatedPaymentDetails(anyList())).thenReturn(riderJobDetails);
		List<RiderPocketDetails> riderPocketDetails = new ArrayList<>();
		riderPocketDetails.add(getRiderPocketDetails(RIDER1, 100.0, 10.0, 100.0, 100.0));
		riderPocketDetails.add(getRiderPocketDetails(RIDER2, 100.0, 10.0, 100.0, 100.0));
		when(pocketServiceClient.getPocketDetailsByRiderIdsInBatch(anyList())).thenReturn(riderPocketDetails);
		List<RiderPaymentDetails> listOfRiderPocketDetails = new ArrayList<>();
		listOfRiderPocketDetails.add(getRiderPaymentDetails(RIDER1, 100.0, 0.0, 60.0, 40.0, 10.0, 50.0, 0.0));
		listOfRiderPocketDetails.add(getRiderPaymentDetails(RIDER1, 100.0, 0.0, 10.0, 90.0, 10.0, 100.0, 50.0));
		when(riderPaymentDetailsRepository.saveAll(anyList())).thenReturn(listOfRiderPocketDetails);
		when(taxServiceFeignClient.sendBatchesForGL(any(GLInvoice.class))).thenThrow(new NullPointerException());
		when(batchInfoRepository.save(any(RiderSettlementBatchInfo.class))).thenReturn(batchInfo);
		when(settlementFileGenerationFeignClient.generateAndUploadS1File(any(SettlementDetails.class))).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(FILE_NAME));
		when(settlementFileGenerationFeignClient.uploadFileToSftp(any(SftpRequest.class))).thenReturn(Boolean.TRUE);
		riderSettlementAsyncService.triggerSettlementBatch(batchInfo);
		verify(batchDetailsRepository, times(ONE_TIME)).saveAll(anyList());
		verifyZeroInteractions(settlementHelper);
	}

	@Test
	void triggerBatchWhenExceptionOccurredWhileGeneratingS1File() {
		Map<String, RiderSettlementDetails> riderJobDetails = new HashMap<>();
		riderJobDetails.put(RIDER1, getRiderSettlementDetails(RIDER1, 100.0, 50.0));
		when(reconciliationServiceClient.getRiderConsolidatedPaymentDetails(anyList())).thenReturn(riderJobDetails);
		List<RiderPocketDetails> riderPocketDetails = new ArrayList<>();
		riderPocketDetails.add(getRiderPocketDetails(RIDER1, 100.0, 100.0, 100.0, 100.0));
		when(pocketServiceClient.getPocketDetailsByRiderIdsInBatch(anyList())).thenReturn(riderPocketDetails);
		List<RiderPaymentDetails> listOfRiderPaymentDetails = new ArrayList<>();
		listOfRiderPaymentDetails.add(getRiderPaymentDetails(RIDER1, 100.0, 50.0, 100.0, 0.0, 100.0, 50.0, 0.0));
		when(riderPaymentDetailsRepository.saveAll(anyList())).thenReturn(listOfRiderPaymentDetails);
		when(settlementFileGenerationFeignClient.generateAndUploadS1File(any(SettlementDetails.class))).thenThrow(new NullPointerException());
		when(batchInfoRepository.save(any(RiderSettlementBatchInfo.class))).thenReturn(batchInfo);
		when(batchInfoRepository.findByBatchRef(eq(SETTLEMENT_BATCH_ID))).thenReturn(Optional.of(batchInfo));
		riderSettlementAsyncService.triggerSettlementBatch(batchInfo);
		verifyZeroInteractions(settlementHelper);
	}

	@Test
	void triggerBatchWhenExceptionOccurredWhileUploadingS1File() {
		Map<String, RiderSettlementDetails> riderJobDetails = new HashMap<>();
		riderJobDetails.put(RIDER1, getRiderSettlementDetails(RIDER1, 100.0, 50.0));
		when(reconciliationServiceClient.getRiderConsolidatedPaymentDetails(anyList())).thenReturn(riderJobDetails);
		List<RiderPocketDetails> riderPocketDetails = new ArrayList<>();
		riderPocketDetails.add(getRiderPocketDetails(RIDER1, 100.0, 100.0, 100.0, 100.0));
		when(pocketServiceClient.getPocketDetailsByRiderIdsInBatch(anyList())).thenReturn(riderPocketDetails);
		List<RiderPaymentDetails> listOfRiderPaymentDetails = new ArrayList<>();
		listOfRiderPaymentDetails.add(getRiderPaymentDetails(RIDER1, 100.0, 50.0, 100.0, 0.0, 100.0, 50.0, 0.0));
		when(riderPaymentDetailsRepository.saveAll(anyList())).thenReturn(listOfRiderPaymentDetails);
		when(taxServiceFeignClient.sendBatchesForGL(any(GLInvoice.class))).thenReturn(Boolean.TRUE);
		when(settlementFileGenerationFeignClient.generateAndUploadS1File(any(SettlementDetails.class))).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(FILE_NAME));
		when(settlementFileGenerationFeignClient.uploadFileToSftp(any(SftpRequest.class))).thenThrow(new NullPointerException());
		when(batchInfoRepository.save(any(RiderSettlementBatchInfo.class))).thenReturn(batchInfo);
		when(batchInfoRepository.findByBatchRef(eq(SETTLEMENT_BATCH_ID))).thenReturn(Optional.of(batchInfo));
		riderSettlementAsyncService.triggerSettlementBatch(batchInfo);
		verifyZeroInteractions(settlementHelper);
	}

	@Test
	void triggerBatchWhenRidersAreEligibleForPayment() {
		Map<String, RiderSettlementDetails> riderJobDetails = new HashMap<>();
		riderJobDetails.put(RIDER1, getRiderSettlementDetails(RIDER1, 100.0, 50.0));
		when(reconciliationServiceClient.getRiderConsolidatedPaymentDetails(anyList())).thenReturn(riderJobDetails);
		List<RiderPocketDetails> riderPocketDetails = new ArrayList<>();
		riderPocketDetails.add(getRiderPocketDetails(RIDER1, 100.0, 100.0, 100.0, 100.0));
		when(pocketServiceClient.getPocketDetailsByRiderIdsInBatch(anyList())).thenReturn(riderPocketDetails);
		List<RiderPaymentDetails> listOfRiderPaymentDetails = new ArrayList<>();
		listOfRiderPaymentDetails.add(getRiderPaymentDetails(RIDER1, 100.0, 50.0, 100.0, 0.0, 100.0, 50.0, 0.0));
		when(riderPaymentDetailsRepository.saveAll(anyList())).thenReturn(listOfRiderPaymentDetails);
		when(taxServiceFeignClient.sendBatchesForGL(any(GLInvoice.class))).thenReturn(Boolean.TRUE);
		when(settlementFileGenerationFeignClient.generateAndUploadS1File(any(SettlementDetails.class))).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(FILE_NAME));
		when(settlementFileGenerationFeignClient.uploadFileToSftp(any(SftpRequest.class))).thenReturn(Boolean.TRUE);
		when(batchInfoRepository.save(any(RiderSettlementBatchInfo.class))).thenReturn(batchInfo);
		riderSettlementAsyncService.triggerSettlementBatch(batchInfo);
		verify(batchDetailsRepository, times(ONE_TIME)).saveAll(anyList());
		verifyZeroInteractions(settlementHelper);
	}

	@Test
	void triggerBatchWhenRidersAreEligibleForPaymentWithDeductionsAdjustedFromSecurityIsLessThanPaidSecurity() {
		Map<String, RiderSettlementDetails> riderJobDetails = new HashMap<>();
		riderJobDetails.put(RIDER1, getRiderSettlementDetails(RIDER1, 100.0, 160.0));
		when(reconciliationServiceClient.getRiderConsolidatedPaymentDetails(anyList())).thenReturn(riderJobDetails);
		List<RiderPocketDetails> riderPocketDetails = new ArrayList<>();
		riderPocketDetails.add(getRiderPocketDetails(RIDER1, 100.0, 50.0, 100.0, 20.0));
		when(pocketServiceClient.getPocketDetailsByRiderIdsInBatch(anyList())).thenReturn(riderPocketDetails);
		List<RiderPaymentDetails> listOfRiderPaymentDetails = new ArrayList<>();
		listOfRiderPaymentDetails.add(getRiderPaymentDetails(RIDER1, 100.0, 30.0, 100.0, 20.0, 50.0, 160.0, 0.0));
		when(riderPaymentDetailsRepository.saveAll(anyList())).thenReturn(listOfRiderPaymentDetails);
		when(taxServiceFeignClient.sendBatchesForGL(any(GLInvoice.class))).thenReturn(Boolean.TRUE);
		when(settlementFileGenerationFeignClient.generateAndUploadS1File(any(SettlementDetails.class))).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(FILE_NAME));
		when(settlementFileGenerationFeignClient.uploadFileToSftp(any(SftpRequest.class))).thenReturn(Boolean.TRUE);
		when(batchInfoRepository.save(any(RiderSettlementBatchInfo.class))).thenReturn(batchInfo);
		riderSettlementAsyncService.triggerSettlementBatch(batchInfo);
		verify(batchDetailsRepository, times(ONE_TIME)).saveAll(anyList());
		verifyZeroInteractions(settlementHelper);
	}

	private RiderPocketDetails getRiderPocketDetails(String riderId, Double pocketBalance, Double incentive, Double securityBalance, Double remainingSecurity) {
		return RiderPocketDetails.builder().riderId(riderId)
				.pocketBalance(pocketBalance)
				.netIncentiveAmount(incentive)
				.securityBalance(securityBalance)
				.remainingSecurityBalance(remainingSecurity).build();
	}

	private static RiderSettlementDetails getRiderSettlementDetails(String riderId, Double totalCreditAmount, Double otherDeductions) {
		RiderSettlementDetails riderSettlementDetails = new RiderSettlementDetails();
		riderSettlementDetails.setRiderId(riderId);
		riderSettlementDetails.setRiderName(RIDER_NAME);
		riderSettlementDetails.setTotalCreditAmount(totalCreditAmount);
		riderSettlementDetails.setTotalDeductions(otherDeductions);
		riderSettlementDetails.setTotalEwtAmount(0.0);
		return riderSettlementDetails;
	}

	private RiderPaymentDetails getRiderPaymentDetails(String riderId, Double jobEarning, Double transferAmount,
														Double securityDeducted, Double remainingSecurityBalance, Double incentiveAmount, Double totalDeductions, Double remainingDeduction) {
		return RiderPaymentDetails.builder()
				.transferDate(LocalDateTime.now())
				.dateSearch(LocalDateTime.now().toString())
				.riderId(riderId)
				.beneficiaryName(RIDER_NAME)
				.pocketBalance(jobEarning)
				.netPaymentAmount(transferAmount)
				.securityAmountDeducted(securityDeducted)
				.remainingSecurityBalance(remainingSecurityBalance)
				.netIncentiveAmount(incentiveAmount)
				.otherPayments(incentiveAmount)
				.otherDeductions(totalDeductions)
				.remainingDeductions(remainingDeduction)
				.pocketBalanceSearch(jobEarning + StringUtils.EMPTY)
				.netPaymentAmountSearch(transferAmount + StringUtils.EMPTY)
				.securityAmountDeductedSearch(securityDeducted + StringUtils.EMPTY)
				.otherPaymentsSearch(incentiveAmount + StringUtils.EMPTY)
				.otherDeductionsSearch(totalDeductions + StringUtils.EMPTY)
				.build();
	}

}
