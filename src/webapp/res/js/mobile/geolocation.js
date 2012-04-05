define(function() {
    // TODO: there's a design problem that will cause the callback from the first request to getGelocation to succeed
    // once the user clicks "Accept" in the browser prompt, but future calls will time out.
    // module should allow an array of callbacks to be registered, and then a module-specific callback can be
    // given to the geolocation.getCurrentPosition call, which would then execute all the registered callbacks

    var _geolocation = null; // store geolocation response from browser
    var geolocationRequested = false; // only ask browser for geolocation once
    var timeSpentWaiting = 0; // track time spent waiting for browser to get back with a response

    var getGeolocation = function(callback) {
        // check to see if we already got a geolocation response from the browser
        if (_geolocation === null) {
            // if
            if (geolocationRequested === false) {
                geolocationRequested = true;
                navigator.geolocation.getCurrentPosition(function(geoposition) {
                    GS.log('got geolocation data: ', geoposition);
                    _geolocation = geoposition;
                    callback(geoposition);
                });
            } else {
                // we've asked the browser for geolocation data but _geolocation is still null
                if (timeSpentWaiting < 10000) {
                    setTimeout(function() {
                        timeSpentWaiting += 250;
                        getGeolocation(callback); // recursion
                    }, 250);
                }
            }
        } else {
            callback(_geolocation);
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