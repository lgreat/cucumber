define(function() {
    var init = function() {
        $('.js-linkToTop').click(function() {
            $("html,body").animate({scrollTop:0}, "slow");
            return false;
        });
    };

    var initializeMap = function(schoolName, lat, lon, gsRating, schoolType, googleUrlSchool) {
        var centerPoint = new google.maps.LatLng(lat, lon);
        var myOptions = {
            center: centerPoint,
            zoom: 12,
            mapTypeId: google.maps.MapTypeId.ROADMAP,
            disableDefaultUI: true,
            draggable:false,
            disableDoubleClickZoom:true,
            scrollwheel: false
        };
        var map = new google.maps.Map(document.getElementById("js_schoolMap"),
            myOptions);
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
        var markerOptions = {
            map: map,
            shadow: markerShadow,
            shape: markerShape,
            position: centerPoint,
            title: schoolName
        };

        var imageUrl = '/res/img/map/GS_gsr_na_forground.png';

        if (gsRating != "") {
            imageUrl = '/res/img/map/GS_gsr_' + gsRating + '_forground.png';
        }

        if (schoolType === 'private') {
            imageUrl = '/res/img/map/GS_gsr_private_forground.png';
        }

        markerOptions.icon = new google.maps.MarkerImage(
            imageUrl, // url
            new google.maps.Size(30, 41), // size
            new google.maps.Point(0, 0), // origin
            new google.maps.Point(14, 39) // anchor
        );
        var marker = new google.maps.Marker(markerOptions);


        google.maps.event.addListener(marker, 'click', function() {
            location = "http://maps.google.com/?q=" + googleUrlSchool;
        });
    };
    return {
        init:init,
        initializeMap:initializeMap
    }
});