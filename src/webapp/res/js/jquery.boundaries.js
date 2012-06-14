
/**
 * BOUNDARIES CONSTRUCTOR DEFINITION
 * =================================
 */
var Boundaries = function(element, options) {
    this.cacheMap = {};
    this.$element = $(element);
    this.options = options;
    this.focused;
    this.map = new google.maps.Map(element, this.options.map);
    google.maps.event.addListener(this.map, 'center_changed', $.proxy(function(){
        this.trigger('moved');
    }, this));
    this.trigger('init');
}
Boundaries.prototype = {
    constructor: Boundaries
    , school: function ( option ) {
        var module = this, success = function(schools){module.school(schools[0]);};
        if ( typeof option == 'object' ) {
            option = module.cache(option);
            module.focus(option.getKey());
            module.trigger('school', option);
            return option;
        }
        if ( this.options.id && this.options.state && this.options.level )
            BoundaryHelper.getSchoolById(this.options.state, this.options.id, this.options.level)
                .done(success);
        else
            BoundaryHelper.getSchoolsByLocation(this.map.getCenter().lat(), this.map.getCenter().lng(), this.options.level)
                .done(success);
    }

    , district: function ( params ){
        var module = this, success = function(district){module.district(district);};
        if ( typeof params == 'object' ) {
            module.focus(params);
            // render schools for this district if enabled
            if (this.options.schools) {
                params.getSchools(this.options.level).done(function(data){
                    module.schools(data);
                });
            }
            module.trigger('district', params);
            return params;
        }
        if ( this.options.id && this.options.state && this.options.level )
            BoundaryHelper.getDistrictById(this.options.state, this.options.id, this.options.level)
                .done(success);
        else
            BoundaryHelper.getDistrictByLocation(this.map.getCenter().lat(), this.map.getCenter().lng(), this.options.level)
                .done(success);
    }

    , nondistrict: function ( params ) {
        var module = this,success = function(schools){
            for ( var i =0; i< schools.length; i++ ) {
                var school = module.cache(schools[i]);
                school.showMarker(module.map);
                google.maps.event.addListener(school.getMarker(), 'click', $.proxy(function(){
                    module.focus(this.getKey());
                }, school));
                module.cache(school);
            }
            module.trigger('nondistrict', schools)
        };
        BoundaryHelper.getNonDistrictSchoolsNearLocation (this.map.getCenter().lat(), this.map.getCenter().lng(), this.options.level, params)
            .done(success);
    }

    , hideNonDistrict: function ( params ) {
        for (var key in this.cacheMap) {
            var obj = this.cacheMap[key];
            if (obj.type == 'school' && obj.schoolType == params ) {
                obj.hideMarker();
            }
        }
    }

    , districts: function ( option ) {
        var module = this;
        BoundaryHelper.getDistrictsNearLocation(this.map.getCenter().lat(), this.map.getCenter().lng(), this.options.level)
            .done(function(districts){
                var all = new Array();
                for (var i=0; i<districts.length; i++) {
                    var district = module.cache(districts[i]);
                    district.showMarker(module.map);
                    google.maps.event.clearListeners(district.getMarker(), 'click');
                    google.maps.event.addListener(district.getMarker(), 'click', $.proxy(function(){
                        module.focus(this.getKey());
                    }, district));
                    all.push(district);
                }
                module.autozoom(all);
                module.trigger('districts', all);
            });
    }

    , schools: function ( params ) {
        var module = this;
        // load the array of schools into the map
        if (typeof params == 'object' && params.length ){
            var all = new Array();
            for (var i=0; i<params.length; i++) {
                var school = this.cache(params[i]);
                school.showMarker(this.map);
                google.maps.event.clearListeners(school.getMarker(), 'click');
                google.maps.event.addListener(school.getMarker(), 'click', $.proxy(function(){
                    module.focus(this.getKey());
                }, school));
                all.push(school);
            }
            this.trigger('schools', all);
        }
    }

    , focus: function ( option ) {
        var obj = (typeof option =='string') ? this.cache( option ) : option
            , module = this;

        if (this.infoWindow) this.infoWindow.close();
        this.hidePolygons(obj.type);

        if (!obj.isMarkerShown()){
            obj.showMarker(this.map);
            google.maps.event.clearListeners(obj.getMarker(), 'click');
            google.maps.event.addListener(obj.getMarker(), 'click', $.proxy(function(){
                module.focus(this.getKey());
            }, obj));
        }
        this.focused = obj;

        if (!obj.isPolygonShown()) obj.showPolygon(this.map, this.options.level).always(function(){module.info(obj)});
        else module.info(obj);

        if (obj.type=='district' && this.options.schools){
            this.hidePolygons('school');
            this.hideMarkers('school');
            obj.getSchools(this.options.level).done($.proxy(function(schools){this.schools(schools);}, this));
        }

        this.autozoom(obj);
        this.cache(obj);
        this.trigger('focus', this.focused);
    }

    , hidePolygons: function (type) {
        for (var key in this.cacheMap) {
            var obj = this.cacheMap[key];
            if (obj.type==type && obj.isPolygonShown()){
                obj.hidePolygon();
            }
            this.cache(obj);
        }
    }

    , hideMarkers: function (type) {
        for (var key in this.cacheMap) {
            var obj = this.cacheMap[key];
            if (obj.type==type && obj.isMarkerShown()){
                obj.hideMarker();
            }
            this.cache(obj);
        }
    }

    , info: function ( option ) {
        if ( !this.options.info ) return;
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
                closeBoxURL: "http://dev.greatschools.org/res/img/googleMaps/16x16_close.png",
                infoBoxClearance: new google.maps.Size(1, 1),
                isHidden: false,
                pane: "floatPane",
                alignBottom:true,
                enableEventPropagation: false
            });
        }

        var content = (this.options.infoWindowMarkupCallback) ? this.options.infoWindowMarkupCallback(option) : option.getInfoWindowMarkup();
        if ( this.infoWindow && content ) {
            this.infoWindow.close();
            this.infoWindow.setContent(content);
            this.trigger('info', {object: option, window: this.infoWindow});
            this.infoWindow.open(this.map, option.getMarker());
        }
    }

    , cache: function( params ){
        if (params && typeof params == 'string') return this.cacheMap[params];
        if (params && typeof params == 'object') {
            if (this.cacheMap[params.getKey()]){
                params = $.extend(params, this.cacheMap[params.getKey()]);
            }
        }
        return this.cacheMap[params.getKey()] = params;
    }

    , reset: function ( option ) {
        if (this.infoWindow) this.infoWindow.close();
        if (!option) {
            for (var key in this.cacheMap){
                var obj = this.cacheMap[key];
                if (obj.isPolygonShown()) obj.hidePolygon();
                if (obj.isMarkerShown()) obj.hideMarker();
                this.cacheMap[key] = null;
            }
            this.cacheMap = {};
        }
        if ( option && option.type=='district' ) {
            for (var key in this.cacheMap){
                var obj = this.cacheMap[key];
                if (obj.isPolygonShown()) obj.hidePolygon();
                if (obj.type=='school' ) obj.hideMarker();
                this.cache(obj);
            }
        }
        if ( option && option.type=='school' ) {
            for (var key in this.cacheMap) {
                var obj = this.cacheMap[key];
                if (obj.isPolygonShown() && obj.type=='school') obj.hidePolygon();
                this.cache(obj);
            }
        }
        this.trigger('reset');
    }

    , autozoom: function () {
        var obj;
        if (!this.options.autozoom) return;
        if (this.options.type=='district' || this.options.type=='school'){
            obj = this.focused;
            this.map.fitBounds(obj.getBounds());
        }
        if (this.options.type=='districts'){
            var bounds = new google.maps.LatLngBounds();
            for (var key in this.cacheMap){
                bounds.extend(this.cacheMap[key].getMarker().getPosition());
            }
            this.map.fitBounds(bounds);
        }
        this.trigger('autozoom', obj);
    }

    , option: function ( option ) {
        this.reset();
        $.extend(this.options, option);
        this[this.options.type]();
    }

    , center: function ( option ) {
        if (typeof option == 'object' && option instanceof google.maps.LatLng ){
            this.map.setCenter(option);
            this.trigger('center', option);
            this[this.options.type]();
        }
    }

    , geocode: function ( input ) {
        var module = this;
        BoundaryHelper.geocode(input).done(function(data){
            var center = new google.maps.LatLng(data[0].lat, data[0].lon);
            module.center(center);
            module.trigger('geocode', center);
        });
    }

    , trigger: function ( option, data ) {
        this.$element.trigger(new jQuery.Event(option), {data: data});
    }
}

/**
 * BOUNDARIES JQUERY PLUGIN DEFINITION
 * ===================================
 */
$.fn.boundaries = function(option, params){
    return this.each(function(){
        var $this = $(this)
            , options = $.extend({}, $.fn.boundaries.defaults, $this.data(), (typeof option =='object' && option))
            , data = $this.data('boundaries');
        if (!data) $this.data('boundaries', data = new Boundaries(this, options));
        if (typeof option == 'string') data[option](params);
    });
}

/**
 * BOUNDARIES DEFAULTS DEFINITION
 * ==============================
 */
$.fn.boundaries.defaults = {
    map: {
        center: new google.maps.LatLng(37.36, -122.03),
        zoom: 11,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    , autozoom: true
}

/**
 * BOUNDARIES DATA-API
 * ===================
 */
$(function(){
    $('[data-boundaries]').each(function(){
        var type = $(this).data('boundaries');
        $(this).removeData('boundaries').removeAttr('data-boundaries');
        $(this).boundaries({type: type}).boundaries(type);
    });
});

var Mappable = function(){
    this.lazy = {boundary: null};
};
Mappable.prototype = {
    constructor: Mappable
    , getBoundary: function( level ) {
        if ( !level ) level = this.level;
        if (!this.lazy.boundary) {
            this.lazy.boundary = new jQuery.Deferred();
            if (this.coordinates) this.lazy.boundary.resolve(this.coordinates);
            else {
                var url = '/geo/boundary/ajax/get';
                url += ( this.type=='district' ) ? 'District' : 'School';
                url += 'BoundaryById.json';
                var request = $.getJSON( url, {state: this.state, id: this.id, level: level});
                request.done($.proxy(function( data ){this.lazy.boundary.resolve(data.boundary);}, this));
                request.fail($.proxy(function(){this.lazy.boundary.reject()}, this));
            }
        }
        return this.lazy.boundary.promise();
    }
    , showMarker: function ( map ) {
        this.getMarker().setMap(map);
    }
    , hideMarker: function () {
        if (this.marker) this.marker.setMap(null);
    }
    , isMarkerShown: function () {
        return (this.marker && this.marker.getMap()!=null);
    }
    , showPolygon: function( map, level ){
        var deferred = new jQuery.Deferred();
        if (this.polygon) {
            this.polygon.setMap(map);
            deferred.resolve(this.polygon);
        }
        else {
            $.when(this.getBoundary(level))
                .then($.proxy(function(boundary){
                this.polygon = new google.maps.Polygon({
                    paths: this.getPolygonPath( boundary ),
                    strokeColor: this.strokeColor || '#FF7800',
                    strokeOpacity: 1,
                    strokeWeight: 2,
                    fillColor: this.fillColor || '#46461F',
                    fillOpacity: 0.25,
                    zIndex: this.zIndex || 1
                });
                if (this.polygon!=null){
                    this.showPolygon(map, level);
                    deferred.resolve(this.polygon);
                } else {
                    this.polygon = undefined;
                    deferred.reject();
                }
            }, this))
                .fail(function(){
                    deferred.reject();
                });

        }
        return deferred.promise();
    }
    , hidePolygon: function () {
        if (this.polygon) this.polygon.setMap()
    }
    , isPolygonShown: function () {
        return (this.polygon && this.polygon.getMap()!=null);
    }
    , getMarker: function(){
        if (this.marker) return this.marker;
        return this.marker = new google.maps.Marker({
            position: new google.maps.LatLng(this.lat, this.lon),
            title: this.name,
            icon: this.getMarkerImage(),
            shape: this.getMarkerShape()
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
    , getPolygonPath: function ( boundary ) {
        var coords = boundary.coordinates
            , paths = new Array();
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
        return paths;
    }
    , getBounds: function() {
        var bounds = new google.maps.LatLngBounds();
        if (this.isPolygonShown()){
            this.polygon.getPaths().forEach(
                function(path){
                    path.forEach(function(latlng){
                        bounds.extend(latlng);
                    });
                }
            );
        }
        else if (this.isMarkerShown()){
            bounds.extend(this.marker.getPosition());
        }
        return bounds;
    }
    , getKey: function() {
        return this.type + '-' + this.state + '-' + this.id;
    }
}

var District = function(option){
    $.extend(this, option);
    this.iconSize = 48;
    this.iconUrl = '/res/img/sprites/icon/mapPins/x48/120524-mapPinsx48.png';
    this.strokeColor = '#2092C4';
    this.zIndex = 1;
    this.fillColor = 'rgba(0,0,0,0.2)';
    Mappable.call(this);
    this.lazy.schools = null;
};
District.prototype = $.extend(Mappable.prototype, {
    getSchools: function( level ){
        if (!level) level = this.level;
        if (!this.lazy.schools) {
            this.lazy.schools = BoundaryHelper.getSchoolsByDistrictId(this.state, this.id, level);
        }
        return this.lazy.schools.promise();
    }
    , getInfoWindowMarkup: function () {
        return '';
    }
});
District.prototype.constructor = District;

var School = function(obj) {
    $.extend(this, obj);
    this.iconSize = 32;
    this.iconUrl = '/res/img/sprites/icon/mapPins/x32/120523-mapPinsx32.png';
    this.zIndex=2
    this.fillColor = 'rgba(255, 158, 0,0.4)';
    Mappable.call(this);
    this.lazy.district = null;
};
School.prototype = $.extend(Mappable.prototype, {
    getDistrict: function(){
        if (this.lazy.district) return this.lazy.district.promise();
        this.lazy.district = new jQuery.Deferred();

        return this.lazy.district.promise();
    },
    getInfoWindowMarkup: function(){
        return '';
    }
});
School.prototype.constructor = School;

/**
 * BoundaryHelper provides the functions
 * for loading objects.
 */
var BoundaryHelper = new (function(){

    Array.prototype.contains = function(obj) {
        var i = this.length;
        while (i--) {
            if (this[i] === obj) {
                return true;
            }
        }
        return false;
    };

    var getDistrictById = function(state, id, level) {
        var deferred = new jQuery.Deferred();
        var request = $.getJSON('/geo/boundary/ajax/getDistrictById.json', {state: state, id: id, level: level})
        request.done(function(data){
            if (data.districts && data.districts.length > 0) deferred.resolve(new District(data.districts[0]));
            else deferred.reject();
        });
        request.fail(function(){
            deferred.reject();
        })
        return deferred.promise();
    };

    var getDistrictByLocation = function(lat, lon, level) {
        var deferred = new jQuery.Deferred();
        var request = $.getJSON('/geo/boundary/ajax/getDistrictsForLocation.json', {lat: lat, lon: lon, level: level});
        request.done(function(data) {
            if ( data.districts && data.districts.length ) {
                var largest = 0, district = null;
                for (var i=0; i< data.districts.length; i++) {
                    if (data.districts[i].coordinates.area > largest){
                        district = new District(data.districts[i]);
                    }
                }
                if (district) deferred.resolve(district);
                else deferred.reject();
            }
        });
        request.fail(function(){
            deferred.reject();
        });
        return deferred.promise();
    };

    var getDistrictsNearLocation = function(lat, lon, level) {
        var deferred = new jQuery.Deferred();
        var request = $.getJSON('/geo/boundary/ajax/getDistrictsNearLocation.json', {lat: lat, lon: lon, level: level});
        request.done(function(data) {
            var array = new Array();
            for (var i=0; i<data.districts.length; i++) {
                array.push(new District(data.districts[i]));
            }
            deferred.resolve(array);
        });
        request.fail(function(){
            deferred.reject();
        });
        return deferred.promise();
    };

    var getNonDistrictSchoolsNearLocation = function (lat, lon, level, type) {
        var deferred = new jQuery.Deferred();
        var urlType = (type=='charter') ? 'Charter' : 'Private';
        var request = $.getJSON('/geo/boundary/ajax/get' + urlType + 'SchoolsNearLocation.json', {lat: lat, lon: lon, level: level});
        request.done(function(data) {
            var array = new Array();
            for (var i=0; i<data.schools.length; i++) {
                array.push(new School(data.schools[i]));
            }
            deferred.resolve(array);
        });
        request.fail(function(){
            deferred.reject();
        });
        return deferred.promise();
    }

    var getSchoolsByDistrictId = function (state, id, level) {
        var deferred = new jQuery.Deferred();
        var data = {id:id, state: state, level: level};
        var request = $.getJSON('/geo/boundary/ajax/getSchoolsByDistrictId.json',data);
        request.done(function(data){
            var schools = new Array();
            for(var i=0; i<data.schools.length; i++) {
                schools.push(new School(data.schools[i]));
            }
            deferred.resolve(schools);
        });
        request.fail(function(){
            deferred.reject();
        });
        return deferred.promise();
    }

    var getSchoolById = function (state, id, level) {
        var deferred = new jQuery.Deferred();
        var data = {state:state, id: id, level: level};
        var request = $.getJSON('/geo/boundary/ajax/getSchoolById.json',data);
        request.done(function(data){
            var schools = new Array();
            for(var i=0; i<data.schools.length; i++) {
                schools.push(new School(data.schools[i]));
            }
            deferred.resolve(schools);
        });
        request.fail(function(){
            deferred.reject();
        });
        return deferred.promise();
    }

    var getSchoolsByLocation = function (lat, lon, level) {
        var deferred = new jQuery.Deferred();
        var data = {lat:lat, lon: lon, level: level};
        var request = $.getJSON('/geo/boundary/ajax/getSchoolsByLocation.json',data);
        request.done(function(data){
            var schools = new Array();
            for(var i=0; i<data.schools.length; i++) {
                schools.push(new School(data.schools[i]));
            }
            deferred.resolve(schools);
        });
        request.fail(function(){
            deferred.reject();
        });
        return deferred.promise();
    }

    var geocode = function (searchInput){
        var deferred = new jQuery.Deferred();
        var geocoder = new google.maps.Geocoder();
        var boundaries = this;
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
                    deferred.resolve(GS_geocodeResults);
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
        getDistrictById: getDistrictById,
        getDistrictByLocation: getDistrictByLocation,
        getDistrictsNearLocation: getDistrictsNearLocation,
        getNonDistrictSchoolsNearLocation: getNonDistrictSchoolsNearLocation,
        getSchoolsByDistrictId: getSchoolsByDistrictId,
        getSchoolById: getSchoolById,
        getSchoolsByLocation: getSchoolsByLocation,
        geocode: geocode
    }
});