define(['jquery', 'uri', 'geocoder'], function($, uri, geocoder) {
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
        var queryString = $('#js-searchByLocation form').serialize();
        queryString = buildQueryString(queryString);
        window.location.href = '/search/search.page' + queryString;
    };

    var buildQueryString = function(queryString) {
        //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
        queryString = uri.removeFromQueryString(queryString, "gradeLevels");
        var checkedGradeLevels = jQuery('#js-gradeLevels :checked');
        var overwriteGradeLevels = true;
        checkedGradeLevels.each(function() {
            if (jQuery(this).val() !== '') {
                queryString = uri.putIntoQueryString(queryString, "gradeLevels", jQuery(this).val(), overwriteGradeLevels);
                overwriteGradeLevels = false;
            }
        });

        return queryString;
    };

    var submitByLocationSearch = function() {
        var byLocationForm = $('#js-searchByLocation form');
        var searchQuery = byLocationForm.find('input[name="searchString"]').val();
        searchQuery = searchQuery.replace(/^\s*/, "").replace(/\s*$/, "");

        if (searchQuery != '' &&
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
        } else {
            alert("Please enter an address, zip code or city and state");
        }

        return false;
    };


    var init = function() {
        $(function() {
            $('#js-searchByLocation').on('click', '.js-submitButton', function() {
                submitByLocationSearch();
            })
        });

    };

    return {
        init:init
    }
});