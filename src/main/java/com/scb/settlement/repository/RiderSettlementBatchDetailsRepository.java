package com.scb.settlement.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.scb.settlement.model.document.RiderSettlementBatchDetails;

@Repository
public interface RiderSettlementBatchDetailsRepository extends MongoRepository<RiderSettlementBatchDetails, String> {
    List<RiderSettlementBatchDetails> findByBatchRefAndProcessingStatusNot(String batchRef, String processingStatus);
    Page<RiderSettlementBatchDetails> findByBatchRef(String batchRef, Pageable paging);
    List<RiderSettlementBatchDetails> findAllByCustomerReferenceNumberAndBatchRefAndProcessingStatus(String riderId, String batchRef, String processingStatus);
    List<RiderSettlementBatchDetails> findAllByCustomerReferenceNumberAndTransferDateBetweenAndProcessingStatusNot(String riderId, LocalDateTime startDate, LocalDateTime endDate, String processingStatus);

    @Aggregation(pipeline = {"{ $addFields: { dateSearch: { $ifNull: ['$dateSearch', ''] } } } ",
    		 "{ $match: {  $and:[{'batchRef': ?7, $and: [{ 'dateSearch': { $regex: /.*?4.*/, $options: 'i'}},"
    		 + "{'beneficiaryAccount': { $regex: /.*?2.*/, $options: 'i' }},"
    		 + "{'processingStatus': { $regex: /.*?5.*/, $options: 'i' }},"
    		 + "{'processingRemarks': { $regex: /.*?6.*/, $options: 'i' }},"
    		 + "{'customerReferenceNumber': { $regex: /.*?0.*/, $options: 'i' }},"
    		 + "{'beneficiaryName': { $regex: /.*?1.*/, $options: 'i' }},"
    		 + "{'netPaymentAmount': { $regex: /^?3.*/, $options: 'i' }} ]}] } }"})
   AggregationResults<RiderSettlementBatchDetails> getAllBatchInfoAndQueryOnFields(String riderId, String riderName,
			String accountNumber, String amount, String date, String status, String remarks,String batchRef);

    @Query("{$and:[{'customerReferenceNumber': ?0},{'$and':[{'beneficiaryName': { $regex: /.*?1.*/, $options: 'i' }},"
            + "{'beneficiaryAccount':{ $regex: /.*?2.*/, $options: 'i' }},"
            + "{'dateSearch': { $regex: /.*?3.*/, $options: 'i' }},"
            + "{'netPaymentAmount': { $regex: /^?4.*/, $options: 'i' }},"
            + "{'processingStatus': { $regex: /.*?5.*/, $options: 'i' }},"
            + "{'processingRemarks': { $regex: /.*?6.*/, $options: 'i' }}]}]}")
    Page<RiderSettlementBatchDetails> getAllRidersAndQueryOnFields(String riderId, String riderName,
                                                                   String accountNumber, String date, String amount, String processingStatus, String processingRemarks,  Pageable pageable);

    
    
    @Query("{'$and':[ {'_id': {$in:?0}}] }")
    Page<RiderSettlementBatchDetails> findAllById(List<ObjectId> ids, Pageable pageable);

    Page<RiderSettlementBatchDetails> findAllByCustomerReferenceNumber(String riderId, Pageable pageable);
    Optional<RiderSettlementBatchDetails> findByCustomerReferenceNumberAndBatchRefAndProcessingStatus(String customerReferenceNumber, String batchRef, String processingStatus);
    List<RiderSettlementBatchDetails> findAllByCustomerReferenceNumberAndProcessingStatusNot(String riderId, String processingStatus);
    void deleteAllByBatchRefAndProcessingStatus(String batchRef, String processingStatus);
    List<RiderSettlementBatchDetails> findByCustomerReferenceNumberAndTransferDateAfterAndProcessingStatusNot(String riderId, LocalDateTime date, String processingStatus);
    List<RiderSettlementBatchDetails> findByBatchRefAndCustomerReferenceNumberAndProcessingStatusNot(String batchRef, String riderId, String processingStatus);
    List<RiderSettlementBatchDetails> findByCustomerReferenceNumberInAndBatchRefAndProcessingStatus(List<String> riderIds, String batchRef, String processingStatus);
}