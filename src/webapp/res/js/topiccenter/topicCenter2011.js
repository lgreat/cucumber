/*function switchTabs(tabToShow, tabToHide) {

    //track user clicks on most popular tab in Omniture.
    if (tabToShow == 'js_mostPopularTab') {
        if (s.tl) {
            s.tl(true, 'o', 'Topic_Center_Most_Popular_Tab');
        }
    }
    var showTab = jQuery('#' + tabToShow);
    var hideTab = jQuery('#' + tabToHide);
    showTab.show();
    hideTab.hide();
}*/

jQuery(function () {
    var tabContainers = jQuery('div.tabs > div');
    tabContainers.hide().filter(':first').show();

    jQuery('div.tabs ul.tabNavigation a').click(
        function () {
            tabContainers.hide();
            tabContainers.filter(this.hash).show();
            jQuery('div.tabs ul.tabNavigation a').removeClass('selected');
            jQuery(this).addClass('selected');
            return false;
        }).filter(':first').click();
});

/* ------------------------------------------------ */
/* -- TOPIC CENTER AND GRADE LEVEL FIND A SCHOOL (moved over from topicCenter2010.js)-- */
/* ------------------------------------------------ */

// change state
$j('#topSchoolsStateSelector').change(function() {
    var url = '/accountInformationAjax.page';
    var pars = 'state=' + $j(this).val() + '&showNotListed=false';
    var cityDiv = $j('#topCitiesCityListSpan');
    var citySelect = $j('#topCitiesCityList');
    var cityLoading = $j('#topCitiesCityLoadingSpan');
    var stateSelect = $j('#topSchoolsStateSelector');

    citySelect.hide();
    stateSelect.hide();

    cityLoading.html('<span>&nbsp;&nbsp;Loading ...</span>');
    cityLoading.show();

    $j.get(url, {state: $j(this).val(), showNotListed: 'false'}, function(data) {
        citySelect.html(data);
        citySelect.show();
        cityLoading.html('');
        cityLoading.hide();
        stateSelect.show();
    });
});


// Returns true if a state has been selected.
function checkStateSelection(id) {
    var select = document.getElementById(id);
    if (select.options[select.selectedIndex].value == "") {
        alert("Please select a state");
        return false;
    } else {
        return true;
    }
}

// Swaps text value with another
function textSwitch(el, target, replace) {
    if (el.value == replace) {
        el.value = target;
    }
}

function changeCity(cityName, stateAbbr) {
    var newHref = window.location.href;
    if (newHref.indexOf('?') > 0) {
        newHref = newHref.substring(0, newHref.indexOf('?'));
    }
    newHref = newHref + '?city=' + encodeURIComponent(cityName) + '&state=' + stateAbbr + '#findASchool';
    window.location.href = newHref;
}


function findAndCompareCheck(queryId, stateSelectorId, pathwaysId) {
    var returnVal = true;
    var queryVal = document.getElementById(queryId).value;
    var stateVal = document.getElementById(stateSelectorId).value;

    var noSearchTerms = (queryVal == 'Enter school, city, or district');
    var noState = (stateVal == ("-" + "-") || stateVal == "");

    if (noSearchTerms && noState) {
        // go to national R&C (/school/research.page)
        window.location.href = '/school/research.page';
        returnVal = false;
    } else if (!noSearchTerms && noState) {
        // show javascript alert
        alert("Please select a state.");
        returnVal = false;
    } else if (noSearchTerms && !noState) {
        // go to state R&C (/school/research.page)
        window.location.href = '/school/research.page?state=' + stateVal;
        returnVal = false;
    }
    return returnVal;
}