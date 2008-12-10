function showMapTab() {
    showTab('mapTab');
//    hideTab('listTab');
    hideTab('searchTab');
}

//function showListTab() {
//    hideTab('mapTab');
//    showTab('listTab');
//    hideTab('searchTab');
//}

function showSearchTab() {
    hideTab('mapTab');
//    hideTab('listTab');
    showTab('searchTab');
}

function showTab(tabId) {
    var tabElem = document.getElementById(tabId);
    var tabBodyElem = document.getElementById(tabId + 'Body');
    if (tabElem && tabBodyElem) {
        tabBodyElem.className = "tabBody selected";
        tabElem.className = "selected";
    }
}

function hideTab(tabId) {
    var tabElem = document.getElementById(tabId);
    var tabBodyElem = document.getElementById(tabId + 'Body');
    if (tabElem && tabBodyElem) {
        tabBodyElem.className = "tabBody";
        tabElem.className = "";
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