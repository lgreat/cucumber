if (GS === undefined) {
    var GS = {};
}
if (GS.module === undefined) {
    GS.module = {};
}
if (GS.form === undefined) {
    GS.form = {};
}



GS.module.SchoolSelect = function() {
    this._levelCode = undefined;
    this._role = undefined;

    this.onStateChange = function() {
        var state = jQuery('#stateSelect').val();
        this.getCities(state);
    }.gs_bind(this);

    this.getCities = function(state) {
        var url = "/community/registrationAjax.page";

        jQuery('#citySelect').html("<option>Loading...</option>");

        jQuery.getJSON(url, {state:state, format:'json', type:'city'}, this.updateCitySelect);
    }.gs_bind(this);

    this.updateCitySelect = function(data) {
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
    }.gs_bind(this);

    this.onCityChange = function() {
        var state = jQuery('#stateSelect').val();
        var city = jQuery('#citySelect').val();

        this.getSchools(state, city)
    }.gs_bind(this);
    this.getSchools = function(state, city) {
        var params = {state : state, city : city,onchange:'schoolChange(this)',includePrivateSchools :true,chooseSchoolLabel :'Choose a school'};
        jQuery.get('/test/schoolsInCity.page', params, this.updateSchoolSelect);
    }.gs_bind(this);

    this.updateSchoolSelect = function(data) {
        jQuery('#schoolSelect').html("");
        jQuery(data).find('option').each(function () {
            jQuery('#schoolSelect').append("<option value=\"" + jQuery(this).attr('value') + "\">" + jQuery(this).text() + "</option>");
        });
    };
    this.onSchoolChange = function() {
        var schoolId = jQuery('#schoolSelect').val();
        var state = jQuery('#stateSelect').val();
        this.getSchool(state, schoolId);
    }.gs_bind(this);
    this.getSchool = function(state, school) {
        if (state != '' && school !== '') {
            var url = '/school/schoolForParentReview.page';
            var params = {schoolId : school, state : jQuery('#stateSelect').val()};
            jQuery.getJSON(url, params, this.updatePageWithSchool);
        }
    }.gs_bind(this);
    this.updatePageWithSchool = function(data) {

        var id = data.id;
        var name = data.name;
        var street1 = data.street1;
        var street2 = data.street2;
        var cityStateZip = data.cityStateZip;
        var county = data.county;
        var greatSchoolsRating = data.greatSchoolsRating;
        var communityRating = data.communityRating;
        var numberOfCommunityRatings = data.numberOfCommunityRatings;
        var espLoginUrl = data.espLoginUrl;

        var schoolNameElement = jQuery('#schoolNameHeader');
        var street1Element = jQuery('#schoolAddressLine1');
        var street2Element = jQuery('#schoolAddressLine2');
        var cityStateZipElement = jQuery('#schoolAddressLine3');
        var countyElement = jQuery('#schoolAddressLine4');

        schoolNameElement.html(name);
        street1Element.html(street1);
        street2Element.html(street2);
        cityStateZipElement.html(cityStateZip);
        if (county !== undefined && county !== '') {
            countyElement.html(county + ' County');
        } else {
            countyElement.html("");
        }

        jQuery('#gs-rating-badge').attr("class","img sprite badge_sm_" + greatSchoolsRating);
        jQuery('#community-rating-badge').attr("class","img sprite stars_sm_" + communityRating);
        if (numberOfCommunityRatings !== undefined) {
            jQuery('#number-of-ratings').html("Based on " + numberOfCommunityRatings + " ratings");
        } else {
            jQuery('#number-of-ratings').html("Be the first to rate!");
        }

        this._levelCode = data.levelCode;
        this.updateAdditionalRatings(this._levelCode, this._role);
    }.gs_bind(this);

    this.onRoleChange = function() {
        this._role = jQuery('#userRoleSelect').val();
        this.updateAdditionalRatings(this._levelCode, this._role);
    }.gs_bind(this);

    this.updateAdditionalRatings = function(levelCode, role) {
        if (role === undefined || role === "" || levelCode === undefined || levelCode === "") {
            return;  //EARLY EXIT
        }

        clearRatings('principalAsString');
        clearRatings('teacherAsString');
        clearRatings('parentAsString');
        clearRatings('pFacilitiesAsString');

        jQuery('#principalsLink').hide();

        jQuery('#addParentReviewForm [name="userRoleAsString"]').val(jQuery('#userRoleSelect').val());

        switch (true) {
            case (role === 'parent' && levelCode === 'p'):
                jQuery('#additionalStarRatingsTitle').show();
                jQuery('#additionalStarRatingTitle').hide();
                jQuery('#teacherStars').show();
                jQuery('#principalStars').hide();
                jQuery('#parentStars').show();
                jQuery('#facilitiesStars').show();
                jQuery('#additionalStarRatingsExplained').show();
                break;
            case (role === 'parent'):
                jQuery('#additionalStarRatingsTitle').show();
                jQuery('#additionalStarRatingTitle').hide();
                jQuery('#teacherStars').show();
                jQuery('#principalStars').show();
                jQuery('#parentStars').show();
                jQuery('#facilitiesStars').hide();
                jQuery('#additionalStarRatingsExplained').show();
                break;
            case (role === 'student' && levelCode.indexOf('h') !== -1):
                jQuery('#additionalStarRatingsTitle').hide();
                jQuery('#additionalStarRatingTitle').show();
                jQuery('#teacherStars').show();
                jQuery('#principalStars').hide();
                jQuery('#parentStars').hide();
                jQuery('#facilitiesStars').hide();
                jQuery('#additionalStarRatingsExplained').show();
                break;
            case (role === 'student'):
                jQuery('#additionalStarRatingsTitle').hide();
                jQuery('#additionalStarRatingTitle').hide();
                jQuery('#teacherStars').hide();
                jQuery('#principalStars').hide();
                jQuery('#parentStars').hide();
                jQuery('#facilitiesStars').hide();
                jQuery('#additionalStarRatingsExplained').hide();
                break;
            case (role === 'teacher'):
                jQuery('#additionalStarRatingsTitle').hide();
                jQuery('#additionalStarRatingTitle').hide();
                jQuery('#teacherStars').hide();
                jQuery('#principalStars').hide();
                jQuery('#parentStars').hide();
                jQuery('#facilitiesStars').hide();
                jQuery('#additionalStarRatingsExplained').hide();
                break;
            case (role === 'other'):
                jQuery('#additionalStarRatingsTitle').hide();
                jQuery('#additionalStarRatingTitle').hide();
                jQuery('#teacherStars').hide();
                jQuery('#principalStars').hide();
                jQuery('#parentStars').hide();
                jQuery('#facilitiesStars').hide();
                jQuery('#additionalStarRatingsExplained').hide();
                break;
            default:
                alert("A valid user role was not set.");
        }
    };


    this.attachEventHandlers = function() {
        jQuery('#stateSelect').change(this.onStateChange);
        jQuery('#citySelect').change(this.onCityChange);
        jQuery('#schoolSelect').change(this.onSchoolChange);
        jQuery('#userRoleSelect').change(this.onRoleChange);
    };

    this.attachEventHandlers();

};

jQuery(function() {
    GS.module.schoolSelect = new GS.module.SchoolSelect();
});


jQuery(function() {

    jQuery('#parentReviewEmail').blur(validateEmailAjax);

    jQuery("#parentReviewTerms").click(function() {
       if (jQuery("#parentReviewTerms").attr("checked")) {
           jQuery("#terms-error").hide();
       } else {
           jQuery("#terms-error").show();
       }
    });
});





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

var validateEmailAjax = function() {
    var email = jQuery('#parentReviewEmail').val();
    jQuery.getJSON('/community/registrationValidationAjax.page', {email:email, field:'email'}, function(data) {
        if (data && data['email']) {
            jQuery('#email-error .bd').html(data['email']);
            jQuery('#email-error').show();
            jQuery('#email-error .bd a.launchSignInHover').click(function() {
                GSType.hover.joinHover.showSignin();
                return false;
            });
        } else {
            jQuery('#email-error').hide();
        }
    });
}

function validateReview() {
    var noError = true;
    var height = 360;
    var terms = jQuery('#terms');

    if (!starSelected || ((jQuery('#reviewText').val() == '') || (jQuery('#reviewText').val() == 'Enter your review here'))) {
        jQuery('#reviewRatingError').show();
        jQuery('#parentRating').css('height', height + 28 + 'px');
        jQuery('#categoryRatings').css('height' + 28 + 'px');
        height = height + 29;
        noError = false;
    } else {
        jQuery('#reviewRatingError').hide();
    }

    if (jQuery('#addParentReviewForm #reviewText').val().length > 1200) {
        noError = false;
        alert("Please keep your comments to 1200 characters or less.")
    }

    if (GS_countWords(document.getElementById('reviewText')) < 15) {
        noError = false;
        alert("Please use at least 15 words in your comment.")
    }

    if (!terms.attr("checked")) {
        alert("Please accept our Terms of User to join GreatSchools.");
        return false;
    }

    if (!GS.isSignedIn()) {
        var email = jQuery('#parentReviewEmail').val();
        if (email == '') {
            jQuery('#email-error').show();
        } else {
            jQuery.getJSON('/community/registrationValidationAjax.page', {email:email, field:'email'}, function(data) {
                if (data && data['email']) {
                    jQuery('#email-error .bd').html(data['email']);
                    jQuery('#email-error').show();
                    jQuery('#email-error .bd a.launchSignInHover').click(function() {
                        GSType.hover.joinHover.showSignin();
                        return false;
                    });
                    noError = false;

                } else {
                    jQuery('#email-error').hide();

                    if (noError) {
                        GS_postSchoolReview();
                    }
                }

                if (!noError) {
                    jQuery('#reviewAndGuidlines').css('marginTop','8px');
                } else {
                    if (jQuery('#reviewText').val() == 'Enter your review here') {
                        jQuery('#reviewText').val("");
                    }
                }
            });
        }
    } else {
        if (!noError) {
            jQuery('#reviewAndGuidlines').css('marginTop','8px');
        } else {
            if (jQuery('#reviewText').val() == 'Enter your review here') {
                jQuery('#reviewText').val("");
            }
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

    if ((jQuery('#stateSelect').val() == 0) || (jQuery('#stateSelect').val() == 'Choose a state')) {
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

    if (jQuery('#selections [name="userRoleAsString"]').val() == '') {
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
        jQuery('#parentReviewEmail').val(email);
    }

    //clear submit fields

    // then post the review
    jQuery.post('/school/review/postReview.page', jQuery('#addParentReviewForm').serialize(), function(data) {
        if (data.showHover != undefined && data.showHover == "validateEmailSchoolReview") {
            subCookie.setObjectProperty("site_pref", "showHover", "validateEmailSchoolReview", 3);
        } else {
            if (data.reviewPosted != undefined) {
                if (data.reviewPosted == "true") {
                    // cookie to show schoolReviewPostedThankYou hover
                    subCookie.setObjectProperty("site_pref", "showHover", "schoolReviewPostedThankYou", 3);
                } else {
                    // cookie to show schoolReviewNotPostedThankYou hover
                    subCookie.setObjectProperty("site_pref", "showHover", "schoolReviewNotPostedThankYou", 3);
                }
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

