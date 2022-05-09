package com.scb.settlement.service.document.helper;

import com.scb.settlement.client.SettlementFileGenerationFeignClient;
import com.scb.settlement.client.TaxServiceFeignClient;
import com.scb.settlement.client.impl.PocketServiceClient;
import com.scb.settlement.client.impl.ReconciliationServiceClient;
import com.scb.settlement.constants.Constants;
import com.scb.settlement.constants.ErrorConstants;
import com.scb.settlement.constants.SearchConstants;
import com.scb.settlement.exception.DataNotFoundException;
import com.scb.settlement.exception.ExternalServiceInvocationException;
import com.scb.settlement.exception.S1BatchRunFailureException;
import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
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
import com.scb.settlement.utils.CommonUtils;
import com.scb.settlement.utils.LoggerUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Log4j2
public class RiderSettlementAsyncServiceImpl implements  RiderSettlementAsyncService {
	@Autowired
	private ReconciliationServiceClient reconciliationServiceClient;
	@Autowired
	private SettlementFileGenerationFeignClient settlementFileGenerationFeignClient;
	@Autowired
	private PocketServiceClient pocketServiceClient;

	@Autowired
	private SettlementHelper settlementHelper;

	@Autowired
	private TaxServiceFeignClient taxServiceFeignClient;
	@Autowired
	private RiderSettlementBatchInfoRepository batchInfoRepository;
	@Autowired
	private RiderSettlementBatchDetailsRepository batchDetailsRepository;

	@Autowired
	private RiderPaymentDetailsRepository riderPaymentDetailsRepository;

	@Async("asyncExecutor")
	@Override
	public void triggerSettlementBatch(RiderSettlementBatchInfo riderSettlementBatchInfo) {
		try{
			log.info("settlement batch {} is triggered", riderSettlementBatchInfo.getBatchRef());
			trigger(riderSettlementBatchInfo);
			log.info("settlement batch {} has been completed", riderSettlementBatchInfo.getBatchRef());
		} catch (Exception ex){
			log.error("Error coming during running S1 batch = {}", ex);
			RiderSettlementBatchInfo batchInfo = batchInfoRepository.findByBatchRef(riderSettlementBatchInfo.getBatchRef()).orElseThrow(
					() -> LoggerUtils.logError(RiderSettlementAsyncService.class, riderSettlementBatchInfo.getBatchRef(), SearchConstants.BATCH_REF));
			batchInfo.setBatchStatus(SettlementBatchStatus.FAILED);
			batchInfo.setEndTime(LocalTime.now());
			batchInfo.setEndTimeSearch(batchInfo.getEndTime().toString());
			if(ExceptionUtils.hasCause(ex, DataNotFoundException.class) || ExceptionUtils.hasCause(ex, S1BatchRunFailureException.class)
					|| ExceptionUtils.hasCause(ex, ExternalServiceInvocationException.class)) {
				batchInfo.setFailureReason(ex.getMessage());
			} else {
				batchInfo.setFailureReason(ErrorConstants.S1_BATCH_FAILURE_MSG);
			}
			batchInfoRepository.save(batchInfo);
		}
	}

	private void trigger(RiderSettlementBatchInfo batchInfo) {
		String batchRef = batchInfo.getBatchRef();
		Map<String, RiderSettlementDetails> riderJobsMap = reconciliationServiceClient.getRiderConsolidatedPaymentDetails(
				Arrays.asList(batchInfo.getReconcileBatchId(), batchInfo.getPointxReconBatchId()));
		log.info("reconbatchId {}, pointxReconBatchId {}", batchInfo.getReconcileBatchId(), batchInfo.getPointxReconBatchId());
		List<RiderPocketDetails> listOfRiderPocketDetails = pocketServiceClient.getPocketDetailsByRiderIdsInBatch(new ArrayList<>(riderJobsMap.keySet()));
		List<RiderPaymentDetails> listOfRiderPaymentDetails = getRidersPaymentDetailsRecords(batchRef, riderJobsMap, listOfRiderPocketDetails);
		double totalPaymentAmount = listOfRiderPaymentDetails.stream().mapToDouble(RiderPaymentDetails::getNetPaymentAmount).sum();
		double totalSecurityAmountDeducted = listOfRiderPaymentDetails.stream().mapToDouble(RiderPaymentDetails::getSecurityAmountDeducted).sum();
		if(totalSecurityAmountDeducted > 0) {
			riderJobsMap.put(Constants.UNREAL_RIDER_ID_FOR_SECURITY, getSecurityDepositAccountEntry(totalSecurityAmountDeducted));
		}
		totalPaymentAmount = CommonUtils.round(Double.sum(totalPaymentAmount, totalSecurityAmountDeducted));
		SettlementDetails settlementDetails = new SettlementDetails();
		settlementDetails.setTotalConsolidatedAmount(totalPaymentAmount);
		settlementDetails.setTotalDebitAmount(totalPaymentAmount);
		settlementDetails.setRiderSettlementDetails(new ArrayList<>(riderJobsMap.values()));
		settlementDetails.setNoOfCredits(settlementDetails.getRiderSettlementDetails().size());
		settlementDetails.setBatchReferenceNumber(batchRef);
		log.info("settlement reconcile details = {}", settlementDetails);

		log.info("Total Payment Amount in running status for S1 batch {} are {}", batchRef, settlementDetails.getTotalConsolidatedAmount());
		batchInfo.setTotalNumberOfTransactions(Integer.toString(settlementDetails.getNoOfCredits()));
		batchInfo.setTotalPaymentAmount(Double.toString(settlementDetails.getTotalConsolidatedAmount()));
		batchInfo.setTotalSecurityAmountDeducted(CommonUtils.round(totalSecurityAmountDeducted));
		batchInfo = batchInfoRepository.save(batchInfo);
		saveRiderPaymentTransferDetails(settlementDetails);
		saveRiderPaymentDetails(batchRef, listOfRiderPaymentDetails);

		if (isPaymentEntriesExists(batchInfo.getReconcileBatchId(), batchRef, listOfRiderPaymentDetails, settlementDetails)) {
			batchInfo.setBatchStatus(SettlementBatchStatus.SUCCESS);
			batchInfoRepository.save(batchInfo);
			return;
		}

		ResponseEntity<String> responseEntity = generateAndUploadS1File(settlementDetails);
		sendBatchesForGLInvoice(batchInfo.getReconcileBatchId(), batchRef);

		log.info("s3 file name = {}", responseEntity.getBody());
		if(HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
			batchInfo.setBatchStatus(SettlementBatchStatus.FILE_GENERATED);
			batchInfo.setS1FilePathUrl(responseEntity.getBody());
			batchInfo = batchInfoRepository.save(batchInfo);

			SftpRequest sftpRequest = new SftpRequest();
			sftpRequest.setBatchReferenceNumber(batchRef);
			sftpRequest.setFileName(batchInfo.getS1FilePathUrl());
			Boolean isSftpFileGenerated = uploadFileToSftp(sftpRequest);
			log.info("is sftp file generated =  {}", isSftpFileGenerated);
			if(Boolean.TRUE.equals(isSftpFileGenerated)){
				batchInfo.setBatchStatus(SettlementBatchStatus.UPLOADED);
				batchInfoRepository.save(batchInfo);
			}
		} else{
			log.error("Exception occurred during generating and uploading s1 file {}", responseEntity.getStatusCode());
			throw new S1BatchRunFailureException("Data failure during generating and uploading s1 file");
		}
	}

	private RiderSettlementDetails getSecurityDepositAccountEntry(double totalSecurityAmountDeducted) {
		RiderSettlementDetails riderSettlementDetails = new RiderSettlementDetails();
		riderSettlementDetails.setRiderId(Constants.UNREAL_RIDER_ID_FOR_SECURITY);
		riderSettlementDetails.setRiderName(StringUtils.EMPTY);
		riderSettlementDetails.setRiderAccountNumber(Constants.SECURITY_DEPOSIT_ACCOUNT_NO);
		riderSettlementDetails.setTotalCreditAmount(totalSecurityAmountDeducted);
		riderSettlementDetails.setTotalDeductions(0.0);
		return riderSettlementDetails;
	}

	private boolean isPaymentEntriesExists(String reconciliationBatchId, String batchRef, List<RiderPaymentDetails> listOfRiderPaymentDetails, SettlementDetails settlementDetails) {
		if(CollectionUtils.isEmpty(settlementDetails.getRiderSettlementDetails())) {
			if(!CollectionUtils.isEmpty(listOfRiderPaymentDetails)){
				settlementHelper.saveRiderDetailsAndDoPocketProcessing(listOfRiderPaymentDetails, null, null, listOfRiderPaymentDetails.get(0).getTransferDate());
				sendBatchesForGLInvoice(reconciliationBatchId, batchRef);
				return true;
			}else{
				log.error("There is no payment to be made for riders");
				throw new S1BatchRunFailureException("There is no payment to be made for riders");
			}
		}
		return false;
	}

	private boolean uploadFileToSftp(SftpRequest sftpRequest) {
		boolean isSftpFileGenerated = false;
		try {
			isSftpFileGenerated = settlementFileGenerationFeignClient.uploadFileToSftp(sftpRequest);
		} catch (Exception e){
			log.error("Data failure during uploading sftp file {}", e.getMessage());
			throw new S1BatchRunFailureException("Data failure during uploading sftp file");
		}
		return isSftpFileGenerated;
	}

	private boolean sendBatchesForGLInvoice(String reconciliationBatchId, String batchRef) {
		boolean bool = false;
		try {
			GLInvoice glInvoice = GLInvoice.builder().reconBatchId(reconciliationBatchId).s1BatchId(batchRef).build();
			bool = taxServiceFeignClient.sendBatchesForGL(glInvoice);
		} catch (Exception e){
			log.error("Exception occurred during sending batch details to tax-service to generate GL invoice {}", e.getMessage());
		}
		log.info("Output during sending batch details to tax-service to generate GL invoice = {}", bool);
		return bool;
	}

	private ResponseEntity<String> generateAndUploadS1File(SettlementDetails settlementDetails) {
		try {
			return settlementFileGenerationFeignClient.generateAndUploadS1File(settlementDetails);
		} catch (Exception e){
			log.error("Exception occurred during generating and uploading s1 file {}", e.getMessage());
			throw new S1BatchRunFailureException("Data failure during generating and uploading s1 file");
		}
	}

	private void saveRiderPaymentTransferDetails(SettlementDetails settlementDetails) {
		if(Objects.nonNull(settlementDetails) && !CollectionUtils.isEmpty(settlementDetails.getRiderSettlementDetails())){
			try{
				batchDetailsRepository.deleteAllByBatchRefAndProcessingStatus(settlementDetails.getBatchReferenceNumber(), Constants.RUNNING);
			} catch(Exception e){
				log.error("{} batch is not present in db", settlementDetails.getBatchReferenceNumber());
			}

			LocalDateTime currentDateTime = LocalDateTime.now();
			List<RiderSettlementBatchDetails> batchDetailsList = new ArrayList<>();
			for(RiderSettlementDetails details : settlementDetails.getRiderSettlementDetails()){
				RiderSettlementBatchDetails settlementBatchDetails = new RiderSettlementBatchDetails();
				settlementBatchDetails.setProcessingStatus(Constants.RUNNING);
				settlementBatchDetails.setProcessingRemarks(Constants.PROCESSING_REMARKS);
				settlementBatchDetails.setBatchRef(settlementDetails.getBatchReferenceNumber());
				settlementBatchDetails.setPostDate(currentDateTime);
				settlementBatchDetails.setTransferDate(currentDateTime);
				settlementBatchDetails.setDateSearch(settlementBatchDetails.getTransferDate().toString());
				settlementBatchDetails.setCustomerReferenceNumber(details.getRiderId());
				settlementBatchDetails.setBeneficiaryAccount(details.getRiderAccountNumber());
				String amt = Double.toString(CommonUtils.round(details.getTotalCreditAmount()));
				settlementBatchDetails.setNetPaymentAmount(amt);
				settlementBatchDetails.setPaymentAmount(amt);
				settlementBatchDetails.setPaymentAmountSort(details.getTotalCreditAmount());
				settlementBatchDetails.setBeneficiaryName(details.getRiderName());
				settlementBatchDetails.setBeneficiaryBankName(details.getRiderName());
				settlementBatchDetails.setBeneficiaryBranchName(details.getRiderName());
				batchDetailsList.add(settlementBatchDetails);
			}
			batchDetailsRepository.saveAll(batchDetailsList);
		}

	}

	public List<RiderPaymentDetails> getRidersPaymentDetailsRecords(String batchRef, Map<String, RiderSettlementDetails> riderJobsMap, List<RiderPocketDetails> listOfRiderPocketDetails) {
		log.info("getting RidersPaymentDetails Records");
		List<RiderPaymentDetails> listOfRiderPaymentDetails = new ArrayList<>();
		if (!CollectionUtils.isEmpty(listOfRiderPocketDetails)) {
			LocalDateTime currentDateTime = LocalDateTime.now();
			for (RiderPocketDetails riderPocketDetails : listOfRiderPocketDetails) {
				if (riderJobsMap.containsKey(riderPocketDetails.getRiderId())) {
					String riderId = riderPocketDetails.getRiderId();
					RiderSettlementDetails riderJobDetails = riderJobsMap.get(riderId);

					Double earning, securityDeducted;
					Double remainingDeduction, transferAmount, remainingSecurityBalance, securityRecovered = 0.0;
					Double jobEarning = CommonUtils.round(riderJobDetails.getTotalCreditAmount());
					Double incentiveAmount = Objects.nonNull(riderPocketDetails.getNetIncentiveAmount()) ? CommonUtils.round(riderPocketDetails.getNetIncentiveAmount()) : 0;
					Double otherDeductions = Objects.nonNull(riderJobDetails.getTotalDeductions()) ? CommonUtils.round(riderJobDetails.getTotalDeductions()) : 0;

					Double excessiveWaitTimeAmount = CommonUtils.round(riderJobDetails.getTotalEwtAmount());

					log.info("otherDeductions {}, jobEarning {}, totalSecurity {}, remainingSecurity {} for riderId {}",
							otherDeductions, jobEarning, riderPocketDetails.getSecurityBalance(), riderPocketDetails.getRemainingSecurityBalance(), riderId);
					Double alreadyPaidSecurity = riderPocketDetails.getSecurityBalance() - riderPocketDetails.getRemainingSecurityBalance();

					if(jobEarning < otherDeductions) {
						remainingDeduction = CommonUtils.round(otherDeductions - jobEarning);
						earning = 0.0;
						if(alreadyPaidSecurity > 0) {
							if(alreadyPaidSecurity < remainingDeduction) {
								remainingDeduction = remainingDeduction - alreadyPaidSecurity;
								securityRecovered = alreadyPaidSecurity;
							} else {
								securityRecovered = remainingDeduction;
								remainingDeduction = 0.0;
							}
						}
					} else {
						remainingDeduction = 0.0;
						earning = jobEarning - otherDeductions;
					}
					earning = CommonUtils.round(Double.sum(earning, Double.sum(incentiveAmount, excessiveWaitTimeAmount)));

					log.info("earning {} incentiveAmount {}, securityRecovered {}, remainingDeduction {} for riderId {}",
							earning, incentiveAmount, securityRecovered, remainingDeduction, riderId);
					if (riderPocketDetails.getRemainingSecurityBalance() < earning) {
						remainingSecurityBalance = 0.0;
						securityDeducted = CommonUtils.round(riderPocketDetails.getRemainingSecurityBalance());
						transferAmount = CommonUtils.round(earning - riderPocketDetails.getRemainingSecurityBalance());
						riderJobDetails.setTotalCreditAmount(transferAmount);
					} else {
						remainingSecurityBalance = CommonUtils.round(riderPocketDetails.getRemainingSecurityBalance() - earning);
						securityDeducted = CommonUtils.round(earning);
						transferAmount = 0.0;
						riderJobDetails.setTotalCreditAmount(transferAmount);
						riderJobsMap.remove(riderJobDetails.getRiderId());
					}
					log.info("remainingSecurityBalance {}, securityDeducted {}, transferAmount {} for riderId {}", remainingSecurityBalance,
							securityDeducted, transferAmount, riderId);

					RiderPaymentDetails record = RiderPaymentDetails.builder()
							.batchRef(batchRef)
							.transferDate(currentDateTime)
							.dateSearch(currentDateTime.toString())
							.riderId(riderId)
							.beneficiaryName(riderJobDetails.getRiderName())
							.beneficiaryAccount(riderJobDetails.getRiderAccountNumber())
							.processingStatus(Constants.RUNNING)
							.processingRemarks(StringUtils.EMPTY)
							.pocketBalance(jobEarning)
							.netPaymentAmount(transferAmount)
							.securityAmountDeducted(securityDeducted)
							.remainingSecurityBalance(remainingSecurityBalance)
							.netIncentiveAmount(incentiveAmount)
							.otherPayments(incentiveAmount)
							.otherDeductions(otherDeductions)
							.remainingDeductions(CommonUtils.round(remainingDeduction))
							.securityRecovered(CommonUtils.round(securityRecovered))
							.pocketBalanceSearch(jobEarning + StringUtils.EMPTY)
							.netPaymentAmountSearch(transferAmount + StringUtils.EMPTY)
							.securityAmountDeductedSearch(securityDeducted + StringUtils.EMPTY)
							.otherPaymentsSearch(incentiveAmount + StringUtils.EMPTY)
							.otherDeductionsSearch(otherDeductions + StringUtils.EMPTY)
							.netExcessiveWaitTimeAmount(excessiveWaitTimeAmount)
							.netExcessiveWaitTimeAmountSearch(excessiveWaitTimeAmount+ StringUtils.EMPTY)
							.build();
					listOfRiderPaymentDetails.add(record);
				}
			}
		}
		return listOfRiderPaymentDetails;
	}

	private void saveRiderPaymentDetails(String batchRef, List<RiderPaymentDetails> listOfRiderPaymentDetails) {
		try {
			riderPaymentDetailsRepository.deleteAllByBatchRef(batchRef);
		} catch (Exception e) {
			log.error("{} batch is not present in db", batchRef);
		}
		riderPaymentDetailsRepository.saveAll(listOfRiderPaymentDetails);
	}
}