function showMapTab() {
    showTab('mapTab');
//    hideTab('listTab');
    hideTab('searchTab');
    hideTab('helpTab');
}

//function showListTab() {
//    hideTab('mapTab');
//    showTab('listTab');
//    hideTab('searchTab');
//}

function showSearchTab() {
    hideTab('mapTab');
    hideTab('helpTab');
//    hideTab('listTab');
    showTab('searchTab');
}

function showHelpTab() {
    hideTab('mapTab');
    hideTab('searchTab');
    showTab('helpTab');
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