Feature: GreatSchools School Profile Test Scores
  As a parent
  I want to see test score data
  So I can make informed school choice decisions

  @javascript
  Scenario Outline: I can pre-display certain test results by using a URL parameter
    Given I am on "Arlington Elementary School Profile Page" page with parameters:
      | tab    | test-scores |
      | testId | <test_key>  |
    Then I see "<test_name>" test results
  Examples:
    | test_key | test_name                                            |
    | 162      | Florida Comprehensive Assessment Test 2 (FCAT 2)     |
    | 3        | Florida School Grades                                |
    | 1        | Florida Comprehensive Assessment Test (FCAT) Results |