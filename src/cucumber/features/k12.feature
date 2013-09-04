Feature: K12 Advertising Lead Gen Page
  As a user
  I want to find information on my local K12 school
  So I can choose to apply

  Scenario Outline: State-specific K12 schools are all represented
    When I am on "<state>"'s K12 page
    Then I see "<schoolName>"
    And I see "<schoolLink>"
    And I see "| <gradeRange> |"
    And GreatSchools affiliate codes are captured including the code from "<schoolLink>"
  Examples:
    | state | schoolName                                     | gradeRange | schoolLink         |
    | AK    | Alaska Virtual Academy                         | K-8        | www.K12.com/AKVA   |
    | AR    | Arkansas Virtual Academy                       | K-8        | www.K12.com/ARVA   |
    | AZ    | Arizona Virtual Academy                        | K-12       | www.K12.com/AZVA   |
    | CA    | California Virtual Academies                   | K-12       | www.K12.com/CAVA   |
    | CO    | Colorado Virtual Academy                       | K-12       | www.K12.com/COVA   |
    | DC    | Community Academy Public Charter School Online | K-8        | www.K12.com/CAPCS  |
    | FL    | Florida Virtual Academies                      | K-12       | www.K12.com/FLVA   |
    | GA    | Georgia Cyber Academy                          | K-11       | www.K12.com/GCA    |
    | HI    | Hawaii Technology Academy                      | K-12       | www.K12.com/HTA    |
    | IA    | Iowa Virtual Academy                           | K-6        | www.K12.com/IAVA   |
    | ID    | Idaho Virtual Academy                          | K-12       | www.K12.com/IDVA   |
    | IL    | Chicago Virtual Charter School                 | K-12       | www.K12.com/CVCS   |
    | IN    | Hoosier Academies                              | K-12       | www.K12.com/HA     |
    | KS    | Lawrence Virtual School                        | K-12       | www.K12.com/LVS    |
    | LA    | Louisiana Virtual Charter Academy              | K-11       | www.K12.com/LAVCA  |
    | MA    | Massachusetts Virtual Academy                  | K-8        | www.K12.com/MAVA   |
    | MI    | Michigan Virtual Charter Academy               | K-12       | www.K12.com/MVCA   |
    | MN    | Minnesota Virtual Academy                      | K-12       | www.K12.com/MNVA   |
    | NJ    | New Jersey Virtual Academy Charter School      | K-10       | www.K12.com/NJVACS |
    | NM    | New Mexico Virtual Academy                     | 6-12       | www.K12.com/NMVA   |
    | NV    | Nevada Virtual Academy                         | K-12       | www.K12.com/NVVA   |
    | OH    | Ohio Virtual Academy                           | K-12       | www.K12.com/OHVA   |
    | OK    | Oklahoma Virtual Charter Academy               | K-12       | www.K12.com/OVCA   |
    | OR    | Oregon Virtual Academy                         | K-11       | www.K12.com/ORVA   |
    | PA    | Agora Cyber Charter School                     | K-12       | www.K12.com/AGORA  |
    | SC    | South Carolina Virtual Charter School          | K-12       | www.K12.com/SCVCS  |
    | TN    | Tennessee Virtual Academy                      | K-8        | www.K12.com/TNVA   |
    | TX    | Texas Virtual Academy                          | 3-12       | www.K12.com/TXVA   |
    | UT    | Utah Virtual Academy                           | K-12       | www.K12.com/UTVA   |
    | VA    | Virginia Virtual Academy                       | K-8        | www.K12.com/VAVA   |
    | WA    | Washington Virtual Academies                   | K-12       | www.K12.com/WAVA   |
    | WI    | Wisconsin Virtual Academy                      | K-12       | www.K12.com/WIVA   |
    | WY    | Wyoming Virtual Academy                        | K-12       | www.K12.com/WYVA   |
    | INT   | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | AL    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | CT    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | DE    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | KY    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | MD    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | ME    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | MO    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | MS    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | MT    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | NC    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | ND    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | NE    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | NH    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | NY    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | RI    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | SD    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | VT    | K12 International Academy                      | K-12       | www.K12.com/INT    |
    | WV    | K12 International Academy                      | K-12       | www.K12.com/INT    |
