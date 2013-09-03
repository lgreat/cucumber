Given /I am on "([^\"]+)"'s K12 page/ do |state|
  # Load K12 page for this state
  visit "/online-education.page?school=#{state}"
end

When /GreatSchools affiliate codes are captured including the code from "([^\"]+)"/ do |linkWithSchoolCode|
  schoolCode = linkWithSchoolCode.partition('/').last.downcase
  myTrackingLinks = page.all ("a.js-k12ClickThrough")
  codeParam = "school=#{schoolCode}"
  myTrackingLinks.each { |link|
    link[:href].should include "affl=gr8t"
    link[:href].should include "page=ot"
    link[:href].should match(%r{#{codeParam}}i) #case-insensitive match
    link[:href].should match(%r{k12\.com}i) #case-insensitive match
  }
end