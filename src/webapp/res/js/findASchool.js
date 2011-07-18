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

var GS_waitForGeocode = true;

function submitSearch() {
    if (!GS_waitForGeocode) {
        return true;
    }

    $('#multipleResults').hide();
    var byLocationForm = $('#findByLocationForm');
    var searchQuery = byLocationForm.find('input[name="searchString"]').val();
    searchQuery = searchQuery.replace(/^\s*/, "").replace(/\s*$/, "");
    byLocationForm.find('input[name="searchString"]').val(searchQuery);
    if (searchQuery != '' &&
        searchQuery != 'Enter city & state or zip code') {
        gsGeocode(searchQuery, function(geocodeResult) {
            if (geocodeResult != null) {
                byLocationForm.find('input[name="lat"]').val(geocodeResult['lat']);
                byLocationForm.find('input[name="lon"]').val(geocodeResult['lon']);
                byLocationForm.find('input[name="state"]').val(geocodeResult['state']);
                byLocationForm.find('input[name="locationType"]').val(geocodeResult['type']);
                byLocationForm.find('input[name="partialMatch"]').val(geocodeResult['partial_match']);

                GS_waitForGeocode = false;

                byLocationForm.submit();
            } else {
                alert("TODO: Take user to no results search page");
            }
        });
    } else {
        alert("Please enter an address, zip code or city and state");
    }

    return false;
}

var GS_geocodeResults;
// also in customizeSchoolSearchWidget.js
// requires http://maps.google.com/maps/api/js?sensor=false
function gsGeocode(searchInput, callbackFunction) {
    var geocoder = new google.maps.Geocoder();
    if (geocoder && searchInput) {
        geocoder.geocode( { 'address': searchInput + ' US'}, function(results, status) {
            var numResults = 0;
            GS_geocodeResults = new Array();
        if (status == google.maps.GeocoderStatus.OK && results.length > 0) {
            numResults = results.length;
            for (var x = 0; x < numResults; x++) {
                var geocodeResult = new Array();
                geocodeResult['lat'] = results[x].geometry.location.lat();
                geocodeResult['lon'] = results[x].geometry.location.lng();
                geocodeResult['normalizedAddress'] = results[x].formatted_address.replace(", USA","");
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
                    if (!('city' in geocodeResult)) {
                        if (results[x].address_components[i].types.contains('sublocality')) {
                            geocodeResult['city'] = results[x].address_components[i].short_name;
                        } else if (results[x].address_components[i].types.contains('locality')) {
                            geocodeResult['city'] = results[x].address_components[i].short_name;
                        } else if (results[x].address_components[i].types.contains('administrative_area_level_3')) {
                            geocodeResult['city'] = results[x].address_components[i].short_name;
                        }
                    }
                    if (results[x].address_components[i].types.contains('country')) {
                        geocodeResult['country'] = results[x].address_components[i].short_name;
                    }
                }
                // http://stackoverflow.com/questions/1098040/checking-if-an-associative-array-key-exists-in-javascript
                if (!('lat' in geocodeResult && 'lon' in geocodeResult &&
                      'city' in geocodeResult && 'state' in geocodeResult &&
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
//                if (GS_geocodeResults[0].partial_match) {
//                    alert("Found " + GS_geocodeResults[0].normalizedAddress + " which is a " + GS_geocodeResults[0].type + " (partial match).");
//                } else {
//                    alert("Found " + GS_geocodeResults[0].normalizedAddress + " which is a " + GS_geocodeResults[0].type + " (exact match).");
//                }
                callbackFunction(GS_geocodeResults[0]);
            } else {
                // ignore multiple results for now
                //handleMultipleResults(GS_geocodeResults);
                callbackFunction(GS_geocodeResults[0]);
            }
      });
    }
}
function handleMultipleResults(geocodeResults) {
    var theDiv = $('#multipleResults');
    var myList = $('<ul></ul>');
    var myListItemHtml;
    var myLinkHtml;
    for (var x=0; x < geocodeResults.length; x++) {
        myLinkHtml = '<a href="#" onclick="loadAddress(' + x + '); return false;">' +
                geocodeResults[x].normalizedAddress + '</a>';
        myListItemHtml = "<li>Did you mean " + myLinkHtml + ' (' + geocodeResults[x].type + ') ';
        myListItemHtml += ((geocodeResults[x].partial_match)?'(partial match)':'(exact match)') + '?</li>';
        $(myListItemHtml).appendTo(myList);
    }
    theDiv.empty().append(myList);
    theDiv.show();
}

function loadAddress(x) {
    var byLocationForm = $('#findByLocationForm');
    byLocationForm.find('input[name="lat"]').val(GS_geocodeResults[x]['lat']);
    byLocationForm.find('input[name="lon"]').val(GS_geocodeResults[x]['lon']);
    byLocationForm.find('input[name="state"]').val(GS_geocodeResults[x]['state']);
    byLocationForm.find('input[name="locationType"]').val(GS_geocodeResults[x]['locationType']);
    byLocationForm.find('input[name="partialMatch"]').val(GS_geocodeResults[x]['partialMatch']);

    GS_waitForGeocode = false;
    byLocationForm.submit();
    return false;
}

var attachSchoolAutocomplete = function(queryBoxId, stateSelectId) {
    var searchBox = $('#' + queryBoxId);
    var searchStateSelect = $('#' + stateSelectId);
    var url = "/search/schoolAutocomplete.page";
    searchBox.autocomplete(url, {
        extraParams: {
            state: function() {
                return searchStateSelect.val();
            }
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

$(function() {
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
});