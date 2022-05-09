package com.scb.settlement.service.document.helper;

import com.scb.settlement.client.impl.PocketServiceClient;
import com.scb.settlement.constants.Constants;
import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
import com.scb.settlement.model.dto.RiderPocketModificationDto;
import com.scb.settlement.repository.RiderPaymentDetailsRepository;
import com.scb.settlement.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class SettlementHelper {

    @Autowired
    private PocketServiceClient pocketServiceClient;

    @Autowired
    public RiderPaymentDetailsRepository riderPaymentDetailsRepository;


    /**
     * This method is to save data in RiderPaymentDetails collection and update pocket balance, security and incentive in pocket-service
     * @param listOfRiderPaymentDetails list Of RiderPaymentDetails for his batch
     * @param batchDetailsMap Data of RiderSettlementBatchDetails collection, required while processing return file result
     * @param paymentFailedRidersSet List of riderIds whose transaction failed, required while processing return file result
     * @param transferDate transaction date
     */
    public void saveRiderDetailsAndDoPocketProcessing(List<RiderPaymentDetails> listOfRiderPaymentDetails, Map<String, RiderSettlementBatchDetails> batchDetailsMap,
                                                      Set<String> paymentFailedRidersSet, LocalDateTime transferDate) {
        log.info("method invoked to update pocket balance, security and incentives");
        if(!CollectionUtils.isEmpty(listOfRiderPaymentDetails)) {
            List<RiderPocketModificationDto> riderPocketModificationDetailsList = new ArrayList<>();

            for (RiderPaymentDetails payDetails : listOfRiderPaymentDetails) {
                String riderId = payDetails.getRiderId();
                RiderSettlementBatchDetails result = !CollectionUtils.isEmpty(batchDetailsMap) ? batchDetailsMap.get(riderId) : null;

                String status = !CollectionUtils.isEmpty(paymentFailedRidersSet) && paymentFailedRidersSet.contains(riderId) ? Constants.FAILED : Constants.SUCCESS;
                String remarks = Objects.nonNull(result) && StringUtils.isNotEmpty(result.getProcessingRemarks()) ? result.getProcessingRemarks() : StringUtils.EMPTY;
                transferDate = Objects.nonNull(result) && Objects.nonNull(result.getTransferDate()) ? result.getTransferDate() : transferDate;
                String dateSearch = Objects.nonNull(result) && Objects.nonNull(result.getDateSearch()) ? result.getDateSearch() : transferDate.toString();

                payDetails.setProcessingStatus(status);
                payDetails.setProcessingRemarks(remarks);
                payDetails.setDateSearch(dateSearch);
                payDetails.setTransferDate(transferDate);

                if (!CollectionUtils.isEmpty(paymentFailedRidersSet) && paymentFailedRidersSet.contains(riderId)) {
                    log.info("transaction failed for rider {} with reason {}", riderId, remarks);
                    continue;
                }
                Double otherDeductions = Objects.nonNull(payDetails.getOtherDeductions()) ? payDetails.getOtherDeductions() : 0;
                Double remainingDeductions = Objects.nonNull(payDetails.getRemainingDeductions()) ? payDetails.getRemainingDeductions() : 0;
                Double securityRecovered = Objects.nonNull(payDetails.getSecurityRecovered()) ? payDetails.getSecurityRecovered() : 0;
                Double actualDeductions = otherDeductions - Double.sum(remainingDeductions, securityRecovered);
                Double incentiveAmount = payDetails.getOtherPayments();
                Double excessiveWaitTimeAmount = Objects.nonNull(payDetails.getNetExcessiveWaitTimeAmount()) ? payDetails.getNetExcessiveWaitTimeAmount() : 0;

                Double pocketBalanceToBeDeducted = Double.sum(payDetails.getNetPaymentAmount(), actualDeductions) - incentiveAmount;
                log.info("jobEarning {}, actualDeductions {}, incentiveAmount {}, securityDeduction {}, remainingSecurity {}, pocketBalanceToBeDeducted {}", payDetails.getPocketBalance(),
                        actualDeductions, incentiveAmount, payDetails.getSecurityAmountDeducted(), payDetails.getRemainingSecurityBalance(), pocketBalanceToBeDeducted);
                riderPocketModificationDetailsList.add(RiderPocketModificationDto.builder()
                        .riderId(payDetails.getRiderId())
                        .pocketBalance(CommonUtils.round(pocketBalanceToBeDeducted))
                        .incentive(payDetails.getNetIncentiveAmount())
                        .securityDeducted(payDetails.getSecurityAmountDeducted())
                        .securityRecovered(payDetails.getSecurityRecovered())
                        .excessiveWaitTimeAmount(excessiveWaitTimeAmount)
                        .build());

            }
            updatePocketDetails(riderPocketModificationDetailsList);
            riderPaymentDetailsRepository.saveAll(listOfRiderPaymentDetails);
        }
    }

    public void updatePocketDetails(List<RiderPocketModificationDto> listOfSecurity) {
        if (!CollectionUtils.isEmpty(listOfSecurity)) {
            log.info("updating pocket balance for riders = {}", listOfSecurity);
            List<Boolean> response = pocketServiceClient.updateRiderPocketDetailsInBatch(listOfSecurity);
            boolean isSuccess = response.stream().allMatch(r -> r);
            log.info("is pocket updated for all batches = {}", isSuccess);
        }
    }

}
