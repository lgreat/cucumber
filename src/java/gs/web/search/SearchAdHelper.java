package gs.web.search;

import gs.data.geo.City;
import gs.data.school.district.District;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.util.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component("searchAdHelper")
public class SearchAdHelper {

    public void addSearchResultsAdKeywords(PageHelper pageHelper, List<SolrSchoolSearchResult> schoolResults) {
        // GS-10448 - search results
        if (schoolResults != null) {
            Set<String> cityNames = new HashSet<String>();
            for (SolrSchoolSearchResult schoolResult : schoolResults) {
                Address address = schoolResult.getAddress();
                if (address != null) {
                    String cityName = address.getCity();
                    if (StringUtils.isNotBlank(cityName)) {
                        cityNames.add(cityName);
                    }
                }
            }
            for (String cityName : cityNames) {
                pageHelper.addAdKeywordMulti("city", cityName);
            }
        }
    }

    public void addSchoolTypeAdKeywords(PageHelper pageHelper, String[] schoolTypes) {
        // GS-10003 - school type
        if (schoolTypes != null) {
            for (String type : schoolTypes) {
                pageHelper.addAdKeywordMulti("type", type.toLowerCase());
            }
        }
    }

    /**
     *
     * @param pageHelper
     * @param gradeLevelCodes p,e,m,h
     */
    public void addLevelCodeAdKeywords(PageHelper pageHelper, String[] gradeLevelCodes) {
        // GS-6875 - level
        if (gradeLevelCodes == null || gradeLevelCodes.length == 0) {
            gradeLevelCodes = new String[] {"p","e","m","h"};
        }
        for (String gradeLevelCode : gradeLevelCodes) {
            pageHelper.addAdKeywordMulti("level", gradeLevelCode);
        }
    }

    public void addZipCodeAdKeyword(PageHelper pageHelper, String searchString) {
        // GS-9323 zip code
        if (searchString != null && searchString.trim().matches("^\\d{5}$")) {
            pageHelper.addAdKeyword("zipcode", searchString.trim());
        }
    }

    public void addSearchQueryAdKeywords(PageHelper pageHelper, String searchString) {
        // GS-10642 - query
        // also consider hyphens to be token separators
        if (searchString != null) {
            String queryString = searchString.replaceAll("-"," ");
            String[] tokens = StringUtils.split(queryString);
            List<String> tokenList = Arrays.asList(tokens);

            Set<String> terms = new HashSet<String>(tokenList);
            for (String term : terms) {
                pageHelper.addAdKeywordMulti("query", term);
            }
        }
    }

    public void addCityAdKeyword(PageHelper pageHelper, City city) {
        if (city != null) {
            // GS-5786 - city browse
            String cityName = WordUtils.capitalize(city.getName());
            cityName = WordUtils.capitalize(cityName, new char[]{'-'});
            pageHelper.addAdKeywordMulti("city", cityName);
        }
    }

    public void addDistrictAdKeywords(PageHelper pageHelper, District district) {
        // GS-10157 - district browse
        if (district != null) {
            pageHelper.addAdKeyword("district_id", String.valueOf(district.getId()));
            pageHelper.addAdKeyword("district_name", district.getName());
        }
    }

    public void addRealtorDotComAdKeywords(PageHelper pageHelper, State state, City city) {
        if (state != null && city != null) {
            String cityName = city.getName();
            // GS-7809 - adsense hints for realtor.com
            StringBuilder adSenseHint = new StringBuilder();
            adSenseHint.append(cityName.toLowerCase());
            adSenseHint.append(" ");
            adSenseHint.append(state.getLongName().toLowerCase());
            adSenseHint.append(" real estate house homes for sale");
            pageHelper.addAdSenseHint(adSenseHint.toString());
        }
    }

    public void addNearbySearchInfoKeywords(PageHelper pageHelper, HttpServletRequest request) {
        // GS-11511 - nearby search by zip code
        if (request.getAttribute("nearbySearchInfo") != null && request.getAttribute("nearbySearchInfo") instanceof Map) {
            Map nearbySearchInfo = (Map) request.getAttribute("nearbySearchInfo");
            Object nearbyZipCode = nearbySearchInfo.get("zipCode");
            Object nearbyState = nearbySearchInfo.get("state");
            Object nearbyCity = nearbySearchInfo.get("city");

            if (nearbyZipCode != null && nearbyZipCode instanceof String) {
                pageHelper.addAdKeyword("zipcode", (String)nearbyZipCode);
            }
            if (nearbyState != null && nearbyState instanceof State) {
                // this overrides the state GAM attribute
                pageHelper.addAdKeyword("state", ((State)nearbyState).getAbbreviation());
            }
            if (nearbyCity != null && nearbyCity instanceof String) {
                pageHelper.addAdKeywordMulti("city", (String)nearbyCity);
            }
        }
    }

}
