package com.scb.settlement.repository;

import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.enumeration.SettlementBatchStatus;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiderSettlementBatchInfoRepository extends MongoRepository<RiderSettlementBatchInfo, String> {
    Optional<RiderSettlementBatchInfo> findByBatchRef(String batchRef);
    Page<RiderSettlementBatchInfo> findByBatchStatus(SettlementBatchStatus batchStatus, Pageable pageable);
    Optional<RiderSettlementBatchInfo> findByReconcileBatchId(String reconciliationBatchId);
    
    @Query("{'$and':[ {'_id': {$in:?0}}] }")
	Page<RiderSettlementBatchInfo> findAllById(List<ObjectId> listname, Pageable pageable);

	@Aggregation(pipeline = {
			"{$addFields: { dateSearch: { $ifNull: "
					+ "['$dateSearch', ''] }, "
					+ "startTimeSearch: { $ifNull: ['$startTimeSearch', ''] }, "
					+ "endTimeSearch: { $ifNull: ['$endTimeSearch', "
					+ "''] },failureReason: {$ifNull: [ '$failureReason', '']}}}",
			"{$match: { $and: [{ 'dateSearch': { $regex: /.*?2.*/, $options: 'i' }}, "
					+ "{'startTimeSearch': { $regex: /.*?5.*/, $options: 'i' }}, "
					+ "{'endTimeSearch': { $regex: /.*?6.*/, $options: 'i' } }, "
					+ "{'fileName': { $regex: /.*?1.*/, $options: 'i'}}, "
					+ "{'batchRef': { $regex: /.*?0.*/, $options: 'i'}}, "
					+ "{'failureReason': { $regex: /.*?3.*/, $options: 'i'}}, "
					+ "{'batchStatus': { $regex: /.*?4.*/, $options: 'i'}} ] }}" })
	AggregationResults<RiderSettlementBatchInfo> getBatchInfoAndQueryOnFields(String batchRef, String fileName,
			String dateOfRun, String reason, String batchStatus, String startTime, String endTime);

	Optional<List<RiderSettlementBatchInfo>> findByCreatedDateBetween(DateTime today, DateTime tomorrow);
}