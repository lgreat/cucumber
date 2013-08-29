@javascript
Feature: Forgot Password
  As a user
  I can get instructions on changing my password
  So that I can access my account

  Background:
    Given I am on "GS Home Page" page

  Scenario: Forgot password hover is hidden by default
    Then the forgot password hover is hidden

  Scenario: Clicking sign in shows sign in hover
    When I click on the "Sign In" link
    And I click on the "Forgot your password?" link
    Then the forgot password hover is visible

  Scenario Outline: Forgot password displays appropriate message on valid and invalid email
    When I click on the "Sign In" link
    And I click on the "Forgot your password?" link
    Then the forgot password hover is visible
    And I submit my "<email>" and Reset password
    Then I see "<message>"
  Examples:
    | email             | message     |
    | skrishna+en@greatschools.org | An email has been sent to skrishna+en@greatschools.org with instructions for selecting a new password. |
    |				   | Email invalid. |
    | invalid@invalid.com	   | Hi, invalid! You have an email address on file, but still need to create a free account with GreatSchools. |