define(['searchResultFilters'], function(searchResultFilters) {
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
        var height = 200;
        if (window.innerHeight){
            height = window.innerHeight - 130;
        }
        $('#js-map-canvas').css({height:height});


        var map = new google.maps.Map(document.getElementById("js-map-canvas"),
            myOptions);

        // perform a new search if the user wants to
        // search in a new area
        $redoBtn.click(function(){
            var latLng = map.getCenter();
            var url = String(window.location).replace(/([\&\?])(lat=)([^\&]+)/, "$1lat=" + latLng.lat()).replace(/([\&\?])(lon=)([^\&]+)/,"$1lon=" + latLng.lng());
            window.location = url;
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

            var imageUrl = '/res/img/map/GS_gsr_na_forground.png';

            if (points[i].gsRating != "") {
                imageUrl = '/res/img/map/GS_gsr_' + points[i].gsRating + '_forground.png';
            }

            if (points[i].schoolType === 'private') {
                imageUrl = '/res/img/map/GS_gsr_private_forground.png';
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
                closeBoxURL: "https://encrypted-tbn3.google.com/images?q=tbn:ANd9GcRY6-LN3wuV7sm6jc98OFRTd9Ri6E-q4JsGZwYfkadb0m9-Lc-1AA",
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

        google.maps.event.addListener(map, 'dragend', function(){
            $redoBtn.show().css('display','block');
        });
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
            var href = $(this).attr('href');
            var newHref = uri.appendQueryString(href, searchResultFilters.getUpdatedQueryString());
            window.location.href = newHref;
            return false;
        });
    };

    return {
        init:init,
        initializeMap:initializeMap
    }
});