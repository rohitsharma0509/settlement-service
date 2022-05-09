@functional
Feature: Settlement search controller test
  Optional description of the feature

  Scenario: Search batch details
    Given  Set Get filtered data service api endpoint
    When   Send a GET request for getting filtered data
    Then  I receive valid HTTP Status Code for filtered data 200
