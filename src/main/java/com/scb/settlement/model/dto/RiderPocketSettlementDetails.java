package com.scb.settlement.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scb.settlement.model.document.RiderSettlementBatchDetails;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Builder
@Getter
@Setter
@ToString
public class RiderPocketSettlementDetails {
	private String batchRef;
	private String riderId;
	private String riderName;
	private String pocketBalanceTransferred;
	private String accountNumber;
	private String processingStatus;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="UTC")
	private Date paymentValueDate;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="UTC")
	private Date paymentPostingDate;

	public static List<RiderPocketSettlementDetails> of(List<RiderSettlementBatchDetails> riderSettlementDetails) {
		ArrayList<RiderPocketSettlementDetails> settlementPocketDetails = new ArrayList<>();
		for(RiderSettlementBatchDetails details : riderSettlementDetails){
			settlementPocketDetails.add(toRiderPocketSettlementDetails(details));
		}
		return settlementPocketDetails;
	}

	public static RiderPocketSettlementDetails toRiderPocketSettlementDetails(RiderSettlementBatchDetails details) {
		return RiderPocketSettlementDetails.builder()
				.batchRef(details.getBatchRef())
				.paymentPostingDate(Objects.nonNull(details.getPostDate()) ? Date.from(details.getPostDate().toInstant(ZoneOffset.UTC)) : null)
				.paymentValueDate(Objects.nonNull(details.getTransferDate()) ? Date.from(details.getTransferDate().toInstant(ZoneOffset.UTC)) : null)
				.accountNumber(details.getBeneficiaryAccount())
				.riderId(details.getCustomerReferenceNumber())
				.riderName(details.getBeneficiaryName())
				.processingStatus(details.getProcessingStatus())
				.pocketBalanceTransferred(details.getNetPaymentAmount()).build();
	}
	
}
