require 'calabash-android/calabash_steps'

Given(/^I am logged in$/) do
	username = "android_1"
	password = "Password1"
	steps %Q{
		When I enter text "#{username}" into field with id "textUsername"
		And I enter text "#{password}" into field with id "textPassword"
		When I press view with id "buttonRegister"
		Then I should see "Registering"
		Then I should see "Registration Successful"
		And I should see "Dispense"
		And I should see "Order"
		And I should see "Receive"
		And I should see "Losses"
		And I should see "Adjustments"
		And I should see "Reports"
		And I should see "Messages"
	}
end



When(/^I select the first commodity category$/) do
	touch(query("Linearlayout id:'layoutCategories' button").first)
end

Then(/^I should see the overlay to select Items$/) do
	steps %Q{
		Then I should see "Close Selection"
	}
end

When(/^I select the first item in the overlay$/) do
	touch(query("gridview relativelayout").first)
end

Then(/^I should see the item on the dispense form$/) do
	unless(query("gridview relativelayout").size > 0 ) 
		fail(msg="Item was not selected")
	end
end

Given(/^I am on the "(.*?)" page$/) do |page|
	steps %Q{
		Given I am logged in
		And I press view with id "button#{page}"
		Then I should see "#{page}"
	}
	unless(query("gridview relativelayout").size == 0) 
		fail(msg="Items already selected")
	end
end

Then(/^I should have (\d+) items selected$/) do |numberOfItems|
	query_result = query("relativelayout gridview index:0 relativelayout")
	selected = query_result.size 
	unless (selected == numberOfItems.to_i)
		fail(msg="#{selected} selected")
	end
end

When(/^I select the commodity category called "(.*?)"$/) do |name|
	touch (query("button text:'#{name}'"))
end

When(/^I select the item called "(.*?)" in the overlay$/) do |name|
	touch(query("relativelayout textview text:'#{name}'"))
end

When(/^I close the overlay$/) do
	tap_mark "Close Selection"
end

Then(/^I should see the item called "(.*?)" on the form$/) do |name|
	unless(query("relativelayout gridview index:0 relativelayout textview text:'#{name}'").size > 0)
		fail(msg="item called #{name} not available")
	end
end

When(/^I remove the item called "(.*?)" from the form$/) do |name|
	query_result = query("relativelayout gridview index:0 relativelayout textview text:'#{name}' sibling imagebutton index:0")
	touch(query_result)
end