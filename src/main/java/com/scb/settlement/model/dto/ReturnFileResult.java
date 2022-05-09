package com.scb.settlement.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class ReturnFileResult {
    private ReturnHeader header;
    private List<ReturnPaymentResult> paymentResults;
    private ReturnTrailer trailer;
}