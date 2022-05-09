package com.scb.settlement.repository;

import com.scb.settlement.model.document.RiderPaymentDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RiderPaymentDetailsRepository extends MongoRepository<RiderPaymentDetails, String>, RiderPaymentDetailsCustomRepository {

    void deleteAllByBatchRef(String batchRef);

    List<RiderPaymentDetails> findByBatchRef(String batchRef);

}
