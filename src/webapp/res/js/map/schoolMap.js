if (GS == undefined) {
    var GS = {};
}

if (GS.map == undefined) {
    GS.map = {};
}


GS.map.MapSchool = function(id, databaseState, name, latitude, longitude) {
    //decided to break encapsulation since this is mostly a
    //transfer object
    this.id = id;
    this.databaseState = databaseState;
    this.name = name;
    this.latitude = latitude;
    this.longitude = longitude;
    this.type = null;
    this.gsRating = null;
    this.parentRating = null;
    this.infoWindowMarkup = null; //I don't know if this should be here, but it was convenient
};

GS.map.greenArrowMarker = new google.maps.MarkerImage('/res/img/map/green_arrow.png',
            new google.maps.Size(39, 34),
            new google.maps.Point(0, 0),
            new google.maps.Point(11, 34));

GS.map.greenArrowShadow = new google.maps.MarkerImage('/res/img/map/green_arrow_shadow.png',
            new google.maps.Size(39, 34),
            new google.maps.Point(0, 0),
            new google.maps.Point(11, 34));

GS.map.standardShadow = new google.maps.MarkerImage('/res/img/map/GS_gsr_1_backgroundshadow.png',
            new google.maps.Size(40,40),
            new google.maps.Point(0,0),
            new google.maps.Point(11,34));

GS.map.SchoolMap = function(id, centerLatitude, centerLongitude, useBubbles) {

    var markers = {};
    var infoWindow = new google.maps.InfoWindow();
    var center = new google.maps.LatLng(centerLatitude, centerLongitude);
    var mapOptions = {
        zoom: 15,
        center: center,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };

    map = new google.maps.Map(document.getElementById(id), mapOptions);

    if (useBubbles) {
        google.maps.event.addListener(map, 'click', function() {
            infoWindow.close();
        });
    }

    //when the map finishes loading, draw the markers and zoom out to see all markers
    google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
        this.drawMarkers();
        this.expandMapToFitMarkers();
    }.gs_bind(this));

    
    //////////////////////////////////////////////////////////////////////////////////
    this.getMap = function() {
        return map;
    };

    this.centerOnSchool = function(state, id) {
        var marker = markers[state + id];
        if (marker != undefined) {
            map.panTo(marker.getPosition());
        }
    };

    this.addMarker = function(marker, databaseState, id, markerClickedCallback) {
        google.maps.event.addListener(marker, "click", function() {
            if (useBubbles) {
                infoWindow.setContent(marker.infoWindowMarkup);
                infoWindow.open(map, marker);
            }
            if (markerClickedCallback != undefined) {
                markerClickedCallback(databaseState, id);
            }
        });

        if (!useBubbles) {
           marker.setClickable(true);
        }
        markers[databaseState + id] = marker;
    };

    this.createAndAddMarker = function(id, databaseState, name, lat, lon, rating, infoWindowMarkup, markerClickedCallback, type) {
        var marker = this.createMarker(id, databaseState, name, lat, lon, rating, type);

        marker.infoWindowMarkup = infoWindowMarkup;

        this.addMarker(marker, databaseState, id, markerClickedCallback);
    };

    this.createAndAddCenterMarker = function(id, databaseState, name, lat, lon, infoWindowMarkup, markerClickedCallback) {
        var marker = this.createCenterMarker(id, databaseState, name, lat, lon);

        marker.infoWindowMarkup = infoWindowMarkup;

        this.addMarker(marker, databaseState, id, markerClickedCallback);
    };

    this.createMarker = function(id, databaseState, name, lat, lon, rating, type) {
        if (id == undefined || databaseState == undefined || lat == undefined || lon == undefined) {
            return;
        }

        var marker = null;
        if (type == "private") {
            marker = this.createPrivateSchoolMarker(lat, lon, name);
        } else if (type == "preschool") {
            marker = this.createPreschoolMarker(lat, lon, name);
        } else {
            marker = this.createSchoolMarker(lat, lon, name, rating);
        }

        return marker;
    };

    this.createCenterMarker = function(id, databaseState, name, lat, lon) {
        if (id == undefined || databaseState == undefined || lat == undefined || lon == undefined) {
            return;
        }

        var position = new google.maps.LatLng(lat,lon);

        var marker = new google.maps.Marker({
            position: position,
            title: name,
            icon: GS.map.greenArrowMarker,
            shadow: GS.map.greenArrowShadow
        });

        return marker;
    };

    this.createAndAddPrivateSchoolMarker = function(id, databaseState, name, lat, lon, rating, infoWindowMarkup, markerClickedCallback) {
        this.createAndAddMarker(id, databaseState, name, lat, lon, rating, infoWindowMarkup, markerClickedCallback, "private");
    };

    this.createAndAddPreschoolMarker = function(id, databaseState, name, lat, lon, rating, infoWindowMarkup, markerClickedCallback) {
        this.createAndAddMarker(id, databaseState, name, lat, lon, rating, infoWindowMarkup, markerClickedCallback, "preschool");
    };

    this.showMarkerBubble = function(state, id) {
        var marker = markers[state + id];
        if (marker != undefined) {
            infoWindow.setContent(marker.infoWindowMarkup);
            infoWindow.open(map, marker);
        }
    };

    this.getBoundsToFitMarkers = function() {
        var bounds = null;
        var position = null;

        for (var marker in markers) {
            position = markers[marker].getPosition();

            if (bounds == null) {
                bounds = new google.maps.LatLngBounds(position);
            } else {
                bounds.extend(position);
            }
        }
        return bounds;
    };

    this.expandMapToFitMarkers = function() {
        var bounds = this.getBoundsToFitMarkers();
        map.fitBounds(bounds);
    };

    this.drawMarkers = function() {
        for (var marker in markers) {
            markers[marker].setMap(map);
        }
    };

    this.removeMarker = function(state, id) {
        delete markers[state + id];
    };

    this.createPrivateSchoolMarker = function(lat, lon, tooltip) {
        var position = new google.maps.LatLng(lat, lon);
        var icon = new google.maps.MarkerImage('/res/img/map/GS_gsr_private_forground.png',
            new google.maps.Size(40, 40),
            new google.maps.Point(0, 0),
            new google.maps.Point(11, 34));

        var marker = new google.maps.Marker({
            position: position,
            title: tooltip,
            icon: icon,
            shadow: GS.map.standardShadow
        });

        return marker;
    };

    this.createPreschoolMarker = function(lat, lon, tooltip) {
        var position = new google.maps.LatLng(lat, lon);
        var icon = new google.maps.MarkerImage('/res/img/map/GS_gsr_preschool_forground.png',
            new google.maps.Size(40,40),
            new google.maps.Point(0,0),
            new google.maps.Point(11,34));

        var marker = new google.maps.Marker({
            position: position,
            title: tooltip,
            icon: icon,
            shadow: GS.map.standardShadow
        });

        return marker;
    };

    this.createSchoolMarker = function(lat, lon, tooltip, rating) {
        var position = new google.maps.LatLng(lat, lon);
        if (rating != '' && rating != undefined) {
            var icon = new google.maps.MarkerImage('/res/img/map/GS_gsr_' + rating + '_forground.png',
                new google.maps.Size(40,40),
                new google.maps.Point(0,0),
                new google.maps.Point(11,34));
        } else {
            var icon = new google.maps.MarkerImage('/res/img/map/GS_gsr_na_forground.png',
                new google.maps.Size(40,40),
                new google.maps.Point(0,0),
                new google.maps.Point(11,34));
        }

        var marker = new google.maps.Marker({
            position: position,
            title: tooltip,
            icon: icon,
            shadow: GS.map.standardShadow
        });

        return marker;
    };

    this.addCenterSchool = function(school, markerClickedCallback) {
      this.createAndAddCenterMarker(school.id, school.databaseState, school.name, school.latitude, school.longitude, school.infoWindowMarkup, markerClickedCallback);
    };

    this.addSchool = function(school, markerClickedCallback) {
      this.createAndAddMarker(school.id, school.databaseState, school.name, school.latitude, school.longitude, school.gsRating, school.infoWindowMarkup, markerClickedCallback, school.type);
    };

    this.addSchools = function(schools, markerClickedCallback) {
        var len = schools.length;
        for (var i = 0; i < len; i++) {
            this.addSchool(schools[i], markerClickedCallback);
        }
    };

    //var displayTooltips = ${not empty tooltips};
    //I'm not sure where this is used so I'm leaving it here
    function doMapSchools() {
        if (GBrowserIsCompatible()) {
            initSchoolInfo();
            initMap();
        }
    }

};

    var markerClickedCallback = function(state, id) {
        jQuery('.bg-color-f4fafd input:not(:checked').each(function(item) {
            jQuery(this).parent().parent().removeClass('bg-color-f4fafd');
        });
        jQuery('#nearby-schools-' + state + id).addClass('bg-color-f4fafd');
    }