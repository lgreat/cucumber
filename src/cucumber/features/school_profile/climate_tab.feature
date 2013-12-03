Feature: GreatSchools School Profile Climate Tab
  As a parent
  I want to see climate survey data
  So I can make informed school choice decisions

  Scenario Outline: I see climate data displayed on a variety of schools
    Given I am on the profile page for <state>-<id> "climate" tab
    Then I see climate data
  Examples:
    | state      | id   |
    | California | 2037 |
    | new-york   | 1790 |

  Scenario Outline: I do not see climate data displayed on a variety of schools
    Given I am on the profile page for <state>-<id> "climate" tab
    Then I do not see climate data
  Examples:
    | state      | id   |
    | Indiana    | 1371 |
    | Texas      | 272  |
    | California | 1    |
    | new-york   | 1337 |
