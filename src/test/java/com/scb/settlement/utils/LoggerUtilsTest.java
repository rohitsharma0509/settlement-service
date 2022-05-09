package com.scb.settlement.utils;

import com.scb.settlement.constants.ErrorConstants;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class LoggerUtilsTest {

    @Test
    public void logError() {
        String key = ErrorConstants.DATA_NOT_FOUND_EX_MSG;
        LoggerUtils.logError(RiderSettlementBatchDetails.class, "batchRef", "batchRef");
    }
}