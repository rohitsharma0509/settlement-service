package com.scb.settlement.repository.impl;

import com.scb.settlement.model.document.RiderPaymentDetails;
import com.scb.settlement.model.dto.RiderPaymentDetailsSearchResponse;
import com.scb.settlement.model.dto.SearchResponseDto;
import com.scb.settlement.repository.RiderPaymentDetailsCustomRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RiderPaymentDetailsCustomRepositoryImpl implements RiderPaymentDetailsCustomRepository {

    private static final String INCLUSIVE = "i";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public SearchResponseDto searchRiderPaymentDetails(String riderId, String accountNumber, String paymentDate, String status
            , String remarks, String netAmount, String pocketBalance, String securityAmount, String otherDeduction, String otherPayments
            , String batchRef, String excessiveWaitAmount, Pageable pageable) {
        final Query query = new Query();
        final List<Criteria> andCriterias = new ArrayList<>();

        if (StringUtils.isNotBlank(riderId)) {
            andCriterias.add(Criteria.where("riderId").is(riderId));
        }
        if (StringUtils.isNotBlank(accountNumber)) {
            andCriterias.add(Criteria.where("beneficiaryAccount").regex(".*" + accountNumber + ".*", "i"));
        }
        if (StringUtils.isNotBlank(paymentDate)) {
            andCriterias.add(Criteria.where("dateSearch").regex(".*" + paymentDate + ".*", "i"));
        }
        if (StringUtils.isNotBlank(status)) {
            andCriterias.add(Criteria.where("processingStatus").regex(".*" + status + ".*", "i"));
        }
        if (StringUtils.isNotBlank(remarks)) {
            andCriterias.add(Criteria.where("processingRemarks").regex(".*" + remarks + ".*", "i"));
        }
        if (StringUtils.isNotBlank(netAmount)) {
            andCriterias.add(Criteria.where("netPaymentAmountSearch").regex("^" + netAmount + ".*", "i"));
        }
        if (StringUtils.isNotBlank(pocketBalance)) {
            andCriterias.add(Criteria.where("pocketBalanceSearch").regex("^" + pocketBalance + ".*", "i"));
        }
        if (StringUtils.isNotBlank(securityAmount)) {
            andCriterias.add(Criteria.where("securityAmountDeductedSearch").regex("^" + securityAmount + ".*", "i"));
        }
        if (StringUtils.isNotBlank(otherDeduction)) {
            andCriterias.add(Criteria.where("otherDeductionsSearch").regex("^" + otherDeduction + ".*", "i"));
        }
        if (StringUtils.isNotBlank(otherPayments)) {
            andCriterias.add(Criteria.where("otherPaymentsSearch").regex("^" + otherPayments + ".*", "i"));
        }
        if (StringUtils.isNotBlank(batchRef)) {
            andCriterias.add(Criteria.where("batchRef").regex("^" + batchRef + ".*", "i"));
        }
        if (StringUtils.isNotBlank(excessiveWaitAmount)) {
            andCriterias.add(Criteria.where("netExcessiveWaitAmountSearch").regex("^" + excessiveWaitAmount + ".*", "i"));
        }
        Criteria finalCriteria = new Criteria().andOperator(andCriterias.toArray(new Criteria[andCriterias.size()]));
        query.addCriteria(finalCriteria);
        long totalRecords = mongoTemplate.count(query, RiderPaymentDetails.class);
        if(totalRecords > 0) {
            List<RiderPaymentDetails> listOfRiderPaymentDetails = mongoTemplate.find(query.with(pageable), RiderPaymentDetails.class);

            if (CollectionUtils.isEmpty(listOfRiderPaymentDetails)) {
                return SearchResponseDto.of(Collections.emptyList(), 0, 0, 0);
            } else {
                int totalPages = ((int) totalRecords / pageable.getPageSize()) + (totalRecords % pageable.getPageSize() == 0 ? 0 : 1);
                List<RiderPaymentDetailsSearchResponse> searchResponseList = RiderPaymentDetailsSearchResponse.toSearchResponse(listOfRiderPaymentDetails);
                return SearchResponseDto.of(searchResponseList, totalPages, totalRecords, pageable.getPageNumber() + 1);
            }
        } else {
            return SearchResponseDto.of(Collections.emptyList(), 0, 0, 0);
        }
    }
}
