package com.scb.settlement.model.dto;

import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class BatchInfoFilterDto {

  private Integer totalPages;

  private Long totalCount;
  
  private Integer currentPage;

  private List<RiderSettlementBatchInfo> batchInfo;


  public static BatchInfoFilterDto of(List<RiderSettlementBatchInfo> jobs, int totalPages, long l, int currentPageNumber) {
    return BatchInfoFilterDto.builder().batchInfo(jobs)
        .totalPages(totalPages).totalCount(l).currentPage(currentPageNumber).build();
    
  }
}
