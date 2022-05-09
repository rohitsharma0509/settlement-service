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
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api/settlement/batch")
public class RiderSettlementController {
    @Autowired
    private RiderSettlementService riderSettlementService;

    public static final String RIDER_SETTLEMENT_INFO = "Getting Rider Settlement Info by id = {}";

    @PutMapping("/{batchRef}")
    public ResponseEntity<RiderSettlementBatchInfo> triggerSettlementBatch(
            @RequestHeader(value = Constants.X_USER_ID, defaultValue = Constants.SYSTEM) String xUserId,
            @PathVariable String batchRef) {
        log.info("triggering S1 file batch");
        return ResponseEntity.status(HttpStatus.CREATED).body(this.riderSettlementService.triggerSettlementBatch(batchRef, xUserId));
    }

    @GetMapping("/{batchRef}")
    public ResponseEntity<RiderSettlementBatchInfo> getS1FileBatchStatus(@PathVariable("batchRef") String batchRef) {
        log.info(RIDER_SETTLEMENT_INFO, batchRef);
        return ResponseEntity.ok(this.riderSettlementService.getSettlementStatus(batchRef));
    }

    @GetMapping("/details/{batchRef}")
    public ResponseEntity<Page<RiderSettlementBatchResponse>> getSettlementBatchDetails(
            @PathVariable("batchRef") String batchRef,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        log.info(RIDER_SETTLEMENT_INFO, batchRef);

        List<RiderSettlementBatchResponse> responses = RiderSettlementBatchResponse.of(this.riderSettlementService.getSettlementBatchDetails(page, size, batchRef).toList());
        PageImpl<RiderSettlementBatchResponse> pageImpl = new PageImpl<>(responses);
        return ResponseEntity.ok(pageImpl);
    }

    @GetMapping("/status/{batchStatus}")
    public ResponseEntity<Page<RiderSettlementBatchInfo>> getAllSettlement(
            @PathVariable SettlementBatchStatus batchStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(this.riderSettlementService.getAllSettlementByStatus(page, size, batchStatus));
    }

    @PutMapping("/{batchId}/reconcile/{reconcileFileName}")
    public ResponseEntity<Boolean> pushReconcileDetails(
            @PathVariable("batchId") String batchId, @PathVariable("reconcileFileName") String reconcileFileName) {
        log.info(RIDER_SETTLEMENT_INFO, batchId);
        return ResponseEntity.ok(this.riderSettlementService.pushReconcileDetails(batchId, reconcileFileName));
    }

    @PostMapping("/save")
    public ResponseEntity<RiderSettlementBatchInfo> addSettlementBatch(@RequestBody CreateSettlementRequest request) {
        log.info("creating settlement batch for reconBatchId {} and pointxReconBatchId {}",
                request.getReconBatchId(), request.getPointxReconBatchId());
        return ResponseEntity.ok(riderSettlementService.addSettlementBatch(request));
    }

    @GetMapping("/reconcile/{reconcileBatchId}")
    public ResponseEntity<RiderSettlementBatchInfo> getReconcileDetails(@PathVariable("reconcileBatchId") String reconcileBatchId) {
        log.info(RIDER_SETTLEMENT_INFO, reconcileBatchId);
        return ResponseEntity.ok(this.riderSettlementService.getReconcileDetails(reconcileBatchId));
    }

    @PostMapping
    public ResponseEntity<Boolean> saveReturnFileResult(@RequestBody ReturnFileResult returnFileResult) {
        log.info("Saving rider settlement details");
        return ResponseEntity.ok(this.riderSettlementService.saveReturnFileResult(returnFileResult));
    }

    @GetMapping("/{batchRef}/rider/{riderId}")
    public ResponseEntity<RiderPocketSettlementDetails> getRiderPaymentsByBatchId(
            @PathVariable("batchRef") String batchRef,
            @PathVariable("riderId") String riderId) {
        log.info("Getting Rider Settlement Details for rider - {}, batchRef - {}", riderId, batchRef);
        RiderSettlementBatchDetails details = riderSettlementService.getRiderPaymentsByBatchId(batchRef, riderId);
        return ResponseEntity.ok(RiderPocketSettlementDetails.toRiderPocketSettlementDetails(details));
    }

    @GetMapping("/{riderId}/all")
    public ResponseEntity<List<RiderPocketSettlementDetails>> getPaymentReconciliation(
            @PathVariable("riderId") String riderId) {
        log.info("Getting Rider Settlement Details  for rider - {}",riderId);
        return ResponseEntity.ok(RiderPocketSettlementDetails.of(this.riderSettlementService.findRiderSettlementDetails(riderId)));
    }

    @GetMapping("/{riderId}/months/{months}")
    public ResponseEntity<List<RiderPocketSettlementDetails>> getPaymentReconciliationWithinMonths(
            @PathVariable("riderId") String riderId,
            @PathVariable("months") int months) {
        log.info("Getting Rider Settlement Details for rider with {} in past {} months", riderId, months);
        return ResponseEntity.ok(RiderPocketSettlementDetails.of(this.riderSettlementService.findRiderSettlementDetailsWithinMonths(riderId, months)));
    }

    @GetMapping("/{riderId}/dates")
    public ResponseEntity<List<RiderPocketSettlementDetails>> getPaymentReconciliationWithinDates(
            @PathVariable("riderId") String riderId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting Rider Settlement Details  for rider - {} between {} and {}",riderId,startDate,endDate);
        return ResponseEntity.ok(RiderPocketSettlementDetails.of(this.riderSettlementService.findRiderSettlementDetailsWithinDates(riderId,startDate,endDate)));
    }

    @GetMapping("/security-balance/{batchRef}")
    public ResponseEntity<List<RiderPaymentDetails>> getRiderPaymentDetails(@PathVariable("batchRef") String batchRef) {
        log.info("Getting Rider Security balances for batchRef {}", batchRef);
        return ResponseEntity.ok(riderSettlementService.getRiderPaymentDetails(batchRef));
    }

    @GetMapping("/pocket-report/{batchRef}")
    public ResponseEntity<Boolean> generatePocketReport(@PathVariable("batchRef") String batchRef) {
        log.info("Request received to generate pocket balance report for batchRef {}", batchRef);
        return ResponseEntity.ok(riderSettlementService.generatePocketReport(batchRef));
    }

    @GetMapping("/settlementBatchInfoStatus")
    public ResponseEntity<List<RiderSettlementBatchInfo>> getSettlementBatchInfo(
            @RequestParam(name = "startDate", defaultValue = "#{T(java.time.LocalDate).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", defaultValue = "#{T(java.time.LocalDate).now().plusDays(1)}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Request received to get settlement batch status for startDate : {} , endDate : {} ", startDate, endDate);
        if (startDate.isAfter(endDate)) {
            log.info("Dates received are incorrect : startDate : {} , endDate : {} ", startDate, endDate);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }

        return ResponseEntity.ok(riderSettlementService.getSettlementStatusByDateIntervals(startDate, endDate));
    }

}