Feature: Losses
	
	Scenario: Navigate to Losses Page
		Given I am logged in
		And I press view with id "buttonLosses"
		Then I should see "Losses"