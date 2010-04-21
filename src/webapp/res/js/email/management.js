jQuery(function() {
    jQuery('#firstName').blur(function() {
        if (isValidFirstName(jQuery(this).val())) {
            jQuery('#firstNameError').hide();            
        } else {
            jQuery('#firstNameError').show();
        }
    });
});

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

function isValidFirstName(firstName) {
    return (firstName.length >= 2 && firstName.length <= 24);
}

function checkForm(){
    if (!isValidFirstName(jQuery('#firstName').val())) {
        alert('First name must be 2-24 characters.');
        return false;
    }
    if(document.manage.seasonal.checked && document.manage.startweek.value == ""){
        alert("Please select a start week for your school so we can personalize your emails.");
        return false;
    }
    return true;
}

function emailStateChange(stateSelect) {
    var url = '/util/ajax/ajaxCity.page';
    var pars = 'state=' + stateSelect.value + "&type=city&notListedOption=2. Choose city";
    var citySelect = $('citySelect');
    $('school').update('<option value="0">3. Choose school</option>');
    citySelect.update('<option value="0">Loading ...</option>');
    new Ajax.Updater(
            'citySelect',
            url,
    {
        method: 'get',
        parameters: pars,
        onComplete: function() {
            emailUpdateElementContents('city', citySelect);
        }
    });
}

function emailUpdateElementContents(elemIdToUpdate, elemToAdd) {
    Element.remove(elemToAdd);
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

function emailCityChange(citySelect) {
    var parentState = $('userState').value;
    var parentCity = citySelect.value;
    var url = '/util/ajax/ajaxCity.page';
    var pars = 'state=' + parentState;
    pars += '&city=' + parentCity;
    pars += '&notListedOption=3. Choose school';
    var schoolSelect = $('school');
    schoolSelect.update('<option value="0">Loading ...</option>');
    new Ajax.Updater(
            'school',
            url,
    {
        method: 'get',
        parameters: pars,
        onComplete: function() {
            emailUpdateElementContents('schoolsInCity', schoolSelect);
        }
    });
}

// don't allow duplicates
// don't allow more than 4
// don't allow invalid options
function addMssSchool() {
    // validate numSchools
    var numMssSchools = parseInt($('numMssSchools').value);
    if (numMssSchools == 4) {
        alert('You can track a maximum of four schools.');
        $('mssAddSchoolSection').hide(); // how'd they get here? Better hide this control
        return;
    }
    var schoolSelect = $('school');
    // validate valid options
    if (parseInt(schoolSelect.value) < 1) {
        alert('Please select a school.');
        return;
    }
    // pull some data
    var selectedSchoolName = schoolSelect.options[schoolSelect.selectedIndex].text;
    var citySelect = $('citySelect');
    var selectedCityName = citySelect.options[citySelect.selectedIndex].text;
    var stateSelect = $('userState');
    var selectedStateName = stateSelect.options[stateSelect.selectedIndex].text;
    var myStateId = selectedStateName + schoolSelect.value;

    // validate duplicates
    var form = $('emailCmd');
    var uniqueStateIds = form.getInputs('hidden', 'uniqueStateId');
    for (var x=0; x < uniqueStateIds.length; x++) {
        var uniqueStateId = uniqueStateIds[x];
        if (uniqueStateId.value == myStateId) {
            alert("You are already subscribed to that school.");
            return;
        }
    }

    numMssSchools = numMssSchools + 1;

    // construct checkbox
    // have to use defautChecked for IE, as it doesn't respect the value of checked before element is in dom
    var schoolCheckbox = new Element("input", {'type':'checkbox', 'checked':'checked',
        'defaultChecked':'true', 'class':'newsletterCheckbox',
        'id':'mssSchool' + numMssSchools, 'onclick':'removeMssSchool(' + numMssSchools + ');'});
    // construct label
    var schoolNameLabel = new Element("label", {'for':'mssSchool' + numMssSchools});
    schoolNameLabel.update(selectedSchoolName + ', ' + selectedCityName + ', ' + selectedStateName);
    // construct hidden input
    var uniqueHiddenValue = new Element("input", {'type':'hidden', 'name':'uniqueStateId',
        'value':myStateId});
    // construct containing div
    var containingDiv = new Element("div", {'class':'mssSchoolLine'});
    containingDiv.insert({bottom: schoolCheckbox});
    containingDiv.insert({bottom: schoolNameLabel});
    containingDiv.insert({bottom: uniqueHiddenValue});
    // insert into dom
    $('mssSchoolSection').insert({bottom: containingDiv});
    // increment numSchools
    $('numMssSchools').value = numMssSchools;
    if (numMssSchools >= 4) {
        $('mssAddSchoolSection').hide();
    }
}

function removeMssSchool(schoolCounter) {
    var schoolCheckbox = $('mssSchool' + schoolCounter);
    Element.extend(schoolCheckbox.parentNode).remove();

    var numMssSchools = parseInt($('numMssSchools').value);
    $('numMssSchools').value = numMssSchools-1;
    $('mssAddSchoolSection').show();
}

