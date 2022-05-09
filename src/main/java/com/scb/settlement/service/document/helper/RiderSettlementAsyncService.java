package com.scb.settlement.service.document.helper;

import com.scb.settlement.model.document.RiderSettlementBatchInfo;

public interface RiderSettlementAsyncService {

    void triggerSettlementBatch(RiderSettlementBatchInfo batchInfo);
}
