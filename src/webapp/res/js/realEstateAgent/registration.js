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

            var form = $('.jq-businessInfoForm:visible');
            var data = {};
            form.find('input').each(function() {
                data[this.name] = this.value;
            });
            data['state'] = form.find('select[name=state]').val();

            $.ajax({
                type : 'POST',
                url : '/real-estate/saveBusinessInfo.page',
                data : data,
                success : function (response) {
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
                },
                error : function (e) {
//                    alert('error: ' + e);
                }
            });
        });

        hover.on('click', '.jq-completeRegistration', function() {
            window.location.href = window.location.protocol + '//' + window.location.host +
                '/real-estate/create-guide.page';
        })
    };

    this.validateFirstName = function() {
        jQuery.getJSON(
            GS.uri.Uri.getBaseHostname() + '/real-estate/registrationValidationAjax.page',
            {firstName: jQuery('.jq-personalInfoForm:visible').find('#jq-fName').val(), fieldName: 'firstName'},
            function(data) {
                GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-personalInfoForm:visible .jq-firstNameFields .errors', data, 'firstNameErrorDetail');
            }
        );
    };

    this.validateLastName = function() {
        jQuery.getJSON(
            GS.uri.Uri.getBaseHostname() + '/real-estate/registrationValidationAjax.page',
            {lastName: jQuery('.jq-personalInfoForm:visible').find('#jq-lName').val(), fieldName: 'lastName'},
            function(data) {
                GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-personalInfoForm:visible .jq-lastNameFields .errors', data, 'lastNameErrorDetail');
            }
        );
    };

    this.validateEmail = function() {
        jQuery.getJSON(
            GS.uri.Uri.getBaseHostname() + '/real-estate/registrationValidationAjax.page',
            {email: jQuery('.jq-personalInfoForm:visible').find('#jq-email').val(), fieldName: 'email'},
            function(data) {
                GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-personalInfoForm:visible .jq-emailFields .errors', data, 'emailErrorDetail');
            }
        );
    };

    this.validatePassword = function() {
        jQuery.getJSON(
            GS.uri.Uri.getBaseHostname() + '/real-estate/registrationValidationAjax.page',
            {password: jQuery('.jq-personalInfoForm:visible').find('#jq-password').val(), fieldName: 'password'},
            function(data) {
                GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-personalInfoForm:visible .jq-passwordFields .errors', data, 'passwordErrorDetail');
            }
        );
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
};

GSType.hover.RealEstateAgentRegistrationHover.prototype = new GSType.hover.HoverDialog('js-registrationHover', 480)

GSType.hover.realEstateAgentRegistrationHover = new GSType.hover.RealEstateAgentRegistrationHover();

jQuery(function(){
    GSType.hover.realEstateAgentRegistrationHover.loadDialog();

    jQuery('#jq-realEstAgentRegisterBtn').on('click', function() {
        GSType.hover.realEstateAgentRegistrationHover.show();
        return false;
    });

    var skipValidation = function() {
        var params = GS.uri.Uri.getQueryData();
        if(params.skipUserCheck === 'true') {
            return true;
        }
        return false;
    }

    if(!skipValidation()) {
        jQuery('#jq-fName').blur(GSType.hover.realEstateAgentRegistrationHover.validateFirstName);
        jQuery('#jq-lName').blur(GSType.hover.realEstateAgentRegistrationHover.validateLastName);
        jQuery('#jq-email').blur(GSType.hover.realEstateAgentRegistrationHover.validateEmail);
        jQuery('#jq-password').blur(GSType.hover.realEstateAgentRegistrationHover.validatePassword);
    }

});

