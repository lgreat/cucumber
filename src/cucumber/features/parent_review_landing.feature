Feature: Parent Review Landing Page (The Scoop)
  As a user
  I want to be able to quickly find a school and submit a review
  So I can share my knowledge with others

  @javascript
  Scenario: I can select a school to review
    Given I am on "parent review landing" page
      And I select my school to review: "Northern Lights ABC" in AK
      And I wait 2 seconds for the school to load
    Then I see "Northern Lights ABC"
      And I see "2424 East Dowling Rd"

  @javascript @readwrite
  Scenario: I can submit a review
    Given I am on "parent review landing" page with parameters:
      | id    | 153 |
      | state | ak  |
      And I select an "Overall" star rating
      And I type "This is an automated test review. This school is cool. Because it's in Alaska ... get it?!?" into "comments"
      And I click on "the terms of use checkbox"
      And I click on "who am I box"
      And I select my role: "Parent"
      And I select a "Teacher quality" star rating
      And I select a "Principal leadership" star rating
      And I select a "Parent involvement" star rating
      And I type "aroy+[TIMESTAMP]@greatschools.org" into "email"
      And I click on "the post button"
      And I wait 3 seconds for the success page to load
    Then "the success page" is visible

#    @javascript
#    Scenario: The email box is pre-checked on the review landing page
#      Given I am on "parent review landing" page with parameters:
#        | id    | 153 |
#        | state | ak  |