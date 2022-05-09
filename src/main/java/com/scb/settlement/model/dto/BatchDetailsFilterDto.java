package com.scb.settlement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class BatchDetailsFilterDto {

  private Integer totalPages;

  private Long totalCount;
  
  private Integer currentPage;

  private List<RiderSettlementBatchResponse> batchInfo;


  public static BatchDetailsFilterDto of(List<RiderSettlementBatchResponse> jobs, int totalPages, long l, int currentPageNumber) {
    return BatchDetailsFilterDto.builder().batchInfo(jobs)
        .totalPages(totalPages).totalCount(l).currentPage(currentPageNumber).build();
    
  }
}
