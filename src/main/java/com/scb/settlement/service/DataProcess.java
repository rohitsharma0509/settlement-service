package com.scb.settlement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.settlement.constants.Constants;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.dto.FinalPaymentReconciliationDetails;
import com.scb.settlement.repository.RiderSettlementBatchDetailsRepository;
import com.scb.settlement.repository.RiderSettlementBatchInfoRepository;
import com.scb.settlement.utils.LoggerUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
public class DataProcess {

    private ObjectMapper objectMapper;
    private RiderSettlementBatchDetailsRepository batchDetailsRepository;
    private RiderSettlementBatchInfoRepository batchInfoRepository;

    @Autowired
    public DataProcess(ObjectMapper objectMapper, RiderSettlementBatchDetailsRepository batchDetailsRepository
            , RiderSettlementBatchInfoRepository batchInfoRepository) {
        this.objectMapper = objectMapper;
        this.batchDetailsRepository = batchDetailsRepository;
        this.batchInfoRepository = batchInfoRepository;
    }

    private static final String RECTIFIED = "Rectified";
    private static final String MSG_RECTIFIED = "Rectified amount using offline Mode";

    public void processKafkaMessage(String message) throws JsonProcessingException {
        log.info("Data Consumed from Kafka topic: {} ", message);
        FinalPaymentReconciliationDetails rDetails = objectMapper.readValue(message, FinalPaymentReconciliationDetails.class);

        RiderSettlementBatchInfo batchInfo = batchInfoRepository.findByReconcileBatchId(rDetails.getBatchId())
                .orElseThrow(() -> LoggerUtils.logError(RiderSettlementBatchInfo.class, rDetails.getBatchId(), "batchId"));
        RiderSettlementBatchDetails details = new RiderSettlementBatchDetails();
        List<RiderSettlementBatchDetails> riderBatchDetails = null;
        if(StringUtils.isNotBlank(rDetails.getRaRiderId())){
            riderBatchDetails = batchDetailsRepository.findAllByCustomerReferenceNumberAndBatchRefAndProcessingStatus(rDetails.getRaRiderId(), batchInfo.getBatchRef(), Constants.SUCCESS);
            if(!CollectionUtils.isEmpty(riderBatchDetails)){
                BeanUtils.copyProperties(riderBatchDetails.get(0), details);
                details.setId(null);
            }
        }
        details.setTransferDate(LocalDateTime.now());
        details.setPostDate(LocalDateTime.now());
        details.setBatchRef(batchInfo.getBatchRef());
        details.setBeneficiaryName(rDetails.getRaRiderName());
        details.setCustomerReferenceNumber(rDetails.getRaRiderId());
        details.setNetPaymentAmount(rDetails.getRaPaymentAmount());
        details.setPaymentAmount(rDetails.getRaPaymentAmount());
        details.setBeneficiaryAccount(rDetails.getAccountNumber());
        details.setProcessingStatus(RECTIFIED);
        details.setProcessingRemarks(MSG_RECTIFIED);
        details.setUpdatedBy(rDetails.getUpdatedBy());
        batchDetailsRepository.save(details);
        Double totalNetPaymentAmt = 0d;
        if(StringUtils.isNotBlank(batchInfo.getTotalPaymentAmount())){
            totalNetPaymentAmt = Double.parseDouble(batchInfo.getTotalPaymentAmount());
        }
        totalNetPaymentAmt += Double.parseDouble(details.getNetPaymentAmount());
        batchInfo.setTotalPaymentAmount(totalNetPaymentAmt.toString());
        Integer totalTransactions = 0;
        if(StringUtils.isNotBlank(batchInfo.getTotalNumberOfTransactions())){
            totalTransactions = Integer.parseInt(batchInfo.getTotalNumberOfTransactions());
        }
        totalTransactions += 1;
        batchInfo.setTotalNumberOfTransactions(totalTransactions.toString());
        batchInfo.setUpdatedBy(rDetails.getUpdatedBy());
        batchInfoRepository.save(batchInfo);
    }
}
