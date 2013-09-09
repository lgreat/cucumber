define_page_selectors "GS Home Page", {
    'Find a School' => '#PN-FindASchool',

}

When /^I click on the "([^\"]+)" link in the menu type is "([^\"]+)"$/ do |link_name,menu|
  element = find(:css, selector_for(menu))
  element.should be_visible
  element.should have_content(link_name)
  element.click_link link_name
end

#work in progress
When /^I mouseover the "([^\"]+)"$/ do |topic|
  element = find(:css, selector_for(topic))
  element.trigger(:mouseover)
  sleep2
end

