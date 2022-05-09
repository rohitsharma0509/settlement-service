package com.scb.settlement;

import com.scb.rider.tracing.tracer.EnableBasicTracer;
import com.scb.rider.tracing.tracer.logrequest.EnableRequestLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRequestLog
@EnableBasicTracer
@EnableMongoAuditing
@EnableFeignClients
@EnableScheduling
public class RiderSettlementApplication {
    public static void main(String[] args) {
        SpringApplication.run(RiderSettlementApplication.class, args);
    }
}
