package com.scb.settlement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaReadServiceTest {

  @Mock
  private DataProcess process;

  @InjectMocks
  private KafkaReadService kafkaReadService;

  @Test
  public void testConsume() throws Exception {
    doNothing().when(process).processKafkaMessage(anyString());
    kafkaReadService.consume("{\"message\": \"test\"}");
    verify(process, times(1)).processKafkaMessage(anyString());
  }

}
