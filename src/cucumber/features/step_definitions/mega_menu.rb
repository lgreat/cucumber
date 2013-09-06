define_page_selectors "GS Home Page", {


}

When /^I click on the "([^\"]+)" link in the menu type is "([^\"]+)"$/ do |link_name,menu|
  element = find(:css, selector_for(menu))
  element.should be_visible
  element.should have_content(link_name)
  element.click_link link_name
end

When /^the link id type is "([^\"]+)"$/ do |link_id|
  find(:css, link_id).should be_visible
end

#When /^I see "([^\"]*)"$/ do |text|
#  page.should have_content(text)
#end
