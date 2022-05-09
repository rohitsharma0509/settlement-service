package com.scb.settlement.IntegrationTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.settlement.RiderSettlementApplication;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = RiderSettlementApplication.class)
class SettlementSearchControllerIntegrationTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	
	private String URL = "/api/settlement/search";

	
	@Test
	@Order(1)
	void get_Batch_Info_thenReturns200() throws Exception {
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL).contentType(MediaType.APPLICATION_JSON)
				).andDo(print()).andReturn();
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
		

	}

	@Test
	@Order(2)
	void get_Batch_details_thenReturns200() throws Exception {
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL+"/batch/details/123").contentType(MediaType.APPLICATION_JSON)
				).andDo(print()).andReturn();
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
		

	}

	@Test
	@Order(3)
	void get_Batch_Info_filter_thenReturns200() throws Exception {
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL+"?filterquery=batchRef:S1000000").contentType(MediaType.APPLICATION_JSON)
				).andDo(print()).andReturn();
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
		

	}
	
	@Order(4)
	void get_Batch_details_filter_thenReturns200() throws Exception {
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL+"/batch/details/123?filterquery=riderId:213").contentType(MediaType.APPLICATION_JSON)
				).andDo(print()).andReturn();
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
		

	}
}
