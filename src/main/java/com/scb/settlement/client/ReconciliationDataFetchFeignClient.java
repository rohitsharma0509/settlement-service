package com.scb.settlement.client;

import com.scb.settlement.model.CustomPageImpl;
import com.scb.settlement.model.dto.FinalPaymentReconciliationDetails;
import com.scb.settlement.model.dto.RiderPaymentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "fetchReconciledData", url = "${rider.client.reconciliation-service}")
public interface ReconciliationDataFetchFeignClient {

    @GetMapping("/api/reconciliation/batch-details/{batchId}")
    CustomPageImpl<FinalPaymentReconciliationDetails> getFinalReconciliationDetailsByBatchId(
            @PathVariable("batchId") String batchId,
            @RequestParam(name="status", defaultValue = "MATCHED") String status,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "10") int size);

    @GetMapping("/reconciliation/{batchId}/ewt-details")
    List<RiderPaymentDto> getRiderEwtPaymentDetails(@PathVariable("batchId") String batchId);

}
