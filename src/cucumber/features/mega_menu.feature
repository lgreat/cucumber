Feature: GreatSchools Mega Menu Tests
  As a parent
  I want to quickly access education and grade level topics
  So that I can educate myself

  Background:
    Given I am on "GS Home Page" page

  Scenario Outline: I can navigate the navigational menu
    When I click on the "<link_name>" link in the menu type is "<menu>"
#    And the title has "<heading_title> | GreatSchools"
      And I see "<link_href>" in the URL


  Examples:
    | link_name               | menu          | link_href                                                    |
    | Find a School           | primary nav   | find-schools/                                                |
    | Review Your School      | primary nav   | school/parentReview.page                                     |
    | Preschool               | primary nav   | preschool/                                                   |
    | Elementary School       | primary nav   | elementary-school/                                           |
    | Middle School           | primary nav   | middle-school/                                               |
    | High School             | primary nav   | high-school/                                                 |
    | Hot Topics              | secondary nav | hot-topics.topic?content=4932                                |
    | Worksheets & Activities | secondary nav | worksheets-activities.topic?content=4313                     |
    | Homework Help           | secondary nav | homework-help.topic?content=1544                             |
    | Parenting Dilemmas      | secondary nav | parenting-dilemmas.topic?content=4321                        |
    | Learning Difficulties   | secondary nav | special-education.topic?content=1541                         |
    | Health & Behavior       | secondary nav | parenting.topic?content=1539                                 |
    | Common Core             | secondary nav | understanding-common-core-state-standards.topic?content=7802 |

