var ID_PREFIX = 'compare_schools_';

function appendCompareSchools(link) {
    var param = getCompareSchoolsParam();
    if (param != '') {
        if (link.href.indexOf("?") != -1) {
            link.href += "&" + param;
        } else {
            link.href += "?" + param;
        }
    }
}

function getCompareSchoolsParam() {
    var inputs = document.getElementsByTagName("input");
    var schoolIds = "";
    for (var i=0; i < inputs.length; i++) {
        var currentElem = inputs[i];
        if (currentElem.id > '' && currentElem.id.indexOf(ID_PREFIX) > -1) {
            if (currentElem.checked) {
                if (schoolIds.length != 0) {
                    schoolIds += ",";
                }

                schoolIds += currentElem.value;
            }
        }
    }

    if (schoolIds != '') {
        return "cmp=" + schoolIds;
    }

    return "";
}

function countChecked() {
    var numChecked = 0;
    var inputs = document.getElementsByTagName("input");
    for (var i=0; i < inputs.length; i++) {
        var currentElem = inputs[i];
        if (currentElem.id > '' && currentElem.id.indexOf(ID_PREFIX) > -1) {
            if (currentElem.checked) {
                numChecked++;
            }
        }
    }
    return numChecked;
}

function setCheckedToDisplayError() {
    var inputs = document.getElementsByTagName("input");
    for (var i=0; i < inputs.length; i++) {
        var currentElem = inputs[i];
        if (currentElem.id > '' && currentElem.id.indexOf(ID_PREFIX) > -1 && currentElem.style.display != "none") {
            if (currentElem.checked) {
                var curId = currentElem.id.substring(ID_PREFIX.length);
                hide(ID_PREFIX + "label_" + curId);
                showInline(ID_PREFIX + "error_" + curId);
                hide(ID_PREFIX + "submit_" + curId);
            }
        }
    }
}

function setCheckedToDisplaySubmit() {
    var inputs = document.getElementsByTagName("input");
    for (var i=0; i < inputs.length; i++) {
        var currentElem = inputs[i];
        if (currentElem.id > '' && currentElem.id.indexOf(ID_PREFIX) > -1 && currentElem.style.display != "none") {
            if (currentElem.checked) {
                var curId = currentElem.id.substring(ID_PREFIX.length);
                hide(ID_PREFIX + "label_" + curId);
                hide(ID_PREFIX + "error_" + curId);
                showInline(ID_PREFIX + "submit_" + curId);
            }
        }
    }
}

function setAllToDisplayLabel() {
    var inputs = document.getElementsByTagName("input");
    for (var i=0; i < inputs.length; i++) {
        var currentElem = inputs[i];
        if (currentElem.id > '' && currentElem.id.indexOf(ID_PREFIX) > -1 && currentElem.style.display != "none") {
            var curId = currentElem.id.substring(ID_PREFIX.length);
            showInline(ID_PREFIX + "label_" + curId);
            hide(ID_PREFIX + "error_" + curId);
            hide(ID_PREFIX + "submit_" + curId);
        }
    }
}

function hide(elementId) {
    document.getElementById(elementId).style.display = "none";
}

function showInline(elementId) {
    document.getElementById(elementId).style.display = "inline";
}

function evaluateCheckboxes(currentElem) {
    // first determine if they are trying to check multiple states
    var numStates = countStates();
    if (numStates > 1) {
        alert("Sorry, you can only compare schools in the same state.");
        // cancel check action
        return false;
    }
    // then see how many they have checked
    var numChecked = countChecked();
    // defaults to "Check to compare"
    setAllToDisplayLabel();
    if (numChecked == 1) {
        // "Compare: check two or more"
        setCheckedToDisplayError();
    } else if (numChecked > 8 ) {
        currentElem.checked = false;
        setCheckedToDisplaySubmit();
        alert("You can compare a maximum of 8 schools at a time.");
        return false;
    } else {
        // "Compare now"
        setCheckedToDisplaySubmit();
    }
    return true;
}

function GS_launchCompare() {
    var inputs = document.getElementsByTagName("input");
    // for each input on the page
    var checkedSchools = [];
    for (var i=0; i < inputs.length; i++) {
        var currentElem = inputs[i];
        // if the id is of the correct form
        if (currentElem.checked && currentElem.id > '' && currentElem.id.indexOf(ID_PREFIX) > -1) {
            // grab the school id, level code, and state
            var schoolId = currentElem.id.substring(ID_PREFIX.length);
            var schoolState = currentElem.value.substring(0, 2);
            var statePlusId = schoolState + schoolId;
            checkedSchools.push(statePlusId);
        }
    }
    return checkedSchools.length > 1 && checkedSchools.length < 9;

}


function countStates() {
    var inputs = document.getElementsByTagName("input");
    var stateMap = new Object;
    var stateAb;
    var numStates = 0;
    for (var i=0; i < inputs.length; i++) {
        var currentElem = inputs[i];
        if (currentElem.id > '' && currentElem.id.indexOf(ID_PREFIX) > -1) {
            if (currentElem.checked) {
                stateAb = currentElem.value.substring(0, 2);
                stateMap[stateAb] = 1;
            }
        }
    }
    for (stateAb in stateMap) {
        numStates++;
    }
    return numStates;
}

/* --------- S O R T I N G &  P A G E  S E T T I N G S ----------------- */

function initializeColumnHeaders(){

}

function initializeResultsPerPage(){
    
}
