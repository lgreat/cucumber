Feature: GreatSchools School Profile College Readiness
  As a parent
  I want to see college readiness data on Indiana public schools
  So I can make informed school choice decisions

  Scenario Outline: I see college preparedness data displayed on a variety of schools
    Given I am on the profile page for <state>-<id> "college-readiness" tab
    Then I see college preparedness data
  Examples:
    | state   | id   |
    | Indiana | 2049 |
    | Indiana | 593  |
    | Indiana | 2298  |

  Scenario Outline: I do not see college preparedness data displayed on a variety of schools
    Given I am on the profile page for <state>-<id> "test-scores" tab
    Then I do not see college preparedness data
  Examples:
    | state      | id   |
    | California | 1    |
    | Indiana    | 1371 |
    | Indiana    | 1967 |
    | Texas      | 272  |