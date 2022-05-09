package com.scb.settlement.events;

import com.scb.settlement.constants.Constants;
import com.scb.settlement.exception.DataNotFoundException;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.enumeration.SettlementBatchStatus;
import com.scb.settlement.service.ReportGenerator;
import com.scb.settlement.service.SequenceGeneratorService;
import com.scb.settlement.service.document.RiderSettlementService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class S1FileBatchRunEventListener extends AbstractMongoEventListener<RiderSettlementBatchInfo> {

    private static final int MAX_ATTEMPTS = 3;

    @Value("${s1.file.batch.db.sequence.prefix}")
    private String prefix;

    public static final String SEQUENCE_NAME = "s1_file_batch_sequence";

    @Autowired
    private SequenceGeneratorService sequenceGenerator;

    @Autowired
    private RiderSettlementService riderSettlementService;

    @Autowired
    private ReportGenerator reportGenerator;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<RiderSettlementBatchInfo> event) {
        if (event.getSource().getBatchRef() == null) {
            event.getSource().setBatchRef(prefix +
                    sequenceGenerator.generateSequence(SEQUENCE_NAME));
        }
    }

    @SneakyThrows
    @Override
    public void onAfterSave(AfterSaveEvent<RiderSettlementBatchInfo> event) {
        RiderSettlementBatchInfo settlementBatchInfo = event.getSource();

        if(Objects.isNull(settlementBatchInfo)) {
            return;
        }

        if(SettlementBatchStatus.READY_FOR_RUN.equals(settlementBatchInfo.getBatchStatus())) {
            log.info("event received to trigger settlement batch with batchId {}", settlementBatchInfo.getBatchRef());
            triggerSettlementBatchWithRetry(settlementBatchInfo.getBatchRef());
        } else if(SettlementBatchStatus.UPLOADED.equals(settlementBatchInfo.getBatchStatus())) {
            log.info("event received to trigger generate settlement report for batchId {}", settlementBatchInfo.getBatchRef());
            reportGenerator.generateSettlementReport(settlementBatchInfo);
        } else if(SettlementBatchStatus.SUCCESS.equals(settlementBatchInfo.getBatchStatus())) {
            log.info("event received to trigger generate pocket report for batchId {}", settlementBatchInfo.getBatchRef());
            reportGenerator.generatePocketReport(settlementBatchInfo);
        }
    }

    public void triggerSettlementBatchWithRetry(String settlementBatchId) throws InterruptedException {
        for (int count = 0; count < MAX_ATTEMPTS; count++) {
            try {
                riderSettlementService.triggerSettlementBatch(settlementBatchId, Constants.SYSTEM);
                break;
            } catch (DataNotFoundException e) {
                log.info("Data not found exception for settlementBatchId {}, retryCount: {}", settlementBatchId, count);
                log.error("Exception occurred while generating tax invoice", e);
                Thread.sleep(1000);
            }
        }
    }
}
