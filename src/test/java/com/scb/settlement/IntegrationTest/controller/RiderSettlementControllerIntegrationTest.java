package com.scb.settlement.IntegrationTest.controller;

import com.scb.settlement.RiderSettlementApplication;
import com.scb.settlement.model.document.RiderSettlementBatchInfo;
import com.scb.settlement.model.enumeration.SettlementBatchStatus;
import com.scb.settlement.repository.RiderSettlementBatchInfoRepository;
import com.scb.settlement.service.document.RiderSettlementService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = RiderSettlementApplication.class)
public class RiderSettlementControllerIntegrationTest {

	@Autowired
	private RiderSettlementService riderSettlementService;
	@Autowired
	private RiderSettlementBatchInfoRepository batchInfoRepository;
	@Autowired
	private MockMvc mockMvc;
	
	private String URL = "/api/settlement/batch";
	private static String batchRef = "";
	@BeforeAll
	static void setUp() {
	}

	@Test
	@Order(1)
	public void push_reconcile_data_thenReturns200() throws Exception {
		String reconciliationBatchId = "RECON000000001";
		String pic = "abc.jpg";
		String url = URL + "/" + reconciliationBatchId + "/reconcile/" + pic;
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(""))
				.andDo(print()).andReturn();
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
		Optional<RiderSettlementBatchInfo> batchInfo = batchInfoRepository.findByReconcileBatchId(reconciliationBatchId);
		batchRef = batchInfo.get().getBatchRef();
	}

	@Test
	public void get_Batch_Info_thenReturns200() throws Exception {
		String url = URL + "/" + batchRef;
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
				).andDo(print()).andReturn();
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
	}

	@Test
	public void get_Batch_Details_thenReturns200() throws Exception {
		String url = URL + "/details/" + batchRef;
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
		).andDo(print()).andReturn();
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

	}

	@Test
	public void get_Batch_Details_By_Status_thenReturns200() throws Exception {
		String url = URL + "/status/" + SettlementBatchStatus.READY_FOR_RUN;
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
		).andDo(print()).andReturn();
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

	}

	@Test
	public void fetch_Batch_Details_By_Rider_thenReturns200() throws Exception {
		String riderId = "RR27411";
		String url = URL + "/" + riderId  +"/all";
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
		).andDo(print()).andReturn();
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
	}



	@Test
	public void fetch_Batch_Details_By_Rider_For_Month_thenReturns200() throws Exception {
		String riderId = "RR27411";
		String url = URL + "/" + riderId  + "/months/" + 1;
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
		).andDo(print()).andReturn();
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
	}

	@Test
	public void fetch_Batch_Details_By_Rider_With_In_Months_thenReturns200() throws Exception {
		String riderId = "RR27411";
		String url = URL + "/" + riderId  +"/dates?startDate=2021-01-01T00:01:43Z&endDate=2021-02-01T00:59:43Z";
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
		).andDo(print()).andReturn();
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
	}

	@Test
	public void get_Settlement_Batch_info_thenReturns200() throws Exception{
		String startDate = "2022-01-16";
		String endDate = "2022-01-18";
		String url = URL + "/" + "settlementBatchInfoStatus?startDate=" +startDate+"&endDate="+endDate;
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
		).andDo(print()).andReturn();
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
	}

	@Test
	public void get_Settlement_Batch_info_thenReturns400() throws Exception {
		String startDate = "2022-01-18";
		String endDate = "2022-01-16";
		String url = URL + "/" + "settlementBatchInfoStatus?startDate=" + startDate + "&endDate=" + endDate;
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
		).andDo(print()).andReturn();
		assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());

	}

}
