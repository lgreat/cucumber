define(function() {
    var initializeMap = function(points) {
        var centerPoint = new google.maps.LatLng(0, 0);
        var bounds = new google.maps.LatLngBounds();
        var infoWindow = new google.maps.InfoWindow();

        var myOptions = {
            center: centerPoint,
            zoom: 12,
            mapTypeId: google.maps.MapTypeId.ROADMAP,
            disableDefaultUI: true,
            infoWindow: infoWindow
        };

        var map = new google.maps.Map(document.getElementById("js-map-canvas"),
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

        for ( var i = 0; i < points.length; i++ ) {
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

            google.maps.event.addListener(marker, 'click', function() {
                infoWindow.setContent(this.infoWindowMarkup);
                infoWindow.open(map, this);
            });
        }

        map.fitBounds(bounds);
    }

    return {
        initializeMap:initializeMap
    }
});