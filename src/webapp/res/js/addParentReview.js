function loadCities() {
    var state = jQuery('#userState').val();
    var url = "/community/registrationAjax.page";

    jQuery('#citySelect').html("<option>Loading...</option>");

    jQuery.getJSON(url, {state:state, format:'json', type:'city'}, parseCities);
}

function parseCities(data) {
    var citySelect = jQuery('#citySelect');
    if (data.cities) {
        citySelect.empty();
        for (var x = 0; x < data.cities.length; x++) {
            var city = data.cities[x];
            if (city.name) {
                citySelect.append("<option value=\"" + city.name + "\">" + city.name + "</option>");
            }
        }
    }
}

function loadSchools() {
    var params = {state : jQuery('#userState').val(), city : jQuery('#citySelect').val(),onchange:'schoolChange(this)',includePrivateSchools :true,chooseSchoolLabel :'Choose a school'}
    jQuery.get('/test/schoolsInCity.page', params, parseSchools);
}

function parseSchools(data) {
    jQuery('#schoolSelect').html("");
    jQuery(data).find('option').each(function () {
        jQuery('#schoolSelect').append("<option value=\"" + jQuery(this).attr('value') + "\">" + jQuery(this).text() + "</option>");
    });
}


jQuery(function() {
    jQuery('#userState').change(loadCities);
    jQuery('#citySelect').change(loadSchools);
    jQuery('#schoolSelect').change(schoolChange);
    
    jQuery('#posterDropdown').change(function() {

        clearRatings('principalAsString');
        clearRatings('teacherAsString');
        clearRatings('parentAsString');
        clearRatings('pFacilitiesAsString');

        jQuery('#principalsLink').hide();

        jQuery('#addParentReviewForm [name="posterAsString"]').val(jQuery('#posterDropdown').val());

        if (this.value == 'parent') {
            jQuery('.principalOrFacilityStars').show();
            jQuery('#teacherStars').show();
            jQuery('#parentStars').show();
            jQuery('#learnMoreLinks').show();
            jQuery('#categoryRatings p').show();
            jQuery('#principalsLink').show();
        } else if (this.value == 'student') {
            jQuery('.principalOrFacilityStars').hide();
            jQuery('#teacherStars').show();
            jQuery('#parentStars').hide();
            jQuery('#learnMoreLinks').show();
            jQuery('#categoryRatings p').show();
            jQuery('#principalsLink').hide();
        } else {
            jQuery('.principalOrFacilityStars').hide();
            jQuery('#teacherStars').hide();
            jQuery('#parentStars').hide();
            jQuery('#learnMoreLinks').hide();
            jQuery('#categoryRatings p').hide();
            jQuery('#principalsLink').show();
        }
    });
});

function schoolChange() {
    var school = jQuery('#schoolSelect').val();

    if (school != '') {
        var url = '/school/schoolForParentReview.page';
        var params = {schoolId : school, state : jQuery('#userState').val()};
        jQuery.get(url, params, showResponse);
    }
}

function showResponse(x) {

    setDisplay('');

    jQuery('#principalAsString').value = '';
    jQuery('#teacherAsString').value = '';
    //jQuery('#activitiesAsString').value = '';
    jQuery('#parentAsString').value = '';
    //jQuery('#safetyAsString').value = '';
    //jQuery('#PProgramAsString').value = '';
    jQuery('#pFacilitiesAsString').value = '';
    //jQuery('#PSafetyPreschoolAsString').value = '';
    //jQuery('#PTeachersPreschoolAsString').value = '';
    //jQuery('#PParentsPreschoolAsString').value = '';

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
    jQuery('#schoolAddress').css("padding-left", "0px");

    var schoolInfoArray = x.split(";");

    var schoolName = schoolInfoArray[0];
    var gsSchoolRating = schoolInfoArray[1];
    var parentRating = schoolInfoArray[2];
    var reviewCount = schoolInfoArray[3];
    var schoolLevelCode = schoolInfoArray[13];
    var isHighSchoolOnly = (x.indexOf('showStudent') != -1);

    jQuery('#schoolNameHeader').html(schoolName);

    jQuery('#gsSchoolRating').children().remove();

    if (gsSchoolRating != "" && isRatingInfoPresent && gsSchoolRating > 0) {
        var image = document.createElement('img');
        image.setAttribute('alt', 'GreatSchools Rating: ' + gsSchoolRating + ' out of 10. Greatschools Ratings are based on test results. 10 is best.');
        image.setAttribute('src', '/res/img/school/ratings/ratings_gs_head_' + gsSchoolRating + '.gif');
        image.setAttribute('class', 'rating_gs');
        jQuery('#gsSchoolRating').append(image);
        jQuery('#gsSchoolRating').show();
    } else {
        jQuery('#gsSchoolRating').hide();
    }

    if (parentRating != "" && parentRating > 0) {
        // TODO-10623 - Parent Rating in alt text cannot check db property
        // TODO-10623 - Parent Rating word in image must be changed in image
        jQuery('#overallParentRating').html('<img class="sm_stars" alt="Parent Rating: ' + parentRating + ' out of 5 stars" src="/res/img/school/ratings/ratings_parent_head_' + parentRating + '.gif"/>');
        jQuery('#overallParentRating').show();
        if (reviewCount != "" && reviewCount > 0) {
            jQuery('#parentRatingCount').html('Based on ' + reviewCount + ' rating' + (reviewCount > 1 ? 's' : ''));
            jQuery('#parentRatingCount').show();
        } else {
            jQuery('#parentRatingCount').hide();
        }
    } else {
        jQuery('#overallParentRating').hide();
        jQuery('#parentRatingCount').hide();
        if (gsSchoolRating != "" && isRatingInfoPresent && gsSchoolRating > 0) {
            jQuery('#schoolAddress').css("padding-left", "50px");
        }
    }

    if (schoolInfoArray[4] != "" && schoolInfoArray[5] == "") {
        jQuery('#schoolAddressLine1').html(schoolInfoArray[4]);
        jQuery('#schoolAddressLine3').html(schoolInfoArray[6] + ',' + schoolInfoArray[7] + ' ' + schoolInfoArray[8]);
        jQuery('#schoolAddressLine4').html(schoolInfoArray[9] + ' ' + 'county');
    }
    else if (schoolInfoArray[4] != "" && schoolInfoArray[5] != "") {
        jQuery('#schoolAddressLine1').html(schoolInfoArray[4]);
        jQuery('#schoolAddressLine2').html(schoolInfoArray[4]);
        jQuery('#schoolAddressLine3').html(schoolInfoArray[6] + ',' + schoolInfoArray[7] + ' ' + schoolInfoArray[8]);
        jQuery('#schoolAddressLine4').html(schoolInfoArray[9] + ' ' + 'county');
    }
    if (isPublic) {
        //jQuery('#weeklyEmails').innerHTML = '<input id="wantMssNL" type="checkbox" name="wantMssNL" value="yes" checked="checked" class="parentReviewChkBoxes"/> <div>Sign me up for weekly email updates from GreatSchools, including periodic updates about ' + schoolInfoArray[0] + '.</div>';
    } else {
        //jQuery('#weeklyEmails').innerHTML = '<input id="wantMssNL" type="checkbox" name="wantMssNL" value="yes" checked="checked" class="parentReviewChkBoxes"/> <div>Sign me up for weekly email updates from GreatSchools.</div>';
    }
    jQuery('#schoolId').val(schoolInfoArray[10]);
    jQuery('#schoolState').val(schoolInfoArray[7]);

    removeChildrenFromNode(jQuery('#principalsLink'));
    var principalsHref = document.createElement('a');
    principalsHref.setAttribute('href', schoolInfoArray[11]);
    principalsHref.appendChild(document.createTextNode("Principals, submit your review here >"));
    jQuery('#principalsLink').append(principalsHref);

    if (isPreschool) {
        jQuery('#principalStars').hide();
        jQuery('#facilityStars').show();
        jQuery('#ratingsExplainedGradeschool').hide();
        jQuery('#ratingsExplainedPreschool').show();
    } else {
        jQuery('#facilityStars').hide();
        jQuery('#principalStars').show();
        jQuery('#ratingsExplainedPreschool').hide();
        jQuery('#ratingsExplainedGradeschool').show();
    }

    var studentOption = '<option value="student">student</option>';

    if (isHighSchoolOnly != undefined && isHighSchoolOnly) {
        if (jQuery('#posterDropdown [value="student"]').length == 0) {
            jQuery('#posterDropdown [value="parent"]').after(studentOption);
        }
    } else {
        jQuery('#posterDropdown [value="student"]').remove();
    }

}

var starSelected = false;
function onLoadCities() {
    loadCities();


    var starHandler = function(event) {
        starSelected = true;
    };

    jQuery('#validateStar1').click(starHandler);
    jQuery('#validateStar2').click(starHandler);
    jQuery('#validateStar3').click(starHandler);
    jQuery('#validateStar4').click(starHandler);
    jQuery('#validateStar5').click(starHandler);

}

function GS_countWords(textField) {
    var text = textField.value;
    var count = 0;
    var a = text.replace(/\n/g, ' ').replace(/\t/g, ' ');
    var z = 0;
    for (; z < a.length; z++) {
        if (a.charAt(z) == ' ' && a.charAt(z - 1) != ' ') {
            count++;
        }
    }
    return count + 1; // # of words is # of spaces + 1
}

function validateReview() {
    var noError = true;
    var height = 360;

    if (!starSelected || ((jQuery('#reviewText').val() == '') || (jQuery('#reviewText').val() == 'Enter your review here'))) {
        jQuery('#reviewRatingError').show();
        jQuery('#parentRating').css('height', height + 28 + 'px');
        jQuery('#categoryRatings').css('height' + 28 + 'px');
        height = height + 29;
        noError = false;
    } else {
        jQuery('#reviewRatingError').hide();
    }

    /*
     if (jQuery('reviewEmail').value == '' || (jQuery('reviewEmail').value == 'Enter your email address') || (validateEmail(jQuery('reviewEmail').value) == false)) {
     jQuery('emailError').style.display = '';
     jQuery('parentRating').style.height = height + 28 + 'px';
     jQuery('categoryRatings').style.height = height + 28 + 'px';
     height = height + 29;
     noError = false;
     } else {
     jQuery('emailError').style.display = 'none';
     }
     */

    /*
     if (!jQuery('permission').checked) {
     jQuery('termsError').style.display = '';
     jQuery('parentRating').style.height = height + 28 + 'px';
     jQuery('categoryRatings').style.height = height + 28 + 'px';
     height = height + 29;
     noError = false;
     } else {
     jQuery('termsError').style.display = 'none';
     }
     */
    if (!noError) {
        jQuery('#reviewAndGuidlines').css('marginTop','8px');
    } else {
        if (jQuery('#reviewText').val() == 'Enter your review here') {
            jQuery('#reviewText').val("");
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
        if (GS.showSchoolReviewHover('/school/parentReviews.page?id=' + jQuery('#schoolId').val() + '&state=' + jQuery('#schoolState').val())) {
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
    jQuery(node).children().remove();
}

function reviewThisSchool() {
    var errMsg = 'Please enter the following fields: ';
    var noError = true;

    if ((jQuery('#userState').val() == 0) || (jQuery('#userState').val() == 'Choose a state')) {
        errMsg = errMsg + '\nPlease select a state.';
        noError = false;
    }

    if ((jQuery('#citySelect').val() == '') || (jQuery('#citySelect').val() == 'Choose a city') || (jQuery('#citySelect').val() == '- Choose city -')) {
        errMsg = errMsg + '\nPlease select a city.';
        noError = false;
    }

    if ((jQuery('#schoolSelect').val() == '') || (jQuery('#schoolSelect').val() == 'Choose a school')) {
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
        jQuery('#addParentReviewForm').show();
        jQuery('#reviewThisSchoolButton').hide();
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
        var successEvents = "";
        if (data.ratingEvent != undefined) {
            successEvents += data.ratingEvent;
        }
        if (data.reviewEvent != undefined) {
            successEvents += data.reviewEvent;
        }
        if (successEvents != "") {
            pageTracking.clear();
            pageTracking.successEvents = successEvents;
            pageTracking.send();
        }
        if (callerFormId) {
            GSType.hover.signInHover.hide();
            GSType.hover.joinHover.hide();
            jQuery('#' + callerFormId).submit();
        } else {
            window.location.href = '/school/parentReviews.page?id=' + jQuery('#schoolId').val() + '&state=' + jQuery('#schoolState').val();
        }
    }, "json");
}

