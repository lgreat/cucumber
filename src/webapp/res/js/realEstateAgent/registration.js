var GS = GS || {};
GS.realEstateAgent = GS.realEstateAgent || {};

GSType.hover.RealEstateAgentRegistrationHover = function() {
    var errors = {};

    this.loadDialog = function() {};

    /*
     * hack for browsers that doesn't support Object.keys -
     * https://developer.mozilla.org/en-US/docs/JavaScript/Reference/Global_Objects/Object/keys
     */
    Object.keys = Object.keys || (function() {
        var hasOwnProperty = Object.prototype.hasOwnProperty,
            hasDontEnumBug = !({toString: null}).propertyIsEnumerable('toString'),
            dontEnums = [
                'toString',
                'toLocaleString',
                'valueOf',
                'hasOwnProperty',
                'isPrototypeOf',
                'propertyIsEnumerable',
                'constructor'
            ],
            dontEnumsLength = dontEnums.length;
        return function (o) {
            if (typeof o != "object" && typeof o != "function" || o === null)
                throw new TypeError("Object.keys called on a non-object");

            var result = [];
            for (var name in o) {
                if (hasOwnProperty.call(o, name))
                    result.push(name);
            }
            if (hasDontEnumBug) {
                for (var i=0; i < dontEnumsLength; i++) {
                    if (hasOwnProperty.call(o, dontEnums[i])) result.push(dontEnums[i]);
                }
            }
            return result;
        };
    })();

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

            $.ajax({
                type : 'POST',
                url : '/real-estate/savePersonalInfo.page',
                data : data,
                success : function (response) {
                    if(response.success) {
                        form.addClass('dn');
                        hover.find('.jq-businessInfoForm').removeClass('dn');
                        GS.realEstateAgent.GS_initializeCustomSelect(".jq-businessInfoForm:visible .js-realEstateState");
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
            if(!GSType.hover.realEstateAgentRegistrationHover.validateBusinessInfoHover()) {
                return false;
            }

            var form = $('.jq-businessInfoForm:visible');
            var data = {};
            form.find('input').each(function() {
                data[this.name] = jQuery.trim(this.value);
            });
            data['state'] = jQuery.trim(form.find('.js-realEstateState .js-selectBoxText').text());

            if(!data['cellNumber'].match(/^\d{10}$/)) {
                data['cellNumber'] = '';
            }

            $.ajax({
                type : 'POST',
                url : '/real-estate/saveBusinessInfo.page',
                data : data,
                success : function (response) {
                    if(response.success) {
                        form.addClass('dn');
                        hover.find('.jq-imageUploaderForm').removeClass('dn');
                        jQuery('.js-registrationHover:visible .js_closeHover').on('click', function() {
                            GS.realEstateAgentPollingViewer.turnPollingOff();}
                        );

                        if(response.companyInfoFields !== undefined) {
                            var companyInfoFields = response.companyInfoFields;
                            $('.jq-businessInfoForm:first input').each(function() {
                                var $this = jQuery(this);
                                if(companyInfoFields[this.name] != null) {
                                    $this.attr('value', companyInfoFields[this.name]);
                                }
                            });
                            if(companyInfoFields.state !== null) {
//                                $('.jq-businessInfoForm:first select option:selected').removeAttr('selected');
//                                $('.jq-businessInfoForm:first select option[value="'+ companyInfoFields.state + '"]').attr('selected','selected')
                                $('.jq-businessInfoForm:first .js-realEstateState .js-selectBoxText').text(companyInfoFields.state);
                            }

                        }

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

                        GS.realEstateAgentPollingViewer.init();
                        GS.realEstateAgentPollingViewer.turnPollingOn();
                    }
                },
                error : function (e) {
//                    alert('error: ' + e);
                }
            });
        });

        hover.on('click', '.jq-completeRegistration', function() {
            jQuery('#agentRegistration').submit();
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
            data.companyNameErrorDetail = 'Please enter your company name to brand your guides.';
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-companyNameFields .errors', data, 'companyNameErrorDetail');
    };

    this.validateWorkNumber = function() {
        var data = {};
        var workNumber = jQuery.trim(jQuery('.jq-businessInfoForm:visible').find('#jq-workNumber').val());
        if(!workNumber.match(/^\d{10}$/)) {
            data.hasError = true;
            data.workNumberErrorDetail = 'Please enter just 10 digits.';
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-workNumberFields .errors', data, 'workNumberErrorDetail');
    };

    this.validateCellNumber = function() {
        var data = {};
        var cellNumber = jQuery.trim(jQuery('.jq-businessInfoForm:visible').find('#jq-cellNumber').val());
        if(!(cellNumber === '' || cellNumber === 'Cell #') && !cellNumber.match(/^\d{10}$/)) {
            data.hasError = true;
            data.cellNumberErrorDetail = 'Please enter just 10 digits.';
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-cellNumberFields .errors', data, 'cellNumberErrorDetail');
    };

    this.validateAddress = function() {
        var data = {};
        var address = jQuery.trim(jQuery('.jq-businessInfoForm:visible').find('#jq-address').val());
        if(address === 'Work Address' || address === '') {
            data.hasError = true;
            data.addressErrorDetail = "Please enter your company's street address.";
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-addressFields .errors', data, 'addressErrorDetail');
    };

    this.validateCity = function() {
        var data = {};
        var city = jQuery.trim(jQuery('.jq-businessInfoForm:visible').find('#jq-city').val());
        if(city === 'City' || city === '') {
            data.hasError = true;
            data.cityErrorDetail = 'Please specify.';
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-cityFields .errors', data, 'cityErrorDetail');
    };

    this.validateState = function() {
        var data = {};
        var state = jQuery.trim(jQuery('.jq-businessInfoForm:visible').find('#jq-state .js-selectBoxText').text());
        if(state === '' || state === 'State') {
            data.hasError = true;
            data.stateErrorDetail = 'Please select.';
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-stateFields .errors', data, 'stateErrorDetail');
    };

    this.validateZip = function() {
        var data = {};
        var zip = jQuery.trim(jQuery('.jq-businessInfoForm:visible').find('#jq-zip').val());
        if(!zip.match(/^\d{5}$/)) {
            data.hasError = true;
            data.zipErrorDetail = 'Enter 5 digits.';
        }
        GSType.hover.realEstateAgentRegistrationHover.validateFieldResponse('.jq-businessInfoForm:visible .jq-zipFields .errors', data, 'zipErrorDetail');
    };

    this.validateFieldResponse = function(fieldSelector, data, errorDetailKey) {
        var errorIcon ='<span class="vam mrs iconx16 i-16-alert "><!-- do not collapse --></span>';
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
        GSType.hover.realEstateAgentRegistrationHover.validateCellNumber();
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

GS.realEstateAgentPollingViewer = new GS.RealEstateAgentPollingViewer();

jQuery(function(){
    GSType.hover.realEstateAgentRegistrationHover.loadDialog();

    jQuery('#jq-realEstAgentRegisterBtn').on('click', function() {
        GSType.hover.realEstateAgentRegistrationHover.show();
        return false;
    });

    jQuery('#jq-myAccount').on('click', function() {
        var form = jQuery('.jq-businessInfoForm');
        form.removeClass('dn');

        GSType.hover.realEstateAgentRegistrationHover.show();
        GS.realEstateAgent.GS_initializeCustomSelect(".jq-businessInfoForm:visible .js-realEstateState");
        return false;
    });

    /*var skipValidation = function() {
        var params = GS.uri.Uri.getQueryData();
        return(params.skipUserCheck === 'true');
    }*/

//    if(!skipValidation()) {}
    //Contact Info Validation
    jQuery('#jq-fName').blur(GSType.hover.realEstateAgentRegistrationHover.validateFirstName);
    jQuery('#jq-lName').blur(GSType.hover.realEstateAgentRegistrationHover.validateLastName);
    jQuery('#jq-email').blur(GSType.hover.realEstateAgentRegistrationHover.validateEmail);
    jQuery('#jq-password').blur(GSType.hover.realEstateAgentRegistrationHover.validatePassword);

    //Company Info Validation
    jQuery('#jq-companyName').blur(GSType.hover.realEstateAgentRegistrationHover.validateCompanyName);
    jQuery('#jq-workNumber').blur(GSType.hover.realEstateAgentRegistrationHover.validateWorkNumber);
    jQuery('#jq-cellNumber').blur(GSType.hover.realEstateAgentRegistrationHover.validateCellNumber);
    jQuery('#jq-address').blur(GSType.hover.realEstateAgentRegistrationHover.validateAddress);
    jQuery('#jq-city').blur(GSType.hover.realEstateAgentRegistrationHover.validateCity);
    jQuery('.js-selectDropDown').on('click', '.js-ddValues', GSType.hover.realEstateAgentRegistrationHover.validateState);
    jQuery('#jq-zip').blur(GSType.hover.realEstateAgentRegistrationHover.validateZip);

});


/**********************************************************************************************
 *
 * @param layerContainer  --- this is the surrounding layer that contains
 .js-selectBox - this is the clickable element to open the drop down
 .js-selectDropDown - this is the dropdown select list container
 .js-ddValues - each element in the select list
 .js-selectBoxText - the text that gets set.  This is the part that should be scrapped for option choice
 * @param callbackFunction - optional function callback when selection is made.
 * @constructor
 */
GS.realEstateAgent.GS_initializeCustomSelect = function(layerContainer, callbackFunction){
    var selectContainer = $(layerContainer); //notify
    var selectBox = selectContainer.find(".js-selectBox");
    var selectDropDownBox = selectContainer.find(".js-selectDropDown");
    var selectDropDownItem = selectContainer.find(".js-ddValues");
    var selectBoxText = selectContainer.find(".js-selectBoxText");

    selectBox.on("click", showSelect);


    selectDropDownBox.on('click', function(event) {
        // Handle the click on the notify div so the document click doesn't close it
        event.stopPropagation();
    });

    function showSelect(event) {
        $(this).off('click');
        selectDropDownBox.show();
        $(document).on("click", hideSelect);
        selectDropDownItem.on("click", showW);
        // So the document doesn't immediately handle this same click event
        event.stopPropagation();
    };

    function hideSelect(event) {
        $(this).off('click');
        selectDropDownItem.off('click');
        selectDropDownBox.hide();
        selectBox.on("click", showSelect);
    }

    function showW(event) {
        hideSelect(event);
        selectBoxText.html($(this).html());
        if(callbackFunction) callbackFunction($(this).html());
    }

    selectDropDownItem.mouseover(function () {
        $(this).addClass("ddValuesHighlight");
    });

    selectDropDownItem.mouseout(function () {
        $(this).removeClass("ddValuesHighlight");
    });
}
