# For mobile requests that need javascript execution, we use a custom selenium driver
# For mobile requests that do NOT need javascript execution, we use a custom mechanize driver

# NOTE: You may need to set "general.useragent.enable_overrides" to "true" in your Firefox about:config for this to work
Before('@mobile', '@javascript') do
  Capybara.current_driver = :selenium_iphone
end

After('@mobile', '@javascript') do
  Capybara.current_driver = :selenium
end

Before('@mobile', '~@javascript') do
  Capybara.current_driver = :mechanize_iphone
end

After('@mobile', '~@javascript') do
  Capybara.current_driver = :mechanize
end

Before('@readwrite') do
  if Capybara.app_host.index('www.greatschools.org') != nil || Capybara.app_host.index('maddy.greatschools.org') != nil
    print "READWRITE TEST DETECTED WITH PRODUCTION HOSTNAME #{Capybara.app_host} -- ABORTING"
    Cucumber.wants_to_quit = true
  end
end