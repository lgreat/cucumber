var ID_PREFIX = 'compare_schools_';

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
        if (currentElem.id > '' && currentElem.id.indexOf(ID_PREFIX) > -1) {
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
        if (currentElem.id > '' && currentElem.id.indexOf(ID_PREFIX) > -1) {
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
        if (currentElem.id > '' && currentElem.id.indexOf(ID_PREFIX) > -1) {
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
    var numChecked = countChecked();
    setAllToDisplayLabel();
    if (numChecked == 1) {
        setCheckedToDisplayError();
    } else {
        setCheckedToDisplaySubmit();
    }
}
