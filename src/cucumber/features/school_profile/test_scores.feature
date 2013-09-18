Feature: GreatSchools School Profile Test Scores
  As a parent
  I want to see test score data
  So I can make informed school choice decisions

  @javascript
  Scenario Outline: I can pre-display California test results by using a URL parameter
    Given I am on "Alameda High School Profile Page" page with parameters:
      | tab    | test-scores |
      | testId | <test_key>  |
    Then I see "<test_name>" test results
  Examples:
    | test_key    | test_name                               |
    | -1          | API                                     |
    | 999         | API                                     |
    | 18          | California Standards Tests              |
    | 18_subgroup | California Standards Tests by Subgroup  |
    | 19          | California High School Exit Examination |
    | 19_subgroup | California High School Exit Examination |
