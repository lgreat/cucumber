Feature: GreatSchools School Overview
  As a parent
  I want to quickly access school data and reviews
  So that I can educate myself

#  Background: I am on the "GreatSchools School Overview" page
#    Given I am on the profile page for Ohio-2755 "overview" tab

#  Scenario: I can navigate the GreatSchools ratings tile         @cliu

@data_brittle
Scenario Outline: I can click through tiles to see detailed views on sub-tabs
   Given I am on the profile page for <state>-<id> "overview" tab
   And I click on "<tile>"
   Then I see "tab=<tabname>" in the URL
   Then I see "<text>"

   Examples:

     | state      | id    | tile                 | tabname            | text                              |
     | ohio       | 2755  | GreatSchools ratings | ratings            | What is the GreatSchools Rating?  |
     | ohio       | 2755  | Community rating     | reviews            | Rate this school                  |
     | ohio       | 2755  | Community reviews    | reviews            | reviews of this school            |
     | ohio       | 2755  | Student diversity    | demographics       | Student ethnicity                 |
     | ohio       | 2755  | Staff                | programs-resources | School basics                     |
     | ohio       | 2755  | Students per teacher | teachers           | Student-teacher ratio             |
     | ohio       | 2755  | Highlights           | programs-resources | School basics                     |
     | wisconsin  | 1110  | Special education    | programs-culture   | Special education / special needs |
     | wisconsin  | 1110  | Transportation       | programs-resources | Resources                         |
     | wisconsin  | 1110  | Programs             | programs-resources | Programs                          |
     | wisconsin  | 1110  | Getting In           | enrollment         | Apply                             |
     | wisconsin  | 1110  | Extracurriculars     | extracurriculars   | Sports                            |
     | california | 1     | About the school     | programs-resources | School basics                     |
     | california | 1     | Tandem               | culture            | Upcoming Events                   |
     | california | 11902 | Awards & recognition | programs-culture   | Awards                            |
     | wisconsin  | 3425  | Special education    | programs-culture   | Special education / special needs |
     | wisconsin  | 3425  | Extended care        | programs-resources | School basics                     |


  @data_brittle
 Scenario Outline: I can click through tiles to see detailed views on other pages
   Given I am on the profile page for <state>-<id> "overview" tab
   And I click on "<tile>"
   Then I see "<url_fragment>" in the URL
   Then I see "<text>"

   Examples:

     | state | id   | tile                 | url_fragment                   | text                                 |
     | ohio  | 2755 | USP promo            | QandA                          | Help others by sharing what you know |
     | ohio  | 2755 | District boundary    | school-district-boundaries-map | School and District Boundaries Map   |
     | ohio  | 2755 | District information | school-district-boundaries-map | School and District Boundaries Map   |
#      | california | 1 |       Finding the right school        | school-choice |   How to choose the right          |
#     | california | 11902 | Add photos        | official-school-profile        | Request a school administrator account |   url not going to alameda
#| wisconsin| 1110 | Finding the right school |  school-choice       |    Finding the right elementary school |   not passing, cant find tile id

@data_brittle
Scenario Outline: I can click through the "Be sure to visit " tile to see school checklists
  Given I am on the profile page for <state>-<id> "overview" tab
  When  I follow the "<school_type>" link in "<tile>"
  Then I see "<url_fragment>" in the URL

Examples:

  | state      | id   | tile             | school_type       | url_fragment                    |
  | ohio       | 2755 | Be sure to visit | Preschool         | preschool-checklist.pdf         |
  | ohio       | 2755 | Be sure to visit | Elementary school | elementary-school-checklist.pdf |
  | california | 246  | Be sure to visit | Middle school     | middle-school-checklist.pdf     |
  | california | 1    | Be sure to visit | High school       | high-school-checklist.pdf       |

@data_brittle
Scenario Outline: I can see the tiles on the overview page      @cliu
  Given I am on the profile page for <state>-<id> "overview" tab
  Then "<tile>" is visible

Examples:
  | state      | id   | tile |
  | wisconsin  | 3425 |   Best known for       |
  | wisconsin  | 3425 |   Neighborhood Info      |
  | wisconsin  | 3425 |   Facebook      |



#  Scenario: I am on the school overview page
#   Given I am on the profile page for Ohio-2755 "overview" tab
#    And I click on "GreatSchools ratings"
#    Then the title has "School Ratings for "
#   Then I see "What is the GreatSchools Rating?"
#
# @javascript @data_brittle
# Scenario: I am on the school overview page         @cliu
#   Given I am on the profile page for Ohio-2755 "overview" tab
#    And I click on ""
#    Then the title has " "
#   Then I see ""







