Feature: Tandem List View

Background:
  Given I am on "Alameda High School" new profile
  And I am on the "Culture" tab

@javascript
Scenario: I see the tandem calendar list view
  Then I should see the tandem calendar list view
