Feature: GreatSchools Home Page Footer Tests
  As a user
  I want to quickly access school and Great Schools information
  So that I can educate myself

@databrittle
Scenario Outline: : I can click through the links to be able to find schools in communities
  Given I am on "GS Home Page" page
   When I click on the "<link_name>" link
   Then I see "<url_fragment>" in the URL
   Then I see "<text>"

  Examples:

    | link_name          | url_fragment               | text                                       |
    | Albuquerque, NM    | new-mexico/albuquerque/    | Top-Rated Albuquerque Public Schools       |
    | Anchorage, AK      | alaska/anchorage/          | Top-Rated Anchorage Public Schools         |
    | Atlanta, GA        | georgia/atlanta/           | Top-Rated Atlanta Public Schools           |
    | Austin, TX         | texas/austin/              | Top-Rated Austin Public Schools            |
    | Baltimore, MD      | maryland/baltimore/        | Top-Rated Baltimore Public Schools         |
    | Billings, MT       | montana/billings/          | Top-Rated Billings Public Schools          |
    | Birmingham, AL     | alabama/birmingham/        | Top-Rated Birmingham Public Schools        |
    | Boise, ID          | idaho/boise/               | Top-Rated Boise Public Schools             |
    | Boston, MA         | massachusetts/boston/      | Top-Rated Boston Public Schools            |
    | Bridgeport, CT     | connecticut/bridgeport/    | Top-Rated Bridgeport Public Schools        |
    | Burlington, VT     | vermont/burlington/        | Top-Rated Burlington Public Schools        |
    | Charleston, WV     | west-virginia/charleston/  | Top-Rated Charleston Public Schools        |
    | Charlotte, NC      | north-carolina/charlotte/  | Top-Rated Charlotte Public Schools         |
    | Cheyenne, WY       | wyoming/cheyenne/          | Top-Rated Cheyenne Public Schools          |
    | Chicago, IL        | illinois/chicago/          | Top-Rated Chicago Public Schools           |
    | Columbia, SC       | south-carolina/columbia/   | Top-Rated Columbia Public Schools          |
    | Columbus, OH       | ohio/columbus/             | Top-Rated Columbus Public Schools          |
    | Dallas, TX         | texas/dallas/              | Top-Rated Dallas Public Schools            |
    | Denver, CO         | colorado/denver/           | Top-Rated Denver Public Schools            |
    | Des Moines, IA     | iowa/des-moines/           | Top-Rated Des Moines Public Schools        |
    | Detroit, MI        | michigan/detroit/          | Finding a Great School in Detroit          |
    | El Paso, TX        | texas/el-paso/             | Top-Rated El Paso Public Schools           |
    | Fargo, ND          | north-dakota/fargo/        | Top-Rated Fargo Public Schools             |
    | Honolulu, HI       | hawaii/honolulu/           | Top-Rated Honolulu Public Schools          |
    | Houston, TX        | texas/houston/             | Top-Rated Houston Public Schools           |
    | Indianapolis, IN   | indiana/indianapolis/      | Top-Rated Indianapolis Public Schools      |
    | Jackson, MS        | mississippi/jackson/       | Top-Rated Jackson Public Schools           |
    | Jacksonville, FL   | florida/jacksonville/      | Top-Rated Jacksonville Public Schools      |
    | Kansas City, MO    | missouri/kansas-city/      | Top-Rated Kansas City Public Schools       |
    | Las Vegas, NV      | nevada/las-vegas/          | Top-Rated Las Vegas Public Schools         |
    | Little Rock, AR    | arkansas/little-rock/      | Top-Rated Little Rock Public Schools       |
    | Los Angeles, CA    | california/los-angeles/    | Top-Rated Los Angeles Public Schools       |
    | Louisville, KY     | kentucky/louisville/       | Top-Rated Louisville Public Schools        |
    | Manchester, NH     | new-hampshire/manchester/  | Top-Rated Manchester Public Schools        |
    | Memphis, TN        | tennessee/memphis/         | Top-Rated Memphis Public Schools           |
    | Miami, FL          | florida/miami/             | Top-Rated Miami Public Schools             |
    | Milwaukee, WI      | wisconsin/milwaukee/       | Finding a Great School in Milwaukee        |
    | Minneapolis, MN    | minnesota/minneapolis/     | Top-Rated Minneapolis Public Schools       |
    | Nashville, TN      | tennessee/nashville/       | Top-Rated Nashville Public Schools         |
    | New Orleans, LA    | louisiana/new-orleans/     | Top-Rated New Orleans Public Schools       |
    | New York City, NY  | new-york/new-york-city/    | Top-Rated New York Public Schools          |
    | Newark, NJ         | new-jersey/newark/         | Top-Rated Newark Public Schools            |
    | Oakland, CA        | california/oakland/        | Top-Rated Oakland Public Schools           |
    | Oklahoma City, OK  | oklahoma/oklahoma-city/    | Top-Rated Oklahoma City Public Schools     |
    | Omaha, NE          | nebraska/omaha/            | Top-Rated Omaha Public Schools        |
    | Philadelphia, PA   | pennsylvania/philadelphia/ | Top-Rated Philadelphia Public Schools |
    | Phoenix, AZ        | arizona/phoenix/           | Top-Rated Phoenix Public Schools      |
    | Portland, ME       | maine/portland/            | Top-Rated Portland Public Schools          |
    | Portland, OR       | oregon/portland/           | Top-Rated Portland Public Schools          |
    | Providence, RI     | rhode-island/providence/   | Top-Rated Providence Public Schools        |
    | Sacramento, CA     | california/sacramento/     | Top-Rated Sacramento Public Schools        |
    | Salt Lake City, UT | utah/salt-lake-city/       | Top-Rated Salt Lake City Public Schools    |
    | San Antonio, TX    | texas/san-antonio/         | Top-Rated San Antonio Public Schools       |
    | San Diego, CA      | california/san-diego/      | Top-Rated San Diego Public Schools         |
    | San Francisco, CA  | california/san-francisco/  | Top-Rated San Francisco Public Schools     |
    | San Jose, CA       | california/san-jose/       | Top-Rated San Jose Public Schools          |
    | Seattle, WA        | washington/seattle/        | Top-Rated Seattle Public Schools           |
    | Sioux Falls, SD    | south-dakota/sioux-falls/  | Top-Rated Sioux Falls Public Schools       |
    | Tucson, AZ         | arizona/tucson/            | Top-Rated Tucson Public Schools            |
    | Virginia Beach, VA | virginia/virginia-beach/   | Top-Rated Virginia Beach Public Schools    |
    | Washington, DC     | washington-dc/washington/  | Top-Rated Washington, DC Public Schools    |
    | Wichita, KS        | kansas/wichita/            | Top-Rated Wichita Public Schools           |
    | Wilmington, DE     | delaware/wilmington/       | Top-Rated Wilmington Public Schools        |

  Scenario Outline: I can click through the links to find more information about GreatSchools
  Given I am on "GS Home Page" page
   When I click on the "<link_name>" link
   Then I see "<url_fragment>" in the URL
   Then I see "<text>"

  Examples:

    | link_name                | url_fragment                        | text                                  |
    | Our mission              | about/aboutUs.page                  | Our Mission                           |
    | Our people               | about/senior-management.page        | Our People                            |
    | Jobs                     | jobs                                | Jobs at GreatSchools                  |
    | Contact us               | about/feedback.page                 | Contact Us                            |
    | Advertise with us        | about/advertiserOpportunities.page  | Advertising Info                      |
    | Partners                 | about/partnerOpportunities.page     | Partners                              |
    | Media room               | about/pressRoom.page                | Media Room                            |
    | Widgets & tools          | about/linkToUs.page                 | Widgets & Tools                       |
    | How we rate schools      | defining-your-ideal/2423-ratings.gs | GreatSchools Ratings FAQ              |
    | School review guidelines | guidelines.page                     | GreatSchools School Review Guidelines |
    | Terms of use             | terms/?state=                       | Terms of Use                          |
    | Privacy policy           | privacy/?state=                     | Privacy Statement                     |
    | Ad Choices               | privacy/#advertiserNotice           | A Notice About Our Advertisers        |