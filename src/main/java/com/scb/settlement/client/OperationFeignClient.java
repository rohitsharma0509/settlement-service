package com.scb.settlement.client;

import com.scb.settlement.model.dto.BatchConfigurationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "operationFeignClient", url = "${rider.client.operation-service}")
public interface OperationFeignClient {

	@GetMapping(value = "/ops/batch-processing/config")
    BatchConfigurationDto getBatchConfiguration();
    
}
