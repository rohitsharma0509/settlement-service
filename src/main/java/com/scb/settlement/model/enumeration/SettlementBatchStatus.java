package com.scb.settlement.model.enumeration;

import java.util.Arrays;
import java.util.List;

public enum SettlementBatchStatus {
	READY_FOR_RUN, IN_PROGRESS, FILE_GENERATED, UPLOADED, FAILED, SUCCESS, ALL;

	public static List<SettlementBatchStatus> valuesForEmail() {
		return Arrays.asList(UPLOADED, FAILED, SUCCESS);
	}
}
