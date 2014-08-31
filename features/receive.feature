Feature: Order
	
	Scenario: Navigate to Order Page
		Given I am logged in
		And I press view with id "buttonOrder"
		Then I should see "Order"