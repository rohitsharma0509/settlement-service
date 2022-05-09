package com.scb.settlement.client;

import com.scb.settlement.model.dto.RiderCredit;
import com.scb.settlement.model.dto.RiderIncentive;
import com.scb.settlement.model.dto.RiderPocketBalanceRequest;
import com.scb.settlement.model.dto.RiderPocketBalanceResponse;
import com.scb.settlement.model.dto.RiderPocketDetails;
import com.scb.settlement.model.dto.RiderPocketModificationDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "pocketServiceFeignClient", url = "${rider.client.pocket-service}")
public interface PocketServiceFeignClient {

   @PostMapping("/pocket/get-riders")
   List<RiderPocketDetails> getRidersWithSecurityBalanceGreaterThanZero(@RequestBody List<String> riderIdList);

   @PostMapping("/pocket/update/pocket-balance")
   Boolean updateRiderNetPocketBalance(@RequestBody List<RiderCredit> riderCredits);

   @PostMapping("/pocket/update/incentive-amount")
   Boolean updateIncentiveAmount(@RequestBody List<RiderIncentive> riderIncentives);

   @PostMapping("/pocket/pocket-balance/latest")
   List<RiderPocketBalanceResponse> getPocketDetailsByTime(@RequestParam(name = "dateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
           , @RequestBody RiderPocketBalanceRequest riderPocketBalanceRequest);

   @PutMapping("/pocket/update/pocket-info")
   Boolean updateRiderPocketDetails(@RequestBody List<RiderPocketModificationDto> riderPocketModificationDto);
}
