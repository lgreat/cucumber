var GS = GS || {};
GS.realEstateAgent = GS.realEstateAgent || {};

GS.realEstateAgent.createGuide = GS.realEstateAgent.createGuide || (function(){
    var errors = {};

    var submitSearch = function() {
        if(!skipValidation() && !validateCreateGuideForm()) {
            return false;
        }

        var propertyDetailsForm = $('#jq-propertyDetailsForm');
        var address = propertyDetailsForm.find('input#jq-propertyAddress').val();
        address = address.replace(/^\s*/, "").replace(/\s*$/, "");

        if (address != '' && address !== 'Property Address') {
            gsGeocode(address, function(geocodeResult) {
                if (geocodeResult != null) {
                    if(geocodeResult['city'] !== undefined) {
                        propertyDetailsForm.find('input#jq-city').val(geocodeResult['city']);
                    }

                    propertyDetailsForm.find('input#jq-lat').val(geocodeResult['lat']);
                    propertyDetailsForm.find('input#jq-lon').val(geocodeResult['lon']);
                    propertyDetailsForm.find('input#jq-state').val(geocodeResult['state']);
                    propertyDetailsForm.find('input#jq-zipcode').val(geocodeResult['zipcode']);
                    propertyDetailsForm.find('input#jq-streetNumber').val(geocodeResult['streetNumber']);
                    propertyDetailsForm.find('input#jq-streetName').val(geocodeResult['streetName']);

                    //TODO: comment skip user validation
                    var skipValidation = function() {
                        var params = GS.uri.Uri.getQueryData();
                        return(params.skipUserCheck === 'true');
                    }
                    propertyDetailsForm.find('input#jq-skipUserCheck').val(skipValidation());

                    //TODO: remove when pdf is ready
                    var hasPageView = function() {
                        var params = GS.uri.Uri.getQueryData();
                        return(params.pageView === 'true');
                    }
                    if(hasPageView()){
                        window.setTimeout(function() {
                            propertyDetailsForm.submit();
                        }, 1);
                        return false;
                    }

                    window.setTimeout(function() {
                        propertyDetailsForm.submit();
                    }, 1);
                } else {
                    alert("Please enter a valid street address.");
                }
            });
        } else {
            alert("Please enter an address.");
        }

        return false;
    };

    var validateAddress = function() {
        var propertyDetailsForm = $('#jq-propertyDetailsForm');
        var address = jQuery.trim(propertyDetailsForm.find('input#jq-propertyAddress').val());

        var data = {};
        if (address === '' || address === 'Property Address') {
            data.hasError = true;
            data.addressErrorDetail = 'Please enter an address.';
        }

        validateFieldResponse('.jq-propertyAddressFields .errors', data, 'addressErrorDetail');
    };

    var validateSqFootage = function() {
        var propertyDetailsForm = $('#jq-propertyDetailsForm');
        var sqFeetField = propertyDetailsForm.find('input#js-sqFeet');
        var sqFeet = jQuery.trim(sqFeetField.val());

        var data = {};
        if(!(sqFeet === '' || sqFeet === 'Square footage') && !sqFeet.match(/^((\d{1,6})|(\d{1,3},\d{3}))(\.\d{1,})?$/)) {
            data.hasError = true;
            data.sqFootageErrorDetail = 'Please enter a number.';
        }

        validateFieldResponse('.jq-sqFootageFields .errors', data, 'sqFootageErrorDetail');
    };

    var validateCreateGuideForm = function() {
        validateAddress();
        validateSqFootage();

        if(Object.keys(errors).length > 0) {
            return false;
        }
        return true;
    };

    var validateFieldResponse = function(fieldSelector, data, errorDetailKey) {
        var errorIcon ='<span class="iconx16 i-16-alert "><!-- do not collapse --></span>';
        var fieldError = jQuery(fieldSelector + ' .invalid');
        var fieldValid = jQuery(fieldSelector + ' .valid');
        fieldError.hide();
        fieldValid.hide();
        if (data && data.hasError) {
            fieldError.html(errorIcon+data[errorDetailKey]);
            fieldError.show();
            errors[errorDetailKey] = data[errorDetailKey];
        } else {
            fieldValid.show();
            delete errors[errorDetailKey];
        }
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
    };

    //TODO: comment skip validation
    var skipValidation = function() {
        var params = GS.uri.Uri.getQueryData();
        return(params.skipUserCheck === 'true');
    };

    return {
        submitSearch:submitSearch,
        gsGeocode: gsGeocode,
        validateAddress: validateAddress,
        validateSqFootage: validateSqFootage,
        skipValidation: skipValidation
    }
})();

jQuery(function() {
    if(!GS.realEstateAgent.createGuide.skipValidation()) {
        //Create guide validation
        jQuery('#jq-propertyAddress').blur(GS.realEstateAgent.createGuide.validateAddress);
        jQuery('#js-sqFeet').blur(GS.realEstateAgent.createGuide.validateSqFootage);
    }

    //TODO: remove when pdf is ready
    var hasPageView = function() {
        var params = GS.uri.Uri.getQueryData();
        return(params.pageView === 'true');
    }
    if(hasPageView()) {
        jQuery('#jq-propertyDetailsForm').attr('action', '/real-estate/guides/neighborhood-guide.page');
        jQuery('#jq-propertyDetailsForm').find('input#jq-pageView').val(hasPageView());
    }
});