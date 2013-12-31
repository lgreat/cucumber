@mobile
Feature: GreatSchools Mobile Parent Review Landing Page Test
  As a mobile user
  I want to be able to quickly find a school and submit a review
  So I can share my knowledge with others

  Background:
    Given I am on "GS Home Page" page

  @javascript
  Scenario: I can select a school to review
    And I click on the "Write a review" link
  Then I see "You know your school best!"

  @javascript
  Scenario: I can select a school to review from mobile school profile
    When I type "1651 Union Street, San Francisco CA 94123" into "searchString"
     And I click the button "Search"
     And I wait 1 second for the JavaScript to execute
    Then the title has "Search Results"
     And I click on the "Sherman Elementary School" link
     And I click on the "Write a review" link
    Then I see "Rate this school"

#  @javascript
#  Scenario Outline: I can navigate the mobile navigational menu
#    When I click on the "<link_name>" link in the menu type is "<menu>"
#    And I click on the "<link_href>" link
#  Then I see "You know your school best!"
#
#  Examples:
#    | link_name | menu | link_href |
#    | Top nav   | topnav_link | school/parentReview.page?fromMobile=true |

