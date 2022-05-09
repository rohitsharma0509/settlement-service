package com.scb.settlement.client;

import com.scb.settlement.model.dto.SettlementDetails;
import com.scb.settlement.model.dto.SftpRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@FeignClient(name = "settlementFileGenerationFeignClient", url = "${rider.client.payment-integration-service}")
public interface SettlementFileGenerationFeignClient {

   @PostMapping("/api/generate/s1")
   public ResponseEntity<String> generateAndUploadS1File(@RequestBody @Valid @NotEmpty final @NotNull SettlementDetails settlementDetails);

   @PostMapping("/api/sftp/upload")
   public Boolean uploadFileToSftp(@RequestBody SftpRequest sftpRequest);
}
