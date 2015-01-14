Feature: Admin Account Debit
  As a client of account-credit-service
  I want to debit from a user's credit balance
  So that I can charge credit for their book purchases

  Scenario: Debit user
    Given a customer with 10.00 in GBP of credit
    And I am logged in as a CSM user
    When I debit the customer 1.01 in GBP
    Then the customer has overall credit balance of 8.99 in GBP

  Scenario: User does not have enough credit
    Given a customer with 10.00 in GBP of credit
    And I am logged in as a CSM user
    When I debit the customer 11.00 in GBP
    Then the request fails because it was invalid
    And the customer has overall credit balance of 10.00 in GBP

  Scenario: Debit user using requestId that has already been used
    Given a customer with 10.00 in GBP of credit
    And I am logged in as a CSM user
    And I debit the customer 1.00 in GBP
    When I try to debit the customer 2.00 in GBP using the same requestId as before
    Then the request was successful
    And the customer has overall credit balance of 9 in GBP

  Scenario Outline: Debit user with invalid amount
    Given a customer with 10.00 in GBP of credit
    And I am logged in as a CSM user
    When I debit the customer <amount> in GBP
    Then the request fails because it was invalid

  Examples:
    |amount|
    |0     |
    |-1    |

  Scenario: Debit unknown user
    Given an unknown customer
    And I am logged in as a CSM user
    When I debit the customer 1.01 in GBP
    Then the request fails because the user was not found

  Scenario: Debit user using a logged out user
    Given a customer with 10.00 in GBP of credit
    And I am a logged out user
    When I debit the customer 1.00 in GBP
    Then the request fails because I am unauthorised

  Scenario: Debit user using a user without admin permissions
    Given a customer with 10.00 in GBP of credit
    And I am a logged in user without admin permissions
    When I debit the customer 1.00 in GBP
    Then the request fails because my role is forbidden