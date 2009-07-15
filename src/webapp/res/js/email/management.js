function toggleNthGraderNewsletters(){
    var elem = document.getElementById("mynth");
    if(document.manage.greatnews.checked){
        elem.style.display = "block";
    } else{
        elem.style.display = "none";
    }
}

function toggleSummerBrainDrain(){
    var elem = document.getElementById("weeks");
    if(document.manage.seasonal.checked){
        elem.style.display = "block";
    } else{
        elem.style.display = "none";
    }
}

function checkForm(){
    if(document.manage.seasonal.checked && document.manage.startweek.value == ""){
        alert("Please select a start week for your school so we can personalize your emails.");
        return false;
    }
    return true;
}

function emailStateChange(stateSelect) {
    var url = '/util/ajax/ajaxCity.page';
    var pars = 'state=' + stateSelect.value + "&type=city&notListedOption=2. Choose city";
    var cityDiv = $('city');
    var citySelect = $('citySelect');
    $('school').update('<option value="0">3. Choose school</option>');
    Element.remove(citySelect);
    cityDiv.update("<span>Loading...</span>");
    new Ajax.Updater(
            'citySelect',
            url,
    {
        method: 'get',
        parameters: pars,
        onComplete: emailUpdateElementContents('city', citySelect)
    });
}

function emailUpdateElementContents(elemIdToUpdate, elemToAdd) {
    $(elemIdToUpdate).update(elemToAdd);
    elemToAdd.show();
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
    var url = '/util/ajax/ajaxCity.page';
    var pars = 'state=' + parentState;
    pars += '&city=' + parentCity;
    pars += '&notListedOption=3. Choose school';
    var schoolDiv = $('schoolsInCity');
    var schoolSelect = $('school');
    Element.remove(schoolSelect);
    schoolDiv.update("<span>Loading...</span>");
    new Ajax.Updater(
            'school',
            url,
    {
        method: 'get',
        parameters: pars,
        onComplete: emailUpdateElementContents('schoolsInCity', schoolSelect)
    });

}
