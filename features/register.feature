Feature: Register feature

  Scenario: As a valid user I can register
    When I enter text "android_1" into field with id "textUsername"
    And I enter text "Password1" into field with id "textPassword"
    When I press view with id "buttonRegister"
    Then I should see "Registering"
    Then I should see "Registration Successful"

  Scenario: A username is required to register
   When I enter text "" into field with id "textUsername"
   When I press view with id "buttonRegister"
   Then I should see "Username is required"

  Scenario: A password is required to register
   When I enter text "textPassword" into field with id "textUsername"
   When I enter text "" into field with id "textPassword"
   When I press view with id "buttonRegister"
   Then I should see "Password is required"
