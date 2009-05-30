function scrollToError() {
    window.location.href = document.location.pathname + '#error';
}

function gradeChange(childNum) {
    var grade = $('grade' + childNum).value;

    if (grade == '') {
        $('schoolDiv' + childNum).innerHTML = '<select name="school' + childNum + '" id="school' + childNum + '" class="selectChildSchool"><option value="">- Choose School -</option></select>';
        return;
    }

    var url = 'registration2Ajax.page';
    var defaultState = $('userState').value;
    var state = defaultState;

    var overrideStateElem = $('state' + childNum);
    if (overrideStateElem != undefined) {
        state = overrideStateElem.value;
    }
    var pars = 'state=' + state;

    var defaultCity = $('citySelect').value;
    var city = defaultCity;
    var overrideCityElem = $('city' + childNum);
    if (overrideCityElem != undefined) {
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

function changeChildCity(){
$('citySelect_1').innerHTML = $('citySelect').innerHTML;
    $('childCityRow_1').style.display = '';
}