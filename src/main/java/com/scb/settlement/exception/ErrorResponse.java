package com.scb.settlement.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class ErrorResponse {
  @JsonProperty
  private String errorCode;
  @JsonProperty
  private String errorMessage;

}
