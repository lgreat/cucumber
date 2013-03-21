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
    var self = this; //needed inside of jquery iterator
    this._levelCode = undefined;
    this._role = undefined;

    var becomeInvalidCallback = null;
    var becomeValidCallback = null;

    this.Field = function(id) {
        this.$element = jQuery('#' + id);
        var error = null;
        this.requiredError = "This field is required.";
        this.validate = function() {
            //TODO: override this instead of writing here
            if (this.$element.val() !== '' && this.$element.val() !== '- Choose city -' &&
                    this.$element.val() !== 'Choose a school' && this.$element.val() !== 'My city is not listed') {
                error = null;
            } else {
                error = this.requiredError;
            }
        };
        this.isValid = function() {
            return error === null;
        };
    };

    this.stateSelect = new this.Field('stateSelect');
    this.stateSelect.requiredError = "Please choose a state";

    this.citySelect = new this.Field('citySelect');
    this.citySelect.requiredError = "Please choose a city";
    this.citySelect.reset = function() {
        this.$element.val('- Choose city -');
    };

    this.schoolSelect = new this.Field('schoolSelect');
    this.schoolSelect.requiredError = "Please choose a school";
    this.schoolSelect.reset = function() {
        this.$element.val('Choose a school');
    };

    this.roleSelect = new this.Field('userRoleSelect');
    this.roleSelect.requiredError = "Please choose a role";


    var valid = false;

    this.validateAllFields = function() {
        this.stateSelect.validate();
        this.citySelect.validate();
        this.schoolSelect.validate();
        this.roleSelect.validate();
    };
    
    this.isValid = function() {
        var valid = false;
        if (this.stateSelect.isValid() && this.citySelect.isValid() &&
                this.schoolSelect.isValid() && this.roleSelect.isValid()) {
            valid = true;
        }
        return valid;
    };

    this.checkIfValid = function() {
        var newValidationState = this.isValid();
        if (valid !== newValidationState) {
            valid = newValidationState;
            if (newValidationState) {
                if (becomeValidCallback !== undefined) {
                    becomeValidCallback();
                }
            } else {
                if (becomeInvalidCallback !== undefined) {
                    becomeInvalidCallback();
                }
            }
        }
    }.gs_bind(this);

    this.registerValidCallback = function(callback) {
        becomeValidCallback = callback;
    };
    this.registerInvalidCallback = function(callback) {
        becomeInvalidCallback = callback;
    };

    this.onStateChange = function() {
        var state = this.stateSelect.$element.val();
        this.citySelect.$element.prop("disabled", true);
        this.citySelect.reset();
        this.schoolSelect.$element.prop("disabled", true);
        this.schoolSelect.reset();
        this.getCities(state);
        this.validateAllFields();
        this.checkIfValid();
    }.gs_bind(this);

    this.getCities = function(state) {
        var url = "/community/registrationAjax.page";

        this.citySelect.$element.html("<option>Loading...</option>");

        jQuery.getJSON(url, {state:state, format:'json', type:'city'}, this.updateCitySelect);
    }.gs_bind(this);

    this.updateCitySelect = function(data) {
        if (data.cities) {
            this.citySelect.$element.empty();
            for (var x = 0; x < data.cities.length; x++) {
                var city = data.cities[x];
                if (city.name) {
                    this.citySelect.$element.append("<option value=\"" + city.name + "\">" + city.name + "</option>");
                }
            }
        }
        this.citySelect.$element.prop("disabled", false);
    }.gs_bind(this);

    this.onCityChange = function() {
        var state = jQuery('#stateSelect').val();
        var city = jQuery('#citySelect').val();
        this.schoolSelect.$element.prop("disabled", true);
        this.getSchools(state, city);
        this.validateAllFields();
        this.checkIfValid();
    }.gs_bind(this);

    this.getSchools = function(state, city) {
        var params = {state : state, city : city,onchange:'schoolChange(this)',includePrivateSchools :true,chooseSchoolLabel :'Choose a school'};
        jQuery.get('/test/schoolsInCity.page', params, this.updateSchoolSelect);
    }.gs_bind(this);

    this.updateSchoolSelect = function(data) {
        self.schoolSelect.$element.html('');
        jQuery(data).find('option').each(function () {
            self.schoolSelect.$element.append("<option value=\"" + jQuery(this).attr('value') + "\">" + jQuery(this).text() + "</option>");
        });
        self.schoolSelect.$element.prop("disabled", false);
    };

    this.onSchoolChange = function() {
        var schoolId = this.schoolSelect.$element.val();
        var state = this.stateSelect.$element.val();

        this.getSchool(state, schoolId);
        this.validateAllFields();
        this.checkIfValid();
    }.gs_bind(this);

    this.getSchool = function(state, school) {
        if (state != '' && school !== '') {
            var url = '/school/schoolForParentReview.page';
            var params = {schoolId : school, state : this.stateSelect.$element.val()};
            jQuery.getJSON(url, params, this.updatePageWithSchool);
        }
    }.gs_bind(this);
    
    this.onRoleChange = function() {
        this._role = this.roleSelect.$element.val();
        jQuery('#posterAsString').val(this._role); //create another hook up for so form can register a callback
        this.updateAdditionalStarRatings(this._role, this._levelCode);
        this.validateAllFields();
        this.checkIfValid();
    }.gs_bind(this);

    this.updatePageWithSchool = function(data) {

        var id = data.id;
        var state = data.state;
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

        //TODO: move this code out to make this JS file more reusable
        //write the id and state into hidden fields in form
        jQuery('#schoolId').val(id);
        jQuery('#schoolState').val(state);
        jQuery('#hdnSchoolName').val(name);

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

        if (espLoginUrl != null && espLoginUrl > '') {
            jQuery('#esp-link').attr('href', espLoginUrl);
        } else {
            jQuery('#esp-link').attr('href', '/cgi-bin/pq_start.cgi/' + state + '/' + id);
        }

        this._levelCode = data.levelCode;
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
        this.stateSelect.$element.change(this.onStateChange);
        this.citySelect.$element.change(this.onCityChange);
        this.schoolSelect.$element.change(this.onSchoolChange);
        this.roleSelect.$element.change(this.onRoleChange);
    }.gs_bind(this);

    this.attachEventHandlers();

    this.validateAllFields();

};


GS.parentReviewLandingPage = {} || GS.parentReviewLandingPage;
GS.parentReviewLandingPage.attachAutocomplete = function () {
    var searchBox = $('.reviewsLanding');
    var url = "/search/schoolAutocomplete.page";

    var formatter = function (row) {
        if (row != null &amp;&amp; row.length > 0) {
            //var suggestion = row[0];
            // capitalize first letter of all words but the last
            // capitalize the entire last word (state)
            //return suggestion.substr(0, suggestion.length-2).replace(/\w+/g, function(word) { return word.charAt(0).toUpperCase() + word.substr(1); }) + suggestion.substr(suggestion.length-2).toUpperCase();
        }
        return row;
    };

    searchBox.autocomplete2(url, {
        extraParams: {
            state: function () {
                //var rval = searchStateSelect.val();
                // TODO: add state
                var rval = "CA";
                if (rval === '') {
                    return null;
                }
                return rval;
            },
            schoolCity: true
        },
        extraParamsRequired: true,
        minChars: 3,
        selectFirst: false,
        cacheLength: 150,
        matchSubset: true,
        max: 6,
        autoFill: false,
        dataType: "text",
        formatItem: formatter,
        formatResult: formatter
    });
};

jQuery(function() {
    GS.module.schoolSelect = new GS.module.SchoolSelect();

    GS.module.schoolSelect.registerValidCallback(function() {
       jQuery('#addParentReviewForm').show();
    });

    GS.module.schoolSelect.registerInvalidCallback(function() {
       jQuery('#addParentReviewForm').hide();
    });

    jQuery('#addParentReviewForm').hide();
    GS.parentReviewLandingPage.attachAutocomplete();

});