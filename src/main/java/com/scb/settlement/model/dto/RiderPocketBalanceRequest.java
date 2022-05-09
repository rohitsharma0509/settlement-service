package com.scb.settlement.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class RiderPocketBalanceRequest {
    private List<String> riderIdList;

    public static RiderPocketBalanceRequest of(List<String> riderIds) {
        return RiderPocketBalanceRequest.builder()
                .riderIdList(riderIds)
                .build();
    }
}
