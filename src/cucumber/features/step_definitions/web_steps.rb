DEFAULT_SELECTORS = {
    'Sign In hover' => '#signInHover',
    'MSL Join hover' => '#joinHover',
    'MSS hover' => '#js-sendMeUpdates',
    'Newsletter hover$' => '#hover_nlSubscription',
    'All school types' => 'div#js-schoolTypes div[data-gs-dropdown-opener]',
    'All grade levels' => 'div#js-gradeLevels div[data-gs-dropdown-opener]',
    '5 miles' => 'div#js-radius div[data-gs-dropdown-opener]',
    'Any rating' => 'div#js-ratingsDropDown div[data-gs-dropdown-opener]',
    'the search results filter by rating dialog' => '#js-ratingsDropDown'
}
PAGE_SELECTORS ||= {}
CURRENT_SELECTORS ||= {}

def setup_selectors(page_name)
  CURRENT_SELECTORS.clear
  CURRENT_SELECTORS.merge! DEFAULT_SELECTORS
  if PAGE_SELECTORS[page_name]
    CURRENT_SELECTORS.merge! PAGE_SELECTORS[page_name]
  end
end

def selector_for(scope) # http://bjeanes.com/2010/09/selector-free-cucumber-scenarios
  case scope
    when /Newsletter hover$/
      '#hover_nlSubscription'
    else
      if CURRENT_SELECTORS[scope]
        return CURRENT_SELECTORS[scope]
      end

      CURRENT_SELECTORS.each do |key, val|
        if scope.match /#{key}/i
          return val
        end
      end

      raise "Can't find mapping from \"#{scope}\" to a selector.\n" + "Now, go and add a mapping in #{__FILE__}"
  end
end

Given /^I am on "([^\"]+)" page$/ do |page_name|
  setup_selectors page_name
  visit URLS[page_name]
end

Then /^I stay on "([^\"]*)"$/ do |page_name|
  page.current_path.should eq URLS[page_name]
end

When /^I see "([^\"]+)" in the URL$/ do |substring|
  sleep 1
  page.current_url.should include substring
end

When /^I click on the "([^\"]+)" link$/ do |link_name|
  page.click_link link_name
end

When /^I click the button "([^\"]+)"$/ do |button_name|
  page.click_button button_name
end

When /^I click on "([^\"]+)"$/ do |scope|
  find(selector_for(scope)).click
end

When /^I type "([^\"]*)" into "([^\"]*)"$/ do |text, field|
  fill_in field, :with => text
end

When /^I select "([^\"]*)" from "([^\"]*)"$/ do |label, selector|
  page.select label, :from => selector
end

When /^I click the checkbox "([^\"]+)"$/ do |selector|
  page.check selector
end

When /^I click the radio button "([^\"]+)"$/ do |selector|
  page.choose selector
end

When /^I see "([^\"]*)"$/ do |text|
  page.should have_content(text)
end

When /^I see "([^\"]*)", "([^\"]*)" and "([^\"]*)"/ do |field1, field2, field3|
  page.should have_content(field1)
  page.should have_content(field2)
  page.should have_content(field3)
end

When /^I do not see "([^\"]*)"$/ do |text|
  page.should have_no_content(text)
end

When /^the title has "(.*?)"$/ do |title|
  # Current version of capybara has inconsistent methods of accessing page title depending on driver
  title_elem = page.find(:xpath, '//title')
  title_text = title_elem.text
  if title_text.nil? || title_text.empty?
    title_text = title_elem[:text]
  end
  title_text.should include title
end

When /^I see an alert with "(.*?)"$/ do |alert_expected|
  page.driver.browser.switch_to.alert.text.should eq alert_expected
  page.driver.browser.switch_to.alert.accept
end

When /^the "([^\"]+)" element is visible$/ do |my_selector|
  find(:css, my_selector).should be_visible
end

# http://bjeanes.com/2010/09/selector-free-cucumber-scenarios
When /^(.*) within ([^:"]+)$/ do |step, scope|
  within(selector_for(scope)) do
    step(step)
  end
end

When /^(.*) within ([^:"]+):$/ do |step, scope, table_or_string|
  within(selector_for(scope)) do
    step("#{step}:", table_or_string)
  end
end

When /^show me the page$/ do
  save_and_open_page
end

When 'I scroll halfway down the page' do
  page.execute_script 'window.scrollBy(0,document.body.scrollHeight/2)'
end
