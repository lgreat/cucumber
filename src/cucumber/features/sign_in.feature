@javascript
Feature: GreatSchools Sign In Tests

Background:
  Given I am on "GS Home Page" page

Scenario: Sign in hover is hidden by default
  Then the sign in hover is hidden

Scenario: Clicking sign in shows sign in hover
  When I click on the "Sign In" link
  Then the sign in hover is visible

Scenario: When I sign in my username is welcomed
  When I sign in as "mbecerra+en@greatschools.org" with password "abc123"
  Then I see "Welcome, Marcelo"
    And I see "Sign Out"