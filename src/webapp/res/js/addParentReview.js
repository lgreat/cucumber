GS = GS || {};
GS.form = GS.form || {};
GS.module = GS.module || {};
GS.validation = GS.validation || {};

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

        if (greatSchoolsRating >= 1 && greatSchoolsRating <= 10) {
            jQuery('#gs-rating-badge').attr("class", "img sprite badge_sm_" + greatSchoolsRating);
        } else {
            jQuery('#gs-rating-badge').attr("class", "img sprite badge_sm_na");
        }

        if (communityRating >= 0 && communityRating <= 5) {
            jQuery('#community-rating-badge').attr("class", "img sprite stars_sm_" + communityRating);
        } else {
            jQuery('#number-of-rating-badge').html("&nbsp;");
        }

        if (numberOfCommunityRatings !== undefined && numberOfCommunityRatings > 0) {
            jQuery('#number-of-ratings').html("Based on " + numberOfCommunityRatings + " ratings");
        } else {
            jQuery('#number-of-ratings').html("Be the first to rate!");
        }

        this._levelCode = data.levelCode;
        this.updateAdditionalStarRatings(this._role, this._levelCode);
    }.gs_bind(this);

    this.onRoleChange = function() {
        this._role = jQuery('#userRoleSelect').val();
        this.updateAdditionalStarRatings(this._role, this._levelCode);
    }.gs_bind(this);

    this.updateAdditionalStarRatings = function(role, levelCode) {
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

function validateEmail(elementValue) {
    var emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    return emailPattern.test(elementValue);
}


function GS_postSchoolReview(email, callerFormId) {
    // first, grab the email from the join/signIn form and use that with the review
    if (email) {
        jQuery('#parentReviewEmail').val(email);
    }

    //clear submit fields

    // then post the review
    jQuery.post(GS.uri.Uri.getBaseHostname() + '/school/review/postReview.page', jQuery('#addParentReviewForm').serialize(), function(data) {
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