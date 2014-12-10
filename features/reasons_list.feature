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
      | CreditRefund              |
      | StaffCredit               |
      | CreditVoucherCode         |
      | Hudl2Promotion            |

  Examples:
    | role |
    | CSM  |
    | CSR  |
