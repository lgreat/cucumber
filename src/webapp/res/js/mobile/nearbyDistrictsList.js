define(['search/nearbyDistricts', 'geolocation'],function(nearbyDistrictsModule, geolocation) {
    var container = null;

    var init = function(selector) {
        GS.log('nearbyDistrictsList init');
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
        $(function() {
            var $listElement = $(container).find('ul:empty');

            for (var i = 0; i < arrayOfObjects.length; i++) {
                $listElement.append(buildListItem(arrayOfObjects[i]));
            }

            show();
        });
    };

    var populateFromLatLon = function(lat, lon) {
        GS.log('nearbyDistrictsList populateFromLatLon');
        nearbyDistrictsModule.getDistricts(lat,lon, function(data) {
            if (data && data.districts && data.districts.length > 0) {
                populateFromArray(data.districts);
            }
        });
    };

    var populate = function() {
        GS.log('nearbyDistrictsList populate');
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