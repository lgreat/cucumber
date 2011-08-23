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
    var queryString = $('#jq-findByLocationForm').serialize();
    queryString = GS.findASchool.buildQueryString(queryString);
    window.location.href = '/search/search.page' + queryString;
};

GS.findASchool.submitByLocationSearch = function() {
    var byLocationForm = $('#jq-findByLocationForm');
    var searchQuery = byLocationForm.find('input[name="searchString"]').val();
    searchQuery = searchQuery.replace(/^\s*/, "").replace(/\s*$/, "");
    if (searchQuery != '' &&
        searchQuery != 'Search by city AND state or address ...' && !GS.findASchool.isTermState(searchQuery)) {
        byLocationForm.find('input[name="searchString"]').val(searchQuery);
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
                alert("Location not found. Please enter a valid address, city, or ZIP.");
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
        autoFill: false,
        dataType: "text"
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
            if (cssIdPrefix === 'js-school-type') {
                customLinkName = 'FAS_filter_type_' + filter;
            } else if (cssIdPrefix === 'js-grade-level') {
                customLinkName = 'FAS_filter_grade_' + gradeLevel[filter];
            } else if (cssIdPrefix === 'js-radius') {
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

GS.findASchool.setDistanceRadius = function() {
    var distanceRadiusRadiobuttonText = jQuery('input[name=distance]:checked + label').text();
    jQuery('#js-radius-label').text(distanceRadiusRadiobuttonText);
};

GS.findASchool.setAllGrades = function() {
    var gradeCheckboxes = jQuery('#js-gradeLevels .jq-grade-level');

    var numGradeLevels = gradeCheckboxes.size();
    var numGradeLevelsChecked = gradeCheckboxes.filter(':checked').size();
    if (numGradeLevelsChecked === numGradeLevels) {
        // TODO-JQ-12088 swap following lines
        jQuery('#js-grade-level-all').attr('checked', 'checked');
//        jQuery('#js-grade-level-all').prop('checked', true);
        jQuery('#js-grade-level-label').empty().append('All grades')
    } else if (numGradeLevelsChecked === 0) {
        // TODO-JQ-12088 swap following lines
        jQuery('#js-grade-level-all').removeAttr('checked');
//        jQuery('#js-grade-level-all').removeProp('checked');
        jQuery('#js-grade-level-label').empty().append('No grades');
    } else {
        // TODO-JQ-12088 swap following lines
        jQuery('#js-grade-level-all').removeAttr('checked');
//        jQuery('#js-grade-level-all').removeProp('checked');
        jQuery('#js-grade-level-label').empty().append('Some grades');
    }
};

GS.findASchool.setAllTypes = function() {
    var typeCheckboxes = jQuery('#js-schoolTypes .jq-school-type');

    var numSchoolTypes = typeCheckboxes.size();
    var numSchoolTypesChecked = typeCheckboxes.filter(':checked').size();
    if (numSchoolTypesChecked === numSchoolTypes) {
        // TODO-JQ-12088 swap following lines
        jQuery('#js-school-type-all').attr('checked', 'checked');
//        jQuery('#js-school-type-all').prop('checked', true);
        jQuery('#js-school-type-label').empty().append('All types')
    } else if (numSchoolTypesChecked === 0) {
        // TODO-JQ-12088 swap following lines
        jQuery('#js-school-type-all').removeAttr('checked');
//        jQuery('#js-school-type-all').removeProp('checked');
        jQuery('#js-school-type-label').empty().append('No types');
    } else {
        // TODO-JQ-12088 swap following lines
        jQuery('#js-school-type-all').removeAttr('checked');
//        jQuery('#js-school-type-all').removeProp('checked');
        jQuery('#js-school-type-label').empty().append('Some types');
    }
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

    GS.findASchool.attachCityAutocomplete('js-findByLocationBox');
    GS.findASchool.attachSchoolAutocomplete('js-findByNameBox', 'js-findByNameStateSelect');
    $('#jq-findByLocationForm').submit(function() {
        return GS.findASchool.submitByLocationSearch();
    });
    $('#jq-findByNameForm').submit(function() {
        var defaultText = "   Search by school or district ...";
        if ($('#js-findByNameBox').val() == defaultText) {
            $('#js-findByNameBox').val('');
        }
        return true;
    });
    jQuery('#js-gradeLevels input').click(function () {
        var cssId = jQuery(this).attr('id');

        var gradeCheckboxes = jQuery('#js-gradeLevels .jq-grade-level');

        // may need to change checkbox checking in jQuery 1.6+
        // http://stackoverflow.com/questions/426258/how-do-i-check-a-checkbox-with-jquery-or-javascript
        if (cssId === 'js-grade-level-all') {
            if (jQuery(this).is(':checked')) {
                // TODO-JQ-12088 swap following lines
                gradeCheckboxes.attr('checked','checked');
//                gradeCheckboxes.prop('checked',true);
            } else {
                // TODO-JQ-12088 swap following lines
                gradeCheckboxes.removeAttr('checked');
//                gradeCheckboxes.removeProp('checked');
            }
        }

        GS.findASchool.setAllGrades();

        GS.findASchool.filterTracking.track(cssId);
    });
    jQuery('#js-schoolTypes input').click(function () {
        var cssId = jQuery(this).attr('id');

        var typeCheckboxes = jQuery('#js-schoolTypes .jq-school-type');

        // may need to change checkbox checking in jQuery 1.6+
        // http://stackoverflow.com/questions/426258/how-do-i-check-a-checkbox-with-jquery-or-javascript
        if (cssId === 'js-school-type-all') {
            if (jQuery(this).is(':checked')) {
                // TODO-JQ-12088 swap following lines
                typeCheckboxes.attr('checked','checked');
//                typeCheckboxes.prop('checked',true);
            } else {
                // TODO-JQ-12088 swap following lines
                typeCheckboxes.removeAttr('checked');
//                typeCheckboxes.removeProp('checked');
            }
        }
        GS.findASchool.setAllTypes();

        GS.findASchool.filterTracking.track(cssId);
    });
    jQuery('#js-radius input').click(function() {
        GS.findASchool.setDistanceRadius();
        
        GS.findASchool.filterTracking.track(jQuery(this).attr('id'));
    });

    // make sure the 'all' check boxes are correctly set on page load (bug from GS-11931)
    GS.findASchool.setDistanceRadius();
    GS.findASchool.setAllGrades();
    GS.findASchool.setAllTypes();

    jQuery('#jq-findByNameForm').submit(function() {
        var stateVal = jQuery('#js-findByNameStateSelect').val();
        if (stateVal == '') {
            alert("Please select a state.");
            return false;
        }
        return true;
    });

    // initialize drawers
    $(".js-cabinet > .js-drawer").hide();
    $(".js-cabinet > .js-drawer").removeClass("hide");
    $(".js-close").parent().hide();

    // open drawer
    $(".js-open").click(function () {
        var anchor = $(this);
        var anchorContainer = anchor.parent();
        var myCabinet = anchorContainer.parent();
        var myDrawer = myCabinet.find(".js-drawer");
        //$(this).parent().parent().find(".js-drawer").show();
        if (myDrawer.is(":hidden")) {
            myDrawer.slideDown(400);
            anchorContainer.hide();
            myCabinet.find(".js-close").parent().show();
        }
    });

    // close drawer
    $(".js-close").click(function () {
        var anchor = $(this);
        var anchorContainer = anchor.parent();
        var myCabinet = anchorContainer.parent();
        var myDrawer = myCabinet.find(".js-drawer");
        //$(this).parent().parent().find(".js-drawer").show();
        if (myDrawer.is(":visible")) {
            myDrawer.slideUp("slow");
            anchorContainer.hide();
            myCabinet.find(".js-open").parent().show();
        }
    });

    // custom links for editorial module
    $(".js_editorialModule ul li a").click(function() {
        if (s.tl) {
            s.tl(this,'o',this.href);
        }
    });

    $("#jq-filterBar .filterCategory").click(function(){
        var catNumber = this.id.replace("category-", "");
        $("#jq-filterBar .dropDown-FAS").not("#dropDown-FAS-" + catNumber).hide();
        $("#dropDown-FAS-" + catNumber).toggle();
        var filterCategory = $("#jq-filterBar .filterCategory");
        filterCategory.find(".dropDown-default").not("#category-" + catNumber + " .dropDown-default").show();
        filterCategory.find(".dropDown-hover").not("#category-" + catNumber + " .dropDown-hover").hide();
        $("#category-" + catNumber + " .dropDown-default, #category-" + catNumber + " .dropDown-hover").toggle();
    });

    $("#jq-filterBar .dropDown-FAS button").click(function(){
        var catNumber = this.id.replace("hideDropDown-", "");
        $("#dropDown-FAS-" + catNumber).toggle();
        $("#category-" + catNumber + " .dropDown-default").show();
        $("#category-" + catNumber + " .dropDown-hover").hide();
    });

//    $("body").not("#js-gradeLevels").click(function(){
//        alert("hide");
//    });

//    $(body).click(function(){
//        $("#dropDown-FAS-1").hide();
//    });
//    state drop for by location
    $("#js-findByNameStateSelect").change(function () {
          var selectedState = $(this).val();
          $(".showState").text(selectedState === "" ? "Select State" : selectedState);
        })
    .trigger("change");
});