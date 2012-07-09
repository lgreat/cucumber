/**
 * BOUNDARIES CONSTRUCTOR DEFINITION
 * =================================
 */
var Boundaries = function(element, options){
    var element = element, options = options, markers, polygons, map
        , markers = new Array(), polygons = new Array(), focused;

    map = new google.maps.Map(element, options.map);
    google.maps.event.addListener(map, 'dragend', $.proxy(function(){this.trigger('dragend');}, this));
    google.maps.event.addListener(map, 'click', $.proxy(function(e){this.trigger('mapclick', e.latLng);}, this));

    this.getElement = function () { return element; }
    this.getMap = function () { return map; }
    this.getMarkers = function () { return markers; }
    this.getPolygons = function () { return polygons; }
    this.getOptions = function () { return options; }

    if (options.schools) {
        this.listen('focus', $.proxy(function (event, object ){
            if (object.data && object.data.type=='district'){
                this.schools(object.data);
            }
        }, this));
    }

    this.listen('dragend.boundaries', $.proxy(this.dragend, this));

    this.trigger('init');
}

Boundaries.prototype = {
    constructor: Boundaries

    , boundary: function (obj) {
        var found = false;
        $.each(this.getPolygons(), $.proxy(function(index, value) {
            if ( value.key == obj.getKey() ){
                this.getPolygons()[index].setMap(this.getMap());
                found = true;
            } else {
                if (obj.getType()=='district' || (obj.getType()=='school' && value.type=='school')){
                    this.getPolygons()[index].setMap(null);
                }
            }
        },this));
        if (!found) {
            var polygon = obj.getPolygon(this.getOptions().level);
            polygon.setMap(this.getMap());
            this.getPolygons().push(polygon);
        }
    }

    , center: function (option) {
        if (this.exists(option) && option instanceof google.maps.LatLng ){
            this.getMap().setCenter(option);
        }
    }

    , district: function (option) {
        var deferred = new jQuery.Deferred()
            , lat=this.getMap().getCenter().lat()
            , lng=this.getMap().getCenter().lng()
            , level=this.getOptions().level;

        if (this.exists(option)){
            lat = option.lat();
            lng = option.lng();
        }

        var success = function (districts) {
            this.show(districts[0]);
            this.trigger('load', districts[0]);
            this.focus(districts[0]);
            deferred.resolve(districts);
        }
        BoundaryHelper.getDistrictsForLocation(lat, lng, level)
            .done($.proxy(success, this)).fail(function(){deferred.reject();});

        return deferred.promise();
    }

    , nondistrict: function ( params ) {
        var deferred = new jQuery.Deferred()
            , lat=this.getMap().getCenter().lat()
            , lng=this.getMap().getCenter().lng()
            , level=this.getOptions().level;

        var success = function(schools) {
            for (var i=0; i<schools.length; i++) {
                schools[i].charterOnly = true;
                this.show(schools[i]);
            }
            deferred.resolve(schools);
        }

        BoundaryHelper.getNonDistrictSchoolsNearLocation(lat, lng, level, params)
            .done($.proxy(success, this)).fail(function(){deferred.reject();});

        return deferred.promise();
    }

    , districts: function () {
        var deferred = new jQuery.Deferred()
            , lat = this.getMap().getCenter().lat()
            , lng = this.getMap().getCenter().lng()
            , level = this.getOptions().level;

        var success = function (districts){
            for(var i=0; i<districts.length; i++) {
                this.show(districts[i]);
            }
            this.autozoom(districts);
            this.trigger('load', districts);
            deferred.resolve(districts);
        }

        BoundaryHelper.getDistrictsNearLocation(lat, lng, level)
            .done($.proxy(success, this)).fail(function(){deferred.reject()});
        return deferred.promise();
    }

    , focus: function (obj) {
        if (obj.getType()=='district'){
            this.hide('school');
        }
        this.boundary(obj);
        this.info(obj);
        this.trigger('focus', obj);
    }

    , geocode: function (option) {
        BoundaryHelper.geocode(option).done(
            $.proxy(function (data) {
                this.center(new google.maps.LatLng(data[0].lat, data[0].lon));
                if (this.getOptions().centerMarker){
                    this.getOptions().centerMarker.setMap(this.getMap());
                    this.getOptions().centerMarker.setPosition(new google.maps.LatLng(data[0].lat, data[0].lon));
                }
                this.refresh();
                this.trigger('geocode', data);
            }, this)
        ).fail(
            $.proxy(function(){
                this.trigger('geocodefail');
            }, this)
        );
    }

    , geocodereverse: function ( option ) {
        $.when(BoundaryHelper.geocodeReverse(option.lat(), option.lng()))
            .then ($.proxy(function (data) {
            this.trigger('geocodereverse', data);
        }, this));
    }

    , info: function (obj) {
        if ( !this.getOptions().infoWindow ) return;
        if ( !this.infoWindow ) {
            this.infoWindow = new InfoBox({
                disableAutoPan: false,
                maxWidth: 0,
                pixelOffset: new google.maps.Size(-150, -45),
                zIndex: 99,
                boxStyle: {
                    opacity: 1,
                    width: "300px"
                },
                closeBoxMargin: "8px",
                // TODO: Change closeBoxURL from staging to www
                closeBoxURL: "/res/img/googleMaps/16x16_close.png",
                infoBoxClearance: new google.maps.Size(1, 1),
                isHidden: false,
                pane: "floatPane",
                alignBottom:true,
                enableEventPropagation: false
            });
        }
        this.infoWindow.setContent('');
        this.infoWindow.close();
        if (this.getOptions().infoWindow) this.infoWindow.setContent(this.getOptions().infoWindow(obj));

        for (var i=0; i<this.getMarkers().length; i++) {
            if (this.getMarkers()[i].key==obj.getKey()){
                this.infoWindow.open(this.getMap(), this.getMarkers()[i]);
                this.trigger('info', obj);
                return;
            }
        }
    }

    , hide: function(type) {

        for (var i=0; i<this.getMarkers().length; i++) {
            var title = this.getMarkers()[i].key
                , marker = this.getMarkers()[i]
                , school = marker.school
                , district = marker.district
                , hide = false;

            if (type=='private' || type=='charter'){
                if (school && school.schoolType=='private') {
                    hide = true;
                }
                else if (school && school.schoolType=='charter' && school.charterOnly) {
                    hide = true;
                }
            }
            else if (type=='district') {
                if (district){
                    hide = true;
                }
            }
            else if (type=='school') {
                if (school){
                    if (school.schoolType=='charter'){
                        if (school.districtId) {
                            hide = true;
                        } else {
                            hide = false;
                        }
                    }
                    else if (school.schoolType=='private'){
                        hide = false;
                    }
                    else {
                        hide = true;
                    }
                }
            }

            if (hide) {this.getMarkers()[i].setMap(null);}
        }
        for (var i=0; i<this.getPolygons().length; i++) {
            if (this.getPolygons()[i].type==type){
                this.getPolygons()[i].setMap(null);
            }
        }
    }

    , level: function ( option ) {
        this.getOptions().level = option;
        this.refresh();
    }

    , listen: function (event, func) {
        $(this.getElement()).on(event+'.boundaries', func);
    }

    , pin: function (obj) {
        var found = false;
        $.each(this.getMarkers(), $.proxy(function(index, marker){
            if (marker.key == obj.getKey()){
                marker.setMap(this.getMap());
                found = true;
            }
        }, this));
        if (!found) {
            var module = this, marker = obj.getMarker();
            if (obj.getType()=='school') marker.school = obj;
            else marker.district = obj;
            marker.key = obj.getKey();
            marker.setZIndex((obj.getType()=='school')?2:3);
            google.maps.event.clearListeners(marker, 'click');
            google.maps.event.addListener(marker, 'click', $.proxy(function(){
                module.focus(this);
                module.trigger('markerclick', this);
            }, obj));
            marker.setMap(this.getMap());
            this.getMarkers().push(marker);
        }
    }

    , refresh: function() {
        if (this.infoWindow) this.infoWindow.close();
        this.hide('school');
        this.hide('district');
        this.center(this.getMap().getCenter());
        this[this.getOptions().type]();
    }

    , school: function () {
        var deferred = new jQuery.Deferred()
            , lat = this.getMap().getCenter().lat()
            , lng = this.getMap().getCenter().lng()
            , level = this.getOptions().level;

        var success = function (schools) {

            for (var i=0; i<schools.length; i++) {
                this.show(schools[i]);
                this.focus(schools[i]);
            }
            deferred.resolve(schools);
            this.trigger('load', schools);
        }

        BoundaryHelper.getSchoolsByLocation(lat, lng, level)
            .done($.proxy(success, this)).fail(function(){deferred.reject();});

        return deferred.promise();
    }

    /**
     * We should show the school boundary for center
     * on the map and also show that schools district
     * boundary if it is loaded on the map.
     */
    , school_with_district:  function () {
        var success = function (schools) {
            if (schools.length && schools.length>0){
                var school = schools[0];
                var dsuccess = function (districts) {
                    for (var i=0; i<districts.length; i++) {
                        var id = (districts[i].id==school.districtId);
                        var state = (districts[i].state==school.state);
                        if (id && state) {
                            this.focus(districts[i]);
                            this.focus(school);
                            break;
                        }
                    }

                }
                $.when(this.districts()).then($.proxy(dsuccess, this));
            }
        };
        $.when(this.school()).then($.proxy(success, this));
    }

    , schools: function () {
        if (arguments.length) {
            var district = arguments[0];
            $.when(BoundaryHelper.getSchoolsForDistrict(district.id, district.state, this.getOptions().level))
                .then($.proxy(function(schools){
                for (var i=0; i<schools.length; i++) {
                    this.show(schools[i]);
                }
                this.trigger('load', schools);
            }, this));
        }
    }

    , show: function ( obj ) {
        this.pin(obj);
    }

    , shown: function (callback) {
        var shown = new Array();
        this.getMarkers().forEach($.proxy(function (marker, index, array){
            if (this.exists(marker) && this.exists(marker.getMap())){
                shown.push(marker);
            }
        }, this));

        return (this.exists(callback)) ? callback(shown) : shown ;
    }

    , trigger: function (event, data) {
        $(this.getElement()).trigger(new jQuery.Event(event), {data: data});
    }

    , dragend: function () {
        var bounds = this.getMap().getBounds();
        for (var i=0; i<this.getMarkers().length; i++) {
            if (this.getMarkers()[i].getMap()!=null && bounds.contains(this.getMarkers()[i].getPosition())){
                this.trigger('inbounds');
                return;
            }
        }
        this.trigger('outbounds');
    }

    , autozoom: function () {
        if (this.getOptions().autozoom) {
            this.shown($.proxy(function(markers){
                var bounds = new google.maps.LatLngBounds();
                for (var i=0; i<markers.length; i++) {
                    bounds.extend(markers[i].getPosition());
                }
                this.getMap().fitBounds(bounds);
            }, this));
        }
    }

    , exists: function (obj){
        return typeof obj !== "undefined" && obj !== null;
    }
};

/**
 * BOUNDARIES JQUERY PLUGIN DEFINITION
 * ===================================
 */
$.fn.boundaries = function (option, params) {
    return this.each(function(){
        var $this = $(this)
            , options = $.extend({}, $.fn.boundaries.defaults, $this.data(), (typeof option =='object' && option))
            , data = $this.data('boundaries');
        if (!data) $this.data('boundaries', data = new Boundaries(this, options));
        if (typeof option == 'string') data[option](params);
    });
}

/**
 * BOUNDARIES JQUERY DEFAULTS
 * ===================================
 */
$.fn.boundaries.defaults = {
    level: 'e',
    schools: true,
    autozoom: false,
    centerMarker: new google.maps.Marker({
        icon:new google.maps.MarkerImage(
            '/res/img/map/green_arrow.png',
            new google.maps.Size(39,34),
            new google.maps.Point(0,0),
            new google.maps.Point(11,34)
        ),
        shape:{type:'poly', coord:[0, 0, 23, 0, 23, 34, 0, 34]},
        shadow:new google.maps.MarkerImage('/res/img/map/green_arrow_shadow.png', new google.maps.Size(39,34), null, new google.maps.Point(11, 34))
    }),
    map: {
        center: new google.maps.LatLng(37.77,-122.419),
        zoom: 11,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    },
    infoWindow: function(obj){}
};

var Mappable = function(){
    var lat, lon, name;
    if (arguments.length) {
        var inputs = arguments[0];
        lat = inputs.lat, lon = inputs.lon;
        name = inputs.name;
    }
    this.getPosition = function(){return new google.maps.LatLng(lat, lon);}
    this.getName = function(){return name;}
};

Mappable.prototype = {
    constructor: Mappable
    , getMarker: function(){
        if (this.marker) return this.marker;
        return this.marker = new google.maps.Marker({
            position: this.getPosition(),
            title: this.getName(),
            icon: this.getMarkerImage(),
            shape: this.getMarkerShape(),
            zIndex: 1
        });
    }
    , getMarkerShape: function() {
        return (this.type=='district') ?
        {coord: [8,4, 37,4, 37,33, 32,33, 23,42, 14,33, 8,33], type: 'poly'} :
        {coord: [1,0, 29,0, 29,31, 1,31], type: 'poly'};
    }
    , getMarkerOrigin: function() {
        var xoffset = this.iconSize * 10;
        if (this.schoolType && this.schoolType=='private') xoffset = this.iconSize * 11;
        if (this.rating > 0 && this.rating < 11) xoffset = xoffset - this.iconSize * this.rating;

        return new google.maps.Point(xoffset, 0);
    }
    , getMarkerAnchor: function() {
        return new google.maps.Point(this.iconSize/2,this.iconSize);
    }
    , getMarkerImage: function(){
        return new google.maps.MarkerImage(this.iconUrl ,
            new google.maps.Size(this.iconSize,this.iconSize), this.getMarkerOrigin(), this.getMarkerAnchor()
        );
    }
    , isPolygonShown: function () {
        return (this.polygon && this.polygon.getMap()!=null && this.polygon.getPaths() && this.polygon.getPaths().length>0);
    }
    , getPolygon: function ( level ){
        if (this.polygon) return this.polygon;
        this.polygon = new google.maps.Polygon({
            paths: this.getPolygonPath( level ),
            strokeColor: this.strokeColor || '#FF7800',
            strokeOpacity: 1,
            strokeWeight: 2,
            fillColor: this.fillColor || '#46461F',
            fillOpacity: 0.25,
            zIndex: this.zIndex || 1
        });
        this.polygon.key = this.getKey();
        this.polygon.type = this.getType();
        return this.polygon;
    }
    , getPolygonPath: function ( level ) {
        var coords, paths = new Array(), url = (this.getType()=='district')?'/geo/boundary/ajax/getDistrictBoundaryById.json':'/geo/boundary/ajax/getSchoolBoundaryById.json';
        if (!this.coordinates) {
            $.ajax({
                url: url,
                cache: true,
                data: {id: this.id, state: this.state, level: level},
                dataType: 'json',
                type: 'GET',
                async: false,
                success: function( data ) {
                    coords = data.boundary.coordinates;
                },
                fail: function () {
                    coords = new Array();
                }
            });
        } else {
            coords = this.coordinates.coordinates;
        }

        if (coords) {
            for (var i=0;i < coords.length;i++){
                for (var j=0;j<coords[i].length;j++){
                    var path=[];
                    for (var k=0;k<coords[i][j].length;k++){
                        var ll = new google.maps.LatLng(coords[i][j][k][1],coords[i][j][k][0]);
                        path.push(ll);
                    }
                    paths.push(path);
                }
            };
        }
        return paths;
    }
    , getType: function () {
        return this.type;
    }
    , getKey: function (){
        return this.type + '-' + this.state + '-' + this.id;
    }
}

var District = function(){
    if (arguments.length) $.extend(this, arguments[0]);
    this.iconSize = 48;
    this.iconUrl = '/res/img/sprites/icon/mapPins/x48/120524-mapPinsx48.png';
    this.strokeColor = '#2092C4';
    this.zIndex = 1;
    this.fillColor = 'rgba(0,0,0,0.2)';
    Mappable.apply(this, arguments);
};
District.prototype = $.extend(Mappable.prototype, {});
District.prototype.constructor = District;

var School = function(){
    if (arguments.length) $.extend(this, arguments[0]);
    this.iconSize = 32;
    this.iconUrl = '/res/img/sprites/icon/mapPins/x32/120523-mapPinsx32.png';
    Mappable.apply(this, arguments);
};
School.prototype = $.extend(Mappable.prototype, {});
School.prototype.constructor = School;

/**
 * BOUNDARY HELPER
 * ===============
 */
var BoundaryHelper = (function($){
    var getDistrictsNearLocation = function(lat, lon, level) {
        var deferred = new jQuery.Deferred();
        var request = $.ajax({
            url: '/geo/boundary/ajax/getDistrictsNearLocation.json',
            cache: true,
            data: {lat: lat, lon: lon, level: level},
            type: 'GET',
            dataType: 'json',
            success: districtSuccess,
            fail: fail,
            context: deferred
        });
        return deferred.promise();
    };

    var getDistrictsForLocation = function (lat, lon, level) {
        var deferred = new jQuery.Deferred();
        var request = $.ajax({
            url:'/geo/boundary/ajax/getDistrictsForLocation.json',
            cache: true,
            data: {lat: lat, lon: lon, level: level},
            type: 'GET',
            dataType: 'json',
            success: districtSuccess,
            fail: fail,
            context: deferred
        });
        return deferred.promise();
    };

    var getNonDistrictSchoolsNearLocation = function (lat, lon, level, type) {
        var deferred = new jQuery.Deferred();
        var urlType = (type=='charter') ? 'Charter' : 'Private';
        $.ajax({
            url: '/geo/boundary/ajax/get' + urlType + 'SchoolsNearLocation.json',
            data: {lat: lat, lon: lon, level: level},
            cache: true,
            type: 'GET',
            dataType: 'json',
            success: schoolSuccess,
            fail: fail,
            context: deferred
        });
        return deferred.promise();
    };

    var getSchoolsForDistrict = function ( id, state, level ) {
        var deferred = new jQuery.Deferred();
        $.ajax({
            url: '/geo/boundary/ajax/getSchoolsByDistrictId.json',
            data: {id: id, state: state, level: level},
            cache: true,
            type: 'GET',
            dataType: 'json',
            success: schoolSuccess,
            fail: fail,
            context: deferred
        });
        return deferred.promise();
    }

    var getSchoolsByLocation = function ( lat, lng, level ) {
        var deferred = new jQuery.Deferred();
        $.ajax({
            url: '/geo/boundary/ajax/getSchoolsByLocation.json',
            data: {lat: lat, lon: lng, level: level},
            cache: true,
            type: 'GET',
            dataType: 'json',
            success: schoolSuccess,
            fail: fail,
            context: deferred
        });
        return deferred.promise();
    }

    var schoolSuccess = function (data) {
        var schools = new Array();
        if (data.schools && data.schools.length) {
            for (var i=0; i<data.schools.length; i++) {
                schools.push(new School(data.schools[i]));
            }
        }
        this.resolve(schools);
    }

    var districtSuccess = function (data) {
        var districts = new Array();
        if ( data.districts && data.districts.length ) {
            for (var i=0; i< data.districts.length; i++) {
                districts.push(new District(data.districts[i]));
            }
        }
        this.resolve(districts);
    };

    var fail = function () {
        this.reject();
    }

    Array.prototype.contains = function(obj) {
        var i = this.length;
        while (i--) {
            if (this[i] === obj) {
                return true;
            }
        }
        return false;
    };

    var geocodeReverse = function (lat, lng) {
        var deferred = new jQuery.Deferred();
        var geocoder = new google.maps.Geocoder();
        if (geocoder && lat && lng) {
            geocoder.geocode({location: new google.maps.LatLng(lat, lng)}, function (results, status) {
                if (status=='OK'){
                    var GS_geocodeResults = new Array();
                    for (var i=0; i<results.length; i++) {
                        var result = {};
                        result.lat = results[i].geometry.location.lat();
                        result.lon = results[i].geometry.location.lng();
                        result.normalizedAddress = results[i].formatted_address;
                        for (var x=0; x<results[i].address_components.length; x++) {
                            if (results[i].address_components[x].types.contains('postal_code')){
                                result.zip = results[i].address_components[x].long_name;
                            }
                        }
                        GS_geocodeResults.push(result);
                    }
                    deferred.resolve(GS_geocodeResults);
                } else {
                    deferred.reject();
                }
            });
        } else {
            deferred.reject();
        }
        return deferred.promise();
    };

    var geocode = function ( searchInput ) {
        var deferred = new jQuery.Deferred();
        var geocoder = new google.maps.Geocoder();
        if (geocoder && searchInput) {
            geocoder.geocode({ 'address': searchInput + ' US'}, function(results, status) {
                var numResults = 0;
                var GS_geocodeResults = new Array();
                if (status == google.maps.GeocoderStatus.OK && results.length > 0) {
                    numResults = results.length;
                    for (var x = 0; x < numResults; x++) {
                        var geocodeResult = new Array();
                        geocodeResult['lat'] = results[x].geometry.location.lat();
                        geocodeResult['lon'] = results[x].geometry.location.lng();
                        geocodeResult['normalizedAddress'] =results[x].formatted_address;
                        geocodeResult['type'] = results[x].types.join();
                        if (results[x].partial_match) {
                            geocodeResult['partial_match'] = true;
                        } else {
                            geocodeResult['partial_match'] = false;
                        }
                        for (var i = 0; i < results[x].address_components.length; i++) {
                            if (results[x].address_components[i].types.contains('administrative_area_level_1')) {
                                geocodeResult['state'] = results[x].address_components[i].short_name;
                            }
                            if (results[x].address_components[i].types.contains('country')) {
                                geocodeResult['country'] = results[x].address_components[i].short_name;
                            }
                        }
                        // http://stackoverflow.com/questions/1098040/checking-if-an-associative-array-key-exists-in-javascript
                        if (!('lat' in geocodeResult && 'lon' in geocodeResult &&
                            'state' in geocodeResult &&
                            'normalizedAddress' in geocodeResult &&
                            'country' in geocodeResult) ||
                            geocodeResult['country'] != 'US') {
                            geocodeResult = null;
                        }
                        if (geocodeResult != null) {
                            GS_geocodeResults.push(geocodeResult);
                        }
                    }

                    if (GS_geocodeResults.length>0)
                        deferred.resolve(GS_geocodeResults);
                    else
                        deferred.reject();
                } else {
                    deferred.reject();
                }
            });
        } else {
            deferred.reject();
        }
        return deferred.promise();

    }

    return {
        getDistrictsNearLocation: getDistrictsNearLocation,
        getDistrictsForLocation: getDistrictsForLocation,
        getSchoolsForDistrict: getSchoolsForDistrict,
        getSchoolsByLocation: getSchoolsByLocation,
        getNonDistrictSchoolsNearLocation: getNonDistrictSchoolsNearLocation,
        geocode: geocode,
        geocodeReverse: geocodeReverse
    }
}(window.jQuery));
