function determinePageState() {
    var elements;
    var i;
    if ($('numStudents').value == 0) {
        $('parentAmbassadorLine').hide();
        elements = document.getElementsByClassName("childSchools");
        for (i=0; i < elements.size(); i++) {
            Element.extend(elements[i]).hide();
        }
        if ($('other').checked) {
            $('childSchoolHeaderRow').hide();
        } else {
            $('childSchoolHeaderRow').show();
        }
    } else {
        $('parentAmbassadorLine').show();
        elements = document.getElementsByClassName("childSchools");
        for (i=0; i < elements.size(); i++) {
            Element.extend(elements[i]).show();
        }
    }
}

function profileStateChange(stateSelect) {
    var url = 'accountInformationAjax.page';
    var pars = 'state=' + stateSelect.value;
    var cityDiv = $('cityDiv');
    var citySelect = $('city');

    Element.remove(citySelect);
    cityDiv.update("<span>Loading ...</span>");
    new Ajax.Updater(
            'city',
            url,
            {
                method: 'get',
                parameters: pars,
                onComplete: updateElementContents('cityDiv', citySelect)
            });
}

function updateElementContents(elemIdToUpdate, elemToAdd) {
    $(elemIdToUpdate).update(elemToAdd);
    elemToAdd.show();
}

function childGradeChange(gradeSelect, childNum) {
    if (gradeSelect.value == '') {
        $('childSchool' + childNum).update('<option value="-2">--</option>');
        return false;
    }

    var childSchoolSelect = $('childSchool' + childNum);
    var childSchoolDiv = $('childSchoolDiv' + childNum);
    var url = 'accountInformationAjax.page';
    var pars = 'state=' + $('childState' + childNum).value;
    pars += '&city=' + $('childCity' + childNum).value;
    pars += '&grade=' + gradeSelect.value;
    Element.remove(childSchoolSelect);
    childSchoolDiv.update("<span>Loading ...</span>");
    new Ajax.Updater(
            'childSchool' + childNum,
            url,
            {
                method: 'get',
                parameters: pars,
                onComplete: updateElementContents('childSchoolDiv' + childNum, childSchoolSelect)
            });
    return true;
}

function childCityChange(citySelect, childNum) {
    resetGradeSchool(childNum);
    $('cityText' + childNum).innerHTML = citySelect.value;
}

function childStateChange(stateSelect, childNum) {
    resetGradeSchool(childNum);

    $('cityText' + childNum).update('--');
    $('stateText' + childNum).update(stateSelect.value);

    var url = 'accountInformationAjax.page';
    var pars = 'state=' + stateSelect.value;
    pars += '&childNum=' + childNum;

    var childCitySelect = $('childCity' + childNum);
    $('citySelectDiv' + childNum).update("<span>Loading ...</span>");
    new Ajax.Updater(
            'childCity' + childNum,
            url,
            {
                method: 'get',
                parameters: pars,
                onComplete: updateElementContents('citySelectDiv' + childNum, childCitySelect)
            });
}

function resetGradeSchool(childNum) {
    $('grade' + childNum).value = '';
    childGradeChange($('grade' + childNum), childNum);
}

function changeLocationToggle(childNum) {
    $('location' + childNum).toggle();
    $('chooseLocation' + childNum).toggle();
    if ($('chooseLocation' + childNum).visible()) {
        $('changeLocationToggle' + childNum).innerHTML = 'Done';
    } else {
        $('changeLocationToggle' + childNum).innerHTML = 'Change city';
    }
}

function addChild() {
    $('addChildSubmitButton').click();
}

function deleteChild(childNum) {
    $('removeChildSubmitButton' + childNum).click();
}
