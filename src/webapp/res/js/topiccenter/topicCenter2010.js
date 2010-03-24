// JavaScript Document
// required to avoid "$j" collisions with Prototype.js
jQuery.noConflict();
var $j = jQuery;

$j(document).ready(function() {
    //
    // Initialize (all twirly content closed)
    var theTwirly = $j('#topicbarGS .hasNested');
    //
    // On twirly click
    theTwirly.click(function () {
        ($j('> span', this).hasClass('twirlyClosed')) ? $j('> span', this).removeClass('twirlyClosed').addClass('twirlyOpen') : $j('> span', this).removeClass('twirlyOpen').addClass('twirlyClosed');
        ($j('> span', this).hasClass('twirlyClosed')) ? $j('> ul', this).slideUp(250) : $j('> ul', this).slideDown(250);
        return false;
    });

    // GS-9690 - show 160x600 ad (and AD word above the ad), only if ad is running
    if ($j('google_ads_div_Library_Article_Page_AboveFold_Left_160x600').length > 0) {
        $j('.skyscraperAd').show();
    }
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

function changeState(stateSelect) {
    var url = '/accountInformationAjax.page';
    var pars = 'state=' + stateSelect.value + '&showNotListed=false';
    var cityDiv = $j('topCitiesCityListSpan');
    var citySelect = $j('topCitiesCityList');

    Element.remove(citySelect);
    Element.hide(stateSelect);
    cityDiv.update("<span>Loading ...</span>");
    new Ajax.Updater(
            'topCitiesCityList',
            url,
            {
                method: 'get',
                parameters: pars,
                onComplete: updateCityAndShowState('topCitiesCityListSpan', citySelect, stateSelect)
            });
}

function updateCityAndShowState(elemIdToUpdate, elemToAdd, stateSelect) {
    $j(elemIdToUpdate).update(elemToAdd);
    elemToAdd.show();
    Element.show(stateSelect);
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

    var noSearchTerms = (queryVal == 'Search for school, district or city');
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
        window.location.href = '/school/research.page?state='+stateVal;
        returnVal = false;
    }
    return returnVal;
}

// EOF