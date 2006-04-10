// Copyright (c) 2006 by Andrew J. Peterson, NDP Software
// For latest version, see ndpsoftware.com.
// In particular, http://ndpsoftware.com/googleMapTooltips.php
// version 1.0, 09-Apr-06
var gMarkers = [];
var map;

var gTooltip;


function setTooltipOnMarker(m, tooltip) {
    m.tooltip = '<div class="tooltip">' + tooltip + '</div>';

    GEvent.addListener(m, "mouseover", function() {
        showTooltip(m);
    });
    GEvent.addListener(m, "mouseout", function() {
        hideTooltip();
    });
}

function showTooltip(marker) {

    gTooltip.innerHTML = marker.tooltip;
    var pt = map.getCurrentMapType().getProjection().fromLatLngToPixel(map.fromDivPixelToLatLng(new GPoint(0, 0), true), map.getZoom());
    var offset = map.getCurrentMapType().getProjection().fromLatLngToPixel(marker.getPoint(), map.getZoom());
    var anchor = marker.getIcon().iconAnchor;
    var wd = marker.getIcon().iconSize.width;
    var ht = gTooltip.clientHeight;
    var pos = new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(offset.x - pt.x - anchor.x + wd, offset.y - pt.y - anchor.y - ht));
    pos.apply(gTooltip);
    gTooltip.style.visibility = "visible";
}

function hideTooltip() {
    gTooltip.style.visibility = "hidden";
}

function addTooltipOnMouseover(e, mi) {
    e.setAttribute("onmouseover", "showTooltip(gMarkers['" + mi + "'])");
    e.setAttribute("onmouseout", "hideTooltip()");
}

function changeClassOnMarkerMouseover(e, mi) {
    GEvent.addListener(gMarkers[mi], "mouseover", function() {
        e.className += " mousedOver";
    });
    GEvent.addListener(gMarkers[mi], "mouseout", function() {
        e.className = e.className.replace(/ ?mousedOver/, " ");
    });
}


