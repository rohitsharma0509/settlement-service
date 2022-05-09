package com.scb.settlement.client.impl;

import com.scb.settlement.client.ReconciliationDataFetchFeignClient;
import com.scb.settlement.exception.ExternalServiceInvocationException;
import com.scb.settlement.model.dto.FinalPaymentReconciliationDetails;
import com.scb.settlement.model.dto.RiderPaymentDto;
import com.scb.settlement.model.dto.RiderSettlementDetails;
import com.scb.settlement.model.enumeration.FinalReconciledStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Component
public class ReconciliationServiceClient {

    private static final int SIZE = 10000;

    @Autowired
    private ReconciliationDataFetchFeignClient reconciliationFeignClient;

    public List<FinalPaymentReconciliationDetails> getFinalReconciliationDetailsByReconBatchId(String reconcileBatchId) {
        Page<FinalPaymentReconciliationDetails> page;
        List<FinalPaymentReconciliationDetails> finalReconciliationList = new ArrayList<>();
        int totalPages = 0;
        do {
            try {
                log.info("getting matched jobs from recon-service with batchId {} pageNumber {}", reconcileBatchId, totalPages);
                page = reconciliationFeignClient.getFinalReconciliationDetailsByBatchId(reconcileBatchId, FinalReconciledStatus.MATCHED.name(), totalPages++, SIZE);
            } catch (Exception e) {
                log.error("Exception while invoking recon service", e);
                throw new ExternalServiceInvocationException("reconciliation-service is not working");
            }
            finalReconciliationList.addAll(page.getContent());
        } while (page.getTotalPages() > totalPages);
        log.info("number of matched jobs in batchId {} are {}", reconcileBatchId, finalReconciliationList.size());
        return finalReconciliationList;
    }

    public List<RiderPaymentDto> getRiderEwtPaymentDetails(String reconcileBatchId) {
        try {
            log.info("getting ewt payment details for batchId: {}", reconcileBatchId);
            return reconciliationFeignClient.getRiderEwtPaymentDetails(reconcileBatchId);
        } catch (Exception e) {
            throw new ExternalServiceInvocationException("reconciliation-service is not working");
        }
    }

    public Map<String, RiderSettlementDetails> getRiderConsolidatedPaymentDetails(List<String> batchIds){
        Map<String, RiderSettlementDetails> riderJobsMap = new HashMap<>();
        Map<String, RiderSettlementDetails> ewtPaymentMap = getRiderEwtPaymentMap(batchIds);
        batchIds.stream().filter(Objects::nonNull).forEach(reconcileBatchId -> {
            List<FinalPaymentReconciliationDetails> finalReconciliationList = getFinalReconciliationDetailsByReconBatchId(reconcileBatchId);
            if (!CollectionUtils.isEmpty(finalReconciliationList)) {
                for (FinalPaymentReconciliationDetails rDetails : finalReconciliationList) {
                    String riderId = rDetails.getRaRiderId();
                    if (riderJobsMap.containsKey(riderId)) {
                        RiderSettlementDetails settlementDetails = riderJobsMap.get(riderId);
                        settlementDetails.setTotalCreditAmount(Double.sum(settlementDetails.getTotalCreditAmount(), Double.parseDouble(rDetails.getRaPaymentAmount())));
                        settlementDetails.setTotalDeductions(Double.sum(settlementDetails.getTotalDeductions(), Objects.nonNull(rDetails.getOtherDeductions()) ? rDetails.getOtherDeductions() : 0));
                    } else {
                        RiderSettlementDetails settlementDetails = new RiderSettlementDetails();
                        settlementDetails.setRiderAccountNumber(rDetails.getAccountNumber());
                        settlementDetails.setRiderId(riderId);
                        settlementDetails.setRiderName(rDetails.getRaRiderName());
                        settlementDetails.setTotalCreditAmount(Double.parseDouble(rDetails.getRaPaymentAmount()));
                        settlementDetails.setTotalDeductions(Objects.nonNull(rDetails.getOtherDeductions()) ? rDetails.getOtherDeductions() : 0);
                        settlementDetails.setTotalEwtAmount(ewtPaymentMap.containsKey(riderId) ? ewtPaymentMap.get(riderId).getTotalEwtAmount() : 0.0);
                        riderJobsMap.put(riderId, settlementDetails);
                        ewtPaymentMap.remove(riderId);
                    }
                }
            }
        });
        //this is to pay ewt in case all the jobs of are mismatched
        if(!CollectionUtils.isEmpty(ewtPaymentMap)) {
            ewtPaymentMap.entrySet().stream().filter(entry -> Objects.nonNull(entry.getValue()) && entry.getValue().getTotalEwtAmount() > 0).
                    forEach(entry -> riderJobsMap.put(entry.getKey(), entry.getValue()));
        }
        return riderJobsMap;
    }

    private Map<String, RiderSettlementDetails> getRiderEwtPaymentMap(List<String> batchIds) {
        Map<String, RiderSettlementDetails> ewtPaymentMap = new HashMap<>();
        batchIds.stream().filter(Objects::nonNull).forEach(reconcileBatchId -> {
            List<RiderPaymentDto> ewtPaymentList = getRiderEwtPaymentDetails(reconcileBatchId);
            if (!CollectionUtils.isEmpty(ewtPaymentList)) {
                ewtPaymentList.stream().forEach(ewtPayment -> {
                    String riderId = ewtPayment.getRiderId();
                    if (ewtPaymentMap.containsKey(riderId)) {
                        RiderSettlementDetails existingDetails = ewtPaymentMap.get(riderId);
                        existingDetails.setTotalEwtAmount(Double.sum(existingDetails.getTotalEwtAmount(), ewtPayment.getTotalEwtAmount()));
                        ewtPaymentMap.put(ewtPayment.getRiderId(), existingDetails);
                    } else {
                        RiderSettlementDetails settlementDetails = new RiderSettlementDetails();
                        settlementDetails.setRiderId(riderId);
                        settlementDetails.setRiderAccountNumber(ewtPayment.getAccountNumber());
                        settlementDetails.setRiderName(ewtPayment.getRiderName());
                        settlementDetails.setTotalCreditAmount(0);
                        settlementDetails.setTotalDeductions(0.0);
                        settlementDetails.setTotalEwtAmount(ewtPayment.getTotalEwtAmount());
                        ewtPaymentMap.put(ewtPayment.getRiderId(), settlementDetails);
                    }
                });
            }
        });
        return ewtPaymentMap;
    }
}
