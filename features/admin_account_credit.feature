@in-progress
Feature: Admin account credit service
  As a Customer Service person
  I want to be able to credit a user's account
  So that I can make customers happy

  Scenario Outline: Credit user
    Given a user with £0 credit balance
    And I am logged in as a <role> user
    When I credit the user 1.01 in GBP with the reason: Goodwill (Book Issue)
    Then the request is successful
    And the user has overall credit balance of 1.01 in GBP

  Examples:
    | role |
    | CSM  |
    | CSR  |

  Scenario Outline: Credit unknown user
    Given an unknown user
    And I am logged in as a <role> user
    When I credit the user 1.01 in GBP with the reason: Goodwill (Technical Issue)
    Then the request fails because the user was not found

  Examples:
    | role |
    | CSM  |
    | CSR  |

  Scenario Outline: Credit user with invalid amount
    Given a user with £0 credit balance
    And I am logged in as a CSR user
    When I credit the user <amount> in GBP with the reason: Goodwill (Book Issue)
    Then the request fails because it was invalid

    Examples:
    |amount|
    |0     |
    |-1    |

  Scenario: Credit user with invalid reason
    Given a user with £0 credit balance
    And I am logged in as a CSR user
    When I credit the user 1 in GBP with the reason: Goodwill (Invalid Reason)
    Then the request fails because it was invalid

  Scenario: Credit user using a logged out user
    Given a user with £0 credit balance
    And I am a logged out user
    When I credit the user 1.01 in GBP with the reason: Goodwill (Technical Issue)
    Then the request fails because I am unauthorised

  Scenario: Credit user using a user without admin permissions
    Given a user with £0 credit balance
    And I am a logged in user without admin permissions
    When I credit the user 1.01 in GBP with the reason: Goodwill (Technical Issue)
    Then the request fails because I was forbidden

  Scenario: Credit user using a requestId that has already been used
    Given a user with £0 credit balance
    And I am logged in as a CSR user
    And I credit the user 1.00 in GBP with the reason: Goodwill (Book Issue)
    When I try to credit the user 2.00 in GBP using the same requestId as before
    Then the request is successful
    And the user has overall credit balance of 1.00 in GBP
