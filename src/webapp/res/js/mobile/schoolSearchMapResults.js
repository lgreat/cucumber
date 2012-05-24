define(['searchResultFilters', 'uri', 'async!http://maps.googleapis.com/maps/api/js?sensor=false'], function(searchResultFilters, uri) {
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

    var initializeMap = function(points) {
        var centerPoint = new google.maps.LatLng(0, 0);
        var bounds = new google.maps.LatLngBounds();
        var infoWindow = new google.maps.InfoWindow();
        var $redoBtn = $('.js-redobtn');
        var newMarkers = [];
        var p_length = points.length;


        var myOptions = {
            center: centerPoint,
            zoom: 12,
            mapTypeId: google.maps.MapTypeId.ROADMAP,
            disableDefaultUI: true,
            infoWindow: infoWindow
        };

        // TODO: readjust height of map window to device size.
        // need to change the difference based on design or
        // if frontend can do the same with css, then remove this block
        var height = 280;
//        if (window.innerHeight){
//            height = window.innerHeight - 130;
//        }
        $('#js-map-canvas').css({height:height});


        var map = new google.maps.Map(document.getElementById("js-map-canvas"),
            myOptions);

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
            window.location.href = '/search/search.page' + uri.getQueryStringFromObject(queryData);
            return false;
        });

        google.maps.event.addListener(map, 'dragend', function(){
            $redoBtn.removeClass('dn').addClass('di');
        });
        google.maps.event.addListener(map, 'click', function(){
            if($('#shownav').is(':visible')){
                $('#shownav').hide('fast');
                $('#topnav_link').find(".iconx24").removeClass('i-24-collapse').addClass('i-24-expand');
                $('#topnav_link').removeClass('but-topnav-on').addClass('but-topnav');
            }
        });

        var markerShadow = new google.maps.MarkerImage(
            "/res/img/map/GS_gsr_1_backgroundshadow.png", // url
            new google.maps.Size(42, 41), // size
            new google.maps.Point(0, 0), // origin
            new google.maps.Point(14, 39) // anchor
        );

        // shape defines clickable region of icon as a series of points
        // coordinates increase in the X direction to the right and in the Y direction down.
        var markerShape = {
            coord: [0, 0, 30, 0, 30, 37, 0, 37],
            type: 'poly'
        };

        for ( var i = 0; i < p_length; i++ ) {
            var position = new google.maps.LatLng(points[i].lat, points[i].lng);
            var markerOptions = {
                map: map,
                shadow: markerShadow,
                shape: markerShape,
                position: position,
                infoWindowMarkup: points[i].infoWindowMarkup,
                title: points[i].name
            };

            var imageUrl = '/res/mobile/img/map_pins/32x32/schoolRating_na.png';

            if (points[i].gsRating != "") {
                imageUrl = '/res/mobile/img/map_pins/32x32/schoolRating_' + points[i].gsRating + '.png';
            }

            if (points[i].schoolType === 'private') {
                imageUrl = '/res/mobile/img/map_pins/32x32/schoolRating_private.png';
            }

            markerOptions.icon = new google.maps.MarkerImage(
                imageUrl, // url
                new google.maps.Size(30, 41), // size
                new google.maps.Point(0, 0), // origin
                new google.maps.Point(14, 39) // anchor
            );
            var marker = new google.maps.Marker(markerOptions);

            bounds.extend(position);

            newMarkers.push(marker);

            var myOptions2 = {
                content: marker.infoWindowMarkup,
                disableAutoPan: false,
                maxWidth: 0,
                pixelOffset: new google.maps.Size(-150, -45),
                zIndex: null,
                boxStyle: {
                  //  background: "url('http://google-maps-utility-library-v3.googlecode.com/svn/trunk/infobox/examples/tipbox.gif') no-repeat",
                    opacity: 1,
                    width: "300px"
                },
                closeBoxMargin: "8px",
                // TODO: Change closeBoxURL from staging to www
                closeBoxURL: "http://staging.greatschools.org/res/mobile/img/icon_close_24x24.png",
                infoBoxClearance: new google.maps.Size(1, 1),
                isHidden: false,
                pane: "floatPane",
                alignBottom:true,
                enableEventPropagation: false};

            //Define the infobox
            newMarkers[i].infobox = new InfoBox(myOptions2);


            google.maps.event.addListener(marker, 'click', (function(marker, i) {
                return function() {
                    for(j=0; j < p_length; j++) newMarkers[j].infobox.close();
                    newMarkers[i].infobox.open(map, this);
                    marker.setZIndex(9999);
                    map.panTo(marker.position);
                }
            })(marker, i));
        }


        // register onclick for link tracking
        $('body').on('click','.js-overview-link', function(){
            if (s.tl) {s.tl(this,'o', 'Mobile_map_click_bubble');} return true;
        });

        map.fitBounds(bounds);
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