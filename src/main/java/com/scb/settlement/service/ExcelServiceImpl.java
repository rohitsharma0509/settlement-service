package com.scb.settlement.service;

import com.scb.settlement.client.impl.OperationServiceClient;
import com.scb.settlement.client.impl.PocketServiceClient;
import com.scb.settlement.constants.Constants;
import com.scb.settlement.constants.ExcelConstants;
import com.scb.settlement.exception.DataNotFoundException;
import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.model.dto.BatchConfigurationDto;
import com.scb.settlement.model.dto.FinalPaymentReconciliationDetails;
import com.scb.settlement.model.dto.RiderPocketBalanceResponse;
import com.scb.settlement.repository.RiderPaymentDetailsRepository;
import com.scb.settlement.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExcelServiceImpl extends AbstractExcelService {

    private static final DecimalFormat df = new DecimalFormat("0.00");

    private static final List<String> settlementRbhHeaders = Arrays.asList(ExcelConstants.ORDER_NO, ExcelConstants.TRANSACTION_TYPE, ExcelConstants.JOB_NO
            , ExcelConstants.RIDER_NAME, ExcelConstants.CHARGE_NO, ExcelConstants.ORDER_TIME
            , ExcelConstants.ORDER_COMPLETED_TIME, ExcelConstants.CUSTOMER_PAYS, ExcelConstants.PAYMENT_METHOD
            , ExcelConstants.RH_MDR, ExcelConstants.RH_VAT, ExcelConstants.NET, ExcelConstants.SETTLEMENT_TIME
    );

    private static final List<String> settlementRaHeaders = Arrays.asList(ExcelConstants.JOB_AMOUNT, ExcelConstants.RA_MDR, ExcelConstants.RA_VAT, ExcelConstants.RBH_AMOUNT_PAID
            , ExcelConstants.RBH_AMOUNT_ABSORB, ExcelConstants.ACCOUNT, ExcelConstants.SECURITY_DEDUCTED, ExcelConstants.REMARK
    );

    private static final List<String> pocketReportHeaders = Arrays.asList(ExcelConstants.RIDER_ID, ExcelConstants.RIDER_NAME_TH, ExcelConstants.ACCOUNT_NUMBER
            , ExcelConstants.TRANSFER_MONEY, ExcelConstants.PAY_DAY, ExcelConstants.PAYMENT_STATUS
            , ExcelConstants.PAYMENT_NOTE, ExcelConstants.BEFORE_UPDATE_POCKET, ExcelConstants.AFTER_UPDATE_POCKET
    );

    @Autowired
    private OperationServiceClient operationServiceClient;

    @Autowired
    private PocketServiceClient pocketServiceClient;

    @Autowired
    private RiderPaymentDetailsRepository riderPaymentDetailsRepository;

    public byte[] createSettlementReport(List<FinalPaymentReconciliationDetails> matchedJobs) throws IOException {
        log.info("generating settlement report");
        try (Workbook workbook = new SXSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(ExcelConstants.SETTLEMENT_REPORT);
            sheet.setDefaultColumnWidth(20);
            sheet.createFreezePane(0, 2);
            CellStyle headerStyle1 = getCellStyle(workbook, IndexedColors.GREY_25_PERCENT, IndexedColors.BLACK, Boolean.TRUE, Boolean.TRUE);
            CellStyle headerStyle2 = getCellStyle(workbook, IndexedColors.YELLOW, IndexedColors.BLACK, Boolean.TRUE, Boolean.TRUE);
            CellStyle configStyle = getCellStyle(workbook, IndexedColors.WHITE, IndexedColors.RED, Boolean.FALSE, Boolean.TRUE);
            CellStyle dataStyle = getCellStyle(workbook, IndexedColors.WHITE, IndexedColors.BLACK, Boolean.FALSE, Boolean.TRUE);
            CellStyle footerStyle = getCellStyle(workbook, IndexedColors.WHITE, IndexedColors.BLACK);

            int rowCount = 0;
            Row row = sheet.createRow(rowCount);
            createMergedCell(sheet, row, 0, ExcelConstants.FROM_POST_ORDER, new CellRangeAddress(rowCount, rowCount, 0, 12), headerStyle1);
            createMergedCell(sheet, row, 13, ExcelConstants.FROM_RIDER_APP, new CellRangeAddress(rowCount, rowCount, 13, 20), headerStyle2);
            rowCount++;

            row = sheet.createRow(rowCount);
            row.setHeight((short)1000);
            createCellsWith2Styles(row, settlementRbhHeaders, settlementRaHeaders, headerStyle1, headerStyle2);
            createCell(row, 23, ExcelConstants.CONFIG_MDR, configStyle);
            createCell(row, 24, ExcelConstants.CONFIG_VAT, configStyle);
            rowCount++;

            double totalRhJobAmount = 0.0;
            double totalRhMdrValue = 0.0;
            double totalRhVatValue = 0.0;
            double totalRhPaymentAmount = 0.0;
            double totalRaJobAmount = 0.0;
            double totalRaMdrValue = 0.0;
            double totalRaVatValue = 0.0;
            double totalRaPaymentAmount = 0.0;
            double totalRbhAbsorbValue = 0.0;

            for (FinalPaymentReconciliationDetails riderJob : matchedJobs) {
                totalRhJobAmount = CommonUtils.sum(totalRhJobAmount, riderJob.getRhJobAmount());
                totalRhMdrValue = CommonUtils.sum(totalRhMdrValue, riderJob.getRhMdrValue());
                totalRhVatValue = CommonUtils.sum(totalRhVatValue, riderJob.getRhVatOnMdr());
                totalRhPaymentAmount = CommonUtils.sum(totalRhPaymentAmount, riderJob.getRhPaymentAmount());
                totalRaJobAmount = CommonUtils.sum(totalRaJobAmount, riderJob.getRaJobAmount());
                totalRaMdrValue = CommonUtils.sum(totalRaMdrValue, riderJob.getMdrValue());
                totalRaVatValue = CommonUtils.sum(totalRaVatValue, riderJob.getVatValue());
                totalRaPaymentAmount = CommonUtils.sum(totalRaPaymentAmount, riderJob.getRaPaymentAmount());
                Double rbhAbsorbValue = CommonUtils.subtract(riderJob.getRaPaymentAmount(), riderJob.getRhJobAmount());
                totalRbhAbsorbValue = Double.sum(totalRbhAbsorbValue, rbhAbsorbValue);

                row = sheet.createRow(rowCount);
                List<String> values = Arrays.asList(
                        riderJob.getRhOrderNumber()
                        , riderJob.getRhTransactionType()
                        , riderJob.getRhJobNumber()
                        , riderJob.getRhRiderName()
                        , riderJob.getRhChargeNo()
                        , riderJob.getRhOrderTime()
                        , riderJob.getRhOrderCompletedTime()
                        , riderJob.getRhJobAmount()
                        , riderJob.getRhPaymentMethod()
                        , riderJob.getRhMdrValue()
                        , riderJob.getRhVatOnMdr()
                        , riderJob.getRhPaymentAmount()
                        , riderJob.getRhSettlementTime()
                        , riderJob.getRaJobAmount()
                        , riderJob.getMdrValue()
                        , riderJob.getVatValue()
                        , riderJob.getRaPaymentAmount()
                        , df.format(rbhAbsorbValue)
                        , riderJob.getAccountNumber()
                        , "0.00"
                        , StringUtils.EMPTY
                );
                createCells(row, values, dataStyle);
                if(rowCount == 2) {
                    BatchConfigurationDto config = operationServiceClient.getBatchConfiguration();
                    createCell(row, 23, config.getMdrPercentage(), configStyle);
                    createCell(row, 24, config.getVatPercentage(), configStyle);
                }

                rowCount++;
            }

            List<String> values = Arrays.asList(
                    StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY
                    , StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY
                    , df.format(totalRhJobAmount)
                    , StringUtils.EMPTY
                    , df.format(totalRhMdrValue)
                    , df.format(totalRhVatValue)
                    , df.format(totalRhPaymentAmount)
                    , StringUtils.EMPTY
                    , df.format(totalRaJobAmount)
                    , df.format(totalRaMdrValue)
                    , df.format(totalRaVatValue)
                    , df.format(totalRaPaymentAmount)
                    , df.format(totalRbhAbsorbValue)
                    , StringUtils.EMPTY
                    , "0.00"
                    , StringUtils.EMPTY
            );
            createRow(sheet, rowCount, values, footerStyle);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            bos.flush();
            bos.close();
            log.info("byte array size: {}", bos.size());
            return bos.toByteArray();
        }
    }

    public byte[] createPocketBalanceReport(String settlementBatchId, List<FinalPaymentReconciliationDetails> matchedJobs) throws IOException {
        log.info("generating pocket balance report for settlement batch {}", settlementBatchId);
        try (Workbook workbook = new SXSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(settlementBatchId);
            sheet.setDefaultColumnWidth(20);
            CellStyle headerStyle = getCellStyle(workbook, IndexedColors.GREY_25_PERCENT, IndexedColors.BLACK, Boolean.TRUE, Boolean.TRUE);
            CellStyle dataStyle = getCellStyle(workbook, IndexedColors.WHITE, IndexedColors.BLACK, Boolean.FALSE, Boolean.TRUE);

            int rowCount = 0;
            createRow(sheet, rowCount, pocketReportHeaders, headerStyle);
            rowCount++;

            List<String> riderIds = matchedJobs.stream().map(job -> job.getRaRiderId()).distinct().collect(Collectors.toList());
            LocalDate reconDate = Objects.isNull(matchedJobs.get(0)) || Objects.isNull(matchedJobs.get(0).getCreatedDate())
                    ? LocalDate.now() : matchedJobs.get(0).getCreatedDate();
            log.info("No. of riders {}, No. of jobs {} from recon {}, reconDate {}", riderIds.size(), matchedJobs.size(), reconDate);

            List<RiderPocketBalanceResponse> listOfPocketBalance = getRiderPocketBalances(reconDate, riderIds);
            List<RiderPaymentDetails> listOfRiderPaymentDetails = riderPaymentDetailsRepository.findByBatchRef(settlementBatchId);

            for (String riderId : riderIds) {
                double beforeUpdateBalance = getCutOffTimePocketBalanceByRiderId(listOfPocketBalance, riderId);
                Optional<RiderPaymentDetails> payDetails = listOfRiderPaymentDetails.stream().filter(Objects::nonNull)
                        .filter(paymentDetails -> riderId.equals(paymentDetails.getRiderId())).findFirst();
                if (!payDetails.isPresent()) {
                    log.info("payment details not found for riderId: {}", riderId);
                    throw new DataNotFoundException("payment details not found for riderId: " + riderId);
                }
                RiderPaymentDetails riderDetails = payDetails.get();

                double afterUpdateBalance = beforeUpdateBalance;
                if (Constants.SUCCESS.equals(riderDetails.getProcessingStatus())) {
                    afterUpdateBalance = CommonUtils.subtract(String.valueOf(beforeUpdateBalance), String.valueOf(riderDetails.getPocketBalance()));
                }
                log.debug("Rider: {}, beforeUpdateBalance: {}, afterUpdateBalance: {}, securityAmountDeducted: {}",
                        riderId, beforeUpdateBalance, afterUpdateBalance, riderDetails.getSecurityAmountDeducted());
                List<String> values = Arrays.asList(
                        riderId
                        , riderDetails.getBeneficiaryName()
                        , riderDetails.getBeneficiaryAccount()
                        , String.valueOf(riderDetails.getNetPaymentAmount())
                        , CommonUtils.getFormattedDateTime(riderDetails.getTransferDate(), Constants.TRANSFER_DATE_FORMAT)
                        , riderDetails.getProcessingStatus()
                        , riderDetails.getProcessingRemarks()
                        , df.format(beforeUpdateBalance)
                        , df.format(afterUpdateBalance)
                );
                createRow(sheet, rowCount, values, dataStyle);
                rowCount++;

                List<FinalPaymentReconciliationDetails> ridersJobs = matchedJobs.stream()
                        .filter(job -> Objects.nonNull(job) && StringUtils.isNotBlank(job.getRaRiderId()))
                        .filter(job -> job.getRaRiderId().equals(riderId)).collect(Collectors.toList());
                log.debug("Found {} jobs for rider {} in batch {}", ridersJobs.size(), riderId, settlementBatchId);

                for (FinalPaymentReconciliationDetails riderJob : ridersJobs) {
                    LocalDateTime jobEndDateTimeTh = CommonUtils.convertToThaiTime(riderJob.getJobEndDateTime());
                    values = Arrays.asList(
                            StringUtils.EMPTY
                            , riderJob.getRaJobNumber()
                            , riderJob.getRaOrderNumber()
                            , riderJob.getRaPaymentAmount()
                            , CommonUtils.getFormattedDateTime(jobEndDateTimeTh, Constants.JOB_DATETIME_FORMAT)
                            , StringUtils.EMPTY
                            , StringUtils.EMPTY
                            , StringUtils.EMPTY
                            , StringUtils.EMPTY
                    );
                    createRow(sheet, rowCount, values, dataStyle);
                    rowCount++;
                }
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            bos.flush();
            bos.close();
            log.info("byte array size: {}", bos.size());
            return bos.toByteArray();
        }
    }

    private List<RiderPocketBalanceResponse> getRiderPocketBalances(LocalDate reconDate, List<String> riderIds) {
        BatchConfigurationDto config = operationServiceClient.getBatchConfiguration();
        LocalTime cutOffTime = CommonUtils.cutOffTimeToLocalTime(config.getRhCutOffTime());
        LocalDateTime reconDateWithCutOffTime = LocalDateTime.of(reconDate, cutOffTime).atZone(ZoneId.of(Constants.BANGKOK_ZONE_ID)).toLocalDateTime();
        LocalDateTime cutOffUtcDateTime = CommonUtils.convertToUtc(reconDateWithCutOffTime);
        return pocketServiceClient.getPocketDetailByTimeInBatch(cutOffUtcDateTime, riderIds);
    }

    private double getCutOffTimePocketBalanceByRiderId(List<RiderPocketBalanceResponse> response, String riderId) {
        Optional<RiderPocketBalanceResponse> pocket = response.stream().filter(Objects::nonNull)
                .filter(riderPocket -> riderId.equals(riderPocket.getRiderId())).findFirst();
        RiderPocketBalanceResponse riderPocket = pocket.isPresent() ? pocket.get() : null;
        return Objects.nonNull(riderPocket) ? riderPocket.getPocketBalance() : 0.0;
    }
}
