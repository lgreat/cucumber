Feature: Test the filters on search by location
  As a parent
  I want to be able to filter search results
  So that I can quickly find a set of schools I am interested in
  and make informed school choices

  Background: From a search by location results page
    Given I am on "Search Alameda By Location" page

  @javascript
  Scenario Outline: I can filter by school type, grade level and distance
    When I apply the "<label>" <filter_type> filter
    And I wait 3 seconds for the school to load
    Then I see "<label>" filter is applied

  Examples:
    | label             | filter_type |
    | Public            | school type |
    | Private           | school type |
    | Public charter    | school type |
    | Preschool         | grade level |
    | Elementary School | grade level |
    | Middle School     | grade level |
    | High School       | grade level |
    | 1 mile            | distance    |
    | 10 miles          | distance    |
    | 60 miles          | distance    |
