package com.scb.settlement.client.impl;

import com.google.common.collect.Lists;
import com.scb.settlement.client.PocketServiceFeignClient;
import com.scb.settlement.exception.ExternalServiceInvocationException;
import com.scb.settlement.model.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
public class PocketServiceClient {

    private static final Integer BATCH_SIZE = 5000;

    @Autowired
    private PocketServiceFeignClient pocketServiceFeignClient;

    public List<RiderPocketBalanceResponse> getPocketDetailByTimeInBatch(LocalDateTime dateTime, List<String> riderIds) {
        List<CompletableFuture<List<RiderPocketBalanceResponse>>> completableFutures = new ArrayList<>();
        List<List<String>> listOfBatchs = Lists.partition(riderIds, BATCH_SIZE);
        log.info("invoking pocket service. Number of batch: {}", listOfBatchs.size());
        listOfBatchs.stream().forEach(batchIds -> completableFutures.add(getPocketDetailByTimeForSingleBatch(dateTime, batchIds)));
        CompletableFuture<List<List<RiderPocketBalanceResponse>>> allCompletableFuture = CompletableFuture
                .allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()])).thenApply(future ->
                        completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList())
                );
        List<RiderPocketBalanceResponse> pocketBalanceResponses = new ArrayList<>();
        try {
            List<List<RiderPocketBalanceResponse>> listOfLists = allCompletableFuture.get();
            pocketBalanceResponses = listOfLists.stream().flatMap(List::stream).collect(Collectors.toList());
        } catch (InterruptedException e) {
            log.error("InterruptedException while processing pocket response from future", e);
            Thread.currentThread().interrupt();
        } catch(ExecutionException e) {
            log.error("ExecutionException while processing pocket response from future", e);
        }
        return pocketBalanceResponses;
    }

    public CompletableFuture<List<RiderPocketBalanceResponse>> getPocketDetailByTimeForSingleBatch(LocalDateTime dateTime, List<String> riderIds) {
        log.info("Fetching Pocket Details for {} riders", riderIds.size());
        return CompletableFuture.supplyAsync(() -> {
            RiderPocketBalanceRequest pocketRequest = RiderPocketBalanceRequest.of(riderIds);
            return getPocketDetailsByTime(dateTime, pocketRequest);
        });
    }

    public List<RiderPocketBalanceResponse> getPocketDetailsByTime(LocalDateTime dateTime, RiderPocketBalanceRequest riderPocketBalanceRequest) {
        try {
            return pocketServiceFeignClient.getPocketDetailsByTime(dateTime, riderPocketBalanceRequest);
        } catch (Exception e) {
            log.error("Exception occurred while invoking pocket-service", e);
            throw new ExternalServiceInvocationException("Exception while invoking pocket-service");
        }
    }

    public List<Boolean> updateRiderPocketDetailsInBatch(List<RiderPocketModificationDto> riderDetails) {
        List<CompletableFuture<Boolean>> completableFutures = new ArrayList<>();
        List<List<RiderPocketModificationDto>> listOfBatchRequests = Lists.partition(riderDetails, BATCH_SIZE);
        log.info("invoking pocket-service to update pocket details. Number of batch: {}", listOfBatchRequests.size());
        listOfBatchRequests.stream().forEach(batchReq -> completableFutures.add(updateRiderPocketDetails(batchReq)));
        return getResponse(completableFutures);
    }

    public CompletableFuture<Boolean> updateRiderPocketDetails(List<RiderPocketModificationDto> riderDetails) {
        log.info("Updating pocket details for {} riders", riderDetails.size());
        return CompletableFuture.supplyAsync(() -> pocketServiceFeignClient.updateRiderPocketDetails(riderDetails));
    }

    public List<Boolean> updatePocketBalanceInBatch(List<RiderCredit> riderCredits) {
        List<CompletableFuture<Boolean>> completableFutures = new ArrayList<>();
        List<List<RiderCredit>> listOfBatchRequests = Lists.partition(riderCredits, BATCH_SIZE);
        log.info("invoking pocket-service to update pocket balance. Number of batch: {}", listOfBatchRequests.size());
        listOfBatchRequests.stream().forEach(batchReq -> completableFutures.add(updatePocketBalance(batchReq)));
        return getResponse(completableFutures);
    }

    public CompletableFuture<Boolean> updatePocketBalance(List<RiderCredit> riderCredits) {
        log.info("Updating Pocket Balance for {} riders", riderCredits.size());
        return CompletableFuture.supplyAsync(() -> pocketServiceFeignClient.updateRiderNetPocketBalance(riderCredits));
    }

    public List<Boolean> updateIncentivesInBatch(List<RiderIncentive> riderIncentives) {
        List<CompletableFuture<Boolean>> completableFutures = new ArrayList<>();
        List<List<RiderIncentive>> listOfBatchRequests = Lists.partition(riderIncentives, BATCH_SIZE);
        log.info("Number of batch: {}", listOfBatchRequests.size());
        IntStream.range(0, listOfBatchRequests.size()).forEach(index -> {
            List<RiderIncentive> batchReq = listOfBatchRequests.get(index);
            log.info("invoking pocket-service to update incentives for batchNo {}, noOfRiders {}", (index + 1), batchReq.size());
            completableFutures.add(updateIncentives(batchReq));
        });
        return getResponse(completableFutures);
    }

    public CompletableFuture<Boolean> updateIncentives(List<RiderIncentive> riderIncentives) {
        return CompletableFuture.supplyAsync(() -> pocketServiceFeignClient.updateIncentiveAmount(riderIncentives));
    }

    public List<RiderPocketDetails> getPocketDetailsByRiderIdsInBatch(List<String> riderIds) {
        List<RiderPocketDetails> pocketBalanceResponses;
        try {
            List<CompletableFuture<List<RiderPocketDetails>>> completableFutures = new ArrayList<>();
            List<List<String>> listOfBatches = Lists.partition(riderIds, BATCH_SIZE);
            log.info("invoking pocket service to get pocket details. Number of batch: {}", listOfBatches.size());
            listOfBatches.stream().forEach(batchIds -> completableFutures.add(getPocketDetailsByRiderIds(batchIds)));
            CompletableFuture<List<List<RiderPocketDetails>>> allCompletableFuture = CompletableFuture
                    .allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()])).thenApply(future ->
                            completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList())
                    );
            List<List<RiderPocketDetails>> listOfLists = allCompletableFuture.get();
            pocketBalanceResponses = listOfLists.stream().flatMap(List::stream).collect(Collectors.toList());
        } catch(Exception e) {
            log.error("Exception while getting pocket details", e);
            throw new ExternalServiceInvocationException("Exception while invoking pocket-service");
        }
        return pocketBalanceResponses;
    }

    public CompletableFuture<List<RiderPocketDetails>> getPocketDetailsByRiderIds(List<String> riderIds) {
        log.info("Fetching Pocket Details for {} riderIds", riderIds.size());
        return CompletableFuture.supplyAsync(() -> pocketServiceFeignClient.getRidersWithSecurityBalanceGreaterThanZero(riderIds));
    }

    private List<Boolean> getResponse(List<CompletableFuture<Boolean>> completableFutures) {
        CompletableFuture<List<Boolean>> allCompletableFuture = CompletableFuture
                .allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()])).thenApply(future ->
                        completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList())
                );
        List<Boolean> listOfResponses = new ArrayList<>();
        try {
            listOfResponses = allCompletableFuture.get();
        } catch (InterruptedException e) {
            log.error("InterruptedException while processing response from future", e);
            Thread.currentThread().interrupt();
        } catch(ExecutionException e) {
            log.error("ExecutionException while processing response from future", e);
        }
        return listOfResponses;
    }
}
