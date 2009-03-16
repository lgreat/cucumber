// linkElem == a
// linkElem.parentNode == li
// linkElem.parentNode.parentNode == ul
function selectBox(linkElem, boxId) {
    var x;
    // de-select all links
    var listItemsArr = Element.extend(linkElem.parentNode.parentNode).childElements();
    for (x=0; x < listItemsArr.length; x++) {
        listItemsArr[x].removeClassName("selected");
    }
    // select current link
    Element.extend(linkElem.parentNode).addClassName("selected");
    // hide all boxes, show current box
    var boxArr = $$('#intro .gradeLevelBox');
    for (x=0; x < boxArr.length; x++) {
        if (boxArr[x].id == boxId) {
            boxArr[x].show();
        } else {
            boxArr[x].hide();
        }
    }
}

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
    var cityDiv = $('topCitiesCityListSpan');
    var citySelect = $('topCitiesCityList');

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
    $(elemIdToUpdate).update(elemToAdd);
    elemToAdd.show();
    Element.show(stateSelect);
}

function changeCity(cityName, stateAbbr) {
    var newHref = window.location.href;
    if (newHref.indexOf('?') > 0) {
        newHref = newHref.substring(0, newHref.indexOf('?'));
    }
    newHref = newHref + '?city=' + encodeURIComponent(cityName) + '&state=' + stateAbbr;
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

 function validateZipCode(zip) {
    var zipCode = document.getElementById(zip).value;
//    var zipcodeDiv = document.getElementById('zipcodeError');
    var zipPattern = /^[0-9]{5}$/;
    if(!zipPattern.test(zipCode)){
        alert("Enter a valid zipcode");
//        zipcodeDiv.className ="zipcodeErrorDisplay";
        return false;
    }
    else{
        return true;
    }
}

function viewTopSchools() {
    var selector = document.getElementById('top5State');
    var longStateName = selector.options[selector.selectedIndex].innerHTML.toLowerCase();
    window.location.href = '/top-high-schools/' + longStateName.replace(' ', '-') + '/';
    return false;
}