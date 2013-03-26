var GS = GS || {};
GS.realEstateAgent = GS.realEstateAgent || {};

GSType.hover.RealEstateAgentRegistrationHover = function() {
    var errors = {};

    this.loadDialog = function() {};

    this.show = function() {
        errors = {};
        GSType.hover.realEstateAgentRegistrationHover.showModal();

        if(s) {
            pageTracking.clear();
            pageTracking.pageName = "Radar Registration Contact Info";
            pageTracking.hierarchy = "real-estate, GreatSchools Radar, Registration";
            pageTracking.successEvents = "event73";
            pageTracking.send();
        }

        var hover = jQuery('.js-registrationHover:visible');

        hover.on('click', '.jq-personalInfoSubmit:visible', function() {

//            console.log(Object.keys(errors).length);
            if(Object.keys(errors).length > 0) {
                return false;
            }

            var form = $('.jq-personalInfoForm:visible');
            var data = {};
            form.find('input').each(function() {
                data[this.name] = this.value;
            });

            var skipValidation = function() {
                var params = GS.uri.Uri.getQueryData();
                if(params.skipUserCheck === 'true') {
                    return true;
                }
                return false;
            }
            //TODO: comment skip user validation
            if(skipValidation()) {
                data.skipUserCheck = true;
            }

            $.ajax({
                type : 'POST',
                url : '/real-estate/savePersonalInfo.page',
                data : data,
                success : function (response) {
                    if(response.success || data.skipUserCheck) {
                        form.addClass('dn');
                        hover.find('.jq-businessInfoForm').removeClass('dn');

                        if(s) {
                            pageTracking.clear();
                            pageTracking.pageName = "Radar Registration Company Info";
                            pageTracking.hierarchy = "real-estate, GreatSchools Radar, Registration";
                            pageTracking.send();
                        }
                    }
                    else {
                        if(response.firstNameErrorDetail) {
                            GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-personalInfoForm:visible .jq-firstNameFields .errors', response, 'firstNameErrorDetail');
                        }
                        if(response.lastNameErrorDetail) {
                            GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-personalInfoForm:visible .jq-lastNameFields .errors', response, 'lastNameErrorDetail');
                        }
                        if(response.emailErrorDetail) {
                            GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-personalInfoForm:visible .jq-emailFields .errors', response, 'emailErrorDetail');
                        }
                        if(response.passwordErrorDetail) {
                            GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-personalInfoForm:visible .jq-passwordFields .errors', response, 'passwordErrorDetail');
                        }
                    }
                },
                error : function (e) {
//                    alert('error: ' + e);
                }
            });
        });

        hover.on('click', '.jq-businessInfoSubmit:visible', function() {
            //TODO: comment skip validation
            var skipValidation = function() {
                var params = GS.uri.Uri.getQueryData();
                if(params.skipUserCheck === 'true') {
                    return true;
                }
                return false;
            }
            if(!skipValidation() && !GSType.hover.realEstateAgentRegistrationHover.validateBusinessInfoHover()) {
                return false;
            }

            var form = $('.jq-businessInfoForm:visible');
            var data = {};
            form.find('input').each(function() {
                data[this.name] = jQuery.trim(this.value);
            });
            data['state'] = form.find('select[name=state]').val();

            if(!data['cellNumber'].match(/^\d{10}$/)) {
                data['cellNumber'] = '';
            }
            //TODO: comment skip user validation
            if(skipValidation()) {
                data.skipUserCheck = true;
            }


            $.ajax({
                type : 'POST',
                url : '/real-estate/saveBusinessInfo.page',
                data : data,
                success : function (response) {
                    if(response.success || data.skipUserCheck) {
                        form.addClass('dn');
                        hover.find('.jq-imageUploaderForm').removeClass('dn');

                        if(s) {
                            pageTracking.clear();
                            pageTracking.pageName = "Radar Registration Image Upload";
                            pageTracking.hierarchy = "real-estate, GreatSchools Radar, Registration";
                            pageTracking.send();
                        }

                        GS.realEstateAgentPhotoUploader = new GS.RealEstateAgentCreatePhotoUploader();
                        GS.realEstateAgentLogoUploader = new GS.RealEstateAgentCreateLogoUploader();
                        GS.realEstateAgentPhotoUploader.init();
                        GS.realEstateAgentLogoUploader.init();
                    }
                },
                error : function (e) {
//                    alert('error: ' + e);
                }
            });
        });

        hover.on('click', '.jq-completeRegistration', function() {
            var registrationComplete = jQuery('input.jq-registrationComplete').val();
            var page = '/real-estate/create-guide.page';
            if(registrationComplete === 'true') {
                page += '?registrationComplete=true';
            }

            window.location.href = window.location.protocol + '//' + window.location.host + page;
        })
    };

    this.validateFirstName = function() {
        jQuery.getJSON(
            GS.uri.Uri.getBaseHostname() + '/real-estate/personalInfoValidationAjax.page',
            {firstName: jQuery('.jq-personalInfoForm:visible').find('#jq-fName').val(), fieldName: 'firstName'},
            function(data) {
                GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-personalInfoForm:visible .jq-firstNameFields .errors', data, 'firstNameErrorDetail');
            }
        );
    };

    this.validateLastName = function() {
        jQuery.getJSON(
            GS.uri.Uri.getBaseHostname() + '/real-estate/personalInfoValidationAjax.page',
            {lastName: jQuery('.jq-personalInfoForm:visible').find('#jq-lName').val(), fieldName: 'lastName'},
            function(data) {
                GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-personalInfoForm:visible .jq-lastNameFields .errors', data, 'lastNameErrorDetail');
            }
        );
    };

    this.validateEmail = function() {
        jQuery.getJSON(
            GS.uri.Uri.getBaseHostname() + '/real-estate/personalInfoValidationAjax.page',
            {email: jQuery('.jq-personalInfoForm:visible').find('#jq-email').val(), fieldName: 'email'},
            function(data) {
                GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-personalInfoForm:visible .jq-emailFields .errors', data, 'emailErrorDetail');
            }
        );
    };

    this.validatePassword = function() {
        jQuery.getJSON(
            GS.uri.Uri.getBaseHostname() + '/real-estate/personalInfoValidationAjax.page',
            {password: jQuery('.jq-personalInfoForm:visible').find('#jq-password').val(), fieldName: 'password'},
            function(data) {
                GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-personalInfoForm:visible .jq-passwordFields .errors', data, 'passwordErrorDetail');
            }
        );
    };

    this.validateCompanyName = function() {
        var data = {};
        var companyName = jQuery.trim(jQuery('.jq-businessInfoForm:visible').find('#jq-companyName').val());
        if(companyName === 'Company Name' || companyName === '') {
            data.hasError = true;
            data.companyNameErrorDetail = 'Please specify your company name.';
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-companyNameFields .errors', data, 'companyNameErrorDetail');
    };

    this.validateWorkNumber = function() {
        var data = {};
        var workNumber = jQuery.trim(jQuery('.jq-businessInfoForm:visible').find('#jq-workNumber').val());
        if(!workNumber.match(/^\d{10}$/)) {
            data.hasError = true;
            data.workNumberErrorDetail = 'Please enter exactly 10 digits.';
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-workNumberFields .errors', data, 'workNumberErrorDetail');
    };

    this.validateAddress = function() {
        var data = {};
        var address = jQuery.trim(jQuery('.jq-businessInfoForm:visible').find('#jq-address').val());
        if(address === 'Work Address' || address === '') {
            data.hasError = true;
            data.addressErrorDetail = 'Please specify the work address.';
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-addressFields .errors', data, 'addressErrorDetail');
    };

    this.validateCity = function() {
        var data = {};
        var city = jQuery.trim(jQuery('.jq-businessInfoForm:visible').find('#jq-city').val());
        if(city === 'City' || city === '') {
            data.hasError = true;
            data.cityErrorDetail = 'Please specify the city.';
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-cityFields .errors', data, 'cityErrorDetail');
    };

    this.validateState = function() {
        var data = {};
        var state = jQuery.trim(jQuery('.jq-businessInfoForm:visible').find('#jq-state').val());
        if(state === '') {
            data.hasError = true;
            data.stateErrorDetail = 'Please select the state.';
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-stateFields .errors', data, 'stateErrorDetail');
    };

    this.validateZip = function() {
        var data = {};
        var zip = jQuery.trim(jQuery('.jq-businessInfoForm:visible').find('#jq-zip').val());
        if(!zip.match(/^\d{5}$/)) {
            data.hasError = true;
            data.zipErrorDetail = 'Please enter exactly 5 digits.';
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-zipFields .errors', data, 'zipErrorDetail');
    };

    this.validateFieldResponse = function(fieldSelector, data, errorDetailKey) {
        var errorIcon ='<span class="iconx16 i-16-alert "><!-- do not collapse --></span>';
        var fieldError = jQuery(fieldSelector + ' .invalid');
        var fieldValid = jQuery(fieldSelector + ' .valid');
        fieldError.hide();
        fieldValid.hide();
        if (data && data.hasError) {
            fieldError.html(errorIcon+data[errorDetailKey]);
            fieldError.show();
            errors[errorDetailKey] = data[errorDetailKey];
        } else {
            fieldValid.show();
            delete errors[errorDetailKey];
        }
    };

    this.validateBusinessInfoHover = function(){
        GSType.hover.realEstateAgentRegistrationHover.validateCompanyName();
        GSType.hover.realEstateAgentRegistrationHover.validateWorkNumber();
        GSType.hover.realEstateAgentRegistrationHover.validateAddress();
        GSType.hover.realEstateAgentRegistrationHover.validateCity();
        GSType.hover.realEstateAgentRegistrationHover.validateState();
        GSType.hover.realEstateAgentRegistrationHover.validateZip();

        if(Object.keys(errors).length > 0) {
            return false;
        }
        return true;
    };
};

GSType.hover.RealEstateAgentRegistrationHover.prototype = new GSType.hover.HoverDialog('js-registrationHover', 480)

GSType.hover.realEstateAgentRegistrationHover = new GSType.hover.RealEstateAgentRegistrationHover();

jQuery(function(){
    GSType.hover.realEstateAgentRegistrationHover.loadDialog();

    jQuery('#jq-realEstAgentRegisterBtn').on('click', function() {
        GSType.hover.realEstateAgentRegistrationHover.show();
        return false;
    });

    jQuery('#jq-myAccount').on('click', function() {
        jQuery('.jq-businessInfoForm').removeClass('dn');
        GSType.hover.realEstateAgentRegistrationHover.show();
        return false;
    });

    //TODO: comment skip user validation
    var skipValidation = function() {
        var params = GS.uri.Uri.getQueryData();
        return(params.skipUserCheck === 'true');
    }

    if(!skipValidation()) {
        //Contact Info Validation
        jQuery('#jq-fName').blur(GSType.hover.realEstateAgentRegistrationHover.validateFirstName);
        jQuery('#jq-lName').blur(GSType.hover.realEstateAgentRegistrationHover.validateLastName);
        jQuery('#jq-email').blur(GSType.hover.realEstateAgentRegistrationHover.validateEmail);
        jQuery('#jq-password').blur(GSType.hover.realEstateAgentRegistrationHover.validatePassword);

        //Company Info Validation
        jQuery('#jq-companyName').blur(GSType.hover.realEstateAgentRegistrationHover.validateCompanyName);
        jQuery('#jq-workNumber').blur(GSType.hover.realEstateAgentRegistrationHover.validateWorkNumber);
        jQuery('#jq-address').blur(GSType.hover.realEstateAgentRegistrationHover.validateAddress);
        jQuery('#jq-city').blur(GSType.hover.realEstateAgentRegistrationHover.validateCity);
        jQuery('#jq-state').blur(GSType.hover.realEstateAgentRegistrationHover.validateState);
        jQuery('#jq-zip').blur(GSType.hover.realEstateAgentRegistrationHover.validateZip);
    }

});

