var GS = GS || {};
GS.map = GS.map || {};
GS.map.getMap = GS.map.getMap ||(function(){
    var GS_openSchoolInfoBubble = null;
    var GS_mapMarkers = {};
    var map = null;
    var infoBoxOptions = null;
    var infoBoxInstance = null;
    var selectedSchool = null;
    var checkedSchools = [];

    $(document).ready(function(){
        var schoolList = $('#schoolList');
        var height = $('#js-map-canvas').css('height');
        schoolList.css({height: height, overflowY: 'scroll'});
        var tooltipInfoBox = null;
        GS.search.results.init(GS.search.filters,GS.search.compare);
        $('.js-mouseover-open-bubble').mouseover(function() {
            var id = jQuery(this).attr('id');
            var schoolIdentifier = id.replace('school-listitem-', '');
            if (GS_openSchoolInfoBubble !== null) {
                // if window is already open on this school, do nothing
                return;
            }
            else {
                var marker = GS_mapMarkers[schoolIdentifier];

                var tooltipOptions = {
                    content: marker.title,
                    maxWidth: 0,
                    disableAutoPan: false,
                    pixelOffset: new google.maps.Size(-15, 5),
                    zIndex: null,
                    boxStyle: {
                        opacity: 1,
                        border: "1px solid black",
                        textAlign: "center",
                        fontSize: "8pt",
                        width: "200px",
                        background: "white"
                    },
                    closeBoxURL: "",
                    infoBoxClearance: new google.maps.Size(1, 1),
                    isHidden: false,
                    pane: "floatPane",
                    enableEventPropagation: false
                };

                tooltipInfoBox = new InfoBox(tooltipOptions);
                tooltipInfoBox.open(map, marker);
                marker.setZIndex(9999);
            }
        });

        $('.js-mouseover-open-bubble').mouseout(function() {
            if(tooltipInfoBox !== null) {
                tooltipInfoBox.close();
            }
        });

        $('.js-mouseover-open-bubble').click(function() {
            if(tooltipInfoBox !== null) {
                tooltipInfoBox.close();
            }

            if(GS_openSchoolInfoBubble !== null) {
                GS_openSchoolInfoBubble = null;
                infoBoxInstance.close();
                removeHighlight();
            }
            var id = jQuery(this).attr('id');
            var schoolIdentifier = id.replace('school-listitem-', '');
            var marker = GS_mapMarkers[schoolIdentifier];

            infoBoxInstance = new InfoBox(infoBoxOptions);
            showInfoBox(marker, infoBoxInstance, schoolIdentifier);
        });
    });

    var initMap = function (points, optionalLat, optionalLon) {
        optionalLat = optionalLat || 0;
        optionalLon = optionalLon || 0;
        var centerPoint = new google.maps.LatLng(optionalLat, optionalLon);
        var bounds = new google.maps.LatLngBounds();
        var infoWindow = new google.maps.InfoWindow();
        var newMarkers = [];
        var p_length = points.length;

        var myOptions = {
            center: centerPoint,
            mapTypeId: google.maps.MapTypeId.ROADMAP,
            disableDefaultUI: true,
            mapTypeControl: true,
            mapTypeControlOptions: {
                mapTypeIds: [google.maps.MapTypeId.ROADMAP, google.maps.MapTypeId.SATELLITE]
            },
            zoomControl: true,
            zoomControlOptions: {
                style: google.maps.ZoomControlStyle.DEFAULT
            },
            streetViewControl: true,
            panControl: true,
            infoWindow: infoWindow
        };

        // need to change the difference based on design or
        // if frontend can do the same with css, then remove this block
        var height = 500;
        if (window.innerHeight){
            height = window.innerHeight - 130;
        }

        $('#js-map-canvas').css({height:height});

        map = new google.maps.Map(document.getElementById("js-map-canvas"), myOptions);

        // shape defines clickable region of icon as a series of points
        // coordinates increase in the X direction to the right and in the Y direction down.
        var markerShape = {
            coord: [1, 0, 27, 0, 27, 32, 1, 32],
            type: 'poly'
        };

        infoBoxOptions = {
            map: map,
            maxWidth: 0,
            disableAutoPan: false,
            pixelOffset: new google.maps.Size(-150, -45),
            zIndex: null,
            boxStyle: {
                opacity: 1,
                width: "300px",
                background: "white"
            },
            closeBoxMargin: "8px",
            closeBoxURL: "http://staging.greatschools.org/res/mobile/img/icon_close_24x24.png",
            infoBoxClearance: new google.maps.Size(1, 1),
            isHidden: false,
            pane: "floatPane",
            alignBottom:true,
            enableEventPropagation: false
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

            var schoolIdentifier = point.state + '-' + point.id;
            GS_mapMarkers[schoolIdentifier] = marker;

            infoBoxInstance = new InfoBox(infoBoxOptions);
//        var infowindow1 = new google.maps.InfoWindow({map: map});
//        var infoBubble = new InfoBubble(infoBoxOptions);

            google.maps.event.addListener(marker, 'click', (function(marker, i, schoolIdentifier) {
                return function() {
                    if(selectedSchool !== null) {
                        removeHighlight();
                    }
                    showInfoBox(marker, infoBoxInstance, schoolIdentifier);
                }
            })(marker, i, schoolIdentifier));

            google.maps.event.addListener(infoBoxInstance,'closeclick', function() {
                closeInfoBox();
            });

            google.maps.event.addListener(infoBoxInstance, 'domready', function() {
                var compareButton = $('.js-compareButton');
                var compareCheck = $('.js-compare-school-checkbox');
                GS.search.compare.updateMapInfoBox(compareCheck, compareButton);

                compareCheck.change(function() {
                    GS.search.compare.addRemoveCheckedSchoolInMap(this, compareButton);
                });
                compareButton.click(function() {
                    GS.search.results.sendToCompare();
                });
            });
        }

        google.maps.event.addListener(map, 'click', function(){
            closeInfoBox();
        });

        google.maps.event.addListener(map, 'dragend', function(){
            closeInfoBox();
        });

        if (!bounds.isEmpty()) {
            map.setCenter(bounds.getCenter(), map.fitBounds(bounds));
        }
    }

    var showInfoBox = function(marker, infoBoxInstance, schoolIdentifier) {
        var div = document.createElement('div');
        div.innerHTML = marker.infoWindowMarkup;
        div.style = "background: white";
        $(div).tabs();
        infoBoxInstance.setContent(div);
        infoBoxInstance.open(map, marker);
        map.panTo(marker.position);
        marker.setZIndex(9999);
        GS_openSchoolInfoBubble = schoolIdentifier;
        addHighlight(schoolIdentifier);
        scrollSchoolList(schoolIdentifier);
    }

    var closeInfoBox =  function() {
        if(infoBoxInstance !== null) {
            infoBoxInstance.close();
        }
        GS_openSchoolInfoBubble = null;
        if(selectedSchool !== null) {
            removeHighlight();
        }
    }

    var addHighlight = function(schoolIdentifier) {
        var isWhite = null;
        var patternBlueClassMarker = /_b/gi;
        var communityRatingToHighlight = $('#school-listitem-' + schoolIdentifier).find('.communityRating .sprite');
        var gsRatingToHighlight = $('#school-listitem-' + schoolIdentifier).find('.gsRating .sprite');
        var communityRatingToHighlightClass = communityRatingToHighlight.attr('class');
        var gsRatingToHighlightClass = gsRatingToHighlight.attr('class');

        (communityRatingToHighlightClass.match(patternBlueClassMarker) === null) ? isWhite = true : isWhite = false;

        if(isWhite){
            var blueCommunityRatingClass = communityRatingToHighlightClass + '_b';
            var blueGsRatingClass = gsRatingToHighlightClass + '_b';
            communityRatingToHighlight.removeClass(communityRatingToHighlightClass).addClass(blueCommunityRatingClass);
            gsRatingToHighlight.removeClass(gsRatingToHighlightClass).addClass(blueGsRatingClass);
        }

        // set appropriate list item background color
        $('#school-listitem-' + schoolIdentifier).addClass('highlight');
        selectedSchool = $('#school-listitem-' + schoolIdentifier);
    }

    var removeHighlight = function() {
        $('.js-mouseover-open-bubble').removeClass('highlight');
        // reset all sprite icons to white background color
        $('.communityRating .sprite').each(function() {
            var patternWhiteCommunityRating = /sprite stars_sm_(\d|[a-z_]{7})/gi;
            var communityRatingClass = $(this).attr('class');
            var whiteCommunityRatingClass = communityRatingClass.match(patternWhiteCommunityRating)[0];
            $(this).removeClass(communityRatingClass).addClass(whiteCommunityRatingClass);
        });
        $('.gsRating .sprite').each(function() {
            var patternWhiteGsRating = /sprite badge_sm_(\d{1,2}|[a-z]{2})/gi;
            var gsRatingClass = $(this).attr('class');
            var whiteGsRatingClass = gsRatingClass.match(patternWhiteGsRating)[0];
            $(this).removeClass(gsRatingClass).addClass(whiteGsRatingClass);
        });
    }

    var scrollSchoolList = function(schoolIdentifier) {
        var schoolList = $('#schoolList');
        var listTop = schoolList.offset().top;
        var listBottom = listTop + schoolList.height();

        var selectedItem = $('#school-listitem-' + schoolIdentifier);
        var itemTop = selectedItem.offset().top;
        var itemBottom = itemTop + selectedItem.height();

        if((itemBottom >= listBottom) || (itemTop <= listTop)) {
            schoolList.animate({
                scrollTop: itemTop - listTop + schoolList.scrollTop()
            });
        }
    }

    return {
        initMap:initMap
    }
})();