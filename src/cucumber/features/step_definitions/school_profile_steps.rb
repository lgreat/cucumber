When /^I click on the (.+) tab$/ do |tab_name|
  page.find(:xpath, "//*/li[@data-gs-tab]/a[text() = '#{tab_name}']").click
end

When /^I am on the (.+) tab$/ do |tab_name|
  page.visit current_url + "?tab=#{tab_name.downcase}"
end

When /^I close the hover$/ do
  # close any visible hover, for now
  page.find(:css, ".js_closeHover", :visible => true).click
end

Given /^I am on "([^\"]+)" school profile$/ do |page_name|
  steps %Q{
    Given I am on "#{page_name} Profile Page" page
    Then the title has "#{page_name}"
      And the title has "School overview"
  }
end

Given /^I am on "([^\"]+)" old profile$/ do |page_name|
  steps %Q{
    Given I am on "#{page_name}" school profile
    Then I see "School Stats"
      But I do not see "Student subgroups"
  }
end

Given /^I am on "([^\"]+)" new profile$/ do |page_name|
  steps %Q{
    Given I am on "#{page_name}" school profile
    Then I see new school profile tab navigation
  }
end

When "I see new school profile tab navigation" do
  steps %Q{
    And I see "Overview"
    And I see "Reviews"
    And I see "Test scores"
    And I see "Students & teachers"
    And I see "Programs & culture"
    And I see "Enrollment"
  }
end

