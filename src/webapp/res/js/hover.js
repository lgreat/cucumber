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
    this.show = function() {
        jQuery('#' + this.hoverId).dialog('open');
        return false;
    };
    this.hide = function() {
        jQuery('#' + this.hoverId).dialog('close');
        return false;
    };
    //template dialog to display based on variable width
    this.dialogByWidth = function (width) {
        jQuery('#' + this.hoverId).dialog({
            bgiframe: true,
            modal: true,
            draggable: false,
            autoOpen: false,
            resizable: false,
            width: width
        });
        jQuery('.' + this.hoverId + '_showHover').click(this.show.gs_bind(this));
        jQuery('.' + this.hoverId + '_hideHover').click(this.hide.gs_bind(this));
    };
};

//EditEmailValidated Hover
GSType.hover.EditEmailValidated = function() {
    this.loadDialog = function () {
        this.dialogByWidth(640);
    }
};
GSType.hover.EditEmailValidated.prototype = new GSType.hover.HoverDialog('valNewEmailDone');

//EmailValidated hover
GSType.hover.EmailValidated = function() {
    this.loadDialog = function () {
        this.dialogByWidth(640);
    }
};
GSType.hover.EmailValidated.prototype = new GSType.hover.HoverDialog('regDone');

//ForgotPasswordHover hover
GSType.hover.ForgotPasswordHover = function() {
    this.loadDialog = function() {
        this.dialogByWidth(590);
    };
    this.addMessage = function(text) {
        jQuery('#hover_forgotPassword .messages').append('<p>' + text + '</p>');
    };
    this.clearMessages = function() {
        jQuery('#hover_forgotPassword .messages').replaceWith('<div class="messages"><!-- not empty --></div>')
    };
    this.showJoin = function() {
        GSType.hover.forgotPassword.hide();
        GSType.hover.joinHover.show();
    };
};
GSType.hover.ForgotPasswordHover.prototype = new GSType.hover.HoverDialog('hover_forgotPassword');

//Join hover
GSType.hover.JoinHover = function() {
    this.schoolName = '';
    this.loadOnExitUrl = null;
    this.baseFields = function() {
        // hide city and state inputs
        jQuery('#joinHover .joinHover_location').hide();
        // hide nth / MSS
        jQuery('#joinHover li.grades p').hide();
        jQuery('#joinHover li.grades ul').hide();
        // hide LD newsletter
        jQuery('#joinHover li.joinHover_ld').hide();
    };
    this.setJoinHoverType = function(type) {
        jQuery('#joinHover form#joinGS input#joinHoverType').attr("value", type);
    };
    this.setTitle = function(title) {
        jQuery('#joinHover h2 span.hoverTitle').html(title);
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
    this.showMssAutoHoverOnExit = function(schoolName, schoolId, schoolState) {
        GSType.hover.joinHover.configureForMss(schoolName, schoolId, schoolState);
        var arr = getElementsByCondition(
            function(el) {
                if (el.tagName == "A") {
                    if (el.target || el.onclick)
                        return false;
                    if (el.href && el.href != '')
                        return el;
                }
                return false;
            }, document
        );

        for (var i=0;i < arr.length;i++) {
            arr[i].onclick = function () {
                gRedirectAnchor = this;
                try {
                    if (mssAutoHoverInterceptor.shouldIntercept('mssAutoHover')){
                        window.destUrl = gRedirectAnchor.href;
                        // show hover
                        GSType.hover.joinHover.loadOnExit(gRedirectAnchor.href);
                        GSType.hover.joinHover.showJoinAuto();
                        return false;
                    }
                } catch (e) {}
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
    this.showJoinAuto = function(schoolName, schoolId, schoolState) {
        GSType.hover.joinHover.configureForMss(schoolName, schoolId, schoolState);
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Send me updates");
        GSType.hover.joinHover.setSubTitle("Keep tabs on " + GSType.hover.joinHover.schoolName,
                "Be the first to know when school performance data is released that affects your child.");
        // show nth / MSS
        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, true);

        GSType.hover.joinHover.setJoinHoverType("Auto");

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinAuto;
        GSType.hover.joinHover.show();
    };
    this.showJoinChooserTipSheet = function() {
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("School Chooser tip sheet");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools",
                "for the best advice on choosing the right school for your family");
        // show city and state inputs
        jQuery('#joinHover joinHover_location').show();

        // set label for weekly updates opt-in
        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, false, false);

        GSType.hover.joinHover.setJoinHoverType("ChooserTipSheet");

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinChooserTipSheet;

        GSType.hover.joinHover.show();

        GSType.hover.joinHover.loadCities();
    };
    this.showLearningDifficultiesNewsletter = function() {
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Learning Difficulties newsletter");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools",
                "to get the resources you need to support your child with a learning difficulty or attention problem");
        // show nth / MSS
        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);
        // show LD newsletter
        jQuery('#joinHover .joinHover_ld').show();

        GSType.hover.joinHover.setJoinHoverType("LearningDifficultiesNewsletter");

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showLearningDifficultiesNewsletter;
        GSType.hover.joinHover.show();
    };
    this.showJoinPostComment = function() {
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Speak your mind");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools",
                "to participate in the parent community and other discussions on our site");
        // show nth / MSS
        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);

        GSType.hover.joinHover.setJoinHoverType("PostComment");

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinPostComment;
        GSType.hover.joinHover.show();
    };
    this.showJoinTrackGrade = function() {
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Is your child on track?");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools",
                "to get the grade-by-grade tips you need to make smart choices about your child's education.");
        // show nth / MSS
        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);

        GSType.hover.joinHover.setJoinHoverType("TrackGrade");
        // auto check grade-by-grade
        jQuery('#opt1').attr('checked', true);

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinTrackGrade;
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
        } else {
            fieldValid.show();
        }
    };
};
GSType.hover.JoinHover.prototype = new GSType.hover.HoverDialog('joinHover');

//SignInHover hover
GSType.hover.SignInHover = function() {
    this.showJoinFunction = GSType.hover.joinHover.showJoinTrackGrade;
    this.loadDialog = function() {
        this.dialogByWidth(590);
        jQuery('#signInHover .redirect_field').val(window.location.href);
    };
    this.addMessage = function(text) {
        jQuery('#signInHover .messages').append('<p>' + text + '</p>');
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
            GSType.hover.joinHover.show();
        } else if (data.userNotValidated) {
            GSType.hover.signInHover.clearMessages();
            GSType.hover.signInHover.hide();
            GSType.hover.emailNotValidated.show();
        } else if (data.email) {
            jQuery('#signInHover .errors .error').html(data.email).show();
        } else if (data.userDeactivated) {
            jQuery('#signInHover .errors .error').html(data.userDeactivated).show();
        } else if (data.passwordMismatch) {
            jQuery('#signInHover .errors .error').html(data.passwordMismatch).show();
        } else {
            jQuery('#signin').submit();

            GSType.hover.signInHover.hide();
        }
    };
    this.showHover = function(email, redirect, showJoinFunction) {
        GSType.hover.signInHover.setEmail(email);
        GSType.hover.signInHover.setRedirect(redirect);
        if (showJoinFunction) {
            GSType.hover.signInHover.showJoinFunction = showJoinFunction;
        }
        GSType.hover.signInHover.show();
        return false;
    };
    this.showJoin = function() {
        GSType.hover.signInHover.hide();
        GSType.hover.signInHover.showJoinFunction();
        return false;
    };
    this.showForgotPassword = function() {
        GSType.hover.signInHover.hide();
        GSType.hover.forgotPassword.show();
        return false;
    }
};
GSType.hover.SignInHover.prototype = new GSType.hover.HoverDialog('signInHover');

//ValidateEditEmail Hover
GSType.hover.ValidateEditEmail = function() {
    this.loadDialog = function() {
        this.dialogByWidth(640);
    }
};
GSType.hover.ValidateEditEmail.prototype = new GSType.hover.HoverDialog('valEditEmail');

//ValidateEmailHover Hover
GSType.hover.ValidateEmailHover = function() {
    this.loadDialog = function() {
        this.dialogByWidth(640);
    }
};
GSType.hover.ValidateEmailHover.prototype = new GSType.hover.HoverDialog('valEmail');

//ValidateLinkExpired hover
GSType.hover.ValidateLinkExpired = function() {
    this.loadDialog = function() {
        this.dialogByWidth(640);
    }
};
GSType.hover.ValidateLinkExpired.prototype = new GSType.hover.HoverDialog('expVer');

//EmailNotValidated hover
GSType.hover.EmailNotValidated = function() {
    this.loadDialog = function() {
        this.dialogByWidth(640);
    }
};
GSType.hover.EmailNotValidated.prototype = new GSType.hover.HoverDialog('valNewEmail');

GSType.hover.forgotPassword = new GSType.hover.ForgotPasswordHover();
GSType.hover.emailValidated = new GSType.hover.EmailValidated();
GSType.hover.editEmailValidated = new GSType.hover.EditEmailValidated();
GSType.hover.emailNotValidated = new GSType.hover.EmailNotValidated();
GSType.hover.validateEmail = new GSType.hover.ValidateEmailHover();
GSType.hover.joinHover = new GSType.hover.JoinHover();
GSType.hover.signInHover = new GSType.hover.SignInHover();
GSType.hover.validateEditEmail = new GSType.hover.ValidateEditEmail();
GSType.hover.validateLinkExpired = new GSType.hover.ValidateLinkExpired();

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
    GSType.hover.signInHover.show();
    GSType.hover.forgotPassword.hide();
};

GS.isCookieSet = function(cookieName){
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
    } else if (location.hostname.match(/dev\.|dev$|\.office\.|cpickslay\.|localhost|127\.0\.0\.1|macbook/)) {
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

GS.showJoinHover = function(email, redirect, showJoinFunction) {
    if (GS.isSignedIn()) {
        return true; // signed in users go straight to destination
    } else if (GS.isMember()) {
        GSType.hover.signInHover.showHover(email,redirect,showJoinFunction); // members get sign in hover
    } else {
        showJoinFunction(); // anons get join hover
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
            GSType.hover.signInHover.showHover('',redirect,GSType.hover.joinHover.showJoinAuto);
        } else {
            GSType.hover.joinHover.showJoinAuto();
        }
    }
    return false;
};

GS.joinHover_checkValidationResponse = function(data) {
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
    } else {
        jQuery('#joinGS').submit();
        GSType.hover.joinHover.hide();
    }

    return false;

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
    GSType.hover.validateLinkExpired.loadDialog();

    jQuery('#hover_forgotPasswordSubmit').click(function() {
        jQuery.getJSON('/community/forgotPasswordValidator.page',
                jQuery('#hover_forgotPasswordForm').serialize(),
                GS.forgotPasswordHover_checkValidationResponse);

        return false;
    });

    jQuery('#joinBtn').click(function() {
        var params = jQuery('#joinGS').serialize();

        //if - Choose city - is selected, just remove this from the form, as if no city was given
        if (jQuery('#joinCity').val() == '- Choose city -') {
            params = params.replace(/&city=([^&]+)/,"");
        }

        var first = true;
        var newsletters = [];
        // TODO: make selector more specific?
        jQuery('[name="grades"]').each(function() {
            if (jQuery(this).attr('checked')) {
                newsletters.push(encodeURIComponent(jQuery(this).val()));
            }
        });

        params += "&grades=" + newsletters.join(',');

        jQuery.getJSON("/community/registrationValidationAjax.page", params, GS.joinHover_checkValidationResponse);
        return false;
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
    });

    jQuery('#hover_forgotPassword').bind('dialogclose', function() {
        GSType.hover.forgotPassword.clearMessages();
    });

    jQuery('.signInHoverLink').click(function() {
        GSType.hover.signInHover.show();
    });

    jQuery('#signinBtn').click(GSType.hover.signInHover.validateFields);

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
    })
});
