Feature: GreatSchools Mega Menu Tests
  As a parent
  I want to quickly access education and grade level topics
  So that I can educate myself

  Background:
    Given I am on "GS Home Page" page

  Scenario Outline: I can navigate the navigational menu @cliu
    When I click on the "<link_name>" link
    And the menu type is "<menu>"
#    And the title has "<heading_title> | GreatSchools"
    Then the link id type is "<link_id>"
      And I see "<link_href>" in the URL


  Examples:
    | link_name             |   menu           | link_id | link_href |
    | Find a School         |    .menuContainer-secondary   | #PN-FindASchool |  find-schools/ |
#    | Review Your School | #gs-primary          | school/parentReview.page |
#    | Preschool          | #gs-primary          | preschool/ |
#    | Elementary School |    #gs-primary        | elementary-school/ |
#    | Middle School     |     #gs-primary       | middle-school/ |
#    | High School       |      #gs-primary      | high-school/ |
#    | Hot Topics        |        #gs-secondary  | hot-topics.topic?content=4932 |
#    | Worksheets & Activities | #gs-primary  | SN-AcademicsActivities | worksheets-activities.topic?content=4313 |
#    | Homework Help           | #gs-secondary  | homework-help.topic?content=1544 |
#    | Parenting Dilemmas      | #gs-secondary  | parenting-dilemmas.topic?content=4321 |
#    | Learning Difficulties   | #gs-secondary  | special-education.topic?content=1541 |
#    | Health & Behavior       | #gs-secondary  | parenting.topic?content=1539 |
#    | Raising a Reader        | #gs-secondary  | raising-a-reader.topic?content=7082 |

#  Scenario: I can navigate to the links in the drop down menu
#    When I ho

