if (GS == undefined) {
    var GS = {};
}
if (GSType == undefined) {
    var GSType = {};
}
if (GSType.hover == undefined) {
    GSType.hover = {};
}

Function.prototype.gs_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

//HoverDialog requires the ID of the element to display as a hover dialog
GSType.hover.HoverDialog = function(id) {
    this.hoverId = id;
    this.pageName = '';
    this.hier1 = '';
    this.show = function() {
        jQuery('#' + this.hoverId).dialog('open');
        if (this.pageName != '') {
            pageTracking.clear();
            pageTracking.pageName = this.pageName;
            pageTracking.hierarchy = this.hier1;
            pageTracking.send();
        }
        return false;
    };
    this.hide = function() {
        jQuery('#' + this.hoverId).dialog('close');
        return false;
    };
    //template dialog to display based on variable width
    this.dialogByWidth = function (width) {
        var thisHover = jQuery('#' + this.hoverId);
        thisHover.dialog({
            bgiframe: true,
            modal: true,
            draggable: false,
            autoOpen: false,
            resizable: false,
            width: width,
            zIndex: 15000
        });
        thisHover.find('.' + this.hoverId + '_showHover').click(this.show.gs_bind(this));
        thisHover.find('.' + this.hoverId + '_hideHover').click(this.hide.gs_bind(this));
    };
};

//EditEmailValidated Hover
GSType.hover.EditEmailValidated = function() {
    this.loadDialog = function () {
        this.pageName='Email Change Verified Hover';
        this.hier1='Hovers,Verification,Email Change Verified Hover';
        this.dialogByWidth(640);
    }
};
GSType.hover.EditEmailValidated.prototype = new GSType.hover.HoverDialog('valNewEmailDone');

//EmailValidated hover
GSType.hover.EmailValidated = function() {
    this.loadDialog = function () {
        this.pageName='Account Verified Hover';
        this.hier1='Hovers,Verification,Account Verified Hover';
        this.dialogByWidth(640);
    }
};
GSType.hover.EmailValidated.prototype = new GSType.hover.HoverDialog('regDone');

//ForgotPasswordHover hover
GSType.hover.ForgotPasswordHover = function() {
    this.loadOnExitUrl = null;
    this.loadDialog = function() {
        this.pageName='Forgot Password Hover';
        this.hier1='Hovers,Sign In,Forgot Password Hover';
        this.dialogByWidth(590);
    };
    this.addMessage = function(text) {
        jQuery('#hover_forgotPassword .messages').html('<p>' + text + '</p>').show();
    };
    this.clearMessages = function() {
        jQuery('#hover_forgotPassword .messages').empty();
        jQuery('#hover_forgotPassword .messages').hide();
    };
    this.loadOnExit = function(url) {
        GSType.hover.forgotPassword.loadOnExitUrl = url;
        jQuery('#hover_forgotPassword').bind('dialogclose', function() {
            window.location = GSType.hover.forgotPassword.loadOnExitUrl;
        });
    };
    this.cancelLoadOnExit = function() {
        GSType.hover.forgotPassword.loadOnExitUrl = null;
        jQuery('#hover_forgotPassword').unbind('dialogclose');
    };
    this.showJoin = function() {
        if (GSType.hover.forgotPassword.loadOnExitUrl) {
            GSType.hover.signInHover.loadOnExit(GSType.hover.forgotPassword.loadOnExitUrl);
            GSType.hover.forgotPassword.cancelLoadOnExit();
        }
        GSType.hover.forgotPassword.hide();
        GSType.hover.signInHover.showJoinFunction();
    };
    this.showSignin = function() {
        if (GSType.hover.forgotPassword.loadOnExitUrl) {
            GSType.hover.signInHover.loadOnExit(GSType.hover.forgotPassword.loadOnExitUrl);
            GSType.hover.forgotPassword.cancelLoadOnExit();
        }
        GSType.hover.forgotPassword.hide();
        GSType.hover.signInHover.show();
    };
};
GSType.hover.ForgotPasswordHover.prototype = new GSType.hover.HoverDialog('hover_forgotPassword');

//Join hover
GSType.hover.JoinHover = function() {
    this.schoolName = null;
    this.loadOnExitUrl = null;
    this.onSubmitCallback = null;

    this.baseFields = function() {
        // hide city and state inputs
        jQuery('#joinHover .joinHover_location').hide();
        // hide nth / MSS
        jQuery('#joinHover li.grades p').hide();
        jQuery('#joinHover li.grades ul').hide();
        // hide LD newsletter
        jQuery('#joinHover li.joinHover_ld').hide();
        // hide BTS tip
        jQuery('#joinHover li.joinHover_btstip').hide();
        //check checkbox for greatnews
        jQuery('#joinHover #opt1').attr('checked', true);
        var stockPhotoUrl = '/res/img/discussion_boards/db_jnStock1_ph.jpg';
        if (jQuery('#joinHover_stockPhoto').attr('src') != stockPhotoUrl) {
            jQuery('#joinHover_stockPhoto').attr('src', stockPhotoUrl);
        }
    };
    //sets a notification message on the join form - can be used to explain why this hover was launched
    this.addMessage = function(text) {
        jQuery('#joinHover .message').html(text).show();
    };
    //method is plural to remain consistent with other hovers. Should always get called when hover closes
    this.clearMessages = function() {
        jQuery('#joinHover .message').empty();
        jQuery('#joinHover .message').hide();
    };
    this.setJoinHoverType = function(type) {
        jQuery('#joinHover form#joinGS input#joinHoverType').attr("value", type);
    };
    this.setTitle = function(title) {
        jQuery('#joinHover div.hoverTitle h2').html(title);
    };
    this.setSubTitle = function(subTitle, subTitleText) {
        jQuery('#joinHover .introTxt h3').html(subTitle);
        jQuery('#joinHover .introTxt p').html(subTitleText);
    };
    this.configAndShowEmailTipsMssLabel = function(includeWeeklyEmails, includeTips, includeMss)
    {
        var labelTextPrefix = "Sign me up for";
        var labelPhrases = "";

        if (includeWeeklyEmails) {
            labelPhrases += " weekly emails from GreatSchools";
        }
        if (includeTips) {
            if (labelPhrases.length > 0) {
                labelPhrases += " with";
            }
            labelPhrases += " grade-by-grade tips"
        }
        if (includeMss) {
            if (labelPhrases.length > 0) {
                labelPhrases += ", including";
            }
            labelPhrases += " periodic updates about <strong>" + GSType.hover.joinHover.schoolName + "</strong>";
        }
        labelPhrases += ".";

        //choose whether to display nth grader checkboxes flyout
        if (includeTips) {
            jQuery('#joinHover li.grades p').show();
            jQuery('#joinHover li.grades ul').show();
        }

        jQuery('#joinHover li.grades label[for="opt1"]').html(labelTextPrefix + labelPhrases);
    };
    this.parseCities = function(data) {
        var citySelect = jQuery('#joinHover #joinCity');
        if (data.cities) {
            citySelect.empty();
            for (var x = 0; x < data.cities.length; x++) {
                var city = data.cities[x];
                if (city.name) {
                    citySelect.append("<option value=\"" + city.name + "\">" + city.name + "</option>");
                }
            }
        }
    };
    this.loadCities = function() {
        var state = jQuery('#joinHover #joinState').val();
        var url = "/community/registrationAjax.page";

        jQuery('#joinHover #joinCity').html("<option>Loading...</option>");

        jQuery.getJSON(url, {state:state, format:'json', type:'city'}, GSType.hover.joinHover.parseCities);
    };
    this.loadDialog = function() {
        GSType.hover.joinHover.dialogByWidth(680);
        jQuery('#joinHover .redirect_field').val(window.location.href);
    };
    this.loadOnExit = function(url) {
        GSType.hover.joinHover.loadOnExitUrl = url;
        jQuery('#joinHover .redirect_field').val(url);
        jQuery('#joinHover').bind('dialogclose', function() {
            window.location = GSType.hover.joinHover.loadOnExitUrl;
        });
    };
    this.cancelLoadOnExit = function() {
        GSType.hover.joinHover.loadOnExitUrl = null;
        jQuery('#joinHover').unbind('dialogclose');
    };
    this.showSignin = function() {
        if (GSType.hover.joinHover.loadOnExitUrl) {
            GSType.hover.signInHover.loadOnExit(GSType.hover.joinHover.loadOnExitUrl);
            GSType.hover.joinHover.cancelLoadOnExit();
        }
        GSType.hover.signInHover.showHover(jQuery('#joinHover #jemail').val(),
                jQuery('#joinHover .redirect_field').val(),
                GSType.hover.signInHover.showJoinFunction,
                GSType.hover.joinHover.onSubmitCallback);
        GSType.hover.joinHover.hide();
        return false;
    };
    this.showMssAutoHoverOnExit = function(schoolName, schoolId, schoolState) {
        GSType.hover.joinHover.configureForMss(schoolName, schoolId, schoolState);
        this.showHoverOnExit(GSType.hover.joinHover.showJoinAuto);
    };
    this.showNthHoverOnExit = function() {
        this.showHoverOnExit(GSType.hover.joinHover.showJoinNth);
    }
    this.showHoverOnExit = function(showHoverFunction) {
        var arr = getElementsByCondition(
                function(el) {
                    if (el.tagName == "A") {
                        if (el.target || el.onclick)
                            return false;
                        if (el.className && el.className.indexOf('no_interrupt') > -1)
                            return false;
                        if (el.href && el.href != '' && el.href != '#' && el.href != (window.location.href+'#'))
                            return el;
                    }
                    return false;
                }, document
                );

        for (var i = 0; i < arr.length; i++) {
            arr[i].onclick = function () {
                gRedirectAnchor = this;
                try {
                    //the reason this is hardcoded to mssAutoHover is because a new hover was added that requires exactly
                    //the same functionality as existing "mss auto hover on exit", but displays depending on school type
                    //Therefore, use the same cookie and don't mess too much with existing code at this time.
                    if (mssAutoHoverInterceptor.shouldIntercept('mssAutoHover')) {
                        window.destUrl = gRedirectAnchor.href;
                        // show hover
                        GSType.hover.joinHover.loadOnExit(gRedirectAnchor.href);
                        showHoverFunction();
                        return false;
                    }
                } catch (e) {
                }
                return true;
            };
        }
    };
    this.configureForMss = function(schoolName, schoolId, schoolState) {
        if (schoolName) {
            GSType.hover.joinHover.schoolName = schoolName;
        }
        if (schoolId) {
            jQuery('#joinHover .school_id').val(schoolId);
        }
        if (schoolState) {
            jQuery('#joinHover .school_state').val(schoolState);
        }
    };
    this.configureOmniture = function(pageName, hier1) {
        GSType.hover.joinHover.pageName=pageName;
        GSType.hover.joinHover.hier1=hier1;
    };
    this.showJoinAuto = function(schoolName, schoolId, schoolState) {
        jQuery('#joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        GSType.hover.joinHover.configureForMss(schoolName, schoolId, schoolState);
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Send me updates");
        GSType.hover.joinHover.setSubTitle("Keep tabs on " + GSType.hover.joinHover.schoolName,
                "Be the first to know when school performance data is released that affects your child.");
        // show nth / MSS
        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, true);

        GSType.hover.joinHover.setJoinHoverType("Auto");

        GSType.hover.joinHover.configureOmniture('MSS Join Hover', 'Hovers,Join,MSS Join Hover');

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinAuto;
        GSType.hover.joinHover.show();
    };
    this.showJoinChooserTipSheet = function(email) {
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("School Chooser tip sheet");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools",
                "for the best advice on choosing the right school for your family");
        // show city and state inputs
        jQuery('#joinHover .joinHover_location').show();

        // set label for weekly updates opt-in
        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, false, false);

        GSType.hover.joinHover.setJoinHoverType("ChooserTipSheet");

        GSType.hover.joinHover.configureOmniture('School Chooser Pack Join Hover', 'Hovers,Join,School Chooser Pack Join Hover');

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinChooserTipSheet;

        jQuery('#jemail').val(email);

        GSType.hover.joinHover.show();

        GSType.hover.joinHover.loadCities();

        jQuery('#joinBtn').click(GSType.hover.joinHover.chooserTipSheetClickHandler);
    };
    this.showSchoolReviewJoin = function(onSubmitCallback) {
        jQuery('#joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        GSType.hover.joinHover.baseFields();
        if (onSubmitCallback) {
            GSType.hover.joinHover.onSubmitCallback = onSubmitCallback;
        }
        GSType.hover.joinHover.setTitle("Almost done!");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools",
                " to submit your review. Once you verify your email address, your review will be posted, provided it meets our guidelines.");

        // set label for weekly updates opt-in
        if (GSType.hover.joinHover.schoolName) {
            GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, true);
        } else {
            GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);
        }

        GSType.hover.joinHover.setJoinHoverType("SchoolReview");
        jQuery('#joinHover_cancel').hide();

        GSType.hover.joinHover.configureOmniture('School Reviews Join Hover', 'Hovers,Join,School Reviews Join Hover');

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showSchoolReviewJoin;
        GSType.hover.joinHover.show();
    };
    this.showLearningDifficultiesNewsletter = function() {
        jQuery('#joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        GSType.hover.joinHover.onSubmitCallback = null;
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Learning Difficulties newsletter");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools",
                "to get the resources you need to support your child with a learning difficulty or attention problem");
        // show nth / MSS
        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);
        // show LD newsletter
        jQuery('#joinHover .joinHover_ld').show();

        //set up checkboxes
        jQuery('#joinHover #opt2').attr('checked', true);

        GSType.hover.joinHover.setJoinHoverType("LearningDifficultiesNewsletter");

        GSType.hover.joinHover.configureOmniture('Special Ed NL Join Hover', 'Hovers,Join,Special Ed NL Join Hover');

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showLearningDifficultiesNewsletter;
        GSType.hover.joinHover.show();
    };
    this.showBackToSchoolTipOfTheDay = function() {
        jQuery('#joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        GSType.hover.joinHover.onSubmitCallback = null;
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Back-to-School Tip of the Day");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools",
                "to get Back-to-School tips delivered straight to your inbox!");
        // show nth / MSS
        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);
        // show BTS tip
        jQuery('#joinHover .joinHover_btstip').show();
        // hide partners
        jQuery('#joinHover .joinHover_partners').hide(); 

        //set up checkboxes
        jQuery('#joinHover #opt4').attr('checked', true);

        GSType.hover.joinHover.setJoinHoverType("BTSTip");

        GSType.hover.joinHover.configureOmniture('Back to School Tips Join Hover', 'Hovers,Join,Back to School Tips Join Hover');

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showBackToSchoolTipOfTheDay;
        GSType.hover.joinHover.show();
    };
    this.showJoinPostComment = function() {
        jQuery('#joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        GSType.hover.joinHover.onSubmitCallback = null;
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Speak your mind");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools",
                "to participate in the parent community and other discussions on our site");
        // show nth / MSS
        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);

        GSType.hover.joinHover.setJoinHoverType("PostComment");

        GSType.hover.joinHover.configureOmniture('Community Join Hover', 'Hovers,Join,Community Join Hover');

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinPostComment;
        GSType.hover.joinHover.show();
    };
    this.showJoinTrackGrade = function() {
        GSType.hover.joinHover.setJoinHoverType("TrackGrade");
        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinTrackGrade;
        GSType.hover.joinHover.showJoinNth();
    };
    this.showJoinGlobalHeader = function() {
        GSType.hover.joinHover.setJoinHoverType("GlobalHeader");
        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinGlobalHeader;
        GSType.hover.joinHover.showJoinNth();
    };
    this.showJoinFooterNewsletter = function() {
        GSType.hover.joinHover.setJoinHoverType("FooterNewsletter");
        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinFooterNewsletter;
        GSType.hover.joinHover.showJoinNth();
    };
    this.showJoinNth = function() {
        jQuery('#joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        GSType.hover.joinHover.onSubmitCallback = null;
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Is your child on track?");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools",
                "to get the grade-by-grade tips you need to make smart choices about your child's education.");
        // show nth / MSS
        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);

        GSType.hover.joinHover.configureOmniture('Weekly NL Join Hover', 'Hovers,Join,Weekly NL Join Hover');

        GSType.hover.joinHover.show();
    };
    this.validateFirstName = function() {
        jQuery.getJSON(
                '/community/registrationValidationAjax.page',
        {firstName:jQuery('#joinGS #fName').val(), field:'firstName'},
                function(data) {
                    GSType.hover.joinHover.validateFieldResponse('#joinGS .joinHover_firstName .errors', 'firstName', data);
                });
    };
    this.validateEmail = function() {
        jQuery.getJSON(
                '/community/registrationValidationAjax.page',
        {email:jQuery('#joinGS #jemail').val(), field:'email'},
                function(data) {
                    GSType.hover.joinHover.validateFieldResponse('#joinGS .joinHover_email .errors', 'email', data);
                });
    };
    this.validateUsername = function() {
        jQuery.getJSON(
                '/community/registrationValidationAjax.page',
        {screenName:jQuery('#joinGS #uName').val(), email:jQuery('#joinGS #jemail').val(), field:'username'},
                function(data) {
                    GSType.hover.joinHover.validateFieldResponse('#joinGS .joinHover_username .errors', 'screenName', data);
                });
    };
    this.validatePassword = function() {
        jQuery.getJSON(
                '/community/registrationValidationAjax.page',
        {password:jQuery('#joinGS #jpword').val(), confirmPassword:jQuery('#joinGS #cpword').val(), field:'password'},
                function(data) {
                    GSType.hover.joinHover.validateFieldResponse('#joinGS .joinHover_password .errors', 'password', data);
                });
        GSType.hover.joinHover.validateConfirmPassword();
    };
    this.validateConfirmPassword = function() {
        jQuery.getJSON(
                '/community/registrationValidationAjax.page',
        {password:jQuery('#joinGS #jpword').val(), confirmPassword:jQuery('#joinGS #cpword').val(), field:'confirmPassword'},
                function(data) {
                    GSType.hover.joinHover.validateFieldResponse('#joinGS .joinHover_confirmPassword .errors', 'confirmPassword', data);
                });
    };
    this.validateFieldResponse = function(fieldSelector, fieldName, data) {
        var fieldError = jQuery(fieldSelector + ' .invalid');
        var fieldValid = jQuery(fieldSelector + ' .valid');
        fieldError.hide();
        fieldValid.hide();
        if (data && data[fieldName]) {
            fieldError.html(data[fieldName]);
            fieldError.show();
            if (fieldName == 'email') {
                jQuery('#joinGS .joinHover_email .invalid a.launchSignInHover').click(function() {
                    GSType.hover.joinHover.showSignin();
                    return false;
                });
            }
        } else {
            fieldValid.show();
        }
    };
    this.clickSubmitHandler = function() {
        var params = jQuery('#joinGS').serialize();
        jQuery('#joinBtn').attr('disabled', 'disabled');


        //if - Choose city - is selected, just remove this from the form, as if no city was given
        if (jQuery('#joinCity').val() == '- Choose city -') {
            params = params.replace(/&city=([^&]+)/, "");
        }

        var first = true;
        var newsletters = [];
        jQuery('#joinGS [name="grades"]').each(function() {
            if (jQuery(this).attr('checked')) {
                newsletters.push(encodeURIComponent(jQuery(this).val()));
            }
        });

        params += "&grades=" + newsletters.join(',');

        jQuery.getJSON("/community/registrationValidationAjax.page", params, GS.joinHover_checkValidationResponse);
        return false;
    };
    this.chooserTipSheetClickHandler = function() {
        var params = jQuery('#joinGS').serialize();
        jQuery('#joinBtn').attr('disabled', 'disabled');


        //if - Choose city - is selected, just remove this from the form, as if no city was given
        if (jQuery('#joinCity').val() == '- Choose city -') {
            params = params.replace(/&city=([^&]+)/, "");
        }

        var first = true;
        var newsletters = [];
        jQuery('#joinGS [name="grades"]').each(function() {
            if (jQuery(this).attr('checked')) {
                newsletters.push(encodeURIComponent(jQuery(this).val()));
            }
        });

        params += "&grades=" + newsletters.join(',');

        jQuery.getJSON("/community/registrationValidationAjax.page", params, GS.chooserHover_checkValidationResponse);
        return false;
    };
};
GSType.hover.JoinHover.prototype = new GSType.hover.HoverDialog('joinHover');

//SignInHover hover
GSType.hover.SignInHover = function() {
    this.showJoinFunction = GSType.hover.joinHover.showJoinTrackGrade;
    this.loadOnExitUrl = null;
    this.onSubmitCallback = null;
    this.loadDialog = function() {
        this.pageName='Sign In Hover';
        this.hier1='Hovers,Sign In,Sign In Hover';
        this.dialogByWidth(590);
        jQuery('#signInHover .redirect_field').val(window.location.href);
    };
    this.addMessage = function(text) {
        jQuery('#signInHover .messages').append('<p><span>\u00BB</span> ' + text + '</p>');
    };
    this.clearMessages = function() {
        jQuery('#signInHover .messages').empty();
        jQuery('#signInHover .errors .error').hide();
    };
    this.setEmail = function(email) {
        jQuery('#signInHover #semail').val(email);
    };
    this.setRedirect = function(redirect) {
        jQuery('#signInHover .redirect_field').val(redirect);
    };
    this.loadOnExit = function(url) {
        GSType.hover.signInHover.loadOnExitUrl = url;
        GSType.hover.signInHover.setRedirect(url);
        jQuery('#signInHover').bind('dialogclose', function() {
            window.location = GSType.hover.signInHover.loadOnExitUrl;
        });
    };
    this.cancelLoadOnExit = function() {
        GSType.hover.signInHover.loadOnExitUrl = null;
        jQuery('#signInHover').unbind('dialogclose');
    };
    this.validateFields = function() {
        jQuery('#signInHover .errors .error').hide();

        var params = {
            email: jQuery('#semail').val(),
            password: jQuery('#spword').val()
        };

        jQuery.getJSON('/community/registration/popup/loginValidationAjax.page', params,
                GSType.hover.signInHover.loginValidatorHandler);

        return false;
    };
    this.loginValidatorHandler = function(data) {
        jQuery('#signInHover .errors .error').hide();
        if (data.noSuchUser) {
            jQuery('#signInHover .errors .error').html(data.noSuchUser).show();
        } else if (data.userNoPassword) {
            GSType.hover.signInHover.clearMessages();
            GSType.hover.signInHover.hide();
            GSType.hover.joinHover.showJoinChooserTipSheet();
            GSType.hover.joinHover.addMessage(data.userNoPassword);
        } else if (data.userNotValidated) {
            GSType.hover.signInHover.clearMessages();
            GSType.hover.signInHover.hide();
            GSType.hover.emailNotValidated.setEmail(jQuery('#semail').val());
            GSType.hover.emailNotValidated.show();
        } else if (data.email) {
            jQuery('#signInHover .errors .error').html(data.email).show();
        } else if (data.userDeactivated) {
            jQuery('#signInHover .errors .error').html(data.userDeactivated).show();
        } else if (data.passwordMismatch) {
            jQuery('#signInHover .errors .error').html(data.passwordMismatch).show();
        } else {
            GSType.hover.signInHover.cancelLoadOnExit();
            if (GSType.hover.signInHover.onSubmitCallback) {
                GSType.hover.signInHover.onSubmitCallback(jQuery('#semail').val(), "signin");
            } else {
                jQuery('#signin').submit();
                GSType.hover.signInHover.hide();
            }
        }
    };
    this.showHover = function(email, redirect, showJoinFunction, onSubmitCallback) {
        if (onSubmitCallback) {
            GSType.hover.signInHover.onSubmitCallback = onSubmitCallback;
        } else {
            GSType.hover.signInHover.onSubmitCallback = null;
        }
        GSType.hover.signInHover.setEmail(email);
        GSType.hover.signInHover.setRedirect(redirect);
        if (showJoinFunction) {
            GSType.hover.signInHover.showJoinFunction = showJoinFunction;
        }
        jQuery('#signinBtn').click(GSType.hover.signInHover.validateFields);
        GSType.hover.signInHover.show();
        return false;
    };
    this.showJoin = function() {
        if (GSType.hover.signInHover.loadOnExitUrl) {
            GSType.hover.joinHover.loadOnExit(GSType.hover.signInHover.loadOnExitUrl);
            GSType.hover.signInHover.cancelLoadOnExit();
        }
        if (GSType.hover.signInHover.onSubmitCallback) {
            GSType.hover.joinHover.onSubmitCallback = GSType.hover.signInHover.onSubmitCallback;
        }
        GSType.hover.signInHover.hide();
        GSType.hover.signInHover.showJoinFunction();
        return false;
    };
    this.showForgotPassword = function() {
        if (GSType.hover.signInHover.loadOnExitUrl) {
            GSType.hover.forgotPassword.loadOnExit(GSType.hover.signInHover.loadOnExitUrl);
            GSType.hover.signInHover.cancelLoadOnExit();
        }
        GSType.hover.signInHover.hide();
        GSType.hover.forgotPassword.show();
        return false;
    }
};
GSType.hover.SignInHover.prototype = new GSType.hover.HoverDialog('signInHover');

//ValidateEditEmail Hover
GSType.hover.ValidateEditEmail = function() {
    this.loadDialog = function() {
        this.pageName='Verify Change Email Hover';
        this.hier1='Hovers,Verification,Verify Change Email Hover';
        this.dialogByWidth(640);
    }
};
GSType.hover.ValidateEditEmail.prototype = new GSType.hover.HoverDialog('valEditEmail');

//ValidateEmailHover Hover
GSType.hover.ValidateEmailHover = function() {
    this.loadDialog = function() {
        this.pageName='Verify Email Hover';
        this.hier1='Hovers,Verification,Verify Email Hover';
        this.dialogByWidth(640);
    }
};
GSType.hover.ValidateEmailHover.prototype = new GSType.hover.HoverDialog('valEmail');

//ValidateEmailSchoolReviewHover Hover
GSType.hover.ValidateEmailSchoolReviewHover = function() {
    this.loadDialog = function() {
        this.pageName='School Reviews Verify Email Hover';
        this.hier1='Hovers,Verification,School Reviews Verify Email Hover';
        this.dialogByWidth(640);
    }
};
GSType.hover.ValidateEmailSchoolReviewHover.prototype = new GSType.hover.HoverDialog('valEmailSchoolReview');


//ValidateLinkExpired hover
GSType.hover.ValidateLinkExpired = function() {
    this.email = '';
    this.loadDialog = function() {
        this.pageName='Email Verification Link Expired Hover';
        this.hier1='Hovers,Verification,Email Verification Link Expired Hover';
        this.dialogByWidth(640);
    };
    this.setEmail = function(email) {
        GSType.hover.validateLinkExpired.email = email;
    }
};
GSType.hover.ValidateLinkExpired.prototype = new GSType.hover.HoverDialog('expVer');

//EmailNotValidated hover
GSType.hover.EmailNotValidated = function() {
    this.email = '';
    this.loadDialog = function() {
        this.pageName='Email Not Verified Hover';
        this.hier1='Hovers,Verification,Email Not Verified Hover';
        this.dialogByWidth(640);
    };
    this.setEmail = function(email) {
        GSType.hover.emailNotValidated.email = email;
    };
};
GSType.hover.EmailNotValidated.prototype = new GSType.hover.HoverDialog('valNewEmail');

//Base School Review Thank You Hover
GSType.hover.SchoolReviewThankYou = function() {
    this.pageName = "";
    this.hierarchy = "";
    
    this.loadDialog = function() {
        this.dialogByWidth(640);
    };
    this.showHover = function() {
        this.setTitle("Thank you for submitting a review");
        this.setBody(this.body());
        jQuery('#schoolReviewThankYou').bind('dialogclose', this.onClose.gs_bind(this));
        this.sendOmnitureTrackingInfo();
        this.show();
    };
    this.sendOmnitureTrackingInfo = function() {
        pageTracking.pageName =  this.getPageName();
        pageTracking.hierarchy = this.getHierarchy();
        pageTracking.server = "www.greatschools.org";
        pageTracking.send();
    }
    this.onClose = function() {
    };
    //override in specific hovers!
    this.body = function() {
        return "Your review has been posted to GreatSchools";
    };
    this.setTitle = function(title) {
        jQuery('#schoolReviewThankYou h2').html(title);
    };
    this.setBody = function(body) {
        jQuery('#schoolReviewThankYou p strong').html(body);
    };
};
GSType.hover.SchoolReviewThankYou.prototype = new GSType.hover.HoverDialog("schoolReviewThankYou");

//School Review Posted Thank You
GSType.hover.SchoolReviewPostedThankYou = function() {
    this.body = function() {
        return "Your review has been posted to GreatSchools.";
    };
    this.onClose = function() {
        //window.location.reload();
    };
    this.getHierarchy = function() {
        return "Account,Registration,Existing Member Review Published Hover";
    };
    this.getPageName = function() {
        return "Existing Member Review Published Hover";
    };
};
GSType.hover.SchoolReviewPostedThankYou.prototype = new GSType.hover.SchoolReviewThankYou();

//School Review Not Posted Thank You
GSType.hover.SchoolReviewNotPostedThankYou = function() {
    this.body = function() {
        return "Please note that it can take up to 48 hours for your review to be posted to our site.";
    };
    this.getHierarchy = function() {
        return "Account,Registration,Existing Member Review Submitted Hover";
    };
    this.getPageName = function() {
        return "Existing Member Review Submitted Hover";
    };
};
GSType.hover.SchoolReviewNotPostedThankYou.prototype = new GSType.hover.SchoolReviewThankYou();

// Email Validated Review Hover
GSType.hover.EmailValidatedSchoolReview = function() {
    this.loadDialog = function() {
        this.dialogByWidth(640);
    };
    this.showHover = function(body) {
        GSType.hover.emailValidatedSchoolReview.setBody(body);
        GSType.hover.emailValidatedSchoolReview.show();
    };
    this.showPublished = function() {
        pageTracking.pageName =  "Account Verified Review Published Hover";
        pageTracking.hierarchy = "Account,Registration,Account Verified Review Published Hover";
        pageTracking.server = "www.greatschools.org";
        pageTracking.send();
        GSType.hover.emailValidatedSchoolReview.showHover("Your registration is complete and your review has been published.");
    };
    this.showQueued = function() {
        pageTracking.pageName =  "Account Verified Review Submitted Hover";
        pageTracking.hierarchy = "Account,Registration,Account Verified Review Submitted Hover";
        pageTracking.server = "www.greatschools.org";
        pageTracking.send();
        GSType.hover.emailValidatedSchoolReview.showHover("Your registration is complete. Please note that it can take up to 48 hours for your review to be posted to our site.");
    };
    this.setBody = function(body) {
        jQuery('#emailValidatedSchoolReviewHover span.fillMeIn').html(body);
    };
};
GSType.hover.EmailValidatedSchoolReview.prototype = new GSType.hover.HoverDialog("emailValidatedSchoolReviewHover");

//Email to a friend hover
GSType.hover.EmailToFriend = function() {
    this.loadDialog = function() {
        this.dialogByWidth(640);
    };
};
GSType.hover.EmailToFriend.prototype = new GSType.hover.HoverDialog("emailToFriendHover");


GSType.hover.PrincipalConfirmation = function() {
    this.loadDialog = function() {
      this.dialogByWidth(640);
    };

    this.show = function(id, state) {
        jQuery('#principalConfirmationForm #hdnSchoolId').val(id);
        jQuery('#principalConfirmationForm #hdnSchoolState').val(state);
        jQuery('#principalConfirmationForm').attr("action", "/school/principalComments.page");
        jQuery('#' + this.hoverId).dialog('open');
        return false;
    };
}
GSType.hover.PrincipalConfirmation.prototype = new GSType.hover.HoverDialog("principalConfirmationHover");

GSType.hover.PrincipalReviewSubmitted = function() {
    this.loadDialog = function() {
      jQuery('#principalReviewSubmittedHover').bind('dialogclose', this.onClose.gs_bind(this));
      this.dialogByWidth(640);
    };

    this.onClose = function() {
        window.location.reload();
    };
};
GSType.hover.PrincipalReviewSubmitted.prototype = new GSType.hover.HoverDialog("principalReviewSubmittedHover");

GSType.hover.forgotPassword = new GSType.hover.ForgotPasswordHover();
GSType.hover.emailValidated = new GSType.hover.EmailValidated();
GSType.hover.editEmailValidated = new GSType.hover.EditEmailValidated();
GSType.hover.emailNotValidated = new GSType.hover.EmailNotValidated();
GSType.hover.validateEmail = new GSType.hover.ValidateEmailHover();
GSType.hover.validateEmailSchoolReview = new GSType.hover.ValidateEmailSchoolReviewHover();
GSType.hover.joinHover = new GSType.hover.JoinHover();
GSType.hover.signInHover = new GSType.hover.SignInHover();
GSType.hover.validateEditEmail = new GSType.hover.ValidateEditEmail();
GSType.hover.validateLinkExpired = new GSType.hover.ValidateLinkExpired();

GSType.hover.schoolReviewPostedThankYou = new GSType.hover.SchoolReviewPostedThankYou();
GSType.hover.schoolReviewNotPostedThankYou = new GSType.hover.SchoolReviewNotPostedThankYou();
GSType.hover.emailValidatedSchoolReview = new GSType.hover.EmailValidatedSchoolReview();

GSType.hover.emailToFriend = new GSType.hover.EmailToFriend();
GSType.hover.principalConfirmation = new GSType.hover.PrincipalConfirmation();
GSType.hover.principalReviewSubmitted = new GSType.hover.PrincipalReviewSubmitted();

GS.forgotPasswordHover_checkValidationResponse = function(data) {
    GSType.hover.forgotPassword.clearMessages();

    if (data.errorMsg) {
        GSType.hover.forgotPassword.addMessage(data.errorMsg);
        return;
    }

    jQuery.post('/community/forgotPassword.page', jQuery('#hover_forgotPasswordForm').serialize());
    var email = jQuery('#fpemail').val();

    GSType.hover.signInHover.addMessage('An email has been sent to ' + email +
                                        ' with instructions for selecting a new password.');
    GSType.hover.forgotPassword.showSignin();
};

GS.isCookieSet = function(cookieName) {
    // if cookie cookieName exists, the cookie is set
    var value = readCookie(cookieName);
    return value != undefined && value.length > 0;
};
GS.getServerName = function() {
    // default to live
    var serverName = 'www';

    // check for staging
    if (location.hostname.match(/staging\.|staging$|clone/)) {
        serverName = 'staging';
    } else if (location.hostname.match(/dev\.|dev$|\.office\.|cmsqa|cpickslay\.|localhost|127\.0\.0\.1|macbook/)) {
        serverName = 'dev';
    }

    return serverName;
};
GS.isSignedIn = function() {
    return GS.isCookieSet('community_' + GS.getServerName());
};
GS.isMember = function() {
    return GS.isCookieSet('isMember');
};

GS.showJoinHover = function(email, signInRedirect, showJoinFunction, joinRedirect) {
    if (GS.isSignedIn()) {
        return true; // signed in users go straight to destination
    } else {
        GSType.hover.signInHover.setRedirect(signInRedirect);
        if (joinRedirect) {
            jQuery('#joinHover .redirect_field').val(joinRedirect);
        }
        if (GS.isMember()) {
            GSType.hover.signInHover.showHover(email, signInRedirect, showJoinFunction); // members get sign in hover
        } else {
            showJoinFunction(); // anons get join hover
        }
    }
    return false;
};

GS.showSchoolReviewHover = function(redirect) {
    if (GS.isSignedIn()) {
        return true; // signed in users go straight to destination
    } else {
        GSType.hover.signInHover.setRedirect(redirect);

        if (GS.isMember()) {
            GSType.hover.signInHover.showHover("", redirect, GSType.hover.joinHover.showSchoolReviewJoin, GS_postSchoolReview);
        } else {
            GSType.hover.joinHover.showSchoolReviewJoin(GS_postSchoolReview);
        }
    }
    return false;
};

GS.showChooserTipSheetHover = function(email, redirect) {
    if (GS.isSignedIn()) {
        return true; // signed in users go straight to destination
    } else {
        GSType.hover.signInHover.setRedirect(redirect);
        jQuery('#joinHover .redirect_field').val(redirect);
        if (GS.isMember()) {
            GSType.hover.signInHover.showHover(email, redirect, GSType.hover.joinHover.showJoinChooserTipSheet); // members get sign in hover
        } else {
            GSType.hover.joinHover.showJoinChooserTipSheet(email); // anons get join hover
        }
    }
    return false;
};

GS.showMssJoinHover = function(redirect, schoolName, schoolId, schoolState) {
    if (GS.isSignedIn()) {
        return true; // signed in users go straight to destination
    } else {
        GSType.hover.joinHover.configureForMss(schoolName, schoolId, schoolState);
        GSType.hover.signInHover.setRedirect(redirect);
        if (GS.isMember()) {
            GSType.hover.signInHover.showHover('', redirect, GSType.hover.joinHover.showJoinAuto);
        } else {
            GSType.hover.joinHover.showJoinAuto();
        }
    }
    return false;
};

//validates response for chooser tip sheet join hover. If validation passes, makes an ajax request to have tips email sent out, then submits join form so controller creates user
GS.chooserHover_checkValidationResponse = function(data) {

    if (GS.joinHover_passesValidationResponse(data)) {

        var emailVal = jQuery('input#cemail').val();

        if (emailVal != undefined) {
            var cks = new Array();
            jQuery('.ck').each(function () {
                if (this.checked) {
                    cks.push(this.name);
                }
            });
            jQuery.post("/promo/schoolChoicePackPromo.page",
            {email : emailVal, levels : cks.join(','), pageName : clickCapture.pageName, redirectForConfirm : window.location.href},
                    function(datax) {
                        omnitureEventNotifier.clear();
                        omnitureEventNotifier.successEvents = datax.omnitureTracking.successEvents;
                        omnitureEventNotifier.eVars = datax.omnitureTracking.eVars;
                        omnitureEventNotifier.send();
                        jQuery("#form_panel").hide();
                        jQuery("#confirm_panel").show();
                        jQuery('#joinHover .redirect_field').val(decodeURIComponent(datax.redirectEncoded));
                        jQuery('#joinGS').submit();
                        jQuery('#joinGS').submit(function() {
                            return false; // prevent multiple submits
                        });
                        GSType.hover.joinHover.hide();
                    }, "json");
        }
    }
    jQuery('#joinBtn').attr('disabled', '');
};

GS.joinHover_checkValidationResponse2 = function(data) {

    jQuery('#joinGS').submit();
    jQuery('#joinGS').submit(function() {
        return false; // prevent multiple submits
    });
    GSType.hover.joinHover.hide();

    jQuery('#joinBtn').attr('disabled', '');
};

GS.joinHover_checkValidationResponse = function(data) {
    if (GS.joinHover_passesValidationResponse(data)) {
        if (GSType.hover.joinHover.loadOnExitUrl) {
            GSType.hover.joinHover.cancelLoadOnExit();
        }
        if (GSType.hover.joinHover.onSubmitCallback) {
            GSType.hover.joinHover.onSubmitCallback(jQuery("#joinGS #jemail").val(), "joinGS");
        } else {
            jQuery('#joinGS').submit();
            jQuery('#joinGS').submit(function() {
                return false; // prevent multiple submits
            });
            GSType.hover.joinHover.hide();
        }
    }
    jQuery('#joinBtn').attr('disabled', '');
};

GS.joinHover_passesValidationResponse = function(data) {
    var firstNameError = jQuery('#joinGS .joinHover_firstName .invalid');
    var emailError = jQuery('#joinGS .joinHover_email .invalid');
    var usernameError = jQuery('#joinGS .joinHover_username .invalid');
    var usernameValid = jQuery('#joinGS .joinHover_username .valid');
    var passwordError = jQuery('#joinGS .joinHover_password .invalid');
    var confirmPasswordError = jQuery('#joinGS .joinHover_confirmPassword .invalid');
    var termsError = jQuery('#joinGS #joinHover_termsNotChecked');
    var locationError = jQuery('#joinGS #joinHover_chooseLocation');

    firstNameError.hide();
    emailError.hide();
    usernameError.hide();
    usernameValid.hide();
    passwordError.hide();
    confirmPasswordError.hide();
    termsError.hide();
    locationError.hide();

    var objCount = 0;
    for (_obj in data) objCount++;

    if (objCount > 0) {
        jQuery('#joinGS #process_error').show();

        if (data.terms) {
            termsError.html(data.terms).show();
        }

        if (data.state) {
            locationError.html(data.state).show();
        }
        if (data.city) {
            locationError.html(data.city).show();
        }

        if (data.firstName) {
            firstNameError.html(data.firstName).show();
        }

        if (data.email) {
            emailError.html(data.email).show();
            jQuery('#joinGS .joinHover_email .invalid a.launchSignInHover').click(function() {
                GSType.hover.joinHover.showSignin();
                return false;
            });
        }

        if (data.password) {
            passwordError.html(data.password).show();
        }

        if (data.confirmPassword) {
            confirmPasswordError.html(data.confirmPassword).show();
        }

        if (data.screenName) {
            usernameError.html(data.screenName).show();
        } else {
            usernameValid.show();
        }

        return false;
    } else {
        return true;
    }
};

jQuery(function() {
    GSType.hover.editEmailValidated.loadDialog();
    GSType.hover.emailNotValidated.loadDialog();
    GSType.hover.emailValidated.loadDialog();
    GSType.hover.forgotPassword.loadDialog();
    GSType.hover.joinHover.loadDialog();
    GSType.hover.signInHover.loadDialog();
    GSType.hover.validateEditEmail.loadDialog();
    GSType.hover.validateEmail.loadDialog();
    GSType.hover.validateEmailSchoolReview.loadDialog();
    GSType.hover.validateLinkExpired.loadDialog();

    GSType.hover.schoolReviewPostedThankYou.loadDialog();
    GSType.hover.schoolReviewNotPostedThankYou.loadDialog();
    GSType.hover.emailValidatedSchoolReview.loadDialog();

    GSType.hover.emailToFriend.loadDialog();
    GSType.hover.principalConfirmation.loadDialog();
    GSType.hover.principalReviewSubmitted.loadDialog();

    jQuery('#hover_forgotPasswordSubmit').click(function() {
        jQuery.getJSON('/community/forgotPasswordValidator.page',
                jQuery('#hover_forgotPasswordForm').serialize(),
                GS.forgotPasswordHover_checkValidationResponse);

        return false;
    });

    jQuery('#clsValNewEmail').click(function() {
        var params = {
            email: GSType.hover.emailNotValidated.email
        };
        jQuery.get('/community/requestEmailValidation.page', params);

        GSType.hover.emailNotValidated.hide();
    });

    jQuery('#clsExpVer').click(function() {
        var params = {
            email: GSType.hover.validateLinkExpired.email
        };
        jQuery.get('/community/requestEmailValidation.page', params);

        GSType.hover.validateLinkExpired.hide();
    });


    jQuery('#joinState').change(GSType.hover.joinHover.loadCities);

    jQuery('#joinHover_cancel').click(function() {
        GSType.hover.joinHover.hide();
        return false;
    });

    jQuery('#joinHover #fName').blur(GSType.hover.joinHover.validateFirstName);
    jQuery('#joinHover #jemail').blur(GSType.hover.joinHover.validateEmail);
    jQuery('#joinHover #uName').blur(GSType.hover.joinHover.validateUsername);
    jQuery('#joinHover #jpword').blur(GSType.hover.joinHover.validatePassword);
    jQuery('#joinHover #cpword').blur(GSType.hover.joinHover.validateConfirmPassword);

    jQuery('#joinHover').bind('dialogclose', function() {
        jQuery('#joinGS .error').hide();
        GSType.hover.joinHover.clearMessages();
    });

    jQuery('#joinHover #lnchSignin').click(GSType.hover.joinHover.showSignin);

    jQuery('#hover_forgotPassword').bind('dialogclose', function() {
        GSType.hover.forgotPassword.clearMessages();
    });

    GSType.hover.forgotPassword.clearMessages();

    jQuery('.signInHoverLink').click(function() {
        GSType.hover.signInHover.show();
    });


    jQuery('#signInHover').bind('dialogclose', GSType.hover.signInHover.clearMessages);

    jQuery('#signInHover_launchJoin').click(GSType.hover.signInHover.showJoin);
    jQuery('#signInHover_launchForgotPassword').click(GSType.hover.signInHover.showForgotPassword);

    jQuery('#signin').attr("action", "/community/loginOrRegister.page");

    jQuery('.joinAutoHover_showHover').click(function() {
        GSType.hover.joinHover.showJoinAuto('Alameda High School', 1, 'CA');
        return false;
    });
    jQuery('.joinChooserHover_showHover').click(function() {
        GSType.hover.joinHover.showJoinChooserTipSheet();
        return false;
    });
    jQuery('.joinLDHover_showHover').click(function() {
        GSType.hover.joinHover.showLearningDifficultiesNewsletter();
        return false;
    });
    jQuery('.joinPostCommentHover_showHover').click(function() {
        GSType.hover.joinHover.showJoinPostComment();
        return false;
    });
    jQuery('.joinTrackGradeHover_showHover').click(function() {
        GSType.hover.joinHover.showJoinTrackGrade();
        return false;
    });
    jQuery('.joinSchoolReview_showHover').click(function() {
        GSType.hover.joinHover.showSchoolReviewJoin(window.location.href);
        return false;
    });

    jQuery('.emailToFriend_showHover').click(function() {
        GSType.hover.emailToFriend.show();
        return false;
    });

    jQuery('#grdShow').click(function() {
        if (jQuery('#grdShow').hasClass('active')) {
            jQuery('#grdShow').removeClass('active');
            jQuery('#moreGrades').removeClass('show');
        } else {
            jQuery('#grdShow').addClass('active');
            jQuery('#moreGrades').addClass('show');
        }
    });

    jQuery('#gradeDone').click(function() {
        jQuery('#grdShow').removeClass('active');
        jQuery('#moreGrades').removeClass('show');
    });

    var sitePreferences = subCookie.getObject("site_pref");
    var showHover = "";

    if (sitePreferences != undefined && sitePreferences.showHover != undefined) {
        showHover = sitePreferences.showHover;
    }

    if (showHover == "validateEmail") {
        GSType.hover.validateEmail.show();
    } else if (showHover == "validateEmailSchoolReview") {
        GSType.hover.validateEmailSchoolReview.show();
    } else if (showHover == "schoolReviewPostedThankYou") {
        GSType.hover.schoolReviewPostedThankYou.showHover();
    } else if (showHover == "schoolReviewNotPostedThankYou") {
        GSType.hover.schoolReviewNotPostedThankYou.showHover();
    } else if (showHover == "emailValidated") {
        GSType.hover.emailValidated.show();
    } else if (showHover == "emailNotValidated") {
        GSType.hover.emailNotValidated.show();
    } else if (showHover == "editEmailValidated") {
        GSType.hover.editEmailValidated.show();
    } else if (showHover == "validateEditEmail") {
        GSType.hover.validateEditEmail.show();
    } else if (showHover == "validationLinkExpired") {
        GSType.hover.validateLinkExpired.show();
    } else if (showHover == "emailValidatedSchoolReviewPosted") {
        GSType.hover.emailValidatedSchoolReview.showPublished();
    } else if (showHover == "emailValidatedSchoolReviewQueued") {
        GSType.hover.emailValidatedSchoolReview.showQueued();
    } else if (showHover == "principalReviewSubmitted") {

        pageTracking.pageName =  "School Officials Comment Thanks Hover";
        pageTracking.hierarchy = "ESP,School Comment Thanks Hover";
        pageTracking.server = "www.greatschools.org";
        pageTracking.send();

        GSType.hover.principalReviewSubmitted.show();
    }

    subCookie.deleteObjectProperty("site_pref", "showHover");

});


