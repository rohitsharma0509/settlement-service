package com.scb.settlement.bdd.stepdefinitons;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Slf4j
public class SettlementSearchControllerStepDefinitions {

	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;
	static final String URL = "/api/settlement/search";
	private String GET_URL = "";

	MvcResult result;
	
	@Given("Set Get filtered data service api endpoint")
	public void set_get_filtered_data_service_api_endpoint() {
		GET_URL=URL;
	}

	@When("Send a GET request for getting filtered data")
	public void send_a_get_request_for_getting_filtered_data() throws Exception {
	   
		 result = mockMvc.perform(MockMvcRequestBuilders.get(URL).contentType(MediaType.APPLICATION_JSON)
				).andDo(print()).andReturn();
	}

	@Then("I receive valid HTTP Status Code for filtered data {int}")
	public void i_receive_valid_http_status_code_for_filtered_data(Integer int1) {
	   int status = result.getResponse().getStatus();
		assertEquals(status, int1, "Incorrect Response Status");
		assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

	}
	
}
