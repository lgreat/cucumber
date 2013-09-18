Feature: Find a school page tests
  As a parent
  I want to be able to search for schools by name or location
  So that I can make informed school choices

  Background:
    Given I am on "Find a School" page

  @javascript
  Scenario: Search Find a School By Location
    When I do a by location search for Washington DC
    Then I see "Schools near Washington, D.C."
    And the title has "GreatSchools.org Search"

  @javascript
  Scenario: Search Find a School By Name
    When I do a by name search for Burbank High School in TX
    Then I see "Burbank High School"
  