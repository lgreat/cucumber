define(['jquery','search/nearbyCities', 'geolocation'],function($, nearbyCitiesModule, geolocation) {
    var container = null;

    var init = function(selector) {
        GS.log('nearbyCitiesList init');
        if (selector !== undefined) {
            container = selector;
        }
    };

    var show = function() {
        $(container).show();
    };

    var buildListItem = function(jsonObj) {
        return '<li><a href="' + jsonObj.url + '">' + jsonObj.name + ', ' + jsonObj.state + '</a></li>';
    };

    var populateFromArray = function(arrayOfObjects) {
        var $listElement = $(container).find('ul:empty');

        for (var i = 0; i < arrayOfObjects.length; i++) {
            $listElement.append(buildListItem(arrayOfObjects[i]));
        }

        show();
    };

    var populateFromLatLon = function(lat, lon) {
        GS.log('nearbyCitiesList populateFromLatLon');
        nearbyCitiesModule.getCities(lat,lon, function(data) {
            if (data && data.cities && data.cities.length > 0) {
                populateFromArray(data.cities);
            }
        });
    };

    var populate = function() {
        GS.log('nearbyCitiesList populate');
        geolocation.getCoordinates(function(coordinates) {
            populateFromLatLon(coordinates.latitude, coordinates.longitude);
        });
    };

    return {
        init:init,
        populateFromLatLon:populateFromLatLon,
        populate:populate
    }
});