@in-progress
Feature: Admin Account Credit
  As a client of account-credit-service
  I want to debit from a user's credit balance
  So that I can charge credit for their book purchases

  Scenario: Debit user
    Given a user with 10.00 in GBP of credit
    And I am logged in as an CSM user
    When I debit the user 1.01 in GBP
    Then the user has overall credit balance of 8.99 in GBP

  Scenario: User does not have enough credit
    Given a user with 10.00 in GBP of credit
    And I am logged in as an CSM user
    When I debit the user 11.00 in GBP
    Then the request fails because it was invalid
    And the user has overall credit balance of 10.00 in GBP

  Scenario: Debit user using requestId that has already been used
    Given a user with 10.00 in GBP of credit
    And I am logged in as an CSM user
    And I debit the user 1.00 in GBP
    When I try to debit the user 2.00 in GBP using the same requestId as before
    Then the request was successful
    And the user has overall credit balance of 9 in GBP

  Scenario Outline: Debit user with invalid amount
    Given a user with 10.00 in GBP of credit
    And I am logged in as an API user
    When I debit the user <amount> in GBP
    Then the request fails because it was invalid

  Examples:
    |amount|
    |0     |
    |-1    |

  Scenario: Debit unknown user
    Given an unknown user
    And I am logged in as an CSM user
    When I debit the user 1.01 in GBP
    Then the request fails because the user was not found

  Scenario: Debit user using a logged out user
    Given a user with 10.00 in GBP of credit
    And I am a logged out user
    When I debit the user 1.00 in GBP
    Then the request fails because I am unauthorised

  Scenario: Debit user using a user without admin permissions
    Given a user with 10.00 in GBP of credit
    And I am a logged in user without admin permissions
    When I debit the user 1.00 in GBP
    Then the request fails because my role is forbidden
