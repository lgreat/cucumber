Array.prototype.contains = function(obj) {
    var i = this.length;
    while (i--) {
        if (this[i] === obj) {
            return true;
        }
    }
    return false;
};

var GS = GS || {};
GS.Boundaries = GS.Boundaries || {};
GS.Boundaries.boundaryHelper = (function() {
    var successFunc = function(deferred, data, options) {
        try {
            if (data.features.length == 0) {
                if (typeof(options.zeroErrorMessage) !== 'undefined') {
                    alert(options.zeroErrorMessage);
                } else if (typeof(options.zeroWarningMessage) !== 'undefined') {
                    GS.Util.log(options.zeroWarningMessage);
                }
            }
            var mapObjectsAdded = GS.Boundaries.boundaryHelper.loadFeatureResponse(data.features, options);
            if (GS.Boundaries.boundaryHelper.globalResponseHandler != null) {
                GS.Boundaries.boundaryHelper.globalResponseHandler(mapObjectsAdded);
            }
        } catch (e) {
            GS.Util.log("ERROR: " + e + ": " + e.msg);
        }
        deferred.resolve(mapObjectsAdded);
    };
    // DATA STRUCTURES
    var MarkerWithBoundary = function() {
        this.init = function(params) {
            this.marker = null;
            this.polygon = null;
            this.name = null;
            this.rating = 0;
            this.url = null;
            this.centroid = {};
            this.area = 0;

            this.state = params.state;
            this.id = params.id;
            this.name = params.name;
            if (typeof(params.rating) !== 'undefined') {
                this.rating = params.rating;
            }
            if (typeof(params.url) !== 'undefined') {
                this.url = params.url;
            }
            this.address = params.address || {};
        };
        this.isDistrict = function() {return false;};
        this.isSchool = function() {return false;};
        this.hasMarker = function() {
            return this.marker != null;
        };
        this.isMarkerShown = function() {
            return this.hasMarker() && this.marker.getMap() != null;
        };
        this.hideMarker = function() {
            if (this.hasMarker() && this.isMarkerShown()) {
                this.marker.setMap(null);
            }
        };
        this.showMarker = function() {
            if (this.hasMarker() && !this.isMarkerShown()) {
                this.marker.setMap(GS.Boundaries.boundaryHelper.map);
            }
        };
        this.hasPolygon = function() {
            return this.polygon != null;
        };
        this.isPolygonShown = function() {
            return this.hasPolygon() && this.polygon.getMap() != null;
        };
        this.showPolygon = function() {
            if (this.hasPolygon() && !this.isPolygonShown()) {
                this.showPolygonImpl();
                this.polygon.setMap(GS.Boundaries.boundaryHelper.map);
            }
        };
        this.hidePolygon = function() {
            if (this.isPolygonShown()) {
                this.polygon.setMap(null);
                this.hidePolygonImpl();
            }
        };
        this.show = function() {
            this.showMarker();
            this.showPolygon();
        };
        this.hide = function() {
            this.hideMarker();
            this.hidePolygon();
        };
    };
    var DistrictWithBoundaryPrototype = function() {
        this.showSchools = function() {
            for (var x=0; x < this.schools.length; x++) {
                this.schools[x].showMarker();
            }
        };
        this.hideSchools = function() {
            for (var x=0; x < this.schools.length; x++) {
                this.schools[x].hideMarker();
                this.schools[x].hidePolygon();
            }
        };
        this.showPolygonImpl = function() {
        };
        this.hidePolygonImpl = function() {
            this.hideSchools();
        };
        this.setMarker = function(marker) {
            this.hideMarker();
            this.marker = marker;
        };
        this.setPolygon = function(polygon) {
            this.hidePolygon();
            this.polygon = polygon;
            polygon.setOptions({strokeColor:'#FF7800', zIndex:1});
        };
        this.getKey = function() {
            return "district-" + this.state + "-" + this.id;
        };
        this.isDistrict = function() {return true;};
    };
    DistrictWithBoundaryPrototype.prototype = new MarkerWithBoundary();
    var DistrictWithBoundary = function(params) {
        this.schools = new Array();
        this.schoolsLoaded = false;

        this.init(params);
    };
    DistrictWithBoundary.prototype = new DistrictWithBoundaryPrototype();
    var SchoolWithBoundaryPrototype = function() {
        this.getDistrictKey = function() {
            if (this.districtId > 0) {
                return "district-" + this.state + "-" + this.districtId;
            }
        };
        this.showPolygonImpl = function() {
        };
        this.hidePolygonImpl = function() {
        };
        this.getKey = function() {
            return "school-" + this.state + "-" + this.id;
        };
        this.setMarker = function(marker) {
            this.hideMarker();
            this.marker = marker;
        };
        this.setPolygon = function(polygon) {
            this.hidePolygon();
            this.polygon = polygon;
            polygon.setOptions({strokeColor:'#78FF00', zIndex:2});
        };
        this.isSchool = function() {return true;};
    };
    SchoolWithBoundaryPrototype.prototype = new MarkerWithBoundary();
    var SchoolWithBoundary = function(params) {
        this.district = null;
        this.districtId = params.districtId;
        this.schoolType = params.schoolType || 'public';
        this.init(params);
    };
    SchoolWithBoundary.prototype = new SchoolWithBoundaryPrototype();

    return {
        map : null,
        globalResponseHandler : null,
        loadFeatureResponse : function(features, options) {
            var mapObjectsAdded = {};
            mapObjectsAdded.topLevel = [];
            mapObjectsAdded.all = [];
            mapObjectsAdded.keyMap = {};
            try {
                options = options || {};
                var showMarkers = typeof(options.showMarkers) !== 'undefined' ? options.showMarkers : true;
                var showPolygons = typeof(options.showPolygons) !== 'undefined' ? options.showPolygons : true;
                for (var featureIndex = 0; featureIndex < features.length; featureIndex++) {
                    var feature = features[featureIndex];
                    var mapObject = this.createMapObject(feature.data);
                    if (feature.hasMarkerInfo) {
                        mapObject.setMarker(GS.Map.Helper.createMarker(feature));
                        if (showMarkers) {
                            mapObject.showMarker();
                        }
                    }
                    if (feature.hasPolygonInfo) {
                        mapObject.setPolygon(GS.Map.Helper.createGoogleMapsPolygon({coordinates:feature.coordinates}));
                        if (typeof(feature.centroid) !== 'undefined') {
                            mapObject.centroid = {lat: feature.centroid.lat, lon: feature.centroid.lon};
                        }
                        if (typeof(feature.area) !== 'undefined') {
                            mapObject.area = feature.area;
                        }
                        if (showPolygons) {
                            mapObject.showPolygon();
                        }
                    }

                    mapObjectsAdded.all.push(mapObject);
                    mapObjectsAdded.topLevel.push(mapObject);
                    mapObjectsAdded.keyMap[mapObject.getKey()] = mapObject;
                    if (typeof(feature.dependents) !== 'undefined' && feature.dependents.length > 0) {
                        var subMap = this.loadFeatureResponse(feature.dependents, options);
                        mapObjectsAdded.all = mapObjectsAdded.all.concat(subMap.all);
                        for (var subKey in subMap.keyMap) {
                            if (subMap.keyMap.hasOwnProperty(subKey)) {
                                var subObj = subMap.keyMap[subKey];
                                var myObj = mapObjectsAdded.keyMap[subKey];
                                if (typeof(myObj) === 'undefined') {
                                    mapObjectsAdded.keyMap[subKey] = subObj;
                                } else {
                                    if (subObj.hasMarker()) {
                                        myObj.setMarker(subObj.marker);
                                    }
                                    if (subObj.hasPolygon()) {
                                        myObj.setPolygon(subObj.polygon);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e) {
                GS.Util.log("ERROR: " + e + ": " + e.message);
            }
            return mapObjectsAdded;
        },
        createMapObject : function(data) {
            var thingOnMap;
            if (data.type == 'district') {
                thingOnMap = new DistrictWithBoundary(data);
            } else if (data.type == 'school') {
                thingOnMap = new SchoolWithBoundary(data);
            }
            return thingOnMap;
        },
        loadDistrictsServingLocationAjax : function(latitude, longitude, options) {
            var deferred = new jQuery.Deferred();
            jQuery.getJSON("/geo/boundary/ajax/getDistrictsForLocation.page",
                {lat:latitude, lon:longitude, level:$('.js_mapLevelCode:checked').val()}
            ).done(function(data) {
                    successFunc
                        (deferred, data,
                            jQuery.extend({zeroWarningMessage: "WARN: No district found at this point for level code " + $('.js_mapLevelCode:checked').val()}, options));
                }).fail(function() {
                    deferred.reject();
                    alert("Error fetching districts for location: " + latitude + "," + longitude);
                });
            return deferred.promise();
        },
        loadDistrictsNearPointAjax : function(lat, lon, options) {
            var deferred = new jQuery.Deferred();
            jQuery.getJSON("/geo/boundary/ajax/getDistrictsNearPoint.page", {lat:lat, lon:lon, level:$('.js_mapLevelCode:checked').val()}
            ).done(function(data) {
                    successFunc(deferred, data, options);
                }).fail(function() {
                    deferred.reject();
                    alert("Error fetching district list");
                });
            return deferred.promise();
        },
        loadSchoolsServingLocationAjax : function(latitude, longitude, options) {
            var deferred = new jQuery.Deferred();
            jQuery.getJSON("/geo/boundary/ajax/getSchoolsForLocation.page", {lat:latitude, lon:longitude, level:$('.js_mapLevelCode:checked').val()}
            ).done(function(data) {
                    successFunc
                        (deferred, data,
                            jQuery.extend({zeroErrorMessage: "No school or district found at this point for level code " + $('.js_mapLevelCode:checked').val()}, options));
                }).fail(function() {
                    deferred.reject();
                    alert("Error fetching schools for location: " + latitude + "," + longitude);
                });
            return deferred.promise();
        },
        getDistrictBoundaryByIdAjax : function(state, id, name, options) {
            var deferred = new jQuery.Deferred();
            jQuery.getJSON(
                "/geo/boundary/ajax/getDistrictBoundaryById.page",
                {state:state, id:id, level:$('.js_mapLevelCode:checked').val()}
            ).done(function(data) {
                    successFunc(deferred, data, options);
                }).fail(function(event) {
                    deferred.reject();
                    if (event.status == 404) {
                        GS.Util.log("WARN: No district boundary found: " + name + " (" + state + ":" + id + ")");
                    } else {
                        alert("Error fetching district boundary: " + name + " (" + state + ":" + id + ")");
                    }
                });
            return deferred.promise();
        },
        getAllSchoolsForDistrictAjax : function(state, id, name, options) {
            var deferred = new jQuery.Deferred();
            jQuery.getJSON(
                "/geo/boundary/ajax/getSchoolsForDistrict.page",
                {state:state, districtId:id, level:$('.js_mapLevelCode:checked').val()}
            ).done(function(data) {
                    successFunc
                        (deferred, data,
                            jQuery.extend({zeroWarningMessage: "No schools found for district " + name}, options));
                }).fail(function() {
                    deferred.reject();
                    alert("Error fetching school list for district: " + name + " (" + state + ":" + id + ")");
                });
            return deferred.promise();
        },
        getSchoolBoundaryByIdAjax : function(state, id, name, options) {
            var deferred = new jQuery.Deferred();
            jQuery.getJSON(
                "/geo/boundary/ajax/getSchoolBoundaryById.page",
                {state:state, id:id, level:$('.js_mapLevelCode:checked').val()}
            ).done(function(data) {
                    successFunc(deferred, data, options);
                }).fail(function(event) {
                    deferred.reject();
                    if (event.status == 404) {
                        GS.Util.log("WARN: No school boundary found: " + name + " (" + state + ":" + id + ")");
                    } else {
                        alert("Error fetching school boundary: " + name + " (" + state + ":" + id + ")");
                    }
                });
            return deferred.promise();
        },
        loadNonDistrictSchoolsNearPoint : function(latitude, longitude, options) {
            var deferred = new jQuery.Deferred();
            var url = '';
            if (options.type === 'charter') {
                url = "/geo/boundary/ajax/getCharterSchoolsNearPoint.page";
            } else if (options.type === 'private') {
                url = "/geo/boundary/ajax/getPrivateSchoolsNearPoint.page"
            }
            if (url === '') {
                deferred.reject();
                return deferred.promise();
            }
            jQuery.getJSON(url,
                {lat:latitude, lon:longitude, level:$('.js_mapLevelCode:checked').val()}
            ).done(function(data) {
                    successFunc
                        (deferred, data,
                            jQuery.extend({zeroWarningMessage: "WARN: No schools found near this point for level code " + $('.js_mapLevelCode:checked').val()}, options));
                }).fail(function() {
                    deferred.reject();
                    alert("Error fetching schools for location: " + latitude + "," + longitude);
                });
            return deferred.promise();
        }
//            debug_getAllSchoolBoundariesForDistrictAjax : function(state, id, name, options) {
//                jQuery.getJSON(
//                        "/geo/boundary/ajax/debug_getSchoolBoundariesForDistrict.page",
//                        {state:state, districtId:id, level:$('.js_mapLevelCode:checked').val()}
//                ).done(debug_getAllSchoolBoundariesForDistrictSuccess
//                ).fail(function() {
//                    alert("Error fetching school boundaries for district: " + name + " (" + state + ":" + id + ")");
//                });
//            }
    };
})();
//GS.Boundaries.boundaryHelper = new GS.Boundaries.BoundaryHelper();


GS.Map = GS.Map || {};
GS.Map.Helper = (function() {
    return {
        highlightPolygon : function(polygon, options) {
            options = options || {};
            var duration = options.duration || 500;
            var strokeWeight = options.strokeWeight || 4;
            polygon.setOptions({strokeWeight:strokeWeight});
            setTimeout(function() {
                polygon.setOptions({strokeWeight:2});
            }, duration);
        },
        createMarker : function(options) {
            var center = new google.maps.LatLng(options.center.latitude, options.center.longitude);
            var size = new google.maps.Size(options.size.width,options.size.height);
            var origin = undefined;
            if (options.origin) {
                origin = new google.maps.Point(options.origin.x, options.origin.y);
            }
            var anchor = undefined;
            if (options.anchor) {
                anchor = new google.maps.Point(options.anchor.x, options.anchor.y);
            }
            var markerImage = new google.maps.MarkerImage(options.url, size, origin, anchor);
            var markerShape = undefined;
            if (options.shape) {
                markerShape = {
                    coord: options.shape.coord,
                    type: options.shape.type
                }
            }
            var shadow = options.shadowUrl;
            if (options.shadow) {
                shadow = new google.maps.MarkerImage(options.shadow.url, options.shadow.size,
                    options.shadow.origin, options.shadow.anchor);
            }
            return new google.maps.Marker
                ({
                    position: center,
                    title:options.name,
                    icon:markerImage,
                    shape:markerShape,
                    shadow:shadow
                });
        },
        createGoogleMapsPolygon : function(options){ // coordinates, zIndex, fillColor, strokeColor
            var coords = options.coordinates;
            var zIndex = options.zIndex || 1;
            var fillColor = options.fillColor || '#46461F';
            var strokeColor = options.strokeColor || '#FF7800';
            var paths = [];
            for (var i=0;i < coords.length;i++){
                for (var j=0;j<coords[i].length;j++){
                    var path=[];
                    for (var k=0;k<coords[i][j].length;k++){
                        var ll = new google.maps.LatLng(coords[i][j][k][1],coords[i][j][k][0]);
                        path.push(ll);
                    }
                    paths.push(path);
                }
            }
            return new google.maps.Polygon({
                paths: paths,
                strokeColor: strokeColor,
                strokeOpacity: 1,
                strokeWeight: 2,
                fillColor: fillColor,
                fillOpacity: 0.25,
                zIndex: zIndex
            });
        },
        // requires Array.prototype.contains to be defined
        geocodeAddress : function(searchInput) {
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
    };
})();

GS.Util = GS.Util || {};
GS.Util.getUrlVars = function() {
    var vars = {};
    window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
};
GS.Util.sortByRating = function(a,b) {
    if (a.rating > 0 && b.rating > 0) {
        return b.rating - a.rating;
    } else if (a.rating > 0) {
        return -1;
    } else if (b.rating > 0) {
        return 1;
    }
    return 0;
};
GS.Util.log = function(msg) {
    if (typeof(console) !== 'undefined') {
        console.log(msg);
    }
};
