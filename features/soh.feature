Feature: Stock on Hand
  As a user of N-LMIS
  I want DHIS2 stock information to be up to date
  So that I can see the “Average Monthly Calculation” (AMC) and “Stock Out Days” reports and act accordingly

  Scenario: Updating with absolute numbers
    Given the server has "5" items of type "A"
    When I update SOH with "Received" "A" "10"
    And I wait for the sync period at "01:00AM"
    And the sync succeeds
    Then the server shows that there are "15" items of type "A" for my clinic

  Scenario: Chaining with absolute numbers
    Given the server has "5" items of type "A"
    When I update SOH with "Received" "A" "10"
    And I update SOH with "Dispensed" "A" "5"
    And I wait for the sync period at "12:00PM"
    And the sync succeeds
    Then the server shows that there are "10" items of type "A" for my clinic

  Scenario: Retry to send SOH only at prescribed intervals
    Given the server has "5" items of type "A"
    When I update SOH with "Received" "A" "10"
    And I wait for the sync period at "01:00AM"
    But the sync fails
    And I wait for the sync period at "12:00PM"
    And the sync succeeds
    Then the server shows that there are "15" items of type "A" for my clinic

  Scenario: Querying the server for current information
    Given the server has "5" items of type "A"
    When I re-install the app on the tablet
    Then the SOH shown on the tablet for item of type "A" is "5" for my clinic
