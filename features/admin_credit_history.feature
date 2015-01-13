Feature: Admin Credit History
  As a Customer Service person
  I want view a user's credit history
  So that I can verify their credit history when dealing with customer service issues

  # CRED-73 bug
  # There is marginal potential for this test to fail if
  # the service runs on multiple hosts whose clocks have diverged. See PT-604.
  Scenario: Credit reasons in credit history
    Given a customer has the following credit history:
      | event_type | amount | reason                    |
      | credit     | 1.00   | GoodwillBookIssue         |
      | credit     | 1.01   | GoodwillTechnicalIssue    |
      | credit     | 0.99   | GoodwillServiceIssue      |
      | credit     | 1.99   | GoodwillCustomerRetention |
      | credit     | 11.00  | StaffCredit               |
      | credit     | 100.00 | CreditVoucherCode         |
      | credit     | 999.00 | Hudl2Promotion            |
    And I am logged in as a CSM user
    When I request for the user's credit history
    Then the request is successful
    And the credit history contains the above events in reverse chronological order

  Scenario: Requesting credit history as CSR user and checking which fields returned
    Given a customer with credit history
    And I am logged in as a CSM user
    When I request for the user's credit history
    Then the event items in the history response contains the following attributes:
      | type     |
      | dateTime |
      | reason   |
      | amount   |
#      | issuer   | # removing check for issuer until functionality is developed (CREDIT-60)

  Scenario: Requesting credit history as CSM user and checking which fields returned
    Given a customer with credit history
    And I am logged in as a CSR user
    When I request for the user's credit history
    Then the event items in the history response contains the following attributes:
      | type     |
      | dateTime |
      | reason   |
      | amount   |
    And the event items in the history response does not contain the following attributes:
      | issuer   |

  Scenario: Requesting credit history as unknown user
    Given a customer with credit history
    And I am a logged out user
    When I request for the user's credit history
    Then the request fails because was I am unauthorised

  Scenario: Requesting credit history as user without admin permissions
    Given a customer with credit history
    And I am a logged in user without admin permissions
    When I request for the user's credit history
    Then the request fails because my role is forbidden

  # CRED-74
  Scenario Outline: Requesting credit history of unknown user
    Given an unknown customer
    And I am logged in as a <role> user
    When I request for the user's credit history
    Then the request is successful
    And the customer has overall credit balance of 0.00 in GBP

  Examples:
    | role |
    | CSM  |
    | CSR  |
