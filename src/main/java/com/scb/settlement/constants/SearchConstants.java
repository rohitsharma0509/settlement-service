

package com.scb.settlement.constants;

import java.util.HashMap;
import java.util.Map;

public class SearchConstants {

    private SearchConstants() {}

	public static final Map<String, String> transferDetailsMap = new HashMap<>();

    public static final String EMPTY_STRING = "";

	public static final String BATCH_REF = "batchRef";

	public static final String FILE_NAME = "fileName";

	public static final String DATE_OF_RUN = "dateOfRun";

	public static final String FAILURE_REASON = "failureReason";
	public static final String START_TIME = "startTime";
	public static final String END_TIME = "endTime";
	public static final String STATUS = "batchStatus";
	
	public static final String RIDER_ID = "riderId";
	public static final String RIDER_NAME = "riderName";
	public static final String ACCOUNT_NUMBER = "accountNumber";
	public static final String AMOUNT = "amount";
	public static final String TRANSFER_AMOUNT = "transferAmount";

	public static final String PAYMENT_STATUS = "paymentStatus";
	public static final String REMARKS = "remarks";
	public static final String DETAILS_RIDER_ID = "customerReferenceNumber";
	public static final String DETAILS_RIDER_NAME = "beneficiaryName";
	public static final String DETAILS_ACCOUNT = "beneficiaryAccount";
	public static final String DETAILS_AMOUNT = "netPaymentAmount";
	public static final String DETAILS_AMOUNT_SORT = "paymentAmountSort";
	public static final String DETAILS_DATE= "transferDate";
	public static final String DETAILS_PAYMENT_STATUS = "processingStatus";
	public static final String DETAILS_PAYMENT_REMARKS = "processingRemarks";
	public static final String POCKET_BALANCE = "pocketBalance";
	public static final String SECURITY_AMOUNT = "securityDeducted";
	public static final String OTHER_DEDUCTION = "otherDeduction";
	public static final String OTHER_PAYMENTS = "otherPayments";
	public static final String EXCESS_WAIT_TIME = "netExcessiveWaitAmount";

	static  {
		transferDetailsMap.put(RIDER_ID, DETAILS_RIDER_ID);
		transferDetailsMap.put(ACCOUNT_NUMBER, DETAILS_ACCOUNT);
		transferDetailsMap.put(TRANSFER_AMOUNT, DETAILS_AMOUNT_SORT);
		transferDetailsMap.put(PAYMENT_STATUS, DETAILS_PAYMENT_STATUS);
		transferDetailsMap.put(REMARKS, DETAILS_PAYMENT_REMARKS);
		transferDetailsMap.put(AMOUNT, DETAILS_AMOUNT);
		transferDetailsMap.put(RIDER_NAME, DETAILS_RIDER_NAME);
	}
}