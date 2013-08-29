Feature: GreatSchools Mega Menu Tests
  As a parent
  I want to quickly access education and grade level topics
  So that I can educate myself

  Background:
    Given I am on "GS Home Page" page
  
  Scenario Outline: I can navigate navigational menu
    When I click on the "<menu>" link
    Then I see "<heading_title>"
    And the title has "<heading_title> | GreatSchools"
  Examples:
    | menu             | heading_title                    |
    | Find a School     | Find a School In Your State     |
    | Preschool         | Preschool                       |
    | Elementary School | Elementary School               |
    | Middle School     | Middle School                   |
    | High School       | High School                     |
    | College           | College Prep                    |
    | Hot Topics        | Hot Parenting Topics            |
    | Worksheets & Activities | Worksheets & Activities   |
