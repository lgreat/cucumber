# Steps specific to the Find a School page

When /^I do a by name search for (.+) in (.+)$/ do |query, state|
  page.find(:css, "#byNameTab.small_bold").click
  steps %Q{
    And I type "#{query}" into "js-findByNameBox"
  }
  page.find('#jq-findByNameForm .stateSelect option', :text => state).select_option
  page.find(:css, "button.button-4.searchSubmit").click
end

When /^I do a by location search for (.+)$/ do |query|
  page.find(:css, "#byLocationTab.small_bold").click
  steps %Q{
    And I type "#{query}" into "js-findByLocationBox"
  }
  page.find(:css, "button.button-4.searchSubmit").click
end
