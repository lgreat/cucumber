Feature: GreatSchools School Profile Enhanced Ratings Tab
  As a parent
  I want to see enhanced rating data
  So I can make informed school choice decisions off of more than just test scores

  @aroy
  Scenario Outline: I see enhanced rating data displayed on a variety of schools
    Given I am on the profile page for <state>-<id> "ratings" tab
    Then I see enhanced ratings including test scores and <breakdowns> ratings
  Examples:
    | state         | id   | breakdowns         |
    | Ohio          | 2755 | growth             |
    | Massachusetts | 912  | growth and college |
    | Colorado      | 499  | growth             |
    | Michigan      | 7    | college            |
