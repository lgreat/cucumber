var GS = GS || {};
GS.map = GS.map || {};
GS.map.getMap = GS.map.getMap ||(function(){
    var GS_openSchoolInfoBubble = null;
    var GS_mapMarkers = {};
    var map = null;
    var infoBoxOptions = null;
    var infoBoxInstance = null;
    var selectedSchool = null;
    var schoolList = null;
    var savedSchools = [];
    var newMarkers = [];
    var tooltipInfoBox = null;
    var center = null;
    var bubblesSticky = true;
    var defaultPageSize = 25;

    var init = function(){
        GS.search.schoolSearchForm.init(GS.search.filters, GS.ui.contentDropdowns);
        GS.search.results.updateSortAndPageSize();
        GS.school.compare.initializeSchoolsInCompare(function() {
            return window.location.pathname + GS.search.filters.getUpdatedQueryString();
        });
        schoolList = $('#js-schoolList');
        var height = getHeight();
        schoolList.css({height: height, overflowY: 'auto'});
        $('.js-mouseover-open-bubble').live('mouseover', function() {
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

                //tooltipInfoBox = new InfoBox(tooltipOptions);
                //tooltipInfoBox.open(map, marker);
                marker.setZIndex(9999);
            }
        });

        $('.js-mouseover-open-bubble').live('mouseout', function() {
            if (!bubblesSticky) {
                closeInfoBox();
            }
        });

        $('.js-mouseover-open-bubble').live('mouseover', function() {
            if(GS_openSchoolInfoBubble !== null) {
                // if a marker is open and it wasnt opened from an event triggered from the list, don't do anything
                if (bubblesSticky) {
                    return;
                }
                GS_openSchoolInfoBubble = null;
                infoBoxInstance.close();
                removeHighlight();
            }
            var id = jQuery(this).attr('id');
            var schoolIdentifier = id.replace('school-listitem-', '');
            var marker = GS_mapMarkers[schoolIdentifier];

            showInfoBox(marker, infoBoxInstance, schoolIdentifier);
            addMouseoverHighlight();
//            google.maps.event.trigger(marker, 'click');
            bubblesSticky = false;
        });

        $('.js-mouseover-open-bubble').live('click', function() {
            if(GS_openSchoolInfoBubble !== null) {
                GS_openSchoolInfoBubble = null;
                infoBoxInstance.close();
                removeHighlight();
            }
            var id = jQuery(this).attr('id');
            var schoolIdentifier = id.replace('school-listitem-', '');
            var marker = GS_mapMarkers[schoolIdentifier];

            showInfoBox(marker, infoBoxInstance, schoolIdentifier);
            scrollSchoolList();
            addHighlight();
//            google.maps.event.trigger(marker, 'click');
            bubblesSticky = true;
        });

        //Bind the custom event that gets triggered after a school is removed from the compare module.
        //This is used to un check the check boxes for the school that is removed from the compare module.
        $('body').on('schoolRemoved', function(event, schoolId, state) {
            var checkBox = $('#' + state + schoolId);
            checkBox.prop('checked', false);
            //After the school is removed, the 'compare now' link should be switched to 'compare' label.
            //Also if there are less than 2 schools remaining in the compare module after this school has been deleted
            //then switch all the the 'compare now' links to 'compare' label.
            if (GS.school.compare.getSchoolsInCompare().length < 2) {
                // change all
                $('.js-compareLabel').html('Compare');
            } else {
                // change just the one
                changeCompareLinkToLabel(state, schoolId);
            }
        });

        $('#contentGS').on('click', '#js_reloadMap', function() {
            if (s.tl) {
                s.tl(this, 'o', 'SearchResults_Map_Redo_Search');
            }
            redoSearchOnMapMove();
        });
    };

    var initMap = function (points, optionalLat, optionalLon) {
        optionalLat = optionalLat || 0;
        optionalLon = optionalLon || 0;
        var centerPoint = new google.maps.LatLng(optionalLat, optionalLon);
        var infoWindow = new google.maps.InfoWindow();

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
        var height = getHeight();

        $('#js-map-canvas').css({height:height});

        map = new google.maps.Map(document.getElementById("js-map-canvas"), myOptions);
        if (points !== undefined) {
            loadMarkers(points);
        } else if (optionalLat !== undefined && optionalLon !== undefined) {
            // if there were no points (because there were no school results) we still want to do the minimum needed to draw the map
            center = map.getCenter();
            map.setZoom(17);
        }
    }

    var getHeight = function() {
        var height;
        if (window.innerHeight){
            height = (window.innerHeight > 1000)? window.innerHeight/2 : 500;
        }
        else {
            height = 500;
        }
        return height;
    };

    var loadMarkers = function (points) {
        var bounds = new google.maps.LatLngBounds();
        var p_length = 0;
        if(points !== null) {
            p_length = points.length;
        }
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
                width: "320px"
            },
            closeBoxMargin: "23px 8px 0 8px",
            closeBoxURL:"/res/img/googleMaps/16x16_close.png",
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

            var imageUrl = '/res/img/sprites/icon/mapPins/x32/120808-mapPinsx32.png';
            var pixelOffset = 320; // default to n/a

            if (point.gsRating != "" && parseInt(point.gsRating) > 0) {
                pixelOffset = (10 - point.gsRating) * 32;
            } else if (point.levelCode === 'p') {
                pixelOffset = 384;
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
            google.maps.event.addListener(marker, 'click', (function(marker, i, schoolIdentifier) {
                return function() {
                    if(selectedSchool !== null) {
                        removeHighlight();
                    }
                    showInfoBox(marker, infoBoxInstance, schoolIdentifier);
                    scrollSchoolList();

                    if (s.tl) {
                        s.tl(true, 'o', 'SearchResults_Map_School_Pin_Click');
                    }
                }
            })(marker, i, schoolIdentifier));

        }

        if(p_length > 0) {
            google.maps.event.addListener(infoBoxInstance,'closeclick', closeInfoBox);

            google.maps.event.addListener(infoBoxInstance, 'domready', attachEventsToBubble);

            google.maps.event.addListener(map, 'click', closeInfoBox);
        }
        else {
            var queryData = GS.uri.Uri.getQueryData();
            var position = new google.maps.LatLng(queryData['lat'], queryData['lon']);
            bounds.extend(position);
        }

        google.maps.event.addListener(map, 'dragend', function() {
            closeInfoBox();
            var currentCenter = map.getCenter();
            var distancePanned = google.maps.geometry.spherical.computeDistanceBetween (center, currentCenter, 3963.1676);
            if(distancePanned >= 3) {
                var $redoSearch = $("#js_reloadMap");
                $redoSearch.show();
            }
        });

        if (!bounds.isEmpty()) {
            map.setCenter(bounds.getCenter(), map.fitBounds(bounds));
            center = map.getCenter();
        }
    };

    var redoSearchOnMapMove = function() {
        var queryString = window.location.search;
        var queryStringData = GS.uri.Uri.getQueryData(queryString);

        var geocoder = new google.maps.Geocoder();
        var currentCenter = map.getCenter();
        var newData = {};
        geocoder.geocode({latLng: currentCenter}, function(results, status) {
            if(status === google.maps.GeocoderStatus.OK) {
                var addressComponents = results[0].address_components;
                $.each(addressComponents, function(i, addressComponent) {
                    if(addressComponent.types[0] === 'administrative_area_level_1') {
                        newData.state = addressComponent.short_name;
                    }
                    else if(addressComponent.types[0] === 'locality') {
                        newData.city = addressComponent.long_name;
                    }
                    else if(addressComponent.types[0] === 'postal_code') {
                        newData.zipCode = addressComponent.short_name;
                    }
                });

                newData.lat = currentCenter.lat();
                newData.lon = currentCenter.lng();

                for(var key in queryStringData) {
                    if(key !== 'search_type' && key !== 'c' && key !== 'view') {
                        delete queryStringData[key];
                    }
                }
                queryStringData.start = 0;
                queryStringData.rs = true;
                queryStringData.sortBy = 'DISTANCE';
                $.extend(queryStringData, newData);

                window.location.search = GS.uri.Uri.getQueryStringFromObject(queryStringData);            }
        });
    };

    var changeCompareLabelToLink = function(state, schoolId) {
        var $compareLabel = $('#js-' +state + schoolId + '-compare-label');
        $compareLabel.html('<a href="javascript:void(0);" class="js_compare_link noInterstitial">Compare Now</a>');
        $compareLabel.find('a').on('click', function () {
            GS.school.compare.compareSchools();
        });
    };

    var changeCompareLinkToLabel = function(state, schoolId) {
        $('#js-' + state + schoolId + '-compare-label').html('Compare');
    };

    var attachEventsToBubble = function() {
        var compareCheck = $('.js-compare-school-checkbox');
        // Bind the behavior when clicking on a compare checkbox in the list
        compareCheck.on('click', function() {
            var schoolCheckbox = $(this);
            var schoolSelected = schoolCheckbox.attr('id');
            var schoolId = schoolSelected.substr(2, schoolSelected.length);
            var state = schoolSelected.substr(0, 2);
            var checked = schoolCheckbox.is(':checked');
            if (checked === true) {
                //If the checkbox was checked then try adding a school to the compare module.
                //If adding a school fails then un check the checkbox.
                GS.school.compare.addSchoolToCompare(schoolId, state).done(
                    function() {
                        //If there are more than 2 schools in compare then the 'compare' label
                        //should be switched to 'compare now' link.
                        var schoolsInCompare = GS.school.compare.getSchoolsInCompare();
                        if (schoolsInCompare.length >= 2) {
                            for (var i = 0; i < schoolsInCompare.length; i++) {
                                changeCompareLabelToLink(schoolsInCompare[i].state, schoolsInCompare[i].schoolId);
                            }
                        }
                    }).fail(
                    function() {
                        schoolCheckbox.prop('checked', false);
                    }
                );
            } else {
                GS.school.compare.removeSchoolFromCompare(schoolId, state);
                //After the school is removed, the 'compare now' link should be switched to 'compare' label.
                changeCompareLinkToLabel(state, schoolId);
            }

            //Add omniture tracking to the check/un check of the compare checkbox.
            if (s.tl) {
                s.tl(true, 'o', 'Search_Map_Compare_Check');
            }

        });

        var schoolsInCompare = GS.school.compare.getSchoolsInCompare();
        if (schoolsInCompare) {
            var myState = compareCheck.attr('id').substr(0,2);
            var myId = compareCheck.attr('id').substr(2);
            for (var index=0; index < schoolsInCompare.length; index++) {
                if (schoolsInCompare[index].state == myState && schoolsInCompare[index].schoolId == myId) {
                    compareCheck.prop('checked', true);
                    if (schoolsInCompare.length >= 2) {
                        changeCompareLabelToLink(schoolsInCompare[index].state, schoolsInCompare[index].schoolId);
                    }
                }
            }
        }

        updateInfoBoxText();
    };

    var refreshMarkers = function(points) {
        closeInfoBox();
        if(tooltipInfoBox !== null) {
            tooltipInfoBox.close();
        }
        deleteMarkers();
        if(points !== undefined && points !== null) {
            loadMarkers(points);
        }
    }

    var deleteMarkers = function() {
        for(var i = 0; i < newMarkers.length; i++) {
            newMarkers[i].setMap(null);
        }
        newMarkers = [];
    }

    var showInfoBox = function(marker, infoBox, schoolIdentifier) {
        var div = document.createElement('div');
        div.innerHTML = marker.infoWindowMarkup;
        div.setAttribute('style', 'background: white');
        $(div).tabs();
        infoBox.setContent(div);
        infoBox.open(map, marker);
        map.panTo(marker.position);
        marker.setZIndex(9999);
        GS_openSchoolInfoBubble = schoolIdentifier;
        selectedSchool = $('#school-listitem-' + schoolIdentifier);
        addHighlight();
    }

    var closeInfoBox =  function() {
        if(infoBoxInstance !== null) {
            bubblesSticky = true;
            infoBoxInstance.close();
        }
        GS_openSchoolInfoBubble = null;
        if(selectedSchool !== null) {
            removeHighlight();
        }
    }

    var updateInfoBoxText = function() {
        var schoolInfo = $('.js-schoolInfo');
        var addMsl = schoolInfo.find('.js-add-msl');
        var addMslLink = addMsl.find('.js-add-msl-link');
        for(var i = 0; i < savedSchools.length; i++) {
            if(addMslLink.attr('id') === savedSchools[i]) {
                var notInMsl = addMsl.find('.js-notInMsl').hide();
                var existsInMsl = addMsl.find('.js-existsInMsl').show();
                return;
            }
        }

        schoolInfo.find('a').each(function() {
            $(this).attr('href', $(this).attr('data-href'));
        });
    }

    var addHighlight = function() {
//        var isWhite = null;
//        var patternBlueClassMarker = /_b/gi;
//        var communityRatingToHighlight = selectedSchool.find('.js-communityRating .sprite');
//        var gsRatingToHighlight = selectedSchool.find('.js-gsRating .sprite');
//        var communityRatingToHighlightClass = communityRatingToHighlight.attr('class');
//        var gsRatingToHighlightClass = gsRatingToHighlight.attr('class');
//
//        (communityRatingToHighlightClass.match(patternBlueClassMarker) === null) ? isWhite = true : isWhite = false;
//
//        if(isWhite){
//            var blueCommunityRatingClass = communityRatingToHighlightClass + '_b';
//            var blueGsRatingClass = gsRatingToHighlightClass + '_b';
//            communityRatingToHighlight.removeClass(communityRatingToHighlightClass).addClass(blueCommunityRatingClass);
//            gsRatingToHighlight.removeClass(gsRatingToHighlightClass).addClass(blueGsRatingClass);
//        }

        // set appropriate list item background color
        selectedSchool.css('background', '#E2F1F7');
    }

    var addMouseoverHighlight = function() {
        selectedSchool.css('background','#F8F8F8');
    }

    var removeHighlight = function() {
        selectedSchool.css('background', '#FFF');
//        var patternWhiteCommunityRating = /sprite stars_sm_(\d|[a-z_]{7})/gi;
//        var communityRating = selectedSchool.find('.js-communityRating .sprite');
//        var communityRatingClass = communityRating.attr('class');
//        var whiteCommunityRatingClass = communityRatingClass.match(patternWhiteCommunityRating)[0];
//        communityRating.removeClass(communityRatingClass).addClass(whiteCommunityRatingClass);
//
//        var patternWhiteGsRating = /fltlft sprite badge_sm_(\d{1,2}|[a-z]{2})/gi;
//        var gsRating = selectedSchool.find('.js-gsRating .sprite');
//        var gsRatingClass = gsRating.attr('class');
//        var whiteGsRatingClass = gsRatingClass.match(patternWhiteGsRating)[0];
//        gsRating.removeClass(gsRatingClass).addClass(whiteGsRatingClass);
        selectedSchool = null;
    }

    var scrollSchoolList = function() {
        var listTop = schoolList.offset().top;
        var listBottom = listTop + schoolList.height();

        var itemTop = selectedSchool.offset().top;
        var itemBottom = itemTop + selectedSchool.height();

        if((itemBottom >= listBottom) || (itemTop <= listTop)) {
            schoolList.animate({
                scrollTop: itemTop - listTop + schoolList.scrollTop()
            });
        }
    }

    var addSavedSchool = function(identifier) {
        savedSchools.push(identifier);
    }

    return {
        init:init,
        initMap:initMap,
        refreshMarkers: refreshMarkers,
        addSavedSchool: addSavedSchool
    }
})();
