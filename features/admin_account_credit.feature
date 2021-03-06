Feature: Admin account credit service
  As a Customer Service person
  I want to be able to credit a user's account
  So that I can make customers happy

  Scenario Outline: Credit user
    Given a customer with 0.00 in GBP of credit
    And I am logged in as a <role> user
    When I credit the customer 1.01 in GBP with the reason: Goodwill (Book Issue)
    Then the request is successful
    And the customer has an overall credit balance of 1.01 in GBP

  Examples:
    | role |
    | CSM  |
    | CSR  |

  @manual @data-dependent
  Scenario Outline: Credit Malformed user id
    Given a malformed customer id of <malformed_id>
    And I am logged in as a CSM user
    When I credit the customer 1.01 in GBP with the reason: Goodwill (Technical Issue)
    Then the request is unsuccessful because the user was not found

  Examples:
    | malformed_id |
    | 9999999999   |
    | -1           |
    | abc          |

  Scenario Outline: Credit user with invalid amount
    Given a customer with 0.00 in GBP of credit
    And I am logged in as a CSM user
    When I credit the customer <amount> in GBP with the reason: Goodwill (Book Issue)
    Then the request fails because it was invalid

    Examples:
    | amount |
    | 0      |
    | -1     |

  Scenario: Credit user with invalid reason
    Given a customer with 0.00 in GBP of credit
    And I am logged in as a CSM user
    When I credit the customer 1 in GBP with the reason: Goodwill (Invalid Reason)
    Then the request fails because it was invalid

  Scenario: Credit user using a logged out user
    Given a customer with 0.00 in GBP of credit
    And I am a logged out user
    When I credit the customer 1.01 in GBP with the reason: Goodwill (Technical Issue)
    Then the request fails because I am unauthorised

  Scenario: Credit user using a user without admin permissions
    Given a customer with 10.00 in GBP of credit
    And I am a logged in user without admin permissions
    When I credit the customer 1.01 in GBP with the reason: Goodwill (Technical Issue)
    Then the request fails because I was forbidden

  Scenario: Credit user using a requestId that has already been used
    Given a customer with 0.00 in GBP of credit
    And I am logged in as a CSM user
    And I credit the customer 1.00 in GBP with the reason: Goodwill (Book Issue)
    When I try to credit the customer 2.00 in GBP using the same requestId as before
    Then the request is successful
    And the customer has an overall credit balance of 1.00 in GBP

  Scenario Outline: Credit user with invalid currency
    Given a customer with 0.00 in GBP of credit
    And I am logged in as a CSM user
    When I credit the customer <amount> in <currency> with the reason: Goodwill (Book Issue)
    Then the request fails because the currency was invalid

  Examples:
    | amount | currency |
    | 1000   | HKD      |
    | 9001   | JPY      |
    | 4      | BLZ      |
    | .20    | IT       |