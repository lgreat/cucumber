var GS = GS || {};
GS.search = GS.search || {};
GS.search.schoolSearchForm = GS.search.schoolSearchForm || (function() {

    var filtersModule;
    var SEARCH_PAGE_PATH = '/search/search.page';

    var init = function(_filtersModule) {
        filtersModule = _filtersModule;

        setupTabs();

        $('#jq-findByLocationForm').submit(function() {
            return byLocation.submitByLocationSearch();
        });

        $('#jq-findByNameForm').submit(function() {
            return byName.submitByNameSearch();
        });

        $('#jq-findByNameForm').submit(function() {
            var defaultText = "   Search by school or district ...";
            if ($('#js-findByNameBox').val() == defaultText) {
                $('#js-findByNameBox').val('');
            }
            return true;
        });



        byName.init();
        byLocation.init();
    };

    var setupTabs = function() {
        $("#byLocationTab").click(function() {
            $('#byLocationTab').addClass('selected');
            $('#byLocationTabBody').show();
            $('#byNameTab').removeClass('selected');
            $('#byNameTabBody').hide();
            $(".js-schoolSearchFiltersPanel").show();
        });
        $("#byNameTab").click(function() {
            $('#byLocationTab').removeClass('selected');
            $('#byLocationTabBody').hide();
            $('#byNameTab').addClass('selected');
            $('#byNameTabBody').show();
            $(".js-schoolSearchFiltersPanel").hide();
        });
    };

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

    var byName = function() {
        var formSelector = '#jq-findByNameForm';
        var searchFieldSelector = formSelector + ' input[name="q"]';
        var stateDropdownSelector = formSelector + ' input[name="state"]';
        var stateValueSelector = formSelector + ' .showState';

        var init = function() {
            attachSchoolAutocomplete();

            $("#js-findByNameStateSelect").change(function () {
                stateValue($(this).val());
            }).trigger("change");

            $("#js-findByNameStateSelect").keyup(function () {
                stateValue($(this).val());
            });
        };

        var stateValue = function(selectedState) {
            $(stateValueSelector).text(selectedState === "" ? "Select State" : selectedState);
        };

        var attachSchoolAutocomplete = function() {
            var searchBox = $(searchFieldSelector);
            var searchStateSelect = $(stateDropdownSelector);
            var url = "/search/schoolAutocomplete.page";

            searchBox.autocomplete2(url, {
                extraParams: {
                    state: function() {
                        var rval = searchStateSelect.val();
                        if (rval === '') {
                            return null;
                        }
                        return rval;
                    },
                    schoolDistrict: true
                },
                extraParamsRequired: true,
                minChars: 3,
                selectFirst: false,
                cacheLength: 150,
                matchSubset: true,
                max: 6,
                autoFill: false,
                dataType: "text"
            });
        };

        var submitByNameSearch = function() {
            var searchString = $(searchFieldSelector).val();
            var state = $(stateValueSelector).text();

            var queryStringDataWithFilters = filtersModule.getUpdatedQueryStringData();

            delete queryStringDataWithFilters.lat;
            delete queryStringDataWithFilters.lon;
            delete queryStringDataWithFilters.distance;
            delete queryStringDataWithFilters.zipCode;
            delete queryStringDataWithFilters.normalizedAddress;
            delete queryStringDataWithFilters.locationType;
            delete queryStringDataWithFilters.totalResults;

            queryStringDataWithFilters.q = searchString;
            queryStringDataWithFilters.state = state;

            window.location.href = window.location.protocol + '//' + window.location.host +
                    SEARCH_PAGE_PATH +
                    GS.uri.Uri.getQueryStringFromObject(queryStringDataWithFilters);

            return false;
        };

        return {
            init:init,
            submitByNameSearch:submitByNameSearch
        }
    }();

    var byLocation = function() {
        var url = "/search/cityAutocomplete.page";
        var formSelector = '#jq-findByLocationForm';
        var searchFieldSelector = 'input[name="searchString"]';

        var init = function() {
            attachCityAutocomplete();
        };

        var submitByLocationSearch = function() {
            var byLocationForm = $(formSelector);
            var searchQuery = byLocationForm.find(searchFieldSelector).val();
            searchQuery = searchQuery.replace(/^\s*/, "").replace(/\s*$/, "");

            if (searchQuery != '' &&
                    searchQuery != 'Search by city AND state or address ...' && !isTermState(searchQuery)) {
                byLocationForm.find('input[name="searchString"]').val(searchQuery);

                //GS-12100 Since its a by location search, strip the words 'schools' from google geocode searches.
                var searchQueryWithFilteredStopWords = searchQuery;
                if (searchQueryWithFilteredStopWords != '') {
                    searchQueryWithFilteredStopWords = searchQueryWithFilteredStopWords.replace(/schools/g, "");
                }

                gsGeocode(searchQueryWithFilteredStopWords, function(geocodeResult) {
                    if (geocodeResult != null) {

                        var data = {};
                        data['lat'] = geocodeResult['lat'];
                        data['lon'] = geocodeResult['lon'];
                        data['zipCode'] = geocodeResult['zipCode'];
                        data['state'] = geocodeResult['state'];
                        data['locationType'] = geocodeResult['type'];
                        data['normalizedAddress'] = geocodeResult['normalizedAddress'];
                        data['totalResults'] = geocodeResult['totalResults'];

                        var queryStringDataWithFilters = filtersModule.getUpdatedQueryStringData();

                        delete queryStringDataWithFilters.q;

                        var queryStringDataWithFilters = GS.uri.Uri.mergeObjectInto(data, queryStringDataWithFilters, true);

                        window.setTimeout(function() {
                            window.location.href = window.location.protocol + '//' + window.location.host +
                                SEARCH_PAGE_PATH +
                                GS.uri.Uri.getQueryStringFromObject(queryStringDataWithFilters);
                        }, 1);
                    } else {
                        alert("Location not found. Please enter a valid address, city, or ZIP.");
                    }
                });
            } else {
                alert("Please enter an address, zip code or city and state");
            }

            return false;
        };

        var formatNormalizedAddress = function(address) {
            var newAddress = address.replace(", USA", "");
            var zipCodePattern = /(\d\d\d\d\d)-\d\d\d\d/;
            var matches = zipCodePattern.exec(newAddress);
            if (matches && matches.length > 1) {
                newAddress = newAddress.replace(zipCodePattern, matches[1]);
            }
            return newAddress;
        };

        var attachCityAutocomplete = function() {
            var searchBox = $(searchFieldSelector);
            var url = "/search/cityAutocomplete.page";

            var locationFormatter = function(row) {
                if (row != null && row.length > 0) {
                    var suggestion = row[0];
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
                formatResult: locationFormatter
            });
        };

        var gsGeocode = function(searchInput, callbackFunction) {
            var geocoder = new google.maps.Geocoder();
            if (geocoder && searchInput) {
                geocoder.geocode( { 'address': searchInput + ' US'}, function(results, status) {
                    var numResults = 0;
                    var GS_geocodeResults = new Array();
                    if (status == google.maps.GeocoderStatus.OK && results.length > 0) {
                        numResults = results.length;
                        for (var x = 0; x < numResults; x++) {
                            var geocodeResult = new Array();
                            geocodeResult['lat'] = results[x].geometry.location.lat();
                            geocodeResult['lon'] = results[x].geometry.location.lng();
                            geocodeResult['normalizedAddress'] = formatNormalizedAddress(results[x].formatted_address);
                            geocodeResult['type'] = results[x].types.join();
                            if (results[x].partial_match) {
                                geocodeResult['partial_match'] = true;
                            } else {
                                geocodeResult['partial_match'] = false;
                            }
                            for (var i = 0; i < results[x].address_components.length; i++) {
                                if (results[x].address_components[i].types.contains('administrative_area_level_1')) {
                                    geocodeResult['state'] = results[x].address_components[i].short_name;
                                }
                                if (results[x].address_components[i].types.contains('country')) {
                                    geocodeResult['country'] = results[x].address_components[i].short_name;
                                }
                                if (results[x].address_components[i].types.contains('postal_code')) {
                                    geocodeResult['zipCode'] = results[x].address_components[i].short_name;
                                }
                            }
                            // http://stackoverflow.com/questions/1098040/checking-if-an-associative-array-key-exists-in-javascript
                            if (!('lat' in geocodeResult && 'lon' in geocodeResult &&
                                    'state' in geocodeResult &&
                                    'normalizedAddress' in geocodeResult &&
                                    'country' in geocodeResult) ||
                                    geocodeResult['country'] != 'US') {
                                geocodeResult = null;
                            }
                            if (geocodeResult != null) {
                                GS_geocodeResults.push(geocodeResult);
                            }
                        }
                    }
                    if (GS_geocodeResults.length == 0) {
                        callbackFunction(null);
                    } else if (GS_geocodeResults.length == 1) {
                        GS_geocodeResults[0]['totalResults'] = 1;
                        callbackFunction(GS_geocodeResults[0]);
                    } else {
                        // ignore multiple results for now
                        GS_geocodeResults[0]['totalResults'] = GS_geocodeResults.length;
                        callbackFunction(GS_geocodeResults[0]);
                    }
                });
            }
        };

        return {
            init:init,
            submitByLocationSearch:submitByLocationSearch
        }
    }();

    return {
        init:init
    }

})();