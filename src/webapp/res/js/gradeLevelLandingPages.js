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
    cityDiv.update("<span>Loading ...</span>");
    new Ajax.Updater(
            'topCitiesCityList',
            url,
            {
                method: 'get',
                parameters: pars,
                onComplete: updateElementContents('topCitiesCityListSpan', citySelect)
            });
}

function updateElementContents(elemIdToUpdate, elemToAdd) {
    $(elemIdToUpdate).update(elemToAdd);
    elemToAdd.show();
}

function changeCity(cityName, stateAbbr) {
    var newHref = window.location.href;
    if (newHref.indexOf('?') > 0) {
        newHref = newHref.substring(0, newHref.indexOf('?'));
    }
    newHref = newHref + '?city=' + encodeURIComponent(cityName) + '&state=' + stateAbbr;
    window.location.href = newHref;
}
