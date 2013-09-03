Feature: My School List

Background:
  Given I am on "GS Home Page" page


# Signed in scenarios:

@javascript
Scenario: See how many schools are in my school list
  Given I sign in as "mbecerra+en@greatschools.org" with password "abc123"
  Then I should see the MSL count

@javascript
Scenario: Clear my school list
  Given I sign in as "mbecerra+en@greatschools.org" with password "abc123"
  And I am on "MSL" page
  When I clear my school list
  Then My school list count should be 0

@javascript
Scenario: Add school to my school list while signed in
  Given I sign in as "mbecerra+en@greatschools.org" with password "abc123"
  And I am on "MSL" page
  And I clear my school list
  And I am on "Alameda High School Profile Page" page
  When I click on the "Add to My School List" link
  And I am on "MSL" page
  Then My school list should have Alameda High School

# Not signed in scenarios:

@javascript
Scenario: Add school to my school list while signed out
  Given I am on "Golda Meir School Profile Page" page
  When I click on the "Add to My School List" link
  Then I should see the MSL Join hover
  # Needing to sleep here seems to be indicative of a problem, since this flow shouldn't break if the "Sign In" hover
  # is immediately launched from the MSL join hover
  And I wait 3 seconds
  When I click on the Sign in link in the hover
  Then I should see the Sign In hover
  When I fill in within Sign in hover:
  | email | mbecerra+en@greatschools.org |
  | password  | abc123 |
  And I click the button "Sign in"
  # I tried using the "wait_until" method with a condition to look for the Welcome message in the global header
  # but couldn't get it to work. Instead just sleep for 3 seconds
  And I wait 3 seconds for the form to submit
  And I am on "MSL" page
  Then My school list should have Golda Meir School
  Then I clear my school list
