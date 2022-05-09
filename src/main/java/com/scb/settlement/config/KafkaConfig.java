package com.scb.settlement.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.util.ResourceUtils;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

    @Value("${kafka.consumerGroupId}")
    private String consumerGroupId; 

    @Value("${kafka.noOfConcurrentMessage}")
    private int noOfConcurrentMessage;

    @Value("${kafka.groupInstanceId}")
    private String groupInstanceId;

    @Value("${secretsPath}")
    private String secretsPath;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        String server = null;
        try {
            URI bootStrapUrl = ResourceUtils.getURL(secretsPath + "/KAFKA_BOOTSTRAP_SERVERS").toURI();
            server = sanitize(Files.readAllBytes(Paths.get(bootStrapUrl)));
            log.info("Extracted kafka bootstrap server url {}",server);
        }
        catch (Exception e){
            log.error("Error extracting kafka url, extracting from properties",e);
        }
        log.info("Initializing Kafka Consumer Factory");
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configProps.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, CustomCooperativeStickyAssignor.class.getName());
        configProps.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, groupInstanceId);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 180000);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(noOfConcurrentMessage);
        factory.setErrorHandler(((exception, data) -> log.error("Error in settlement service kafka process with Exception {} and the record is {}", exception, data)));
        return factory;
    }

    private String sanitize(byte[] strBytes) {
        return new String(strBytes).replace("\r", "").replace("\n", "");
    }
    
}
