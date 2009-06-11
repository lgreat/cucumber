function doSomething(){
    var elem = document.getElementById("mynth");
    if(document.manage.greatnews.checked){
        elem.style.display = "block";
    } else{
        elem.style.display = "none";
    }
}

function doSomethingSeasonal(){
    var elem = document.getElementById("weeks");
    if(document.manage.seasonal.checked){
        elem.style.display = "block";
    } else{
        elem.style.display = "none";
    }
}

function checkForm(){
    if(document.manage.seasonal.checked && document.manage.startweek.value == ""){
        alert("Please select a start week for your Summer Brain Drain newsletter so we can personalize your emails.");
        return false;
    }
    return true;
}

function emailStateChange2(stateSelect) {
    var elem = document.getElementById("city");
    elem.innerHTML = '<select name="city" class="selectCity"><option value="">Loading ...</option></select>';
}

function emailStateChange(stateSelect) {
    //var url = '/community/registrationAjax.page';
    var url = '/util/ajax/ajaxCity.page';
    var pars = 'state=' + stateSelect.value + "&type=city&showNotListed=true";
    $('city').innerHTML = '<select name="city" class="selectCity"><option value="">Loading ...</option></select>';
    var myAjax = new Ajax.Updater(
            'city',
            url,
    {
        method: 'get',
        parameters: pars
    });
}
/*
 schoolDiv${child} -> schoolsInCity
 school${child} -> schoolAdd
 selectChildSchool -> selectSchool
 userCmd.studentRows[child-1].schools -> emailCmd.schoolsToAdd
 forget schoolVal
 */

//create a util/schoolAjaxController
//create a util/cityAjaxController
function emailSchoolChange(citySelect) {
    var parentState = $('userState').value;
    var parentCity = $('citySelect').value;
    //var url = '/community/registration2Ajax.page';
    var url = '/util/ajax/ajaxCity.page';
    var pars = 'state=' + parentState;
    pars += '&city=' + parentCity;
    $('schoolsInCity').innerHTML = '<select name="school" id="school" class="selectChildSchool"><option value="">Loading ...</option></select>';
    var myAjax = new Ajax.Updater(
            'schoolsInCity',
            url,
    {
        method: 'get',
        parameters: pars
    });

}
