Feature: Dispense
	
	Scenario: Navigate to Dispense Page
		Given I am logged in
		And I press view with id "buttonDispense"
		Then I should see "Dispense"
		And I should see "Prescription No:"

	Scenario: Select Items for Dispense
		Given I am on the "Dispense" page
		Then I should have 0 items selected
		When I select the commodity category called "Family Planning" 
		Then I should see the overlay to select Items
		When I select the item called "Implants - 5 yrs_pieces" in the overlay
		And I close the overlay 
		Then I should see the item called "Implants - 5 yrs_pieces" on the form

	Scenario: Remove Items from Dispense
		Given I am on the "Dispense" page
		When I select the commodity category called "Family Planning" 
		When I select the item called "Implants - 5 yrs_pieces" in the overlay
		And I close the overlay
		And I remove the item called "Implants - 5 yrs_pieces" from the form
		Then I should have 0 items selected