var GS = GS || {};
GS.realEstateAgent = GS.realEstateAgent || (function(){

    var testSubmit = function() {
        alert('submit');
    }

    var submitSearch = function() {
        var propertyDetailsForm = $('#jq-propertyDetailsForm');
        var address = propertyDetailsForm.find('input#jq-address').val();
        address = address.replace(/^\s*/, "").replace(/\s*$/, "");

        if (address != '') {
            gsGeocode(address, function(geocodeResult) {
                if (geocodeResult != null) {
                    var data = {};
//                    data['lat'] = geocodeResult['lat'];
//                    data['lon'] = geocodeResult['lon'];
//                    data['zipCode'] = geocodeResult['zipCode'];
//                    data['state'] = geocodeResult['state'];
//                    data['normalizedAddress'] = geocodeResult['normalizedAddress'];
//                    data['totalResults'] = geocodeResult['totalResults'];
//                    data['locationSearchString'] = address;

                    if(geocodeResult['city'] !== undefined) {
                        propertyDetailsForm.find('input#jq-city').val(geocodeResult['city']);
                    }

                    propertyDetailsForm.find('input#jq-lat').val(geocodeResult['lat']);
                    propertyDetailsForm.find('input#jq-lon').val(geocodeResult['lon']);
                    propertyDetailsForm.find('input#jq-state').val(geocodeResult['state']);
                    propertyDetailsForm.find('input#jq-zipcode').val(geocodeResult['zipcode']);
                    propertyDetailsForm.find('input#jq-streetNumber').val(geocodeResult['streetNumber']);
                    propertyDetailsForm.find('input#jq-streetName').val(geocodeResult['streetName']);

                    window.setTimeout(function() {
                        propertyDetailsForm.submit();
                    }, 1);
                } else {
                    alert("Location not found. Please enter a valid address, city, or ZIP.");
                }
            });
        } else {
            alert("Please enter a valid address");
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
                            if ($.inArray('administrative_area_level_1', results[x].address_components[i].types) != -1) {
                                geocodeResult['state'] = results[x].address_components[i].short_name;
                            }
                            if ($.inArray('country', results[x].address_components[i].types) != -1) {
                                geocodeResult['country'] = results[x].address_components[i].short_name;
                            }
                            if ($.inArray('postal_code', results[x].address_components[i].types) != -1) {
                                geocodeResult['zipcode'] = results[x].address_components[i].short_name;
                            }
                            if ($.inArray('locality', results[x].address_components[i].types) != -1) {
                                geocodeResult['city'] = results[x].address_components[i].long_name;
                            }
                            if ($.inArray('street_number', results[x].address_components[i].types) != -1) {
                                geocodeResult['streetNumber'] = results[x].address_components[i].long_name;
                            }
                            if ($.inArray('route', results[x].address_components[i].types) != -1) {
                                geocodeResult['streetName'] = results[x].address_components[i].long_name;
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
    }
    return {
        submitSearch:submitSearch,
        gsGeocode: gsGeocode,
        testSubmit: testSubmit
    }
})();