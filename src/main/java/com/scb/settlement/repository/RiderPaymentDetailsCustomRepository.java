package com.scb.settlement.repository;

import com.scb.settlement.model.dto.SearchResponseDto;
import org.springframework.data.domain.Pageable;

public interface RiderPaymentDetailsCustomRepository {
    SearchResponseDto searchRiderPaymentDetails(String riderId, String accountNumber, String date, String status
            , String remarks, String netAmount, String pocketBalance, String securityAmount, String otherDeduction, String otherPayments
            , String batchRef, String excessiveWaitAmount, Pageable pageable);
}
