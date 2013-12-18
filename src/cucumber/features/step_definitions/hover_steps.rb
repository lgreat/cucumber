HOVER_SELECTORS = {
    'MSL Join' => '#joinHover',
    'Sign In' => '#signInHover'
}

HOVER_TITLES = {
    'MSL Join' => 'Join GreatSchools',
    'Sign In' => 'Sign in to GreatSchools',
    'Newsletter' => 'Like this article?'
}

=begin
Then /^I should see the (.+) hover$/ do |name|
  find(:css, HOVER_SELECTORS[name]).visible?.should be_true
  find(:css, HOVER_SELECTORS[name]).should have_content(HOVER_TITLES[name])
end
=end

Then /^I (?:should )?see the (.+) hover$/ do |name|
  find(:css, selector_for("#{name} hover"), :match => :prefer_exact).visible?.should be_true
  find(:css, selector_for("#{name} hover"), :match => :prefer_exact).should have_content(HOVER_TITLES[name])
end

When "I click on the $link_name link in the hover" do |link_name|
  within '.js-modal' do
    step("I click on the \"#{link_name}\" link")
  end
end

Then /^the mss hover is "(.*?)"$/ do |visibility|
  if visibility == 'hidden'
    should_be = false
  else
    should_be = true
  end
  page.find(:css, "#joinHover", :visible => false, :match => :prefer_exact).visible?.should == should_be
end

Then /^the sign in hover is (\w+)$/ do |visibility|
  if visibility == 'hidden'
    should_be = false
  else
    should_be = true
  end
  page.find(:css, "#signInHover", :visible => false, :match => :prefer_exact).visible?.should == should_be
end

Then /^the forgot password hover is (\w+)$/ do |visibility|
  if visibility == 'hidden'
    should_be = false
  else
    should_be = true
  end
  page.find(:css, "#hover_forgotPassword", :visible => false, :match => :prefer_exact).visible?.should == should_be
end

Then /^the almost there hover is (\w+)$/ do |visibility|	
  if visibility == 'hidden'
    should_be = false
  else
    should_be = true
  end  
  page.find(:css, "#schoolEspThankYou", :visible => false, :match => :prefer_exact).visible?.should == should_be
end

Then /^the join us hover is (\w+)$/ do |visibility|
  if visibility == 'hidden'
    shouldBe = false
  else
    shouldBe = true
  end
  page.find(:css, "#joinHover", :visible => false, :match => :prefer_exact).visible?.should == shouldBe
end

Then /^the Validate Email hover is visible$/ do
  page.should have_content("Please verify your email address")
  steps %Q{
    Then I see the Validate your email hover
  }
end

