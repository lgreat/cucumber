parent_review_landing_page_selectors = {
    'the state dropdown' => '#js-reviewLandingState .arrow_btn',
    'the terms of use checkbox' => '#js-reviewLandingCheckboxTerms .js-checkBoxSpriteOff',
    'who am I box' => '#js-reviewLandingIAm .js-selectBox',
    'the post button' => '#js-submitParentReview',
    'the success page' => '.js-pageThreeReviewLandingPage',
    'Overall' => '#starRatingContainerReview',
    'Teacher quality' => '#starRatingContainerReviewTeacher',
    'Principal leadership' => '#starRatingContainerReviewPrincipal',
    'Parent involvement' => '#starRatingContainerReviewParent',
    'monthly email updates box' => '#js-reviewLandingCheckboxEmail',
    'message validation' => '.js-reviewContent-error',
    'rating validation' =>  '.js-overallAsString-error',
    'role validation' => '.js-posterAsString-error',
    'terms validation' => '.js-parentReviewTerms-error',
    'email validation' => '.js-email-error',

}

define_page_selectors 'parent review landing', parent_review_landing_page_selectors
define_page_selectors 'learning issues landing', parent_review_landing_page_selectors

When /^I cannot select "([^\"]+)" in ([A-Z]{2}) to review$/ do |school_name, state|
  step 'I click on "the state dropdown"'
  page.find('li', :text=>state, :match => :prefer_exact).click
  page.fill_in('js-parentReviewLandingPageSchoolSelectInput', :with => school_name)
  sleep 3 # for autocomplete
  page.first('a', :text=>school_name).should be_nil
end

When /^I select my school to review: "([^\"]+)" in ([A-Z]{2})$/ do |school_name, state|
  step 'I click on "the state dropdown"'
  page.find('li', :text=>state, :match => :prefer_exact).click
  sleep 1
  page.fill_in('js-parentReviewLandingPageSchoolSelectInput', :with => school_name)
  sleep 3 # for autocomplete
  page.find('a', :text=>school_name).click
  page.click_button 'js_submitSelectSchool'
end

When /^I select my role: "([^\"]+)"$/ do |role|
  page.find('.js-ddValues', :text=>role).click
end

When /^I select a[n]? "([^\"]+)" star rating$/ do |scope|
  star_container_selector = selector_for(scope)
  page.execute_script "$('#{star_container_selector}').trigger({type:'click', pageX:$('#{star_container_selector}').offset().left});"
end

When /^the monthly email updates box is by default already checked$/ do
  my_selector = selector_for('monthly email updates box')
  find(:css, my_selector +' .js-checkBoxSpriteOn').should be_visible
  find(:css, my_selector + ' .js-checkBoxSpriteOff', :visible => false).should_not be_visible
end

When /^I see the following content:$/ do |table|
  table.rows_hash.each do |pageElement, message|
  element = find(:css, selector_for(pageElement))
  element.should be_visible
  element.should have_content(message)
  end
end