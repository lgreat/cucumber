GS = GS || {};
GS.findASchool = GS.findASchool || {};

// also in customizeSchoolSearchWidget.js
// http://stackoverflow.com/questions/237104/javascript-array-containsobj
Array.prototype.contains = function(obj) {
  var i = this.length;
  while (i--) {
    if (this[i] === obj) {
      return true;
    }
  }
  return false;
};

GS.findASchool.loadResultsPage = function() {
    var queryString = $('#findByLocationForm').serialize();
    queryString = GS.findASchool.buildQueryString(queryString);
    window.location.href = '/search/search.page' + queryString;
};

GS.findASchool.submitByLocationSearch = function() {
    $('#multipleResults').hide();
    var byLocationForm = $('#findByLocationForm');
    var searchQuery = byLocationForm.find('input[name="searchQuery"]').val();
    searchQuery = searchQuery.replace(/^\s*/, "").replace(/\s*$/, "");
    byLocationForm.find('input[name="searchQuery"]').val(searchQuery);
    if (searchQuery != '' &&
        searchQuery != 'Enter city & state or zip code' && !GS.findASchool.isTermState(searchQuery)) {
        GS.findASchool.gsGeocode(searchQuery, function(geocodeResult) {
            if (geocodeResult != null) {
                byLocationForm.find('input[name="lat"]').val(geocodeResult['lat']);
                byLocationForm.find('input[name="lon"]').val(geocodeResult['lon']);
                byLocationForm.find('input[name="state"]').val(geocodeResult['state']);
                byLocationForm.find('input[name="locationType"]').val(geocodeResult['type']);
//                byLocationForm.find('input[name="partialMatch"]').val(geocodeResult['partial_match']);
                byLocationForm.find('input[name="normalizedAddress"]').val(geocodeResult['normalizedAddress']);
                byLocationForm.find('input[name="totalResults"]').val(geocodeResult['totalResults']);

                window.setTimeout(GS.findASchool.loadResultsPage, 1);
            } else {
                alert("TODO: Take user to no results search page");
            }
        });
    } else {
        alert("Please enter an address, zip code or city and state");
    }

    return false;
};

GS.findASchool.isTermState = function(term) {
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

GS.findASchool.formatNormalizedAddress = function(address) {
    var newAddress = address.replace(", USA", "");
    var zipCodePattern = /(\d\d\d\d\d)-\d\d\d\d/;
    var matches = zipCodePattern.exec(newAddress);
    if (matches && matches.length > 1) {
        newAddress = newAddress.replace(zipCodePattern, matches[1]);
    }
    return newAddress;
};

// also in customizeSchoolSearchWidget.js
// requires http://maps.google.com/maps/api/js?sensor=false
GS.findASchool.gsGeocode = function(searchInput, callbackFunction) {
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
                geocodeResult['normalizedAddress'] = GS.findASchool.formatNormalizedAddress(results[x].formatted_address);
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

GS.findASchool.attachSchoolAutocomplete = function(queryBoxId, stateSelectId) {
    var searchBox = $('#' + queryBoxId);
    var searchStateSelect = $('#' + stateSelectId);
    var url = "/search/schoolAutocomplete.page";
    searchBox.autocomplete(url, {
        extraParams: {
            state: function() {
                return searchStateSelect.val();
            },
            schoolDistrict: true
        },
        minChars: 3,
        selectFirst: false,
        cacheLength: 150,
        matchSubset: true,
        max: 6,
        autoFill: false
    });

    searchStateSelect.blur(function() {
        searchBox.flushCache();
    });
};

GS.findASchool.attachCityAutocomplete = function(queryBoxId) {
    var searchBox = $('#' + queryBoxId);
    var url = "/search/cityAutocomplete.page";
    searchBox.autocomplete(url, {
        minChars: 3,
        selectFirst: false,
        cacheLength: 150,
        matchSubset: true,
        max: 6,
        autoFill: false
    });
};

GS.findASchool.buildQueryString = function(queryString) {
    //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
    queryString = GS.uri.Uri.removeFromQueryString(queryString, "gradeLevels");
    var checkedGradeLevels = jQuery('#js-gradeLevels :checked');
    var overwriteGradeLevels = true;
    checkedGradeLevels.each(function() {
        if (jQuery(this).val() !== '') {
            queryString = GS.uri.Uri.putIntoQueryString(queryString, "gradeLevels", jQuery(this).val(), overwriteGradeLevels);
            overwriteGradeLevels = false;
        }
    });

    queryString = GS.uri.Uri.removeFromQueryString(queryString, "st");
    var checkedSchoolTypes = jQuery('#js-schoolTypes :checked');
    var overwriteSchoolTypes = true;
    checkedSchoolTypes.each(function() {
        if (jQuery(this).val() !== '') {
            queryString = GS.uri.Uri.putIntoQueryString(queryString, "st", jQuery(this).val(), overwriteSchoolTypes);
            overwriteSchoolTypes = false;
        }
    });

    return queryString;
};

GS.findASchool.FilterTracking = function() {
    var gradeLevel = new Object();
    gradeLevel['p'] = 'PK';
    gradeLevel['e'] = 'elem';
    gradeLevel['m'] = 'middle';
    gradeLevel['h'] = 'high';
    gradeLevel['all'] = 'all';

    this.track = function(cssId) {
        var lastHyphenIndex = cssId.lastIndexOf('-');
        var customLinkName;
        if (lastHyphenIndex > 0) {
            var cssIdPrefix = cssId.substr(0,lastHyphenIndex);
            var filter = cssId.substr(lastHyphenIndex + 1);
            if (cssIdPrefix === 'school-type') {
                customLinkName = 'FAS_filter_type_' + filter;
            } else if (cssIdPrefix === 'grade-level') {
                customLinkName = 'FAS_filter_grade_' + gradeLevel[filter];
            } else if (cssIdPrefix === 'radius') {
                customLinkName = 'FAS_filter_distance_' + filter;
            }

            if (customLinkName != undefined) {
                if (s.tl) {
                    s.tl(true, 'o', customLinkName);
                }
            }
        }
    };
};

$(function() {
    GS.findASchool.filterTracking = new GS.findASchool.FilterTracking();

    $("#byLocationTab").click(function() {
        $('#byLocationTab').addClass('selected');
        $('#byLocationTabBody').show();
        $('#byNameTab').removeClass('selected');
        $('#byNameTabBody').hide();
    });
    $("#byNameTab").click(function() {
        $('#byLocationTab').removeClass('selected');
        $('#byLocationTabBody').hide();
        $('#byNameTab').addClass('selected');
        $('#byNameTabBody').show();
    });
    GS.findASchool.attachCityAutocomplete('findByLocationBox');
    GS.findASchool.attachSchoolAutocomplete('findByNameBox', 'findByNameStateSelect');
    $('#findByLocationForm').submit(function() {
        return GS.findASchool.submitByLocationSearch();
    });
    jQuery('#js-gradeLevels input').click(function () {
        var cssId = jQuery(this).attr('id');

        var gradeCheckboxes = jQuery('ul.filterBar .jq-grade-level');

        // may need to change checkbox checking in jQuery 1.6+
        // http://stackoverflow.com/questions/426258/how-do-i-check-a-checkbox-with-jquery-or-javascript
        if (cssId === 'grade-level-all') {
            if (jQuery(this).is(':checked')) {
                gradeCheckboxes.attr('checked','checked');
            } else {
                gradeCheckboxes.removeAttr('checked');
            }
        }
        var numGradeLevels = gradeCheckboxes.size();
        var numGradeLevelsChecked = gradeCheckboxes.filter(':checked').size();
        if (numGradeLevels == numGradeLevelsChecked) {
            jQuery('#grade-level-all').attr('checked','checked');
        } else {
            jQuery('#grade-level-all').removeAttr('checked');
        }

        GS.findASchool.filterTracking.track(cssId);
    });
    jQuery('#js-schoolTypes input').click(function () {
        var cssId = jQuery(this).attr('id');

        var typeCheckboxes = jQuery('ul.filterBar .jq-school-type');

        // may need to change checkbox checking in jQuery 1.6+
        // http://stackoverflow.com/questions/426258/how-do-i-check-a-checkbox-with-jquery-or-javascript
        if (cssId === 'school-type-all') {
            if (jQuery(this).is(':checked')) {
                typeCheckboxes.attr('checked','checked');
            } else {
                typeCheckboxes.removeAttr('checked');
            }
        }
        var numSchoolTypes = typeCheckboxes.size();
        var numSchoolTypesChecked = typeCheckboxes.filter(':checked').size();
        if (numSchoolTypes == numSchoolTypesChecked) {
            jQuery('#school-type-all').attr('checked','checked');
        } else {
            jQuery('#school-type-all').removeAttr('checked');
        }

        GS.findASchool.filterTracking.track(cssId);
    });
    jQuery('#js-radius input').click(function() {
        GS.findASchool.filterTracking.track(jQuery(this).attr('id'));
    });

});