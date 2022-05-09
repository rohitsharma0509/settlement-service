package com.scb.settlement.service.document;

import com.scb.settlement.constants.Constants;
import com.scb.settlement.constants.SearchConstants;
import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.dto.CreateSettlementRequest;
import com.scb.settlement.model.dto.ReturnHeader;
import com.scb.settlement.model.dto.ReturnFileResult;
import com.scb.settlement.model.dto.ReturnHeader;
import com.scb.settlement.model.dto.ReturnPaymentResult;
import com.scb.settlement.model.dto.ReturnTrailer;
import com.scb.settlement.model.enumeration.SettlementBatchStatus;
import com.scb.settlement.repository.RiderPaymentDetailsRepository;
import com.scb.settlement.repository.RiderSettlementBatchDetailsRepository;
import com.scb.settlement.repository.RiderSettlementBatchInfoRepository;
import com.scb.settlement.service.ReportGenerator;
import com.scb.settlement.service.document.helper.RiderSettlementAsyncService;
import com.scb.settlement.service.document.helper.SettlementHelper;
import com.scb.settlement.utils.CommonUtils;
import com.scb.settlement.utils.LoggerUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RiderSettlementService {

    @Autowired
    private RiderSettlementBatchInfoRepository batchInfoRepository;

    @Autowired
    private RiderSettlementBatchDetailsRepository batchDetailsRepository;

    @Autowired
    private SettlementHelper settlementHelper;

    @Autowired
    private RiderSettlementAsyncService riderSettlementAsyncService;

    @Autowired
    private RiderPaymentDetailsRepository riderPaymentDetailsRepository;

    @Autowired
    private ReportGenerator reportGenerator;

    @Autowired
    private RiderSettlementBatchInfoRepository riderSettlementBatchInfoRepository;


    public RiderSettlementBatchInfo triggerSettlementBatch(String batchRef, String xUserId) {
        log.info("Creating S1 batch entry ........ for batchRef = {}", batchRef);
        RiderSettlementBatchInfo batchInfo = batchInfoRepository.findByBatchRef(batchRef)
                .orElseThrow(() -> LoggerUtils.logError(RiderSettlementService.class, batchRef, SearchConstants.BATCH_REF));

        if (SettlementBatchStatus.READY_FOR_RUN.equals(batchInfo.getBatchStatus())
                || SettlementBatchStatus.FAILED.equals(batchInfo.getBatchStatus())) {
            batchInfo.setUpdatedBy(xUserId);
            batchInfo.setDateOfRun(LocalDate.now());
            batchInfo.setDateSearch(batchInfo.getDateOfRun().toString());
            batchInfo.setStartTime(LocalTime.now());
            batchInfo.setStartTimeSearch(LocalTime.now().toString());
            batchInfo.setEndTimeSearch("");
            batchInfo.setBatchStatus(SettlementBatchStatus.IN_PROGRESS);
            batchInfo.setFailureReason("");
            batchInfo = batchInfoRepository.save(batchInfo);
            riderSettlementAsyncService.triggerSettlementBatch(batchInfo);
        }
        return batchInfo;
    }

    public RiderSettlementBatchInfo getSettlementStatus(String batchRef) {
        log.info("Getting S1 file batch details = {}", batchRef);
        return batchInfoRepository.findByBatchRef(batchRef)
                .orElseThrow(() -> LoggerUtils.logError(RiderSettlementService.class, batchRef, SearchConstants.BATCH_REF));
    }

    public void saveSettlementStatus(String batchRef, SettlementBatchStatus batchStatus) {
        log.info("Getting S1 file batch details = {}", batchRef);
        RiderSettlementBatchInfo fileBatchRunDetails = batchInfoRepository.findByBatchRef(batchRef)
                .orElseThrow(() -> LoggerUtils.logError(RiderSettlementService.class, batchRef, SearchConstants.BATCH_REF));
        fileBatchRunDetails.setBatchStatus(batchStatus);
        batchInfoRepository.save(fileBatchRunDetails);
    }

    public Page<RiderSettlementBatchInfo> getAllSettlementByStatus(int page, int size, SettlementBatchStatus batchStatus) {
        Pageable paging = PageRequest.of(page, size);
        if (SettlementBatchStatus.ALL.equals(batchStatus)) {
            return batchInfoRepository.findAll(paging);
        }
        return batchInfoRepository.findByBatchStatus(batchStatus, paging);
    }

    public RiderSettlementBatchInfo addSettlementBatch(CreateSettlementRequest request) {
        log.info("adding settlement batch for reconBatchId {} and pointxReconBatchId {}",
                request.getReconBatchId(), request.getPointxReconBatchId());
        RiderSettlementBatchInfo settlementBatchInfo = new RiderSettlementBatchInfo();
        settlementBatchInfo.setBatchStatus(SettlementBatchStatus.READY_FOR_RUN);
        settlementBatchInfo.setReconcileBatchId(request.getReconBatchId());
        settlementBatchInfo.setFileName(request.getReconFileName());
        settlementBatchInfo.setPointxReconBatchId(request.getPointxReconBatchId());
        settlementBatchInfo.setPointxReconFileName(request.getPointxReconFileName());
        return batchInfoRepository.save(settlementBatchInfo);
    }

    public Boolean pushReconcileDetails(String batchId, String reconcileFileName) {
        log.info("Creating S1 batch entry ........");
        RiderSettlementBatchInfo settlementBatchInfo = new RiderSettlementBatchInfo();
        settlementBatchInfo.setBatchStatus(SettlementBatchStatus.READY_FOR_RUN);
        settlementBatchInfo.setReconcileBatchId(batchId);
        settlementBatchInfo.setFileName(reconcileFileName);
        settlementBatchInfo.setDateSearch("");
        settlementBatchInfo.setEndTimeSearch("");
        settlementBatchInfo.setStartTimeSearch("");
        batchInfoRepository.save(settlementBatchInfo);
        return Boolean.TRUE;
    }

    public Boolean saveReturnFileResult(ReturnFileResult details) {
        ReturnHeader header = details.getHeader();
        String batchRef = header.getBatchReferenceNumber();
        try {
            List<ReturnPaymentResult> paymentResults = details.getPaymentResults();
            ReturnTrailer trailer = details.getTrailer();
            LocalDateTime transferDate = LocalDateTime.parse(header.getValueDate().concat(header.getCreationTime()), DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT));
            LocalDateTime postDate = LocalDateTime.parse(header.getCreationDate().concat(header.getCreationTime()), DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT));

            Optional<RiderSettlementBatchInfo> settlementBatch = batchInfoRepository.findByBatchRef(batchRef);
            if (!settlementBatch.isPresent()) {
                log.error("settlement batch {} is not exists", batchRef);
                return Boolean.FALSE;
            } else if (settlementBatch.isPresent() && SettlementBatchStatus.SUCCESS.equals(settlementBatch.get().getBatchStatus())) {
                log.error("settlement batch {} has already been processed", batchRef);
                return Boolean.TRUE;
            }
            RiderSettlementBatchInfo batchInfo = settlementBatch.get();
            List<String> riderIds = paymentResults.stream().map(ReturnPaymentResult::getCustomerReferenceNumber).collect(Collectors.toList());
            List<RiderSettlementBatchDetails> listOfSettlementDetails = batchDetailsRepository.findByCustomerReferenceNumberInAndBatchRefAndProcessingStatus(
                    riderIds, batchRef, Constants.RUNNING);
            if(CollectionUtils.isEmpty(listOfSettlementDetails)) {
                log.error("riders settlement details not found for settlement batch {}", batchRef);
                return Boolean.FALSE;
            }
            List<RiderSettlementBatchDetails> settlementBatchDetails = new ArrayList<>();
            Map<String, RiderSettlementBatchDetails> riderSettlementBatchDetailsMap = new HashMap<>();
            Set<String> paymentFailedRidersSet = new HashSet<>();
            for (ReturnPaymentResult paymentResult : paymentResults) {
                String riderId = paymentResult.getCustomerReferenceNumber();
                Optional<RiderSettlementBatchDetails> riderSettlementDetails = listOfSettlementDetails.stream().filter(Objects::nonNull)
                        .filter(securityDetails -> riderId.equals(securityDetails.getCustomerReferenceNumber())).findFirst();
                if (!riderSettlementDetails.isPresent()) {
                    log.error("rider with riderId {} not exists in settlement batch {}", riderId, batchRef);
                    return Boolean.FALSE;
                }
                RiderSettlementBatchDetails result = riderSettlementDetails.get();
                String riderName = result.getBeneficiaryName();
                String accountNumber = result.getBeneficiaryAccount();
                BeanUtils.copyProperties(paymentResult, result);

                result.setBeneficiaryName(riderName);
                result.setPaymentAmountSort(CommonUtils.round(Double.parseDouble(paymentResult.getNetPaymentAmount())));
                result.setBeneficiaryAccount(accountNumber);
                result.setCustomerReferenceNumber(riderId);
                result.setProcessingStatus(Constants.FAILED);
                if (StringUtils.isBlank(result.getProcessingRemarks())) {
                    result.setProcessingRemarks(StringUtils.EMPTY);
                }
                if (StringUtils.isNotBlank(paymentResult.getProcessingStatus()) && paymentResult.getProcessingStatus().equalsIgnoreCase("s")) {
                    result.setProcessingStatus(Constants.SUCCESS);
                }else{
                    paymentFailedRidersSet.add(riderId);
                }
                result.setTransferDate(transferDate);
                result.setDateSearch(result.getTransferDate().toString());
                result.setPostDate(postDate);
                settlementBatchDetails.add(result);
                riderSettlementBatchDetailsMap.put(riderId, result);
            }
            batchInfo.setBatchStatus(SettlementBatchStatus.SUCCESS);
            batchInfo.setEndTime(LocalTime.now());
            batchInfo.setEndTimeSearch(batchInfo.getEndTime().toString());
            log.info("S1 return file Total Payment Amount for S1 batch {} are {}", batchRef, trailer.getTotalPaymentAmount());
            batchInfo.setTotalNumberOfTransactions(trailer.getTotalNumberOfTransactions());
            batchInfo.setTotalPaymentAmount(trailer.getTotalPaymentAmount());
            List<RiderPaymentDetails> listOfRiderPaymentDetails = riderPaymentDetailsRepository.findByBatchRef(batchRef);
            settlementHelper.saveRiderDetailsAndDoPocketProcessing(listOfRiderPaymentDetails, riderSettlementBatchDetailsMap, paymentFailedRidersSet, transferDate);
            batchDetailsRepository.saveAll(settlementBatchDetails);
            batchInfoRepository.save(batchInfo);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("Exception while processing return file with batchRef " + batchRef, e);
            return Boolean.FALSE;
        }
    }

    public Page<RiderSettlementBatchDetails> getSettlementBatchDetails(int page, int size, String batchRef) {
        Pageable paging = PageRequest.of(page, size);
        return batchDetailsRepository.findByBatchRef(batchRef, paging);
    }

    public RiderSettlementBatchDetails getRiderPaymentsByBatchId(String batchRef, String riderId) {
        return batchDetailsRepository.findByCustomerReferenceNumberAndBatchRefAndProcessingStatus(riderId, batchRef, Constants.SUCCESS)
                .orElseThrow(() -> LoggerUtils.logError(RiderSettlementService.class, batchRef, SearchConstants.BATCH_REF));
    }

    public List<RiderSettlementBatchDetails> findRiderSettlementDetails(String riderId) {
        return batchDetailsRepository.findAllByCustomerReferenceNumberAndProcessingStatusNot(riderId, Constants.RUNNING);

    }

    public List<RiderSettlementBatchDetails> findRiderSettlementDetailsWithinMonths(String riderId, int months) {
        LocalDateTime date = LocalDateTime.now().minusMonths(months);
        return batchDetailsRepository.findByCustomerReferenceNumberAndTransferDateAfterAndProcessingStatusNot(riderId, date, Constants.RUNNING);
    }

    public List<RiderSettlementBatchDetails> findRiderSettlementDetailsWithinDates(String riderId, LocalDateTime startDate, LocalDateTime endDate) {
        return batchDetailsRepository.findAllByCustomerReferenceNumberAndTransferDateBetweenAndProcessingStatusNot(riderId, startDate, endDate, Constants.RUNNING);
    }

    public RiderSettlementBatchInfo getReconcileDetails(String reconcileBatchId) {
        return batchInfoRepository.findByReconcileBatchId(reconcileBatchId)
                .orElseThrow(() -> LoggerUtils.logError(RiderSettlementService.class, reconcileBatchId, "reconcileBatchId"));
    }

	public List<RiderPaymentDetails> getRiderPaymentDetails(String batchRef) {
		return riderPaymentDetailsRepository.findByBatchRef(batchRef);
	}

	public boolean generatePocketReport(String settlementBatchId) {
        Optional<RiderSettlementBatchInfo> settlementBatchInfo = batchInfoRepository.findByBatchRef(settlementBatchId);
        log.info("is settlement record present in database: {}", settlementBatchInfo.isPresent());
        if(settlementBatchInfo.isPresent()) {
            RiderSettlementBatchInfo settlementInfo = settlementBatchInfo.get();
            log.info("settlement batch status: {}, batchRef {}", settlementInfo.getBatchStatus(), settlementBatchId);
            if(SettlementBatchStatus.SUCCESS.equals(settlementInfo.getBatchStatus())) {
                reportGenerator.generatePocketReport(settlementBatchInfo.get());
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public List<RiderSettlementBatchInfo> getSettlementStatusByDateIntervals(LocalDate startDate, LocalDate endDate) {
        DateTime startDateTime = convertLocalDateToDateTime(startDate);
        DateTime endDateTime = convertLocalDateToDateTime(endDate);
        Optional<List<RiderSettlementBatchInfo>> riderSettlementBatchInfo = riderSettlementBatchInfoRepository.findByCreatedDateBetween(startDateTime, endDateTime);
        if (riderSettlementBatchInfo.isPresent() && !riderSettlementBatchInfo.get().isEmpty()) {
            return riderSettlementBatchInfo.get();
        }
        return Collections.emptyList();
    }

    private DateTime convertLocalDateToDateTime(LocalDate date) {
        org.joda.time.LocalDate convertedDate = new org.joda.time.LocalDate(date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth());
        DateTimeZone timeZone = DateTimeZone.forID("UTC");
        return convertedDate.toDateTime(new org.joda.time.LocalTime(0, 0), timeZone);
    }
}