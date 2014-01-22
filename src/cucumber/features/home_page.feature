Feature: GreatSchools Home Page Tests 555

  Background:
    Given I am on "GS Home Page" page

  Scenario: Verify title
    Then the title has "GreatSchools - Public and Private School Ratings, Reviews and Parent Community"

  @mobile
  Scenario: Verify mobile title
    Then the title has "Public, Private, and Charter School Ratings and Reviews"
