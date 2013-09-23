Feature: Test the filters on search by name
  As a parent
  I want to be able to filter search results
  So that I can quickly find a set of schools I am interested in
  and make informed school choices

  @javascript
  Scenario Outline: I can filter by school type and grade level
    Given I am on "Search Alameda By Name" page
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

  @javascript
  Scenario Outline: I can filter by rating on a Packard by name search
    Given I am on "Search Oakland By Name" page
    When I apply the "<label>" <filter_type> filter
    And I wait 3 seconds for the school to load
    Then I see "<label>" filter is applied

  Examples:
    | label                | filter_type |
    | Below average (1-3)  | GS rating   |
    | Average (4-7)        | GS rating   |
    | Above average (8-10) | GS rating   |
