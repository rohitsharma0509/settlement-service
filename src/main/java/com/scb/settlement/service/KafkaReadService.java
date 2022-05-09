package com.scb.settlement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public final class KafkaReadService {
	
	@Autowired
	private DataProcess process;

    @KafkaListener(topics = "${kafka.topic}")
    public void consume(String message) throws JsonProcessingException {
    	process.processKafkaMessage(message);
    }
}
