var hasChildCityState_1 = false;
var hasChildCityState_2 = false;
var hasChildCityState_3 = false;
var hasChildCityState_4 = false;
var hasChildCityState_5 = false;
var hasChildCityState_6 = false;
var hasChildCityState_7 = false;
var hasChildCityState_8 = false;
var hasChildCityState_9 = false;


function scrollToError() {
    window.location.href = document.location.pathname + '#error';
}

function gradeChange(childNum, isChangeParentCity) {
    if (isChangeParentCity) {
        //when parent city is changed the childnum = looptimes
        for (var i = 1; i <= childNum; i++) {
            if (!eval('hasChildCityState_' + i)) {
                var grade = $('grade' + i).value;
                if (grade == '') {
                    $('schoolDiv' + i).innerHTML = '<select name="school' + i + '" id="school' + i + '" class="selectChildSchool"><option value="">Choose School -</option></select>';
                    return;
                }
                var paremtState = $('userState').value;
                var parentCity = $('citySelect').value;
                var url = 'registration2Ajax.page';
                var pars = 'state=' + paremtState;
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
        var url = 'registration2Ajax.page';

        var defaultState = $('userState').value;
        var state = defaultState;
        var overrideStateElem = $('stateSelectChild_' + childNum);
        if (overrideStateElem != undefined) {
            state = overrideStateElem.value;
        }

        var pars = 'state=' + state;

        var defaultCity = $('citySelect').value;
        var city = defaultCity;
        var overrideCityElem = $('citySelectChild_' + childNum);
        if (overrideCityElem != undefined && overrideCityElem.value != '' && eval('hasChildCityState_' + childNum)) {
            city = overrideCityElem.value;
        }

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

function stateChange(stateSelect, childNum) {
    var url = 'registrationAjax.page';
    var pars = 'state=' + stateSelect.value + "&type=city&showNotListed=true";
    $('grade' + childNum).value = '';
    $('schoolDiv' + childNum).innerHTML = '<select name="school' + childNum + '" id="school' + childNum + '" class="selectChildSchool"><option value="">- Choose School -</option></select>';
    $('city').innerHTML = '<select name="city" class="selectCity"><option value="">Loading ...</option></select>';
    var myAjax = new Ajax.Updater(
            'city',
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
    var url = 'registrationAjax.page';
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
    var url = 'registrationAjax.page';
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
    $('citySelectChild_' + childNum).name = "city_" + childNum;
    $('citySelectChild_' + childNum).onchange = function() {
        gradeChange(childNum);
    };

    $('citySelectChild_' + childNum).value = $('citySelect').value;
    $('stateSelectChild_' + childNum).value = $('userState').value;
    $('childCityRow_' + childNum).style.display = '';
    $('changeChildCity_' + childNum).style.display = 'none';

    eval('hasChildCityState_' + childNum + ' = true;');
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