HOW_TO_VERIFY_FILTER_MAP = {
    'Public' => 'I see "st=public" in the URL',
    'Private' => 'I see "st=private" in the URL',
    'Public charter' => 'I see "st=charter" in the URL',
    'Preschool' => 'I see "gradeLevels=p" in the URL',
    'Elementary School' => 'I see "gradeLevels=e" in the URL',
    'Middle School' => 'I see "gradeLevels=m" in the URL',
    'High School' => 'I see "gradeLevels=h" in the URL',
    '1 mile' => 'I see "distance=1" in the URL',
    '10 miles' => 'I see "distance=10" in the URL',
    '60 miles' => 'I see "distance=60" in the URL',
    'Below average (1-3)' => 'I see "ratingCategories=low" in the URL',
    'Average (4-7)' => 'I see "ratingCategories=average" in the URL',
    'Above average (8-10)' => 'I see "ratingCategories=high" in the URL',
}

HOW_TO_APPLY_FILTER_MAP = {
    'school type' => {:filter_input_type=>'checkbox', :filter_outer_label=>'All school types'},
    'grade level' => {:filter_input_type=>'checkbox', :filter_outer_label=>'All grade levels'},
    'distance' => {:filter_input_type=>'radio button', :filter_outer_label=>'5 miles'},
    'GS rating' => {:filter_input_type=>'checkbox', :filter_outer_label=>'Any rating', :extra_step=>'I click the button "Save" within the search results filter by rating dialog'}
}

When /^I see "([^\"]+)" filter is applied$/ do |filter_name|
  if HOW_TO_VERIFY_FILTER_MAP.has_key?(filter_name)
    step HOW_TO_VERIFY_FILTER_MAP[filter_name]
  else
    raise "Can't find mapping from filter \"#{filter_name}\" to a filter verification action.\n" +
              "Now, go and add a mapping in #{__FILE__}"
  end
end

When /^I apply the "([^\"]+)" (school type|grade level|distance|GS rating) filter$/ do |filter_label, filter_type|
  if HOW_TO_APPLY_FILTER_MAP.has_key?(filter_type)
    steps %Q{
      When I click on "#{HOW_TO_APPLY_FILTER_MAP[filter_type][:filter_outer_label]}"
      And I click the #{HOW_TO_APPLY_FILTER_MAP[filter_type][:filter_input_type]} "#{filter_label}"
    }
    if HOW_TO_APPLY_FILTER_MAP[filter_type][:extra_step]
      step HOW_TO_APPLY_FILTER_MAP[filter_type][:extra_step]
    end
  else
    raise "Can't find mapping from filter \"#{filter_type}\" to a filter apply action.\n" +
              "Now, go and add a mapping in #{__FILE__}"
  end
end