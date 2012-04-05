define(['jquery'], function($) {

    var deferred = new $.Deferred();

    var _geolocation = null; // store geolocation response from browser
    var geolocationRequested = false; // only ask browser for geolocation once

    var askForGeolocation = function() {
        navigator.geolocation.getCurrentPosition(function(geoposition) {
            GS.log('got geolocation data: ', geoposition);
            // store geolocation
            _geolocation = geoposition;
            deferred.resolve();
        }, function() {
            GS.log('geolocation request failed');
        });
    };

    var getGeolocation = function(callback) {
        deferred.done(function() {
           callback(_geolocation);
        });

        // check to see if we already got a geolocation response from the browser
        if (!deferred.isResolved()) {
            // if
            if (geolocationRequested === false) {
                geolocationRequested = true;
                askForGeolocation();
            }
        }
    };

    var getCoordinates = function(callback) {
        getGeolocation(function(geoposition) {
            GS.log('getGeoloation callback got geoposition: ', geoposition);
            callback(geoposition.coords);
        });
    };

    var init = function() {

    };

    return {
        init:init,
        getGeolocation:getGeolocation,
        getCoordinates:getCoordinates
    }

});