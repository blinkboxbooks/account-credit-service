Feature: Health Check Endpoint
  As an IT Operations person
  I want to easily check if the credit service is running
  So that I can know of the service status

  @smoke
  Scenario: Health Check
    When I make a request to the health check endpoint
    Then the request is successful
