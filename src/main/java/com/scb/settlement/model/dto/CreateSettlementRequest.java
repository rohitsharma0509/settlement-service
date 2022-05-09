package com.scb.settlement.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CreateSettlementRequest {
    private String reconBatchId;
    private String reconFileName;
    private String pointxReconBatchId;
    private String pointxReconFileName;
}
