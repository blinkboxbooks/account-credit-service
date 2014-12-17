Feature: Reasons List Endpoint
  As a Customer Service Person
  I want to choose a credit reason from a fixed list of reasons
  So that reasons for crediting a user can be tracked and are consistent

  Scenario Outline: Get reasons list
    Given I am logged in as a <role> user
    When I make a request for the reasons list
    Then the request is successful
    And the reasons returned are:
      | GoodwillBookIssue         |
      | GoodwillTechnicalIssue    |
      | GoodwillServiceIssue      |
      | GoodwillCustomerRetention |
      | StaffCredit               |
      | CreditVoucherCode         |
      | Hudl2Promotion            |

  Examples:
  | role |
  | CSM  |
  | CSR  |

  Scenario: Request reasons list as a logged out user
    Given I am a logged out user
    When I make a request for the reasons list
    Then the request fails because I am unauthorised

  Scenario: Request reasons list as a user without admin permissions
    Given I am a logged in user without admin permissions
    When I make a request for the reasons list
    Then the request fails because my role is forbidden
