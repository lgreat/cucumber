function scrollToError() {
    window.location.href = document.location.pathname + '#error';
}

function childSchoolChange(childNum, hideOnly) {
    var notice = $('childMessage' + childNum);
    var citySel;
    if ($('locationOverride_' + childNum).value == 'true') {
        citySel = $('citySelectChild_' + childNum);
    } else {
        citySel = $('citySelect');
    }

    if (citySel.selectedIndex == 0) {
        if (!hideOnly) {
            notice.update('Choose a City.').style.display = '';
            notice.className = "ajaxMessage ajaxError childSchoolError";
        }
    } else {
        notice.update('').style.display = 'none';
        notice.className = "ajaxMessage ajaxSuccess";
    }
}

function makeChildSchoolSelect(childNum, optionText) {
    return '<select name="school' + childNum + '" id="school' + childNum +
           '" class="selectChildSchool" onclick="childSchoolChange(' + childNum + ', false);"><option value="">' +
           optionText + '</option></select>';
}

// TODO: Break out into two methods: parentGradeChange and childGradeChange
function gradeChange(childNum, isChangeParentCity) {
    if (isChangeParentCity) {
        //when parent city is changed the childnum = looptimes
        for (var i = 1; i <= childNum; i++) {
            if ($('locationOverride_' + i).value != 'true') {
                var grade = $('grade' + i).value;
                if (grade == '') {
                    $('schoolDiv' + i).innerHTML = makeChildSchoolSelect(i, "- Choose School -");
                    childSchoolChange(i, true);
                    return;
                }
                var parentState = $('userState').value;
                var parentCity = $('citySelect').value;
                var url = '/community/registration2Ajax.page';
                var pars = 'state=' + parentState;
                pars += '&city=' + parentCity;
                pars += '&grade=' + grade;
                pars += '&childNum=' + i;
                pars += '&onclick=childSchoolChange(' + i + ');';
                $('schoolDiv' + i).innerHTML = makeChildSchoolSelect(i, "Loading ...");
                var myAjax = new Ajax.Updater(
                        'schoolDiv' + i,
                        url,
                {
                    method: 'get',
                    parameters: pars
                });
                childSchoolChange(i, true);
            }
        }
    } else {
        var grade = $('grade' + childNum).value;
        if (grade == '') {
            $('schoolDiv' + childNum).innerHTML = makeChildSchoolSelect(childNum, "- Choose School -");
            childSchoolChange(childNum, true);
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
        pars += '&onclick=childSchoolChange(' + childNum + ');';
        $('schoolDiv' + childNum).innerHTML = makeChildSchoolSelect(childNum, "Loading ...");
        var myAjax = new Ajax.Updater(
                'schoolDiv' + childNum,
                url,
        {
            method: 'get',
            parameters: pars
        });
        childSchoolChange(childNum, true);
    }
}

function parentStateChange(stateSelect, numChildren) {
    var url = '/community/registrationAjax.page';
    var citySpan = $('city');
    var citySelect = citySpan.getElementsByTagName('select')[0];

    var pars = 'state=' + stateSelect.value + "&type=city&showNotListed=true" + "&citySelectName=" + citySelect.name;
    pars += "&onchange=gradeChange(" + numChildren + ",true);";
    if (citySelect.id != '') {
        pars += "&citySelectId=" + citySelect.id;
    }
    
    for (var i = 1; i <= numChildren; i++) {
        if ($('locationOverride_' + i).value != 'true') {
            // if child's location is not overridden, clear out his school because parent has changed state
            $('schoolDiv' + i).innerHTML = makeChildSchoolSelect(i, "- Choose School -");
        }
    }
    citySpan.innerHTML = '<select name="city" class="selectCity"><option value="">Loading ...</option></select>';
    var myAjax = new Ajax.Updater(
            'city',
            url,
    {
        method: 'get',
        parameters: pars
    });
}

function childStateChange(stateSelect, childNum) {
    var state = stateSelect.value;
    if (state == '') {
        $('schoolDiv' + childNum).innerHTML = makeChildSchoolSelect(childNum, "- Choose School -");
        childSchoolChange(childNum, true);
    }

    var url = '/community/registrationAjax.page';
    var pars = 'state=' + state + "&type=city&showNotListed=true";
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
    var url = '/community/registrationAjax.page';
    var pars = 'fn=' + $('firstName').value;
    var notice = $('firstNameMessage');
    var myAjax = new Ajax.Request(
            url,
        {
            method: 'get',
            parameters: pars,
            onSuccess: function(transport) {
                if (transport.responseText == "invalid_length") {
                    notice.update('First name must be 2-24 characters long.').style.display = '';
                    notice.className = "ajaxMessage ajaxError";
                } else if (transport.responseText == "invalid_chars") {
                    notice.update('Please remove the numbers or symbols.').style.display = '';
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
                notice.update('This username is already in use.').style.display = '';
                notice.className = "ajaxMessage ajaxError";
            } else if (transport.responseText == "invalid") {
                notice.update('Username must be 6-14 characters.').style.display = '';
                notice.className = "ajaxMessage ajaxError";
            } else if (transport.responseText == "invalidchars") {
                notice.update('Username may only contain letters and numbers.').style.display = '';
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

function validateAndSubmit(element){
//    if(document.getElementById("brainDrainNewsletterField").checked && document.getElementById("startweek").value == ''){
//             alert("Please select a start week for your school so we can personalize your emails.");
//             return false;
//      }else{
        doSingleClick(element);
//    }
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
}

function css_browser_selector(u){var ua = u.toLowerCase(),is=function(t){return ua.indexOf(t)>-1;},g='gecko',w='webkit',s='safari',h=document.getElementsByTagName('html')[0],b=[(!(/opera|webtv/i.test(ua))&&/msie\s(\d)/.test(ua))?('ie ie'+RegExp.$1):is('firefox/2')?g+' ff2':is('firefox/3')?g+' ff3':is('gecko/')?g:/opera(\s|\/)(\d+)/.test(ua)?'opera opera'+RegExp.$2:is('konqueror')?'konqueror':is('chrome')?w+' chrome':is('applewebkit/')?w+' '+s+(/version\/(\d+)/.test(ua)?' '+s+RegExp.$1:''):is('mozilla/')?g:'',is('j2me')?'mobile':is('iphone')?'iphone':is('ipod')?'ipod':is('mac')?'mac':is('darwin')?'mac':is('webtv')?'webtv':is('win')?'win':is('freebsd')?'freebsd':(is('x11')||is('linux'))?'linux':'','js']; c = b.join(' '); h.className += ' '+c; return c;}
css_browser_selector(navigator.userAgent);


function displayStartWeekDropdowns(){
     var elem = document.getElementById("weeks");
    if(document.getElementById("brainDrainNewsletterField").checked){
        elem.style.display = "block";
    } else{
        elem.style.display = "none";
    }
}