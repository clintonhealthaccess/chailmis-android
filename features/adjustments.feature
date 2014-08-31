Feature: Adjustments
	
	Scenario: Navigate to Adjustments Page
		Given I am logged in
		And I press view with id "buttonAdjustments"
		Then I should see "Adjustments"