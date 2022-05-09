package com.scb.settlement.service;

import com.scb.settlement.client.impl.ReconciliationServiceClient;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.dto.FinalPaymentReconciliationDetails;
import com.scb.settlement.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ReportGenerator {

    private static final String SETTLEMENT_REPORTS = "settlement/reports";
    private static final String SETTLEMENT_REPORT_SUBJECT = "Report for reconcile rider settlement";
    private static final String SETTLEMENT_REPORT_TEXT = "Please find attached reconcile rider settlement report";
    private static final String POCKET_REPORT_SUBJECT = "Report for pocket balance";
    private static final String POCKET_REPORT_TEXT = "Please find attached Report for pocket balance";

    @Autowired
    private ReconciliationServiceClient reconciliationServiceClient;

    @Autowired
    private ExcelServiceImpl excelServiceImpl;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AmazonS3Service amazonS3Service;

    @Async("asyncExecutor")
    public void generateSettlementReport(RiderSettlementBatchInfo settlementBatchInfo) {
        try {
            List<FinalPaymentReconciliationDetails> matchedJobs = getMatchedJobs(Arrays.asList(settlementBatchInfo.getReconcileBatchId(), settlementBatchInfo.getPointxReconBatchId()));

            if(CollectionUtils.isEmpty(matchedJobs)) {
                log.info("generateSettlementReport: No Matched jobs found for reconId: {}", settlementBatchInfo.getReconcileBatchId());
                return;
            }
            byte[] bytes = excelServiceImpl.createSettlementReport(matchedJobs);
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String fileName = CommonUtils.getSettlementFileName();
            uploadToS3(inputStream, settlementBatchInfo.getBatchRef(), fileName);
            emailService.sendMailWithAttachment(fileName, bytes, SETTLEMENT_REPORT_SUBJECT, SETTLEMENT_REPORT_TEXT);
        } catch (Exception e) {
            log.error("Error while generating settlement report", e);
        }
    }

    @Async("asyncExecutor")
    public void generatePocketReport(RiderSettlementBatchInfo settlementBatchInfo) {
        try {
            List<FinalPaymentReconciliationDetails> matchedJobs = getMatchedJobs(Arrays.asList(settlementBatchInfo.getReconcileBatchId(), settlementBatchInfo.getPointxReconBatchId()));
            if(CollectionUtils.isEmpty(matchedJobs)) {
                log.info("generatePocketReport: No Matched jobs found for reconId: {}", settlementBatchInfo.getReconcileBatchId());
                return;
            }
            byte[] bytes = excelServiceImpl.createPocketBalanceReport(settlementBatchInfo.getBatchRef(), matchedJobs);
            InputStream inputStream = new ByteArrayInputStream(bytes);
            String fileName = CommonUtils.getPocketReportName(settlementBatchInfo.getBatchRef());
            uploadToS3(inputStream, settlementBatchInfo.getBatchRef(), fileName);
            emailService.sendMailWithAttachment(fileName, bytes, POCKET_REPORT_SUBJECT, POCKET_REPORT_TEXT);
        } catch (Exception e) {
            log.error("Error while generating pocket report", e);
        }
    }

    private void uploadToS3(InputStream inputStream, String key, String fileName) {
        try {
            amazonS3Service.uploadInputStream(inputStream, SETTLEMENT_REPORTS, key, fileName);
        } catch (Exception e) {
            log.error("Error while uploading file to s3 bucket", e);
        }
    }

    private List<FinalPaymentReconciliationDetails> getMatchedJobs(List<String> batchIds) {
        List<FinalPaymentReconciliationDetails> matchedJobs = new ArrayList<>();
        batchIds.stream().filter(Objects::nonNull).forEach(batchId ->
            matchedJobs.addAll(reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(batchId))
        );
        return matchedJobs;
    }
}
