define_page_selectors 'MSL', {
    'school list' => '#js-schoolListData',
    'MSL Join hover' => '#joinHover',
    'Join hover' => '#joinHover'
}

Then /^I should see the MSL count$/ do
  count_text = page.find('#utilLinks li.last a').text
  count_text.should_not be_nil
  count_match = count_text.match /\d+/
  count = nil
  if !count_match.nil? && count_match.length > 0
    count = count_match[0].to_i
  end
  count.should_not be_nil
  count
end

Then /^I clear my school list$/ do
  count = step('I should see the MSL count')
  if !count.nil? && count > 0
      (1..count).each do
        first('.fr .media a').click
      end
  end
end

Then /^My school list count should be (\d)$/ do |count|
  actual_count = step('I should see the MSL count')
  actual_count.should == count.to_i
end

Then /^My school list should have (.+)$/ do |school_name|
  page.find(selector_for('school list')).should have_content(school_name)
end