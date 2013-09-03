define_page_selectors 'parent review landing', {
    'the state dropdown' => '#js-reviewLandingState .arrow_btn',
    'the terms of use checkbox' => '#js-reviewLandingCheckboxTerms .js-checkBoxSpriteOff',
    'who am I box' => '#js-reviewLandingIAm .js-selectBox',
    'the post button' => '#js-submitParentReview',
    'the success page' => '.js-pageThreeReviewLandingPage',
    'Overall' => '#starRatingContainerReview',
    'Teacher quality' => '#starRatingContainerReviewTeacher',
    'Principal leadership' => '#starRatingContainerReviewPrincipal',
    'Parent involvement' => '#starRatingContainerReviewParent',
}

When /^I select my school to review: "([^\"]+)" in ([A-Z]{2})$/ do |school_name, state|
  step 'I click on "the state dropdown"'
  page.find('li', :text=>state).click
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