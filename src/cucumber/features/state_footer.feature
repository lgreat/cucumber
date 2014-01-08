Feature: GreatSchools State Footer Tests
  As a user
  I want to quickly access school and Great Schools information
  So that I can educate myself

  Scenario Outline: : I can click through the links to be able to find schools in communities
    Given I am on "<community>" page
    When I click on the "<link_name>" link
    Then I see "<url_fragment>" in the URL
    Then I see "<text>"

  Examples:

    | community        | link_name | url_fragment                 | text                               |
    | GS New York page | New York  | new-york/new-york-city/      | Top-Rated New York Public Schools  |
    | GS New York page | Buffalo   | new-york/buffalo/            | Top-Rated Buffalo Public Schools   |
    | GS New York page | Rochester | new-york/rochester/          | Top-Rated Rochester Public Schools |
    | GS New York page | A         | schools/cities/New_York/NY/A | Albany schools                     |
    | GS New York page | B         | schools/cities/New_York/NY/B | Brooklyn schools                   |
    | GS New York page | M         | schools/cities/New_York/NY/M | Manhattan schools                  |
#    | GS California page | Los Angeles   | california/los-angeles/      | Top-Rated Los Angeles Public Schools   |
#    | GS California page | San Francisco | california/san-francisco/    | Top-Rated San Francisco Public Schools |
#    | GS California page | San Jose      | california/san-jose/         | Top-Rated San Jose Public Schools      |