define(['sessionStorage'], function(browserStorage) {
    var controllerUri = '/search/nearby/districts.json';
    var cacheKeyPrefix = 'nearbyDistricts-';

    var getCacheKey = function(lat, lon) {
        var key = cacheKeyPrefix + lat.toFixed(3) + ',' + lon.toFixed(3);
        GS.log('cache key for nearbyDistricts: ', key);
        return key;
    };

    var fetchFromServer = function(lat, lon, successCallback, options) {
        $.ajax({
            url:controllerUri,
            data: {
                lat:lat,
                lon:lon,
                radius:20
            }
        }).done(function(data) {
            GS.log('got nearby districts from server: ', data);

            if (!options || !options['noStorage']) {
                // persist data from server
                browserStorage.setItem(getCacheKey(lat,lon), data);
            }

            successCallback(data);

        }).fail(function() {
            GS.log('get districts call failed');
        });
    };

    var getDistricts = function(lat,lon, callback, options) {
        var data = null;
        options = options || {};
        if (!options['noStorage']) {
            data = browserStorage.getItem(getCacheKey(lat,lon));
        }

        if (data !== null) {
            GS.log('got districts from storage: ', data);
            callback(data);
        } else {
            fetchFromServer(lat,lon, callback, options);
        }
    };

    return {
        getDistricts:getDistricts
    }


});