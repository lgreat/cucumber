GS = GS || {};

GS.schoolSearchResultsPage = GS.schoolSearchResultsPage || (function() {
    var listResultsLinkSelector = '.js-listResultsLink';
    var mapResultsLinkSelector = '.js-mapResultsLink';

    var init = function() {
        registerEventHandlers();
    };

    var registerEventHandlers = function() {
        $(listResultsLinkSelector).on('click', function() {
            if(GS.uri.Uri.getFromQueryString('view') === undefined) {
                return;
            }
            else {
                var uri = window.location.search;
                uri = GS.uri.Uri.removeFromQueryString(uri, 'view');
                window.location.search = uri;
            }
        });
        $(mapResultsLinkSelector).on('click', function() {
            if(GS.uri.Uri.getFromQueryString('view') === 'map') {
                return;
            }
            else {
                var uri = window.location.search;
                uri = GS.uri.Uri.putIntoQueryString(uri, 'view', 'map', true);
                window.location.search = uri;
            }
        });
    };

    return {
        init:init

    }

})();

jQuery(function () {
    GS.schoolSearchResultsPage.init();
});

GS.search.getMap = function (points, optionalLat, optionalLon) {
    optionalLat = optionalLat || 0;
    optionalLon = optionalLon || 0;
    var centerPoint = new google.maps.LatLng(optionalLat, optionalLon);
    var bounds = new google.maps.LatLngBounds();
    var infoWindow = new google.maps.InfoWindow();
    var newMarkers = [];
    var p_length = points.length;
    var myOptions2 = {
        disableAutoPan: false,
        maxWidth: 0,
        pixelOffset: new google.maps.Size(-150, -45),
        zIndex: null,
        boxStyle: {
//            background: "url('C:\\Users\\rramachandran\\Downloads\ramprasad.gif') no-repeat",
            opacity: 1,
            width: "400px"
        },
        closeBoxMargin: "8px",

        closeBoxURL: "http://staging.greatschools.org/res/mobile/img/icon_close_24x24.png",
        infoBoxClearance: new google.maps.Size(1, 1),
        isHidden: false,
        pane: "floatPane",
        alignBottom:true,
        enableEventPropagation: false};

    var myOptions = {
        center: centerPoint,
        zoom: 12,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        disableDefaultUI: true,
        infoWindow: infoWindow,
        mapTypeControl: true,
        mapTypeControlOptions: {
            mapTypeIds: [google.maps.MapTypeId.ROADMAP, google.maps.MapTypeId.SATELLITE]
        }
    };


    // need to change the difference based on design or
    // if frontend can do the same with css, then remove this block
    var height = 280;
//        if (window.innerHeight){
//            height = window.innerHeight - 130;
//        }
    $('#js-map-canvas').css({height:height});

    var map = new google.maps.Map(document.getElementById("js-map-canvas"),
        myOptions);

    // shape defines clickable region of icon as a series of points
    // coordinates increase in the X direction to the right and in the Y direction down.
    var markerShape = {
        coord: [1, 0, 27, 0, 27, 32, 1, 32],
        type: 'poly'
    };

    for ( var i = 0; i < p_length; i++ ) {
        var point = points[i];
        var position = new google.maps.LatLng(point.lat, point.lng);
        var markerOptions = {
            map: map,
            shape: markerShape,
            position: position,
            infoWindowMarkup: point.infoWindowMarkup,
            title: point.name
        };

        var imageUrl = '/res/mobile/img/map_pins/32x32/schoolRatingMapPinSprite.png';
        var pixelOffset = 320; // default to n/a

        if (point.gsRating != "" && parseInt(point.gsRating) > 0) {
            pixelOffset = (10 - point.gsRating) * 32;
        } else if (point.schoolType === 'private') {
            pixelOffset = 352;
        }

        markerOptions.icon = new google.maps.MarkerImage(
            imageUrl, // url
            new google.maps.Size(32, 32), // size
            new google.maps.Point(pixelOffset, 0), // origin
            new google.maps.Point(16, 32) // anchor
        );
        var marker = new google.maps.Marker(markerOptions);

        bounds.extend(position);

        newMarkers.push(marker);

        var infoBoxInstance = new InfoBox(myOptions2);
        var infowindow1 = new google.maps.InfoWindow({map: map});

        google.maps.event.addListener(marker, 'click', (function(marker, i) {
            return function() {
                var div = document.createElement('div');
                div.innerHTML = marker.infoWindowMarkup;
                $(div).tabs();
                infowindow1.setContent(div);
                infowindow1.open(map, marker);
//                infoBoxInstance.setContent(marker.infoWindowMarkup);
//                infoBoxInstance.open(map, marker);
                map.panTo(marker.position);
                marker.setZIndex(9999);
            }
        })(marker, i));
        google.maps.event.addListener(map, 'click', function(){
            infowindow1.close();
        });
        google.maps.event.addListener(map, 'dragend', function(){
//            $redoBtn.removeClass('dn').addClass('di');
//            closeTopNav();
            infowindow1.close();
        });
    }

//    google.maps.event.addListener(map, 'dragend', function(){
//        $redoBtn.removeClass('dn').addClass('di');
//        closeTopNav();
//    });
//    google.maps.event.addListener(map, 'click', function(){
//        closeTopNav();
//    });

    if (!bounds.isEmpty()) {
        map.fitBounds(bounds);
    }
}