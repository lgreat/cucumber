define(['uri', 'geocoder', 'validation', 'geolocation', 'jquery.autocomplete', 'nearbyCitiesList', 'nearbyDistrictsList'], function(uri, geocoder, validation, geolocation) {
    var JS_GRADE_LEVELS_CONTAINER_SELECTOR = '#js-gradeLevels';
    var BY_LOCATION_FORM_SELECTOR = '#js-searchByLocation';
    var BY_NAME_FORM_SELECTOR = '#search-form';
    var TABS_SELECTOR = '.gsTabs';


    // copied from findASchool.js
    var isTermState = function(term) {
        var stateTermList = new Array
                ("AK","Alaska","AL","Alabama","AR","Arkansas","AZ","Arizona",
                        "CA","California","CO","Colorado","CT","Connecticut","DC",
                        // Do not include state names that are identical to city names
//        "Washington, D.C.", "Washington, DC", "Washington D.C.", "Washington DC",
                "DE","Delaware","FL","Florida","GA","Georgia","HI","Hawaii","IA","Iowa",
                "ID","Idaho","IL","Illinois","IN","Indiana","KS","Kansas","KY","Kentucky",
                "LA","Louisiana","MA","Massachusetts","MD","Maryland","ME","Maine","MI","Michigan",
                "MN","Minnesota","MO","Missouri","MS","Mississippi","MT","Montana",
                "NC","North Carolina","ND","North Dakota","NE","Nebraska","NH","New Hampshire",
                "NJ","New Jersey","NM","New Mexico","NV","Nevada","NY","New York",
                "OH","Ohio","OK","Oklahoma","OR","Oregon","PA","Pennsylvania",
                "RI","Rhode Island","SC","South Carolina","SD","South Dakota",
                "TN","Tennessee","TX","Texas","UT","Utah","VA","Virginia","VT","Vermont",
                "WA","Washington","WI","Wisconsin","WV","West Virginia","WY","Wyoming");
        for (var i=0; i < stateTermList.length; i++) {
            if (stateTermList[i].toLowerCase() == term.toLowerCase()) {
                return true;
            }
        }
        return false;
    };

    var loadResultsPage = function() {
        var queryString = $(BY_LOCATION_FORM_SELECTOR).serialize();
        if (queryString.indexOf('Current+Location') >= 0) {
            queryString = queryString.replace('searchString=Current+Location','');
            queryString = queryString.replace('&&','&');
        }
        queryString = buildQueryString(queryString);
        window.location.href = '/search/search.page' + queryString;
    };

    var buildQueryString = function(queryString) {
        var $gradeLevelsContainer = $(JS_GRADE_LEVELS_CONTAINER_SELECTOR);
        //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
        queryString = uri.removeFromQueryString(queryString, "gradeLevels");

        var gradeLevel = $gradeLevelsContainer.find('[name=gradeLevels]').val();
        queryString = uri.putIntoQueryString(queryString, "gradeLevels", gradeLevel);

        return queryString;
    };

    var submitByLocationSearch = function() {
        var byLocationForm = $(BY_LOCATION_FORM_SELECTOR);
        var searchQuery = byLocationForm.find('input[name="searchString"]').val();
        searchQuery = searchQuery.replace(/^\s*/, "").replace(/\s*$/, "");

        if (searchQuery != '' && searchQuery !== 'Current Location' &&
                searchQuery != 'Search by city AND state or address ...' && !isTermState(searchQuery)) {
            byLocationForm.find('input[name="searchString"]').val(searchQuery);

            //GS-12100 Since its a by location search, strip the words 'schools' from google geocode searches.
            var searchQueryWithFilteredStopWords = searchQuery;
            if (searchQueryWithFilteredStopWords != '') {
                searchQueryWithFilteredStopWords = searchQueryWithFilteredStopWords.replace(/schools/g, "");
            }

            geocoder.geocode(searchQueryWithFilteredStopWords, function(geocodeResult) {
                GS.log('got geocode result ', geocodeResult);
                if (geocodeResult != null) {
                    byLocationForm.find('input[name="lat"]').val(geocodeResult['lat']);
                    byLocationForm.find('input[name="lon"]').val(geocodeResult['lon']);
                    byLocationForm.find('input[name="state"]').val(geocodeResult['state']);
                    byLocationForm.find('input[name="locationType"]').val(geocodeResult['type']);
//                byLocationForm.find('input[name="partialMatch"]').val(geocodeResult['partial_match']);
                    byLocationForm.find('input[name="normalizedAddress"]').val(geocodeResult['normalizedAddress']);
                    byLocationForm.find('input[name="totalResults"]').val(geocodeResult['totalResults']);

                    window.setTimeout(loadResultsPage, 1);
                } else {
                    alert("Location not found. Please enter a valid address, city, or ZIP.");
                }
            });
        } else if (searchQuery === 'Current Location') {
            window.setTimeout(loadResultsPage, 1); // lat and lon have already been set
        } else {
            alert("Please enter an address, zip code or city and state");
        }

        return false;
    };

    var init = function() {
        $(function() {
            /* initiate tabs  */
            $('.gsTabs').each(function(){
                var tab = $(this);
                var tabNav = tab.find('ul:first'); // get only the first ul not all of the descendents
                tabNav.find('li').each(function(){
                    $(this).find('a').click(function(){ //When any link is clicked
                        tab.children('div').hide(); // hide all layers
                        var tabNum = tabNav.find('li').index($(this).parent());// find reference to the content
                        tab.children('div').eq(tabNum).show();// show the content
                        tabNav.find('li a').removeClass('selected');// turn all of them off
                        $(this).addClass('selected');// turn selected on
                        return false;
                    });
                });
            });

            jQuery('#js-gradeLevels input').click(function () {
                var cssId = jQuery(this).attr('id');

                var gradeCheckboxes = jQuery('#js-gradeLevels .jq-grade-level');
                GS.log('checkbox clicked:', cssId);

                if (cssId === 'js-grade-level-all') {
                    if (jQuery(this).is(':checked')) {
                        gradeCheckboxes.prop('checked',true);
                    } else {
                        gradeCheckboxes.prop('checked', false);
                    }
                }
            });

            $(BY_LOCATION_FORM_SELECTOR).on('submit', function() {
                submitByLocationSearch();
                return false;
            });

            $(BY_NAME_FORM_SELECTOR).on('submit', function() {
                var validations = [];
                validations.push(validation.validateOne($('#stateSelector'), BY_NAME_FORM_SELECTOR));
                validations.push(validation.validateOne($(BY_NAME_FORM_SELECTOR + ' [name=q]'), BY_NAME_FORM_SELECTOR));

                for (var arrayIndex in validations) {
                    if (validations[arrayIndex] === false) {
                        return false;
                    }
                }
                return true;
            });

            validation.init();
            validation.attachValidationHandlers('#search-form');

            attachCityAutocomplete('.js-searchByLocationQuery'); // add autocomplete right away. geolocation might never happen

            geolocation.getCoordinates(function(coordinates) {
                var byLocationForm = $(BY_LOCATION_FORM_SELECTOR);
                byLocationForm.find('input[name="lat"]').val(coordinates.latitude);
                byLocationForm.find('input[name="lon"]').val(coordinates.longitude);
                // geolocation worked, unbind and re-bind autocomplete since we know current location now, and the
                // "current location" autocomplete option comes back from the server within autocomplete results.
                $('.js-searchByLocationQuery').unbind(".autocomplete");
                attachCityAutocomplete('.js-searchByLocationQuery');
            });

        });
    };

    var attachCityAutocomplete = function(queryBoxSelector) {
        var searchBox = $(queryBoxSelector);
        var url = "/search/cityAutocomplete.page";
        var prependCurrentLocation = geolocation.hasGeolocation();

        var locationFormatter = function(row) {
            if (row != null && row.length > 0) {
                var suggestion = row[0];
                if (suggestion === "Current Location") {
                    return suggestion;
                }
                // capitalize first letter of all words but the last
                // capitalize the entire last word (state)
                return suggestion.substr(0, suggestion.length-2).replace(/\w+/g, function(word) { return word.charAt(0).toUpperCase() + word.substr(1); }) + suggestion.substr(suggestion.length-2).toUpperCase();
            }
            return row;
        };
        searchBox.autocomplete2(url, {
            minChars: 3,
            selectFirst: false,
            cacheLength: 150,
            matchSubset: true,
            max: 6,
            autoFill: false,
            dataType: "text",
            formatItem: locationFormatter,
            formatResult: locationFormatter,
            extraParams: {
                prependCurrentLocation:prependCurrentLocation
            }
        });
    };

    return {
        init:init
    }
});