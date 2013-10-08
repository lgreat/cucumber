Feature: GreatSchools Newsletter Signups

  @javascript
  Scenario: Prompted with newsletter signup
    Given I am on "an article" page
    When I scroll halfway down the page
    Then I should see the Newsletter hover
