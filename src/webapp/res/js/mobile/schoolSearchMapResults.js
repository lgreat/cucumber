// depend on tracking to make sure clickCapture is defined
define(['searchResultFilters', 'uri', 'tracking'], function(searchResultFilters, uri) {
    // filters info
    var filtersSelector = '.js-searchResultFilters';

    var init = function() {
        attachEventHandlers();
        searchResultFilters.init(filtersSelector, applyFilters);
    };

    var applyFilters = function() {
        var queryString = searchResultFilters.getUpdatedQueryString();
        window.location.search = queryString;
    };

    var initializeMap = function(points, optionalLat, optionalLon) {
        require(['async!http://maps.googleapis.com/maps/api/js?sensor=false'], function() {
            $.getScript('/res/js/mobile/googleMapsInfobox.min.js').done(function() {
                initializeMapImpl(points, optionalLat, optionalLon);
            });
        });
    };

    var initializeMapImpl = function(points, optionalLat, optionalLon) {
        if (points === undefined) {
            return;
        }
        optionalLat = optionalLat || 0;
        optionalLon = optionalLon || 0;
        var centerPoint = new google.maps.LatLng(optionalLat, optionalLon);
        var bounds = new google.maps.LatLngBounds();
        var infoWindow = new google.maps.InfoWindow();
        var $redoBtn = $('.js-redobtn');
        var newMarkers = [];
        var p_length = points.length;
        var myOptions2 = {
            disableAutoPan: false,
            maxWidth: 0,
            pixelOffset: new google.maps.Size(-150, -45),
            zIndex: null,
            boxStyle: {
                opacity: 1,
                width: "300px"
            },
            closeBoxMargin: "0px",
            closeBoxURL: "/res/mobile/img/icon_close_44x44.png",
            infoBoxClearance: new google.maps.Size(1, 1),
            isHidden: false,
            pane: "floatPane",
            alignBottom:true,
            enableEventPropagation: false};
        var infoBoxInstance = new InfoBox(myOptions2);

        var myOptions = {
            center: centerPoint,
            zoom: 12,
            mapTypeId: google.maps.MapTypeId.ROADMAP,
            disableDefaultUI: true,
            infoWindow: infoWindow,
            zoomControlOptions: {style:google.maps.ZoomControlStyle.SMALL}
        };

        // readjust height of map window to device size.
        var height = 280;
        if (window.innerHeight){
            height = window.innerHeight - 90;
        }
        if (height < 140) {
            height = 140;
        }
        $('#js-map-canvas').css({height:height});
        $('#js-map-canvas-container').css({height:'auto'});

        var map = new google.maps.Map(document.getElementById("js-map-canvas"),
            myOptions);

        // Remove spinner when all tiles have loaded
        google.maps.event.addListener(map, 'tilesloaded', function() {
            $('#js-map-wait-spinner').remove();
        });
        // In case something is preventing that event from firing, always remove spinner after 2 seconds
        window.setTimeout(function() {$('#js-map-wait-spinner').remove();}, 2000);

        // perform a new search if the user wants to
        // search in a new area
        $redoBtn.click(function(){
            var latLng = map.getCenter();
            var newQueryString = searchResultFilters.getUpdatedQueryString();
            if (newQueryString.length > 0) {
                newQueryString = newQueryString.substring(1);
            }
            var queryData = uri.getQueryData(newQueryString);
            queryData['lat'] = latLng.lat();
            queryData['lon'] = latLng.lng();
            // remove query parameters that are only relevant to the previous search
            delete queryData['locationType'];
            delete queryData['normalizedAddress'];
            delete queryData['totalResults'];
            delete queryData['searchString'];
            delete queryData['state'];
            // default distance to 25 if it isn't explicitly set already
            if (queryData['distance'] === undefined) {
                queryData['distance'] = '25';
            }
            queryData['sortBy'] = 'distance';
            clickCapture.capture("events", "event65");
            window.location.href = '/search/search.page' + uri.getQueryStringFromObject(queryData);
            return false;
        });

        google.maps.event.addListener(map, 'dragend', function(){
            $redoBtn.removeClass('dn').addClass('di');
        });

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

            google.maps.event.addListener(marker, 'click', (function(marker, i) {
                return function() {
                    infoBoxInstance.setContent(marker.infoWindowMarkup);
                    infoBoxInstance.open(map, marker);
                    map.panTo(marker.position);
                    marker.setZIndex(9999);
                }
            })(marker, i));
        }

        if (!bounds.isEmpty()) {
            map.fitBounds(bounds);
        }

    };

    var listFilterToggle = function(){
        $('#js-school-search-results-table').toggle();
        $('.js-searchResultFilters').toggle();
    };

    var attachEventHandlers = function() {
        $('#searchFilter').on('click', function(){
            listFilterToggle();
        });
        $('.js-searchCancel').on('click',function(){
            listFilterToggle();
            window.scrollTo(0, 1);
        });
        $('.js-listResultsLink').on('click', function() {
            var newQueryString = searchResultFilters.getUpdatedQueryString();
            newQueryString = uri.removeFromQueryString(newQueryString, 'view');
            if (newQueryString == '?') {
                newQueryString = '';
            }
            window.location.search = newQueryString;
            return false;
        });
    };

    return {
        init:init,
        initializeMap:initializeMap
    }
});