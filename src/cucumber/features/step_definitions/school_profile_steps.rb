When /^I click on the "([^\"]+)" tab$/ do |tab_name|
  page.find(:xpath, "//*/li[@data-gs-tab]/a[text() = '#{tab_name}']").click
end

When /^I am on the "([^\"]+)" tab$/ do |tab_name|
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

When /^I am on "([^\"]+)" page "([^\"]+)" tab$/ do |page_name, tab_name|
  setup_selectors page_name
  visit (URLS[page_name] + "?tab=" + tab_name)
end

When /^I am on the profile page for ([a-zA-Z\-]+)-(\d+) "([^\"]+)" tab$/ do |state, id, tab|
  visit "/#{state}/city/#{id}-school/?tab=#{tab}"
end

When 'I see college preparedness data' do
  steps %Q{
    Then I see "College preparedness"
      And I see "Enroll in college immediately after high school graduation"
      And I see "Need remediation"
      And I see "Average first year GPA"
      And I see "Average number of units completed in first year"
      And I see "Enroll in college for a second year"
  }
  end

When 'I do not see college preparedness data' do
  step 'I do not see "College preparedness"'
  step 'I do not see "Enroll in college immediately after high school graduation"'
  step 'I do not see "Need remediation"'
  step 'I do not see "Average first year GPA"'
  step 'I do not see "Average number of units completed in first year"'
  step 'I do not see "Enroll in college for a second year"'
end

When /^I see "([^\"]+)" test results$/ do |test_name|
  test_header_elem = page.find(:css, '#js_testLabelHeader')
  test_header_elem.should be_visible
  test_header_elem.should have_content(test_name)
end