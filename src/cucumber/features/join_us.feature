@javascript
Feature: GreatSchools Join Us Tests

Background:
  Given I am on "GS Home Page" page

Scenario: Clicking join us shows join us hover
  When I click on the "Join" link
  Then the join us hover is visible
  
@readwrite
Scenario: Sign up with valid data
  When I click on the "Join" link
  And I fill in:
#  | fName | Swathi |
  | jemail | devnull+[TIMESTAMP]@greatschools.org |
#  | uName  | Cucu[TIMESTAMP] |
  | jpword | abc123 |
  | cpword | abc123 |
  And I click the button "Join now"
  And I wait 2 seconds for the registration to succeed
  Then the Validate Email hover is visible