var GS_MAP_TAB_NAME = "mapTab";
var GS_SEARCH_TAB_NAME = "searchTab";
var GS_HELP_TAB_NAME = "helpTab";

function showMapTab() {
    showTab(GS_MAP_TAB_NAME);
    hideTab(GS_SEARCH_TAB_NAME);
    hideTab(GS_HELP_TAB_NAME);
}

function showSearchTab() {
    hideTab(GS_MAP_TAB_NAME);
    hideTab(GS_HELP_TAB_NAME);
    showTab(GS_SEARCH_TAB_NAME);
}

function showHelpTab() {
    hideTab(GS_MAP_TAB_NAME);
    hideTab(GS_SEARCH_TAB_NAME);
    showTab(GS_HELP_TAB_NAME);
}

function closeHelpTab() {
    hideTab(GS_HELP_TAB_NAME);
    showTab(GS_MAP_TAB_NAME);
}

function showTab(tabId) {
    var tabElem = document.getElementById(tabId);
    var tabBodyElem = document.getElementById(tabId + 'Body');
    if (tabElem) {
        tabElem.className = "selected";
    }
    if (tabBodyElem) {
        tabBodyElem.className = "tabBody selected";
    }
}

function hideTab(tabId) {
    var tabElem = document.getElementById(tabId);
    var tabBodyElem = document.getElementById(tabId + 'Body');
    if (tabElem) {
        tabElem.className = "";
    }
    if (tabBodyElem) {
        tabBodyElem.className = "tabBody";
    }
}

function textSwitch(el, target, replace) {
    if (el.value == replace) {
        el.value = target;
    }
}

function toggleFilter(levelCode, checked, searchQuery) {
    document.getElementById('filter_' + levelCode + '_value').value = checked;
    document.getElementById('searchInput').value = searchQuery;

    var noneChecked =
        !document.getElementById('filter_e').checked &&
        !document.getElementById('filter_m').checked &&
        !document.getElementById('filter_h').checked;
    if (document.getElementById('filter_p') != null) {
        noneChecked =
            noneChecked && !document.getElementById('filter_p').checked; 
    }

    document.getElementById('zoom').value = GS_map.getZoom();
    document.getElementById('lat').value = GS_map.getCenter().lat();
    document.getElementById('lon').value = GS_map.getCenter().lng();

    if (noneChecked) {
        clearMarkers();
    } else {
        document.forms['searchForm'].submit();
    }

}

function submitSearch() {
    var noneChecked =
        !document.getElementById('filter_e').checked &&
        !document.getElementById('filter_m').checked &&
        !document.getElementById('filter_h').checked;
    if (document.getElementById('filter_p') != null) {
        noneChecked =
            noneChecked && !document.getElementById('filter_p').checked;
    }
    var newSearch =
        (document.getElementById(GS_SEARCH_TAB_NAME).className == "selected");
    if (noneChecked || newSearch) {
        if (document.getElementById('filter_p_value') != null) {
            document.getElementById('filter_p_value').value = 'true';
        }
        document.getElementById('filter_e_value').value = 'true';
        document.getElementById('filter_m_value').value = 'true';
        document.getElementById('filter_h_value').value = 'true';
    }
    document.getElementById('zoom').value = 0;
    document.getElementById('lat').value = 0;
    document.getElementById('lon').value = 0;
    return true;
}