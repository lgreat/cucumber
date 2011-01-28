module("schoolMap");

test("school map", function() {
    var schoolMap = new GS.map.SchoolMap("map", -75, -75, true);

    var marker = schoolMap.createSchoolMarker(-50, -50, "tooltip", 5);

    equals(marker.getPosition().lat(), -50, "Latitude equals given latitude");
    equals(marker.getTitle(), "tooltip", "Marker title equals given tooltip");
});
