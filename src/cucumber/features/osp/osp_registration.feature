Feature: OSP Registration
    
  @javascript @readwrite
  Scenario: Submit for a school's OSP
    Given I am on "GS Home Page" page
    And I click on the "School Officials" link
    And I fill in:
    | email | devnull+[TIMESTAMP]@greatschools.org |
    | screenName | user[TIMESTAMP] |
    | password  | abc123 |
    | confirmPassword | abc123 |
    And I pick:
    | state | FL |
    | city | Jacksonville |
    | schoolId | Arlington Elementary School |
    And I fill in:
    | firstName | Cucumber |
    | lastName | Tester |
    And I pick:
    | jobTitle | Teacher |
    When I click the button "Continue"
    And I wait 3 seconds
    Then I stay on "Arlington Elementary School Profile Page"
    And the almost there hover is visible
    
  Scenario: Pre-filled OSP registration form
    Given I am on "Akula Elitnaurvik School Programs Page" page
    #Given I am on "Alameda High School Profile Page" page
    And I click on the "here" link
    Then I see "state=ak" in the URL
    And I see "city=Kasigluk" in the URL
    And I see "schoolId=12" in the URL
    And I see elements:
    | state | AK |
    # Need to check with engineer how to verify city & schoolId
    #| city | Kasigluk |
    #| schoolId | 12 |