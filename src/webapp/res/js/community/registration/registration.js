function scrollToError() {
    window.location.href = document.location.pathname + '#error';
}

// TODO: Break out into two methods: parentGradeChange and childGradeChange
function gradeChange(childNum, isChangeParentCity) {
    if (isChangeParentCity) {
        //when parent city is changed the childnum = looptimes
        for (var i = 1; i <= childNum; i++) {
            if ($('locationOverride_' + i).value != 'true') {
                var grade = $('grade' + i).value;
                if (grade == '') {
                    $('schoolDiv' + i).innerHTML = '<select name="school' + i + '" id="school' + i + '" class="selectChildSchool"><option value="">- Choose School -</option></select>';
                    return;
                }
                var parentState = $('userState').value;
                var parentCity = $('citySelect').value;
                var url = '/community/registration2Ajax.page';
                var pars = 'state=' + parentState;
                pars += '&city=' + parentCity;
                pars += '&grade=' + grade;
                pars += '&childNum=' + i;
                $('schoolDiv' + i).innerHTML = '<select name="school' + i + '" id="school' + i + '" class="selectChildSchool"><option value="">Loading ...</option></select>';
                var myAjax = new Ajax.Updater(
                        'schoolDiv' + i,
                        url,
                {
                    method: 'get',
                    parameters: pars
                });
            }
        }
    }
    else {
        var grade = $('grade' + childNum).value;
        if (grade == '') {
            $('schoolDiv' + childNum).innerHTML = '<select name="school' + childNum + '" id="school' + childNum + '" class="selectChildSchool"><option value="">- Choose School -</option></select>';
            return;
        }
        var url = '/community/registration2Ajax.page';

        var state =  $('userState').value;
        var city = $('citySelect').value;

        if ($('locationOverride_' + childNum).value == 'true') {
            state = $('stateSelectChild_' + childNum).value;
            city = $('citySelectChild_' + childNum).value;
        }

        var pars = 'state=' + state;
        pars += '&city=' + city;
        pars += '&grade=' + grade;
        pars += '&childNum=' + childNum;
        $('schoolDiv' + childNum).innerHTML = '<select name="school' + childNum + '" id="school' + childNum + '" class="selectChildSchool"><option value="">Loading ...</option></select>';
        var myAjax = new Ajax.Updater(
                'schoolDiv' + childNum,
                url,
        {
            method: 'get',
            parameters: pars
        });
    }

}

function parentStateChange(stateSelect, numChildren) {
    var url = '/community/registrationAjax.page';
    var pars = 'state=' + stateSelect.value + "&type=city&showNotListed=true";
    pars += "&onchange=gradeChange(" + numChildren + ",true);";
    for (var i = 1; i <= numChildren; i++) {
        if ($('locationOverride_' + i).value != 'true') {
            // if child's location is not overridden, clear out his school because parent has changed state
            $('schoolDiv' + i).innerHTML = '<select name="school' + i + '" id="school' + i + '" class="selectChildSchool"><option value="">- Choose School -</option></select>';
        }
    }
    $('city').innerHTML = '<select name="city" class="selectCity"><option value="">Loading ...</option></select>';
    var myAjax = new Ajax.Updater(
            'city',
            url,
    {
        method: 'get',
        parameters: pars
    });
}

// TODO: when changing child's state, clear out child's school dropdown
function childStateChange(stateSelect, childNum) {
    var url = '/community/registrationAjax.page';
    var pars = 'state=' + stateSelect.value + "&type=city&showNotListed=true";
    pars += '&citySelectId=citySelectChild_' + childNum;
    pars += '&citySelectName=city' + childNum;
    pars += "&onchange=gradeChange(" + childNum + ",false);";
    $('citySelectSpan_' + childNum).innerHTML = '<select name="city' + childNum + '" class="selectCity"><option value="">Loading ...</option></select>';
    var myAjax = new Ajax.Updater(
            'citySelectSpan_' + childNum,
            url,
    {
        method: 'get',
        parameters: pars
    });
}

function validateFirstName() {
    var notice = $('firstNameMessage');
    var fn = $('firstName').value;

    if (fn.length < 2 || fn.length > 24) {
        notice.update('Your first name must be 2-24 characters long.').style.display = '';
        notice.className = "ajaxMessage ajaxError";
    } else {
        notice.update('').style.display = '';
        notice.className = "ajaxMessage ajaxSuccess";
    }
}

function validateEmail() {
    var url = '/community/registrationAjax.page';
    var pars = 'email=' + $('email').value;
    var notice = $('emailMessage');
    var myAjax = new Ajax.Request(
            url,
    {
        method: 'get',
        parameters: pars,
        onSuccess: function(transport) {
            if (transport.responseText == "invalid") {
                notice.update('Please enter a valid email address.').style.display = '';
                notice.className = "ajaxMessage ajaxError";
            } else if (transport.responseText == "valid") {
                notice.update('').style.display = '';
                notice.className = "ajaxMessage ajaxSuccess";
            } else {
                notice.update('').style.display = 'none';
                notice.className = "ajaxMessage";
            }
        },
        onFailure: function(transport) {
            notice.update('').style.display = 'none';
            notice.className = "ajaxMessage";
        }
    }
            );
}

function validateUN() {
    var url = '/community/registrationAjax.page';
    var pars = 'un=' + $('screenName').value;
    var notice = $('usernameMessage');
    var myAjax = new Ajax.Request(
            url,
    {
        method: 'get',
        parameters: pars,
        onSuccess: function(transport) {
            if (transport.responseText == "inuse") {
                notice.update('This user name is already in use.').style.display = '';
                notice.className = "ajaxMessage ajaxError";
            } else if (transport.responseText == "invalid") {
                notice.update('User name must be 6-14 characters.').style.display = '';
                notice.className = "ajaxMessage ajaxError";
            } else if (transport.responseText == "valid") {
                notice.update('').style.display = '';
                notice.className = "ajaxMessage ajaxSuccess";
            } else {
                notice.update('').style.display = 'none';
                notice.className = "ajaxMessage";
            }
        },
        onFailure: function(transport) {
            notice.update('').style.display = 'none';
            notice.className = "ajaxMessage";
        }
    }
            );
}

function validatePW() {
    var notice = $('passwordMessage');
    var pw = $('password').value;

    if (pw.length < 6 || pw.length > 14) {
        notice.update('Password should be 6-14 characters.').style.display = '';
        notice.className = "ajaxMessage ajaxError";
    } else {
        notice.update('').style.display = '';
        notice.className = "ajaxMessage ajaxSuccess";
    }

    validateConfirmPW();
}

function validateConfirmPW() {
    var notice = $('confirmPWMessage');
    var pw = $('confirmPassword').value;

    if (pw != '') {
        if (pw != $('password').value) {
            notice.update('Passwords do not match.').style.display = '';
            notice.className = "ajaxMessage ajaxError";
        } else {
            notice.update('').style.display = '';
            notice.className = "ajaxMessage ajaxSuccess";
        }
    }
}

function doSingleClick(element) {
    element.onclick = new Function('return false;');
    return true;
}

function changeChildCityAndState(childNum) {

    var cityChildSelectSpan = $('citySelectSpan_' + childNum);
    cityChildSelectSpan.innerHTML = $('city').innerHTML.replace('citySelect', 'citySelectChild_' + childNum);
    $('citySelectChild_' + childNum).className = "childCities";
    $('citySelectChild_' + childNum).name = "city" + childNum;
    $('citySelectChild_' + childNum).onchange = function() {
        gradeChange(childNum, false);
    };

    $('citySelectChild_' + childNum).value = $('citySelect').value;
    $('stateSelectChild_' + childNum).value = $('userState').value;
    $('childCityRow_' + childNum).style.display = '';
    $('changeChildCity_' + childNum).style.display = 'none';

    $('locationOverride_' + childNum).value = true;
}

function addAnotherChild(childNum) {
    var nextChildNum = childNum + 1;
    $('childRow_' + nextChildNum).style.display = '';
    $('addAnotherChild_' + childNum).style.display = 'none';
    if (childNum == 8) {
        $('addAnotherChild_' + nextChildNum).style.display = 'none';
    }
    $('requiredFld').hide();
    $('requiredFld').show();
    $('privacyBox').hide();
    $('privacyBox').show();
}