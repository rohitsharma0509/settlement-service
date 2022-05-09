package com.scb.settlement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class RiderPocketModificationDto {
    private String riderId;
    private Double pocketBalance;
    private Double securityDeducted;
    private Double incentive;
    private Double excessiveWaitTimeAmount;
    private Double securityRecovered;
}
