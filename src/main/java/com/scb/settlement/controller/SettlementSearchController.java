package com.scb.settlement.controller;

import com.scb.settlement.constants.SearchConstants;
import com.scb.settlement.model.dto.BatchDetailsFilterDto;
import com.scb.settlement.model.dto.BatchInfoFilterDto;
import com.scb.settlement.model.dto.SearchResponseDto;
import com.scb.settlement.service.document.SettlementSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api/settlement/search")
@Api(value = "settlement Search Endpoints")
public class SettlementSearchController {

  @Autowired
  private SettlementSearchService settlementSearchService;
  
  @GetMapping
  public ResponseEntity<BatchInfoFilterDto> getBatchInfoSearchTerm(
   
      @ApiParam(value = "filterquery", example = "batchRef:000",
          required = false) @RequestParam(name = "filterquery", required = false)
      List<String> filterquery,
      @PageableDefault(page = 0, size = 5) @SortDefault.SortDefaults(@SortDefault(sort = "batchRef",
          direction = Sort.Direction.ASC)) Pageable pageable) {
    if(!ObjectUtils.isEmpty(filterquery)) {
      filterquery.forEach(log::info);
    }
    return ResponseEntity.ok(this.settlementSearchService
    		.getSettlementBatchInfoBySearchTermWithFilterQuery(filterquery, pageable ));
  }

  @GetMapping("/batch/details/{batchRef}")
  public ResponseEntity<BatchDetailsFilterDto> getBatchDetailsSearchTerm(
		  @PathVariable("batchRef") String batchRef,
      @ApiParam(value = "filterquery", example = "batchRef:000",
          required = false) @RequestParam(name = "filterquery", required = false)
      List<String> filterquery,
      @PageableDefault(page = 0, size = 5) @SortDefault.SortDefaults(@SortDefault(sort = "riderId",
          direction = Sort.Direction.ASC)) Pageable pageable) {
    if(!ObjectUtils.isEmpty(filterquery)) {
      filterquery.forEach(log::info);
    }
    
    return ResponseEntity.ok(this.settlementSearchService
    		.getSettlementBatchDetailsBySearchTermWithFilterQuery(batchRef,filterquery, pageable ));
  }

  @GetMapping("/details/{riderId}")
  public ResponseEntity<SearchResponseDto> searchRiderPaymentDetails(
          @PathVariable(name = "riderId") String riderId,
          @RequestParam(name = "filterQuery", required = false) List<String> filterQuery,
          @PageableDefault(page = 0, size = 50) @SortDefault.SortDefaults(@SortDefault(sort = SearchConstants.DETAILS_DATE,
                  direction = Sort.Direction.DESC)) Pageable pageable) {
    log.info("Getting Rider Settlement Details by rider id = {}", riderId);
    return ResponseEntity.ok(settlementSearchService.findRiderSettlementDetailsByRiderId(pageable, riderId, filterQuery));
  }
}
