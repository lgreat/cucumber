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
    document.forms['searchForm'].submit();
}