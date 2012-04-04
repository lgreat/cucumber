define(['jquery'], function($) {
    var coordinates = {};

    var geolocationCallback = function(geoPosition) {
        coordinates = geoPosition.coords;
        GS.log('got geolocation data', geoPosition);
    };

    var promptForGeolocation = function() {
        navigator.geolocation.getCurrentPosition(geolocationCallback);
    };

    var getCoordinates = function() {
        return coordinates;
    };

    var init = function() {
        $(function() {
            promptForGeolocation();
        });
    };

    return {
        promptForGeoLocation:promptForGeolocation,
        geoCoordinates: getCoordinates,
        init:init
    }
});