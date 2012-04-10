define(['sessionStorage'], function(browserStorage) {
    var controllerUri = '/search/nearby/cities.json';
    var cacheKeyPrefix = 'nearbyCities-';

    var getCacheKey = function(lat, lon) {
        var key = cacheKeyPrefix + lat.toFixed(3) + ',' + lon.toFixed(3);
        GS.log('cache key for nearbyCities: ', key);
        return key;
    };

    var fetchFromServer = function(lat, lon, successCallback) {
        $.ajax({
            url:controllerUri,
            data: {
                lat:lat,
                lon:lon,
                radius:20
            }
        }).done(function(data) {
            GS.log('got nearby cities from server ', data);

            // persist data from server
            browserStorage.setItem(getCacheKey(lat,lon), data);

            successCallback(data);

        }).fail(function() {
            GS.log('get cities call failed');
        });
    };

    var getCities = function(lat,lon, callback) {
        var data = browserStorage.getItem(getCacheKey(lat,lon));

        if (data !== null) {
            GS.log('got cities from storage: ', data);
            callback(data);
        } else {
            fetchFromServer(lat,lon, callback);
        }
    };

    return {
        getCities:getCities
    }


});