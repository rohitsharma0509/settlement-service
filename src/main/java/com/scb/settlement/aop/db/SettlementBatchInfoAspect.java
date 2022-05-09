package com.scb.settlement.aop.db;

import com.scb.settlement.constants.Constants;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.enumeration.SettlementBatchStatus;
import com.scb.settlement.repository.RiderSettlementBatchInfoRepository;
import com.scb.settlement.service.EmailService;
import com.scb.settlement.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Aspect
@Component
@Profile(value = "!local & !test")
public class SettlementBatchInfoAspect {

    private static final String SUBJECT = "Settlement Batch Status Update";
    private static final String TEMPLATE_NAME = "settlementStatusUpdateEmailTmpl.html";

    @Autowired
    private EmailService emailService;

    @Autowired
    private RiderSettlementBatchInfoRepository settlementInfoRepository;

    @Around("execution(* com.scb.settlement.repository.RiderSettlementBatchInfoRepository.save(..))")
    public Object aroundSettlementBatchInfoRepositorySave(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        RiderSettlementBatchInfo documentToPersist = (RiderSettlementBatchInfo) args[0];
        Optional<RiderSettlementBatchInfo> existingDocument = settlementInfoRepository.findByBatchRef(documentToPersist.getBatchRef());
        SettlementBatchStatus oldStatus = existingDocument.isPresent() ? existingDocument.get().getBatchStatus() : null;
        log.info("Before save settlementStatus = {}", oldStatus);
        Object result = joinPoint.proceed();
        RiderSettlementBatchInfo persistedDocument = (RiderSettlementBatchInfo) result;
        SettlementBatchStatus newStatus = persistedDocument.getBatchStatus();
        log.info("after save settlementStatus = {}", newStatus);

        if (Objects.nonNull(newStatus) && !newStatus.equals(oldStatus) && SettlementBatchStatus.valuesForEmail().contains(newStatus)) {
            emailService.sendTemplateMail(TEMPLATE_NAME, SUBJECT, getVariables(persistedDocument));
        }
        return result;
    }

    private Map<String, Object> getVariables(RiderSettlementBatchInfo settlement) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("batchId", settlement.getBatchRef());
        variables.put("fileName", settlement.getFileName());
        variables.put("date", CommonUtils.getFormattedDate(settlement.getDateOfRun(), Constants.TRANSFER_DATE_FORMAT));
        LocalTime startTime = CommonUtils.toThaiTime(settlement.getDateOfRun(), settlement.getStartTime());
        variables.put("startTime", CommonUtils.getFormattedTime(startTime, Constants.TIME_FORMAT));
        LocalTime endTime = CommonUtils.toThaiTime(settlement.getDateOfRun(), settlement.getStartTime());
        variables.put("endTime", CommonUtils.getFormattedTime(endTime, Constants.TIME_FORMAT));
        variables.put("status", settlement.getBatchStatus());
        variables.put("remarks", settlement.getFailureReason());
        return variables;
    }
}
