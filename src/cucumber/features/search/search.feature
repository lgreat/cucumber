Feature: GreatSchools Global Search Tests
  As a parent
  I want to be able to quickly search for schools by name or city
  So that I can make informed school choices

  Background:
    Given I am on "GS Home Page" page

  @javascript
  Scenario: Search for a school with no state
    When I global search for "Golda Meir School" in --
    Then I see an alert with "Please select a state."
    And I stay on "GS Home Page"

  Scenario: Search for a school
    When I global search for "Golda Meir School" in WI
    Then I see "1555 N Martin Luther King Dr"
    And the title has "GreatSchools.org Search: Golda Meir School"

  Scenario: Search for San Francisco city
    When I global search for "San Francisco" in CA
    Then the title has "San Francisco Schools - San Francisco, CA | GreatSchools"

  Scenario Outline: Search on Local cities should redirect to its city browse page
    When I global search for "<city>" in <state>
    Then I see "<cityBrowse>"
    And the title has "<title> | GreatSchools"
  Examples:
    | city         | state | cityBrowse           | title                                   |
    | Indianapolis | IN    | Indianapolis Schools | Indianapolis Schools - Indianapolis, IN |
    | Speedway     | IN    | Speedway Schools     | Speedway Schools - Speedway, IN         |
    | Beech Grove  | IN    | Beech Grove Schools  | Beech Grove Schools - Beech Grove, IN   |
    | Milwaukee    | WI    | Milwaukee Schools    | Milwaukee Schools - Milwaukee, WI       |
    | Washington   | DC    | Washington Schools   | Washington, DC Schools - Washington, DC |

  Scenario: I can search for Alameda High School and navigate to its profile page
    When I global search for "Alameda" in CA
    And I click on the "Alameda High School" link
    Then the title has "Alameda High School"
    And I see new school profile tab navigation

