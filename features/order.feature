Feature: Order
	
	Scenario: Navigate to Order Page
		Given I am logged in
		And I press view with id "buttonOrder"
		Then I should see "Order"
	
	Scenario: Select Items for order
		Given I am on the "Order" page
		Then I should have 0 items selected
		When I select the commodity category called "Family Planning" 
		Then I should see the overlay to select Items
		When I select the item called "Implants - 5 yrs_pieces" in the overlay
		And I close the overlay 
		Then I should see the item called "Implants - 5 yrs_pieces" on the form
	
	Scenario: Remove Items from Order
		Given I am on the "Order" page
		When I select the commodity category called "Family Planning" 
		When I select the item called "Implants - 5 yrs_pieces" in the overlay
		And I close the overlay 
		And I remove the item called "Implants - 5 yrs_pieces" from the form
		Then I should have 0 items selected