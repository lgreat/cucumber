Feature: Topical Parent Review Landing Page (Learning Issues)
  As a user
  I want to be able to quickly find a school and submit a topical review
  So I can share my specific knowledge with others

  Scenario: I can load a learning differences review landing page
    Given I am on "learning issues landing" page
    Then I see "learning & attention issues"

  @javascript
  Scenario: I can select a school to topically review
    Given I am on "learning issues landing" page
    And I select my school to review: "Northern Lights ABC" in AK
    And I wait 2 seconds for the school to load
    Then I see "Northern Lights ABC"
    And I see "2424 East Dowling Rd"

  @javascript
  Scenario: I cannot select a preschool to topically review
    Given I am on "learning issues landing" page
    Then I cannot select "Tots University" in CA to review

  @javascript @readwrite
  Scenario: I can submit a topical review
    Given I am on "learning issues landing" page with parameters:
      | id    | 153 |
      | state | ak  |
    And I select an "Overall" star rating
    And I type "This is an automated test review. This school is cool. Because it's in Alaska ... get it?!?" into "comments"
    And I click on the "Add a category" link
    And I click on the "Reading" link
    And I click on "the terms of use checkbox"
    And I click on "who am I box"
    And I select my role: "Parent"
    And I type "devnull+[TIMESTAMP]@greatschools.org" into "email"
    And I click on "the post button"
    And I wait 3 seconds for the success page to load
    Then "the success page" is visible

  @javascript
  Scenario: The email box is pre-checked on the topical review landing page
    Given I am on "learning issues landing" page with parameters:
      | id    | 153 |
      | state | ak  |
    Then the monthly email updates box is by default already checked

  @javascript
  Scenario: I cannot submit a blank topic review
    Given I am on "learning issues landing" page with parameters:
      | id    | 153 |
      | state | ak  |
    And I click on "the post button"
    Then I see the following content:
      | message validation | Your review length must be less than 1200 chars and at least 15 words. |
      | rating validation  | Please rate the school by choosing a star.                             |
      | role validation    | Please select your role.                                               |
      | terms validation   | You must agree to the terms of use and guidelines.                     |
      | email validation   | You must enter a valid email address.                                  |