// JavaScript Document

jQuery(function () {
    var tabContainers = jQuery('div.tabs > div');
    tabContainers.hide().filter(':first').show();

    jQuery('div.tabs ul.tabNavigation a').click(
        function () {
            var tabToShowHash = this.hash;
            tabContainers.hide();
            tabContainers.filter(tabToShowHash).show();
            jQuery('div.tabs ul.tabNavigation a').removeClass('selected');
            jQuery(this).addClass('selected');
            //track user clicks on most popular tab in Omniture.
            if (tabToShowHash === '#js_mostPopularTab') {
                if (s.tl) {
                    s.tl(true, 'o', 'Topic_Center_Most_Popular_Tab');
                }
            }
            return false;
        }).filter(':first').click();
});

jQuery(function () {
    var nestedContainers = jQuery('li.hasNested > .nested');
    var twirlies = jQuery('li.hasNested > div > span');
    nestedContainers.hide();
    twirlies.removeClass('i-twirly-open').addClass('i-twirly-closed');

    jQuery('li.hasNested > div').click(
        function () {
            var twirly = jQuery(this).find('span');
            var nestedToShow = jQuery(this).parent().find('.nested');
            if (twirly.hasClass('i-twirly-closed')) {
                twirly.removeClass('i-twirly-closed').addClass('i-twirly-open')
                nestedToShow.slideDown(250);
            } else {
                twirly.removeClass('i-twirly-open').addClass('i-twirly-closed');
                nestedToShow.slideUp(250);
            }
        return false;
    });
});

/* ------------------------------------------------ */
/* -- TOPIC CENTER AND GRADE LEVEL FIND A SCHOOL (moved over from topicCenter2010.js)-- */
/* ------------------------------------------------ */

// change state
jQuery('#topSchoolsStateSelector').change(function() {
    var url = '/accountInformationAjax.page';
    var pars = 'state=' + jQuery(this).val() + '&showNotListed=false';
    var cityDiv = jQuery('#topCitiesCityListSpan');
    var citySelect = jQuery('#topCitiesCityList');
    var cityLoading = jQuery('#topCitiesCityLoadingSpan');
    var stateSelect = jQuery('#topSchoolsStateSelector');

    citySelect.hide();
    stateSelect.hide();

    cityLoading.html('<span>&nbsp;&nbsp;Loading ...</span>');
    cityLoading.show();

    jQuery.get(url, {state: jQuery(this).val(), showNotListed: 'false'}, function(data) {
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