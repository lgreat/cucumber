define(['search/nearbyCities', 'geolocation'],function(nearbyCitiesModule, geolocation) {
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
        return '<div class="listdiv"><a href="' + jsonObj.url + '">' + jsonObj.name + ', ' + jsonObj.state + '</a></div>';
    };

    var populateFromArray = function(arrayOfObjects) {
        $(function() {
            var $listElement = $(container).find('.listcontainer');

            for (var i = 0; i < arrayOfObjects.length; i++) {
                $listElement.append(buildListItem(arrayOfObjects[i]));
            }

            show();
        });
    };

    var populateFromLatLon = function(lat, lon, options) {
        GS.log('nearbyCitiesList populateFromLatLon');
        options = options || {};
        nearbyCitiesModule.getCities(lat,lon, function(data) {
            if (data && data.cities && data.cities.length > 0) {
                populateFromArray(data.cities);
            }
        }, options);
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