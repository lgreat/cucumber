# Samson
begin require 'rspec/expectations'; rescue LoadError; require 'spec/expectations'; end
####

require 'capybara/mechanize'
require 'capybara/cucumber'
#require 'rspec/expectations'
require 'selenium/webdriver' # Added by Anthony so I can override the user agent below

# Added by Samson
require 'capybara/dsl'

# NOTE: You may need to manually set "general.useragent.enable_overrides" to "true" in your Firefox about:config for this to work
Capybara.register_driver :selenium_iphone do |app|
  profile = Selenium::WebDriver::Firefox::Profile.new
  profile['general.useragent.enable_overrides'] = 'true'
  profile['general.useragent.override'] = 'Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_1 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8B117 Safari/6531.22.7'
  profile['general.description.override'] = 'Mozilla' # appCodeName
  profile['general.appname.override'] = 'Netscape'
  profile['general.appversion.override'] = '5.0 (iPhone; U; CPU iPhone OS 4_1 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8B117 Safari/6531.22.7'
  profile['general.platform.override'] = 'iPhone'
  profile['general.useragent.vendor'] = 'Apple Computer, Inc.'
  profile['general.useragent.vendorSub'] = ''
  Capybara::Selenium::Driver.new(app, :profile => profile)
end

Capybara.register_driver :mechanize_iphone do |app|
  driver = Capybara::Mechanize::Driver.new(app)
  driver.browser.agent.user_agent_alias = 'iPhone'
  driver
end

Capybara.app_host = "http://localhost" # Do not edit this line! See below
# Set the following environment variable to point cucumber at various hosts.
# Please note the host should not end in a slash
if ENV['APP_HOST'] != nil
  Capybara.app_host = ENV['APP_HOST']
end
puts "Running tests against #{Capybara.app_host}"

Capybara.default_driver = :mechanize

# Added by Greg to try and get Sauce working.  Comment out if needed:
if ENV['SAUCE_USERNAME'] != nil

  my_driver = (ENV['BROWSER'] && ENV['BROWSER'].to_sym) || :selenium
  puts ""
  puts "my_driver:  #{my_driver}"

  Capybara.default_driver = :mechanize
  Capybara.javascript_driver = my_driver

  SAUCE_USERNAME = ENV['SAUCE_USERNAME'] || 'GreatSchools'
  SAUCE_ACCESS_KEY = ENV['SAUCE_ACCESS_KEY'] || 'cb157f01-1dfe-49e8-8bd4-df76f1ec169a'
  SAUCE_PORT = ENV['SAUCE_ONDEMAND_PORT'] || '4445'

  def sauce_build
    if ENV['REMOTE'] == 'true'
      "remote: #{ENV['APP_HOST'] || Capybara.app_host}"
    else
      `git describe --always --dirty`.strip
    end
  end

  def sauce_name
    ENV['JOB_NAME'] || "GreatSchools Cucumber (#{`whoami`.strip})"
  end

  base_opts = {:username => SAUCE_USERNAME, :access_key => SAUCE_ACCESS_KEY, :build => sauce_build, :name => sauce_name, :'parent-tunnel' => (ENV['SAUCE_PARENT_TUNNEL'] || nil), :'max-duration' => '3600'}

  SAUCE_CONNECT_URL = ENV['REMOTE'] == 'true' ? "http://#{SAUCE_USERNAME}:#{SAUCE_ACCESS_KEY}@ondemand.saucelabs.com:80/wd/hub" : "http://localhost:#{SAUCE_PORT}/wd/hub"

  # Windows 7, Chrome
  Capybara.register_driver :sauce_chrome_win7 do |app|
    caps = base_opts.merge({:platform => 'Windows 7'})
    Capybara::Selenium::Driver.new(app,
       :browser => :remote,
       :url => SAUCE_CONNECT_URL,
       :desired_capabilities => Selenium::WebDriver::Remote::Capabilities.chrome(caps))
  end

  # Windows 7, IE9
  Capybara.register_driver :sauce_ie9_win7 do |app|
    caps = base_opts.merge({:platform => 'Windows 7', :version => '9'})
    Capybara::Selenium::Driver.new(app,
       :browser => :remote,
       :url => SAUCE_CONNECT_URL,
       :desired_capabilities => Selenium::WebDriver::Remote::Capabilities.internet_explorer(caps))
  end

  # Windows 7, Firefox 22
  Capybara.register_driver :sauce_firefox22_win7 do |app|
    caps = base_opts.merge({:platform => 'Windows 7', :version => '22'})
    Capybara::Selenium::Driver.new(app,
       :browser => :remote,
       :url => SAUCE_CONNECT_URL,
       :desired_capabilities => Selenium::WebDriver::Remote::Capabilities.firefox(caps))
  end


end
# End of Greg's stuff



#World(Test::Unit::Assertions)
World(Capybara) 




#####################################
