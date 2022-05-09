package com.scb.settlement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
public class SearchResponseDto {
    private Integer totalPages;
    private Long totalCount;
    private Integer currentPage;
    private List<?> batchInfo;

    public static SearchResponseDto of(List<?> batchInfo, int totalPages, long totalCount, int currentPageNumber) {
        return SearchResponseDto.builder()
                .batchInfo(batchInfo).totalPages(totalPages)
                .totalCount(totalCount).currentPage(currentPageNumber)
                .build();

    }
}