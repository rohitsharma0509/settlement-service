package com.scb.settlement.service;

import com.scb.settlement.client.impl.ReconciliationServiceClient;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.dto.FinalPaymentReconciliationDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportGeneratorTest {

    private static final String RECON_BATCH_ID = "RECON00000001";
    private static final String BATCH_ID = "S10000000001";
    private static final String SETTLEMENT_REPORTS = "settlement/reports";
    private static final String SETTLEMENT_REPORT_SUBJECT = "Report for reconcile rider settlement";
    private static final String SETTLEMENT_REPORT_TEXT = "Please find attached reconcile rider settlement report";
    private static final String POCKET_REPORT_SUBJECT = "Report for pocket balance";
    private static final String POCKET_REPORT_TEXT = "Please find attached Report for pocket balance";
    private static final int INVOKED_ONCE = 1;

    @InjectMocks
    private ReportGenerator reportGenerator;

    @Mock
    private ReconciliationServiceClient reconciliationServiceClient;

    @Mock
    private ExcelServiceImpl excelServiceImpl;

    @Mock
    private EmailService emailService;

    @Mock
    private AmazonS3Service amazonS3Service;

    @Test
    void shouldNotGenerateSettlementReportWhenExceptionOccurred() {
        when(reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(eq(RECON_BATCH_ID))).thenThrow(new NullPointerException());
        RiderSettlementBatchInfo settlementInfo = new RiderSettlementBatchInfo();
        settlementInfo.setBatchRef(BATCH_ID);
        settlementInfo.setReconcileBatchId(RECON_BATCH_ID);
        reportGenerator.generateSettlementReport(settlementInfo);
        verifyZeroInteractions(excelServiceImpl, emailService, amazonS3Service);
    }

    @Test
    void shouldNotGenerateSettlementReportWhenNoMatchedJobs() {
        when(reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(eq(RECON_BATCH_ID))).thenReturn(new ArrayList<>());
        RiderSettlementBatchInfo settlementInfo = new RiderSettlementBatchInfo();
        settlementInfo.setBatchRef(BATCH_ID);
        settlementInfo.setReconcileBatchId(RECON_BATCH_ID);
        reportGenerator.generateSettlementReport(settlementInfo);
        verifyZeroInteractions(excelServiceImpl, emailService, amazonS3Service);
    }

    @Test
    void shouldSendMailButNotUploadSettlementReport() throws IOException, MessagingException {
        FinalPaymentReconciliationDetails paymentDetails = new FinalPaymentReconciliationDetails();
        List<FinalPaymentReconciliationDetails> matchedJobs = Arrays.asList(paymentDetails);
        when(reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(eq(RECON_BATCH_ID))).thenReturn(matchedJobs);
        when(excelServiceImpl.createSettlementReport(anyList())).thenReturn(new byte[1]);
        when(amazonS3Service.uploadInputStream(any(InputStream.class), eq(SETTLEMENT_REPORTS), eq(BATCH_ID), anyString())).thenThrow(new NullPointerException());
        RiderSettlementBatchInfo settlementInfo = new RiderSettlementBatchInfo();
        settlementInfo.setBatchRef(BATCH_ID);
        settlementInfo.setReconcileBatchId(RECON_BATCH_ID);
        reportGenerator.generateSettlementReport(settlementInfo);
        verify(emailService, times(INVOKED_ONCE)).sendMailWithAttachment(anyString(), any(byte[].class), eq(SETTLEMENT_REPORT_SUBJECT), eq(SETTLEMENT_REPORT_TEXT));
    }

    @Test
    void shouldSendMailAndUploadSettlementReport() throws IOException, MessagingException {
        FinalPaymentReconciliationDetails paymentDetails = new FinalPaymentReconciliationDetails();
        List<FinalPaymentReconciliationDetails> matchedJobs = Arrays.asList(paymentDetails);
        when(reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(eq(RECON_BATCH_ID))).thenReturn(matchedJobs);
        when(excelServiceImpl.createSettlementReport(anyList())).thenReturn(new byte[1]);
        RiderSettlementBatchInfo settlementInfo = new RiderSettlementBatchInfo();
        settlementInfo.setBatchRef(BATCH_ID);
        settlementInfo.setReconcileBatchId(RECON_BATCH_ID);
        reportGenerator.generateSettlementReport(settlementInfo);
        verify(amazonS3Service, times(INVOKED_ONCE)).uploadInputStream(any(InputStream.class), eq(SETTLEMENT_REPORTS), eq(BATCH_ID), anyString());
        verify(emailService, times(INVOKED_ONCE)).sendMailWithAttachment(anyString(), any(byte[].class), eq(SETTLEMENT_REPORT_SUBJECT), eq(SETTLEMENT_REPORT_TEXT));
    }

    @Test
    void shouldNotGeneratePocketReportWhenNoMatchedJobs() {
        when(reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(eq(RECON_BATCH_ID))).thenReturn(new ArrayList<>());
        RiderSettlementBatchInfo settlementInfo = new RiderSettlementBatchInfo();
        settlementInfo.setBatchRef(BATCH_ID);
        settlementInfo.setReconcileBatchId(RECON_BATCH_ID);
        reportGenerator.generatePocketReport(settlementInfo);
        verifyZeroInteractions(excelServiceImpl, emailService, amazonS3Service);
    }

    @Test
    void shouldNotGeneratePocketReportWhenExceptionOccurred() {
        when(reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(eq(RECON_BATCH_ID))).thenThrow(new NullPointerException());
        RiderSettlementBatchInfo settlementInfo = new RiderSettlementBatchInfo();
        settlementInfo.setBatchRef(BATCH_ID);
        settlementInfo.setReconcileBatchId(RECON_BATCH_ID);
        reportGenerator.generatePocketReport(settlementInfo);
        verifyZeroInteractions(excelServiceImpl, emailService, amazonS3Service);
    }

    @Test
    void shouldSendMailAndUploadPocketReport() throws IOException, MessagingException {
        FinalPaymentReconciliationDetails paymentDetails = new FinalPaymentReconciliationDetails();
        List<FinalPaymentReconciliationDetails> matchedJobs = Arrays.asList(paymentDetails);
        when(reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(eq(RECON_BATCH_ID))).thenReturn(matchedJobs);
        when(excelServiceImpl.createPocketBalanceReport(eq(BATCH_ID), anyList())).thenReturn(new byte[1]);
        RiderSettlementBatchInfo settlementInfo = new RiderSettlementBatchInfo();
        settlementInfo.setBatchRef(BATCH_ID);
        settlementInfo.setReconcileBatchId(RECON_BATCH_ID);
        reportGenerator.generatePocketReport(settlementInfo);
        verify(amazonS3Service, times(INVOKED_ONCE)).uploadInputStream(any(InputStream.class), eq(SETTLEMENT_REPORTS), eq(BATCH_ID), anyString());
        verify(emailService, times(INVOKED_ONCE)).sendMailWithAttachment(anyString(), any(byte[].class), eq(POCKET_REPORT_SUBJECT), eq(POCKET_REPORT_TEXT));
    }
}
