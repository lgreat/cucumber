Feature: Verify MSS hover displays

@javascript @readwrite
Scenario: Click mss hover on New profile
  Given I am on "Golda Meir School" new profile
    And I click on the "Send me updates" link
    And I see the MSS hover
    And I see "Send me updates", "Email" and "Confirm email"
  When I submit my "mbecerra+" and confirmation email and Sign up
  Then the MSS Email Validation hover is visible
