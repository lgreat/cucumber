school_profile_page_selectors = {
    'GreatSchools ratings' => '#js-tile-1-3 .js-trigger',
    'USP promo' => '.js-tile-2-0',
    'District boundary' => '.js-tile-3-0',
    'District information' => '#js-tile-13-1 .js-trigger',
    'Community rating' => '#js-tile-4-0 .js-trigger',
    'Community reviews' => '#js-tile-5-0 .js-trigger',
    'Student diversity' => '#js-tile-7-0 .js-trigger',
    'Staff' => '#js-tile-8-1 .js-trigger',
    'Students per teacher' => '#js-tile-9-1-1 .js-trigger',
    'Highlights' => '#js-tile-10-2 .js-trigger',
    'Special education' => '#js-tile-8-0-1 .js-trigger',
    'Extended care' => '#js-tile-8-0-2 .js-trigger',
    'Transportation' => '#js-tile-9-0 .js-trigger',
    'Programs' => '#js-tile-10-0 .js-trigger',
    'Getting In' => '#js-tile-11-1 .js-trigger',
    'Extracurriculars' => '#js-tile-14-0 .js-trigger',
    'Finding the right school' => '.js-tile-4-3',
    'Tandem' => '#js-tile-tandem .js-trigger',
    'About the school' => '#js-tile-1-2 .js-trigger',
    'Awards & recognition' => '#js-tile-1-1 .js-trigger',
    'Add photos' => '#no-osp-add-pictures',
    'Be sure to visit' => '#js-tile-11-0',
    'Best known for' => '.bestKnownFor',
    'Neighborhood Info' => '#js-tile-13-2',
    'Facebook' => '#js_facebookModuleOverviewPage',
    #'' => '',
}

define_page_selectors 'school profiles', school_profile_page_selectors

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
  setup_selectors "school profiles"
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

When 'I see climate data' do
  step 'I see "Based on surveys from"'
end

When 'I do not see climate data' do
  step 'I do not see "Based on surveys from"'
end

When /^I see enhanced ratings including test scores and ([^\"]+)* ratings$/ do |breakdowns|
  step 'I see "What is the GreatSchools Rating?"'
  step 'I see "Breaking down the GreatSchools Rating"'
  step 'I see "How schools in the state rate"'
  step 'I see "Test score rating 20"'
  step 'I do not see "Climate ratings"'
  if breakdowns.match('growth')
    step 'I see "Student growth rating 20"'
  end
  if breakdowns.match('college')
    step 'I see "College readiness rating 20"'
  end
end

When /^I follow the "([^\"]*)" link in "([^\"]*)"$/ do |name, element|
  #blah = page.find(:css, element)
  within(find(selector_for(element))) do
    click_link(name)
  end
end

