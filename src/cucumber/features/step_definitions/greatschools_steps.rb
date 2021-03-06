URLS = {
  "GS Home Page" => "/",
  "Golda Meir School Profile Page" => "/wisconsin/milwaukee/1110-Golda-Meir-School/",
  "Alameda High School Profile Page" => "/california/alameda/1-Alameda-High-School/",
  "Sanders School Profile Page" => "/indiana/indianapolis/1917-Sanders-School/",
  "Franklin Central High School Profile" => "/indiana/indianapolis/593-Franklin-Central-High-School/",
  "Bayside Montessori Association Profile Page" => "/california/alameda/preschools/Bayside-Montessori-Association/13358/",
  "Arlington Elementary School Profile Page" => "/florida/jacksonville/946-Arlington-Elementary-School/",
  "Find a School" => "/find-schools/",
  "MSL" => "/mySchoolList.page",
  "OSP" => "/official-school-profile/register.page",
  'Search Alameda By Location' => '/search/search.page?lat=37.7652065&lon=-122.24163550000003&state=CA&normalizedAddress=Alameda,%20CA&locationSearchString=Alameda,%20ca&city=Alameda&sortBy=DISTANCE',
  'Search Alameda By Name' => '/search/search.page?q=Alameda&state=CA',
  'Search Oakland By Name' => '/search/search.page?q=Oakland&state=CA',
  'Akula Elitnaurvik School Programs Page' => '/alaska/kasigluk/12-Akula-Elitnaurvik-School/?tab=programs-resources',
  'an article' => '/students/travel/635-flying-with-kids.gs',
  'parent review landing' => '/school/parentReview.page',
  'learning issues landing' => '/school/parentReview.page?topicId=1',
  'OECD test for schools' => '/OECDTestForSchools.page',
}

if defined? Capybara && Capybara.app_host=~/localhost/
  URLS['GS Home Page'] = "/index.page" # Get it to work on localhost
end

When /^I global search for "([^\"]*)" in ([a-zA-Z-][a-zA-Z-])$/ do |query, state|
  # duplicate id on page, so use this to select out the right one
  page.find('#topnav_search_school').find('#qNew').set query
  if state == '--'
    page.find('#topnav_search_school .stateSelect option', :text => 'State').select_option
  else
    page.find('#topnav_search_school .stateSelect option', :text => state).select_option
  end
  page.find(:css, "#topnav_search .searchBarSchool button").click
end

When /^I pick:$/ do |table|
  table.rows_hash.each do |field, value|
    page.select value, :from => field	
  end 
end

When /^I sign in as "([^\"]*)" with password "([^\"]*)"$/ do |email, password|
  page.click_link "Sign In"
  fill_in "semail", :with => email
  fill_in "spword", :with => password
  click_button "signinBtn"
  sleep 3
end

When /^I fill in:$/ do |table|

  table.rows_hash.each do |field, value|
    timestamp = Time.new.to_time.to_i.to_s
    value.gsub! '[TIMESTAMP]', timestamp

    fill_in field, :with => value, :match => :prefer_exact
  end
end

When /^I see elements:$/ do |table|
  table.rows_hash.each do |field, value|
    page.should have_select(field, :selected => value)
  end
end

Then /^I submit my "(.*?)" and confirmation email and Sign up$/ do |o_email|
  timestamp = Time.new.to_time.to_i.to_s
  fill_in 'Email address', :with => o_email+timestamp+"@greatschools.org"
  fill_in 'Confirm email address', :with => o_email+timestamp+"@greatschools.org"
  within selector_for 'MSS hover' do
    click_button 'Sign up'
  end
end

Then(/^I submit my "(.*?)" and Reset password$/) do |fpemail|
  fill_in 'Email address', :with => fpemail
  click_button 'Reset password'
  sleep 3 
end

Then "the MSS Email Validation hover is visible" do
  page.should have_content("Your subscription has been confirmed")
  steps %Q{
    Then I see the Subscription Confirmed hover
  }
end

When /^I wait (\d+) second[s]?/ do |seconds|
  sleep seconds.to_i
end