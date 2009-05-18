var ID_PREFIX = 'compare_schools_';

function appendCompareSchools(link) {
    var param = getCompareSchoolsParam();
    if (param.length > 0) {
        link.href += "&" + param;
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

    if (schoolIds.length > 0) {
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

function evaluateCheckboxes() {
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
    } else {
        // "Compare now"
        setCheckedToDisplaySubmit();
    }
    return true;
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
