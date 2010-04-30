function cityChange(citySelect, selectId) {
    $(selectId).innerHTML = '<select name="city" class="selectCity"><option value="">Loading ...</option></select>';
    new Ajax.Updater(selectId, '/test/schoolsInCity.page', {
        method: 'get',
        parameters: {state : $('userState').value, city : citySelect.value,onchange:'schoolChange(this)',includePrivateSchools :true,chooseSchoolLabel :'Choose a school'},
        onComplete: function(transport) {
            if (transport.responseText.lastIndexOf("option") < 125) {
                alert('No schools found in this city. Please choose again.');
            }
        }
    });
}

function stateChange(stateSelect) {
    if(stateSelect.value != ''){
        var url = '/community/registrationAjax.page';
    var pars = 'state=' + stateSelect.value + "&type=city&showNotListed=false&onchange=cityChange(this,'schools')";
    $('schools').innerHTML = '<select id="schoolSelect" name="sid" class="selectSchool"><option value="Choose a school">Choose a school</option></select>';

    $('city').innerHTML = '<select name="city" class="selectCity"><option value="">Loading ...</option></select>';
    new Ajax.Updater(
            'city',
            url,
    {
        method: 'get',
        parameters: pars,
        onComplete: function(transport) {
        }
    });

    }else{
         $('city').innerHTML = '<select name="city"  id="citySelect" class="selectCity"><option value="Choose a city">Choose a city</option></select>';
    }

}

jQuery(function() {
   jQuery('[name="posterAsString"]').change(function() {
        if (this.value == 'parent') {
            jQuery('#principalOrFacilityStars').show();
            jQuery('#teacherStars').show();
            jQuery('#parentStars').show();
            jQuery('#learnMoreLinks').show();
            jQuery('#categoryRatings p').show();
        } else if (this.value == 'student') {
            jQuery('#principalOrFacilityStars').hide();
            jQuery('#teacherStars').show();
            jQuery('#parentStars').hide();
            jQuery('#learnMoreLinks').show();
            jQuery('#categoryRatings p').show();
        } else {
            jQuery('#principalOrFacilityStars').hide();
            jQuery('#teacherStars').hide();
            jQuery('#parentStars').hide();
            jQuery('#learnMoreLinks').hide();
            jQuery('#categoryRatings p').hide();
        }
    }); 
});

function schoolChange(school) {
    var schoolObj = school;
    if (school.value != '') {
        var url = '/school/schoolForParentReview.page';
        var params = {schoolId : school.value, state : $('userState').value};
        jQuery.get(url, params, showResponse);
    }
}

function showResponse(x) {

    setDisplay('');

    $('principalAsString').value = '';
    $('teacherAsString').value = '';
    //$('activitiesAsString').value = '';
    $('parentAsString').value = '';
    //$('safetyAsString').value = '';
    //$('PProgramAsString').value = '';
    $('pFacilitiesAsString').value = '';
    //$('PSafetyPreschoolAsString').value = '';
    //$('PTeachersPreschoolAsString').value = '';
    //$('PParentsPreschoolAsString').value = '';

    clearRatings('principalAsString');
    clearRatings('teacherAsString');
    //clearRatings('activitiesAsString');
    clearRatings('parentAsString');
    //clearRatings('safetyAsString');
    //clearRatings('PProgramAsString');
    clearRatings('pFacilitiesAsString');
    //clearRatings('PSafetyPreschoolAsString');
    //clearRatings('PTeachersPreschoolAsString');
    //clearRatings('PParentsPreschoolAsString');

    var isRatingInfoPresent = (x.indexOf('noRatingInfo') == -1);
    var isPreschool = (x.indexOf('isPreschool') != -1);
    var isPublic = (x.indexOf('isPublic') != -1);
    $('schoolAddress').style.paddingLeft = '0px';
    var schoolInfoArray = x.split(";");
    $('schoolNameHeader').innerHTML = schoolInfoArray[0];

    if ($('gsSchoolRating').childNodes.length > 0) {
        removeChildrenFromNode($('gsSchoolRating'));
    }

    if (schoolInfoArray[1] != "" && isRatingInfoPresent && schoolInfoArray[1] > 0) {
        var image = document.createElement('img');
        image.setAttribute('alt', 'GreatSchools Rating: ' + schoolInfoArray[1] + ' out of 10. Greatschools Ratings are based on test results. 10 is best.');
        image.setAttribute('src', '/res/img/school/ratings/ratings_gs_head_' + schoolInfoArray[1] + '.gif');
        image.setAttribute('class', 'rating_gs');
        $('gsSchoolRating').appendChild(image);
        $('gsSchoolRating').style.display = '';
    } else {
        $('gsSchoolRating').style.display = 'none';
    }

    if (schoolInfoArray[2] != "" && schoolInfoArray[2] > 0) {
        $('overallParentRating').innerHTML = '<img class="sm_stars" alt="Parent Rating: ' + schoolInfoArray[2] + ' out of 5 stars" src="/res/img/school/ratings/ratings_parent_head_' + schoolInfoArray[2] + '.gif"/>';
        $('overallParentRating').style.display = '';
        if (schoolInfoArray[3] != "" && schoolInfoArray[3] > 0) {
            $('parentRatingCount').innerHTML = 'Based on ' + schoolInfoArray[3] + ' ratings';
            $('parentRatingCount').style.display = '';
        } else {
            $('parentRatingCount').style.display = 'none';
        }
    } else {
        $('overallParentRating').style.display = 'none';
        $('parentRatingCount').style.display = 'none';
        if (schoolInfoArray[1] != "" && isRatingInfoPresent && schoolInfoArray[1] > 0) {
            $('schoolAddress').style.paddingLeft = '50px';
        }
    }

    if (schoolInfoArray[4] != "" && schoolInfoArray[5] == "") {
        $('schoolAddressLine1').innerHTML = schoolInfoArray[4];
        $('schoolAddressLine3').innerHTML = schoolInfoArray[6] + ',' + schoolInfoArray[7] + ' ' + schoolInfoArray[8];
        $('schoolAddressLine4').innerHTML = schoolInfoArray[9] + ' ' + 'county';
    }
    else if (schoolInfoArray[4] != "" && schoolInfoArray[5] != "") {
        $('schoolAddressLine1').innerHTML = schoolInfoArray[4];
        $('schoolAddressLine2').innerHTML = schoolInfoArray[4];
        $('schoolAddressLine3').innerHTML = schoolInfoArray[6] + ',' + schoolInfoArray[7] + ' ' + schoolInfoArray[8];
        $('schoolAddressLine4').innerHTML = schoolInfoArray[9] + ' ' + 'county';
    }
    if (isPublic) {
        //$('weeklyEmails').innerHTML = '<input id="wantMssNL" type="checkbox" name="wantMssNL" value="yes" checked="checked" class="parentReviewChkBoxes"/> <div>Sign me up for weekly email updates from GreatSchools, including periodic updates about ' + schoolInfoArray[0] + '.</div>';
    } else {
        //$('weeklyEmails').innerHTML = '<input id="wantMssNL" type="checkbox" name="wantMssNL" value="yes" checked="checked" class="parentReviewChkBoxes"/> <div>Sign me up for weekly email updates from GreatSchools.</div>';
    }
    $('schoolId').value = schoolInfoArray[10];
    $('schoolState').value = schoolInfoArray[7];

    removeChildrenFromNode($('principalsLink'));
    var principalsHref = document.createElement('a');
    principalsHref.setAttribute('href', schoolInfoArray[11]);
    principalsHref.appendChild(document.createTextNode("Principals, submit your review here >"));
    $('principalsLink').appendChild(principalsHref);

    if (isPreschool) {

        jQuery('#principalStars').hide();
        jQuery('#facilityStars').show();
        jQuery('#posterDropdown [value=Student]').hide();
    } else {

        jQuery('#facilityStars').hide();
        jQuery('#principalStars').show();
        jQuery('#posterDropdown [value=Student]').show();
    }


}

var starSelected = false;
function onLoadCities() {
    if ($('userState').value != '') {
        var url = '/community/registrationAjax.page';
        var pars = 'state=' + $('userState').value + "&type=city&showNotListed=false&onchange=cityChange(this,'schools')";
        $('city').innerHTML = '<select name="city" class="selectCity"><option value="">Loading ...</option></select>';
        var myAjax = new Ajax.Updater(
                'city',
                url,
        {
            method: 'get',
            parameters: pars,
            onComplete: function(transport) {
            }
        });

    } else {
        $('city').innerHTML = '<select name="city"  id="citySelect" class="selectCity"><option value="Choose a city">Choose a city</option></select>';
    }

    var starHandler = function(event) {
        starSelected = true;
    };

    $('validateStar1').observe('click', starHandler);
    $('validateStar2').observe('click', starHandler);
    $('validateStar3').observe('click', starHandler);
    $('validateStar4').observe('click', starHandler);
    $('validateStar5').observe('click', starHandler);

}

function GS_countWords(textField) {
    var text = textField.value;
    var count = 0;
    var a = text.replace(/\n/g,' ').replace(/\t/g,' ');
    var z = 0;
    for (; z < a.length; z++) {
        if (a.charAt(z) == ' ' && a.charAt(z-1) != ' ') { count++; }
    }
    return count+1; // # of words is # of spaces + 1
}

function validateReview() {
    var noError = true;
    var height = 360;

    if (!starSelected && (($('reviewText').value == '') || ($('reviewText').value == 'Enter your review here'))) {
        $('reviewRatingError').style.display = '';
        $('parentRating').style.height = height + 28 + 'px';
        $('categoryRatings').style.height = height + 28 + 'px';
        height = height + 29;
        noError = false;
    } else {
        $('reviewRatingError').style.display = 'none';
    }

    /*
    if ($('reviewEmail').value == '' || ($('reviewEmail').value == 'Enter your email address') || (validateEmail($('reviewEmail').value) == false)) {
        $('emailError').style.display = '';
        $('parentRating').style.height = height + 28 + 'px';
        $('categoryRatings').style.height = height + 28 + 'px';
        height = height + 29;
        noError = false;
    } else {
        $('emailError').style.display = 'none';
    }
    */

    /*
    if (!$('permission').checked) {
        $('termsError').style.display = '';
        $('parentRating').style.height = height + 28 + 'px';
        $('categoryRatings').style.height = height + 28 + 'px';
        height = height + 29;
        noError = false;
    } else {
        $('termsError').style.display = 'none';
    }
    */
    if (!noError) {
        $('reviewAndGuidlines').style.marginTop = '8px';
    } else {
        if ($('reviewText').value == 'Enter your review here') {
            $('reviewText').value = "";
        }
    }

    if (jQuery('#addParentReviewForm #reviewText').val().length > 1200) {
        noError = false;
        alert("Please keep your comments to 1200 characters or less.")
    }
    if (GS_countWords(document.getElementById('reviewText')) < 15) {
        noError = false;
        alert("Please use at least 15 words in your comment.")
    }

    if (noError) {
        if (GS.showSchoolReviewHover(window.location.href)) {
                GS_postSchoolReview();
        }
    }

    return false;
}

function validateEmail(elementValue) {
    var emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    return emailPattern.test(elementValue);
}


function removeChildrenFromNode(node)
{
    var len = node.childNodes.length;

    while (node.hasChildNodes())
    {
        node.removeChild(node.firstChild);
    }
}

function reviewThisSchool() {
    var errMsg = 'Please enter the following fields: ';
    var noError = true;

    if (($('userState').value == 0) || ($('userState').value == 'Choose a state')) {
        errMsg = errMsg + '\nPlease select a state.';
        noError = false;
    }

    if (($('citySelect').value == '') || ($('citySelect').value == 'Choose a city')) {
        errMsg = errMsg + '\nPlease select a city.';
        noError = false;
    }

    if (($('schoolSelect').value == '') || ($('schoolSelect').value == 'Choose a school')) {
        errMsg = errMsg + '\nPlease select a school.';
        noError = false;
    }

     if (jQuery('#selections [name="posterAsString"]').val() == '') {
            errMsg = errMsg + '\nPlease select your affiliation with the school.';
            noError = false;
        }

    if (!noError) {
        alert(errMsg);
    } else {
        if ($('addParentReviewForm').hasClassName('hide')) {
            $('addParentReviewForm').removeClassName('hide');
            $('addParentReviewForm').addClassName('show');
            $('reviewThisSchoolButton').style.display = 'none';
        }
    }

    return false;
}



function GS_postSchoolReview(email, callerFormId) {
    // first, grab the email from the join/signIn form and use that with the review
    if (email) {
        jQuery('#addParentReviewForm [name="email"]').val(email);
    }

    //clear submit fields

    // then post the review
    jQuery.post('/school/review/postReview.page', jQuery('#addParentReviewForm').serialize(), function(data) {
        if (data.reviewPosted != undefined) {
            if (data.reviewPosted == "true") {
                // cookie to show schoolReviewPostedThankYou hover
                subCookie.setObjectProperty("site_pref", "showHover", "schoolReviewPostedThankYou", 3);
            } else {
                // cookie to show schoolReviewNotPostedThankYou hover
                subCookie.setObjectProperty("site_pref", "showHover", "schoolReviewNotPostedThankYou", 3);
            }
        }
        if (callerFormId) {
            GSType.hover.signInHover.hide();
            GSType.hover.joinHover.hide();
            jQuery('#' + callerFormId).submit();
        } else {
            window.location.href=window.location.href;
            window.location.reload();
        }
    }, "json");
}



/*
function reviewThisSchool() {
    var errMsg = 'Please enter the following fields: ';
    var noError = true;

    if (($('userState').value == 0) || ($('userState').value == 'Choose a state')) {
        jQuery('#selections .stateError').show();
        hasError = true;
    }

    if (($('citySelect').value == '') || ($('citySelect').value == 'Choose a city')) {
        jQuery('#selections .cityError').show();
        hasError = true;
    }

    if (($('schoolSelect').value == '') || ($('schoolSelect').value == 'Choose a school')) {
        jQuery('#selections .schoolError').show();
        hasError = true;
    }

    if (jQuery('#selections [name="posterAsString"]').val() == '') {
            jQuery('#selections .whoError').show();
            hasError = true;
        }

    if (hasError) {
        jQuery('#selections errors').hide();
    } else {
        if ($('addParentReviewForm').hasClassName('hide')) {
            $('addParentReviewForm').removeClassName('hide');
            $('addParentReviewForm').addClassName('show');
            $('reviewThisSchoolButton').style.display = 'none';
        }
    }

    return false;
}
*/