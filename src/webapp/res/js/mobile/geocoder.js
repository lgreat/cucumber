define(['async!http://maps.google.com/maps/api/js?sensor=false'], function() {
    // also in customizeSchoolSearchWidget.js and findASchool.js
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

    var defaultAddressFormatter = function(address) {
        var newAddress = address.replace(", USA", "");
        var zipCodePattern = /(\d\d\d\d\d)-\d\d\d\d/;
        var matches = zipCodePattern.exec(newAddress);
        if (matches && matches.length > 1) {
            newAddress = newAddress.replace(zipCodePattern, matches[1]);
        }
        return newAddress;
    };

    // also in customizeSchoolSearchWidget.js and findASchool.js
    // requires http://maps.google.com/maps/api/js?sensor=false
    var gsGeocode = function(searchInput, callbackFunction, options) {
        options = options || {};
        var formatAddress = options.addressFormatter || defaultAddressFormatter;
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
                        geocodeResult['normalizedAddress'] = formatAddress(results[x].formatted_address);
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

    return {
        geocode:gsGeocode
    }

});