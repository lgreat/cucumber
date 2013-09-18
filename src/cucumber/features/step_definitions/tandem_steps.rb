Then /^I should see the tandem calendar list view$/ do
  page.should have_selector('#js-calendar-list-event-template', visible: true)
end

Then /^it should default to the current month$/ do
	current_month = Date.today.strftime("%B")
	page.find(:css, "#js-school-event-list", :visible => false).visible?.should have_content(current_month)
end