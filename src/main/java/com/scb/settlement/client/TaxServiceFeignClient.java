package com.scb.settlement.client;

import com.scb.settlement.model.dto.GLInvoice;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "taxServiceFeignClient", url = "${rider.client.tax-service}")
public interface TaxServiceFeignClient {

   @PostMapping("/api/gl-invoice")
   Boolean sendBatchesForGL(@RequestBody GLInvoice glInvoice);
}
