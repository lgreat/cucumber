@mobile
Feature: GreatSchools Mobile Home Page Search Tests
  As a parent
  I want to be able to quickly search for schools by name or city on my phone
  So that I can make informed school choices

  Background:
    Given I am on "GS Home Page" page

  @javascript
  Scenario: Search for a school on mobile
    When I type "1651 Union Street, San Francisco CA 94123" into "searchString"
      And I click the button "Search"
    Then the title has "Search Results"
      And I see "Sherman Elementary School"

  @javascript
  Scenario: Search for an elementary school on mobile
    When I type "1025 14th Street, San Francisco CA 94114" into "searchString"
      And I select "Elementary School" from "gradeLevels"
      And I click the button "Search"
    Then the title has "Search Results"
      And I see "McKinley Elementary School"

  @javascript
  Scenario: Search for elementary schools on mobile does not return high schools
    When I type "1101 Eucalyptus Dr., San Francisco CA 94132" into "searchString"
      And I select "Elementary School" from "gradeLevels"
      And I click the button "Search"
    Then the title has "Search Results"
      And I do not see "Lowell High School"

  @javascript
  Scenario: Search for a middle school on mobile
    When I type "460 Arguello Boulevard, San Francisco CA 94118" into "searchString"
      And I select "Middle School" from "gradeLevels"
      And I click the button "Search"
    Then the title has "Search Results"
      And I see "Roosevelt Middle School"

  @javascript
  Scenario: Search for middle schools on mobile does not return high schools
    When I type "1101 Eucalyptus Dr., San Francisco CA 94132" into "searchString"
      And I select "Middle School" from "gradeLevels"
      And I click the button "Search"
    Then the title has "Search Results"
      And I do not see "Lowell High School"

  @javascript
  Scenario: Search for a high school on mobile
    When I type "1101 Eucalyptus Dr., San Francisco CA 94132" into "searchString"
      And I select "High School" from "gradeLevels"
      And I click the button "Search"
    Then the title has "Search Results"
      And I see "Lowell High School"

  @javascript
  Scenario: Search for high schools on mobile does not return elementary schools
    When I type "1025 14th Street, San Francisco CA 94114" into "searchString"
      And I select "Middle School" from "gradeLevels"
      And I click the button "Search"
    Then the title has "Search Results"
      And I do not see "McKinley Elementary School"

  @javascript
  Scenario: Search for a preschool on mobile
    When I type "1550 Eddy Street, San Francisco CA 94115" into "searchString"
      And I select "Preschool" from "gradeLevels"
      And I click the button "Search"
    Then the title has "Search Results"
      And I see "Montessori School of the Bay Area"

  @javascript
  Scenario: Search for preschools on mobile does not return elementary schools
    When I type "1025 14th Street, San Francisco CA 94114" into "searchString"
      And I select "Preschool" from "gradeLevels"
      And I click the button "Search"
    Then the title has "Search Results"
      And I do not see "McKinley Elementary School"

  @javascript
  Scenario: Search for a school by name on mobile
    When I click on "By name tab"
      And I type "Lowell High School" into "q"
      And I select "CA" from "state"
      And I click the button "Search"
    Then the title has "Search Results"
      And I see "Lowell High School"

  @javascript
  Scenario: Cannot search by location without a search string mobile
    When I click the button "Search"
    Then I see "Please enter an address"

  @javascript
  Scenario: Cannot search by name without a search string and state mobile
    When I click on "By name tab"
      And I click the button "Search"
    Then I see "Please enter a school name"
      And I see "Please select a state"

