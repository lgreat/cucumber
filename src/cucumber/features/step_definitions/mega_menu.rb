#define_page_selectors "GS Home Page", {
#   #'primary nav' => '#gs_primary',
#   #'secondary nav' => '#gs-secondary',
#
#}

When /^the menu type is "([^\"]+)"$/ do |menu|
  should have_selector(menu)
  #find(:css, menu).should be_visible
end

When /^the link id type is "([^\"]+)"$/ do |link_id|
  find(:css, link_id).should be_visible
end

#When /^I see "([^\"]*)"$/ do |text|
#  page.should have_content(text)
#end
