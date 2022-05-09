package com.scb.settlement.service.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.scb.settlement.model.dto.BatchDetailsFilterDto;
import com.scb.settlement.model.dto.BatchInfoFilterDto;
import com.scb.settlement.model.dto.RiderSettlementBatchResponse;
import com.scb.settlement.model.dto.SearchResponseDto;
import com.scb.settlement.repository.RiderPaymentDetailsRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.scb.settlement.constants.SearchConstants;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.repository.RiderSettlementBatchDetailsRepository;
import com.scb.settlement.repository.RiderSettlementBatchInfoRepository;
import com.scb.settlement.utils.CommonUtils;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class SettlementSearchService {

	@Autowired
	private RiderSettlementBatchInfoRepository riderSettlementBatchInfoRepository;

	@Autowired
	private RiderSettlementBatchDetailsRepository riderSettlementBatchDetailsRepository;

	@Autowired
	private RiderPaymentDetailsRepository riderPaymentDetailsRepository;

	@Autowired
	public MongoTemplate mongoTemplate;

	public BatchInfoFilterDto getSettlementBatchInfoBySearchTermWithFilterQuery(List<String> filterquery,
			Pageable pageable) {

		List<Sort.Order> orders = CommonUtils.getSortedOrderList(pageable, SearchConstants.BATCH_REF);
		pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));

		Page<RiderSettlementBatchInfo> pageRiderProfile = null;

		log.info("Search By Filter query for batch details", filterquery);
		Map<String, String> filtersQuery = new HashMap<>();
		if (!CollectionUtils.isEmpty(filterquery)) {
			filterquery.stream().forEach(filter -> {
				String[] filterValue = filter.split(":");
				if (filterValue.length >= 2) {
					if ((filterValue[0].equals(SearchConstants.START_TIME)
							|| filterValue[0].equals(SearchConstants.END_TIME)) && filterValue.length >= 3) {
						filtersQuery.put(filterValue[0], String.format("%s:%s", filterValue[1], filterValue[2]));
					} else {
						filtersQuery.put(filterValue[0], filterValue[1]);
					}
				}
			});

			pageRiderProfile = getBatchInfoByStatusAndColumnLevelQuery(filtersQuery, pageable);
		} else {
			pageRiderProfile = riderSettlementBatchInfoRepository.findAll(pageable);

		}

		if (ObjectUtils.isEmpty(pageRiderProfile) || !pageRiderProfile.hasContent()) {
			List<RiderSettlementBatchInfo> jobList = new ArrayList<>();
			return BatchInfoFilterDto.of(jobList, 0, 0, 0);
		}

		return BatchInfoFilterDto.of(pageRiderProfile.getContent(), pageRiderProfile.getTotalPages(),
				pageRiderProfile.getTotalElements(), pageRiderProfile.getNumber() + 1);

	}

	private Page<RiderSettlementBatchInfo> getBatchInfoByStatusAndColumnLevelQuery(Map<String, String> filtersQuery,
			Pageable pageable) {

		String batchRef = extractValue(filtersQuery, SearchConstants.BATCH_REF);
		String fileName = Pattern.quote(extractValue(filtersQuery, SearchConstants.FILE_NAME));
		String dateOfRun = extractValue(filtersQuery, SearchConstants.DATE_OF_RUN);
		String reason = Pattern.quote(extractValue(filtersQuery, SearchConstants.FAILURE_REASON));
		String status = extractValue(filtersQuery, SearchConstants.STATUS);
		String startTime = extractValue(filtersQuery, SearchConstants.START_TIME);
		String endTime = extractValue(filtersQuery, SearchConstants.END_TIME);

		AggregationResults<RiderSettlementBatchInfo> filterData = riderSettlementBatchInfoRepository
				.getBatchInfoAndQueryOnFields(batchRef, fileName, dateOfRun, reason, status, startTime, endTime);

		List<ObjectId> ids = new LinkedList<>();
		filterData.getMappedResults().stream().forEach(data -> ids.add(new ObjectId(data.getId())));
		return riderSettlementBatchInfoRepository.findAllById(ids, pageable);
	}

	private String extractValue(Map<String, String> filtersQuery, String key) {
		return filtersQuery.getOrDefault(key.trim(), SearchConstants.EMPTY_STRING);
	}

	public BatchDetailsFilterDto getSettlementBatchDetailsBySearchTermWithFilterQuery(String batchRef,
			List<String> filterquery, Pageable pageable) {
		Page<RiderSettlementBatchDetails> pageRiderProfile = null;

		List<Sort.Order> orders = CommonUtils.getSortedOrderList(pageable, SearchConstants.BATCH_REF);
		pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));

		log.info("Search By Filter query", filterquery);
		Map<String, String> filtersQuery = new HashMap<>();
		if (!CollectionUtils.isEmpty(filterquery)) {
			filterquery.stream().forEach(filter -> {
				String[] filterValue = filter.split(":");
				if (filterValue.length >= 2) {
					filtersQuery.put(filterValue[0], filterValue[1]);
				}
			});

			pageRiderProfile = getBatchDetailsByStatusAndColumnLevelQuery(batchRef, filtersQuery, pageable);
		} else {
			Page<RiderSettlementBatchDetails> data = riderSettlementBatchDetailsRepository.findByBatchRef(batchRef,
					pageable);
			List<RiderSettlementBatchResponse> responses = RiderSettlementBatchResponse.of(data.getContent());

			return BatchDetailsFilterDto.of(responses, data.getTotalPages(), data.getTotalElements(),
					data.getNumber() + 1);

		}

		if (ObjectUtils.isEmpty(pageRiderProfile) || !pageRiderProfile.hasContent()) {
			List<RiderSettlementBatchResponse> jobList = new ArrayList<>();
			return BatchDetailsFilterDto.of(jobList, 0, 0, 0);
		}

		List<RiderSettlementBatchResponse> responses = RiderSettlementBatchResponse.of(pageRiderProfile.getContent());

		return BatchDetailsFilterDto.of(responses, pageRiderProfile.getTotalPages(),
				pageRiderProfile.getTotalElements(), pageRiderProfile.getNumber() + 1);

	}

	private Page<RiderSettlementBatchDetails> getBatchDetailsByStatusAndColumnLevelQuery(String batchRef,
			Map<String, String> filtersQuery, Pageable pageable) {

		String riderId = extractValue(filtersQuery, SearchConstants.RIDER_ID);
		String riderName = extractValue(filtersQuery, SearchConstants.RIDER_NAME);
		String accountNumber = extractValue(filtersQuery, SearchConstants.ACCOUNT_NUMBER);
		String amount = extractValue(filtersQuery, SearchConstants.TRANSFER_AMOUNT);
		String date = extractValue(filtersQuery, SearchConstants.DETAILS_DATE);
		String status = extractValue(filtersQuery, SearchConstants.PAYMENT_STATUS);
		String remarks = extractValue(filtersQuery, SearchConstants.REMARKS);

		AggregationResults<RiderSettlementBatchDetails> filterData = riderSettlementBatchDetailsRepository
				.getAllBatchInfoAndQueryOnFields(riderId, riderName, accountNumber, amount, date, status, remarks,
						batchRef);

		List<ObjectId> ids = new LinkedList<>();
		filterData.getMappedResults().stream().forEach(data -> ids.add(new ObjectId(data.getId())));

		return riderSettlementBatchDetailsRepository.findAllById(ids, pageable);

	}

	public SearchResponseDto findRiderSettlementDetailsByRiderId(Pageable pageable, String riderId,
																 List<String> filterQuery) {
		log.info("Search By Filter query", filterQuery);
		Map<String, String> filtersQuery = new HashMap<>();

		if (!CollectionUtils.isEmpty(filterQuery)) {
			filterQuery.stream().forEach(filter -> {
				String[] filterValue = filter.split(":");
				if (filterValue.length >= 2) {
					if ((filterValue[0].equals(SearchConstants.DETAILS_DATE) && filterValue.length >= 4)) {
						filtersQuery.put(filterValue[0], String.format("%s:%s:%s", filterValue[1], filterValue[2], filterValue[3]));
					} else {
						filtersQuery.put(filterValue[0], filterValue[1]);
					}
				}
			});
		}
		String accountNumber = extractValue(filtersQuery, SearchConstants.ACCOUNT_NUMBER);
		String date = extractValue(filtersQuery, SearchConstants.DETAILS_DATE);
		String amount = extractValue(filtersQuery, SearchConstants.TRANSFER_AMOUNT);
		String processingStatus = extractValue(filtersQuery, SearchConstants.PAYMENT_STATUS);
		String remarks = extractValue(filtersQuery, SearchConstants.REMARKS);
		String pocketBalance = extractValue(filtersQuery, SearchConstants.POCKET_BALANCE);
		String securityAmount = extractValue(filtersQuery, SearchConstants.SECURITY_AMOUNT);
		String otherDeduction = extractValue(filtersQuery, SearchConstants.OTHER_DEDUCTION);
		String otherPayments = extractValue(filtersQuery, SearchConstants.OTHER_PAYMENTS);
		String batchRef = extractValue(filtersQuery, SearchConstants.BATCH_REF);
		String excessiveWaitAmount = extractValue(filtersQuery, SearchConstants.EXCESS_WAIT_TIME);

		return riderPaymentDetailsRepository.searchRiderPaymentDetails(riderId, accountNumber, date, processingStatus, remarks
				, amount, pocketBalance, securityAmount, otherDeduction, otherPayments, batchRef,excessiveWaitAmount, pageable);
	}
}
