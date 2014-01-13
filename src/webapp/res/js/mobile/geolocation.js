define(['sessionStorage'], function(sessionStorage) {

    var deferred = new $.Deferred();
    var COORDINATES_KEY = 'coordinates';
    var GEOLOCATION_FAILED_KEY = "geolocation_failed";

    var _geolocation = null; // store geolocation response from browser
    var geolocationRequested = false; // only ask browser for geolocation once

    var askForGeolocation = function() {
        var options = {timeout:5000};

        if (sessionStorage.getItem(GEOLOCATION_FAILED_KEY) == 'true') {
            deferred.reject();
        } else {
            navigator.geolocation.getCurrentPosition(function(geolocation) {
                // store geolocation
                _geolocation = geolocation;
                deferred.resolve();
            }, function(error) {
                // TODO: depending on reason for failure, store failure in session storage and dont ask again
                GS.log('geolocation request failed');
                sessionStorage.setItem(GEOLOCATION_FAILED_KEY, 'true');
            }, options);

            // firefox doesn't seem to honor the error callback, and never calls it when I close the geolocation prompt
            // or hit "Not Now". So call a function with setTimeout where timeout is greater than timeout that's sent
            // to the getCurrentPosition call. If we don't have a geolocation then the call failed
            setTimeout(function() {
                if (_geolocation === null) {
                    GS.log('geolocation call did not succeed. will not try again.');
                    sessionStorage.setItem(GEOLOCATION_FAILED_KEY, 'true');
                }
            }, 6000);
        }
    };

    var getGeolocation = function(successCallback) {
        deferred.done(function() {
           successCallback(_geolocation);
        });

        // check to see if we already got a geolocation response from the browser
        if (deferred.state() != 'resolved') {
            // if
            if (geolocationRequested === false) {
                geolocationRequested = true;
                askForGeolocation();
            }
        }
    };

    var getCoordinates = function(successCallback) {

        var coordinates = sessionStorage.getItem(COORDINATES_KEY);
        if (!coordinates) {
            getGeolocation(function(geoposition) {
                GS.log('getGeoloation callback got geoposition: ', geoposition);
                sessionStorage.setItem(COORDINATES_KEY, {
                    latitude:geoposition.coords.latitude,
                    longitude:geoposition.coords.longitude
                });
                successCallback(geoposition.coords);
            });
        } else {
            GS.log('got coordinates from session storage: ', coordinates);
            successCallback(coordinates);
        }
    };

    var hasGeolocation = function() {
        if (_geolocation !== null) {
            return true;
        } else {
            var coordinates = sessionStorage.getItem(COORDINATES_KEY);
            if (coordinates !== undefined && coordinates !== null) {
                return true;
            }
        }
    };

    var init = function() {

    };

    return {
        init:init,
        getGeolocation:getGeolocation,
        getCoordinates:getCoordinates,
        hasGeolocation:hasGeolocation
    }

});