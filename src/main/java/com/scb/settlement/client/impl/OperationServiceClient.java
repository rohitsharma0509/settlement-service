package com.scb.settlement.client.impl;

import com.scb.settlement.client.OperationFeignClient;
import com.scb.settlement.exception.ExternalServiceInvocationException;
import com.scb.settlement.model.dto.BatchConfigurationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OperationServiceClient {

    @Autowired
    private OperationFeignClient operationFeignClient;

    public BatchConfigurationDto getBatchConfiguration() {
        try {
            return operationFeignClient.getBatchConfiguration();
        } catch (Exception e) {
            log.error("Exception occurred while invoking operation-service", e);
            throw new ExternalServiceInvocationException("Exception while invoking operation-service");
        }
    }

}
