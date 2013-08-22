/*
 Requires: /uri/Uri.js
 */

if (GS == undefined) {
    var GS = {};
}
if (GSType == undefined) {
    var GSType = {};
}
if (GSType.hover == undefined) {
    GSType.hover = {};
}
GS.community = GS.community || {};

Function.prototype.gs_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};
GS.log = GS.log || function() {
    if (GS.uri.Uri.getQueryData().logging == true) {
        console.log(arguments);
    }
};

GS.FACEBOOK_BUTTON_IN_JOIN_SELECTOR = '.js-facebook-login-join';
GS.FACEBOOK_BUTTON_IN_SIGNIN_SELECTOR = '.js-facebook-login-signin';

//HoverDialog requires the ID of the element to display as a hover dialog
GSType.hover.HoverDialog = function(id,width) {
    this.hoverId = id;
    this.pageName = '';
    this.hier1 = '';
    this.width = width;
    this.initialized = false;
    this.show = function() {
        this.showModal();
    };
    this.showModal = function(){
        var self = this;
        if (!this.initialized) {
            this.dialogByWidth();
            this.initialized = true;
        }
        ModalManager.showModal({
            'layerId' :  this.hoverId
        });
        var gs_eventclickclose = (GS.util.isBrowserTouch()) ? "touchstart" : "click";
        $(document).on(gs_eventclickclose,"#"+this.hoverId + ' .js_closeHover', function() {
            self.hide();
        });

        if (this.pageName != '') {
            pageTracking.clear();
            pageTracking.pageName = this.pageName;
            pageTracking.hierarchy = this.hier1;
            pageTracking.send();
        }
        return false;
    };
    this.hide = function() {
        ModalManager.hideModal({
            'layerId' : this.hoverId
        });
        return false;
    };
    //template dialog to display based on variable width
    this.dialogByWidth = function () {
//        var thisHover = jQuery('#' + this.hoverId);
//        thisHover.dialog({
//            bgiframe: true,
//            modal: true,
//            draggable: false,
//            autoOpen: false,
//            resizable: false,
//            width: this.width,
//            open: function(event, ui) {
//                window.setTimeout(function() {
//                    jQuery(document).unbind('mousedown.dialog-overlay')
//                        .unbind('mouseup.dialog-overlay');
//                }, 100);
//            },
//            zIndex: 15000
//        });
//        this.dialogByWidth = function () {
//            var thisHover = jQuery('#' + this.hoverId);
//            thisHover.dialog({
//                bgiframe: true,
//                modal: true,
//                draggable: false,
//                autoOpen: false,
//                resizable: false,
//                width: this.width,
//                open: function(event, ui) {
//                    window.setTimeout(function() {
//                        jQuery(document).unbind('mousedown.dialog-overlay')
//                            .unbind('mouseup.dialog-overlay');
//                    }, 100);
//                },
//                zIndex: 15000
//            });
//        thisHover.find('.' + this.hoverId + '_showHover').click(this.show.gs_bind(this));
//        thisHover.find('.' + this.hoverId + '_hideHover').click(this.hide.gs_bind(this));
    };
};

//EditEmailValidated Hover
GSType.hover.EditEmailValidated = function() {
    this.loadDialog = function () {
        this.pageName='Email Change Verified Hover';
        this.hier1='Hovers,Verification,Email Change Verified Hover';
        //this.dialogByWidth();
    }
};
GSType.hover.EditEmailValidated.prototype = new GSType.hover.HoverDialog('valNewEmailDone',640);

//EmailValidated hover
GSType.hover.EmailValidated = function() {
    this.loadDialog = function () {
        this.pageName='Account Verified Hover';
        this.hier1='Hovers,Verification,Account Verified Hover';
        //this.dialogByWidth();
    }
};
GSType.hover.EmailValidated.prototype = new GSType.hover.HoverDialog('regDone',640);

//SubscriptionEmailValidated hover
GSType.hover.SubscriptionEmailValidated = function() {
    this.loadDialog = function () {
        this.pageName='Email Module Verify Email Hover';
        this.hier1='Hovers,Verification,Email Module Verify Email Hover';
        //this.dialogByWidth();
    }
};
GSType.hover.SubscriptionEmailValidated.prototype = new GSType.hover.HoverDialog('subscriptionEmailValidated',640);

//ForgotPasswordHover hover
GSType.hover.ForgotPasswordHover = function() {
    this.loadOnExitUrl = null;
    this._osp = false;
    this.loadDialog = function() {
        this.pageName='Forgot Password Hover';
        this.hier1='Hovers,Sign In,Forgot Password Hover';
        //this.dialogByWidth();
    };
    this.addMessage = function(text) {
        var hoverForgotPassword = jQuery('#hover_forgotPassword');
        hoverForgotPassword.find('.messages .bd').html( text );
        hoverForgotPassword.find('.messages').show();
    };
    this.clearMessages = function() {
        var hoverForgotPassword = jQuery('#hover_forgotPassword');
        var hoverForgotPasswordMessages = hoverForgotPassword.find('.messages');
        hoverForgotPasswordMessages.find('.bd').empty();
        hoverForgotPasswordMessages.hide();
    };
    this.loadOnExit = function(url) {
        GSType.hover.forgotPassword.loadOnExitUrl = url;
        jQuery('#hover_forgotPassword').bind('dialogclose', function() {
            //alert('dialogclose #hover_forgotPassword);
            if (GSType.hover.forgotPassword) {
                window.location = GSType.hover.forgotPassword.loadOnExitUrl;
            }
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
        if(GSType.hover.forgotPassword.isOsp()) {
            GSType.hover.modalUspRegistration.show();
        }
        else {
            GSType.hover.signInHover.showJoinFunction();
        }
    };
    this.showSignin = function() {
        if (GSType.hover.forgotPassword.loadOnExitUrl) {
            GSType.hover.signInHover.loadOnExit(GSType.hover.forgotPassword.loadOnExitUrl);
            GSType.hover.forgotPassword.cancelLoadOnExit();
        }
        GSType.hover.forgotPassword.hide();
        if(GSType.hover.forgotPassword.isOsp()){
            GSType.hover.modalUspSignIn.show();
        }
        else{
            GSType.hover.signInHover.show();
        }
    };
    this.isOsp = function(){
        return this._osp;
    };
    this.setOsp = function(osp){
        this._osp = osp;
    };
};
GSType.hover.ForgotPasswordHover.prototype = new GSType.hover.HoverDialog('hover_forgotPassword',590);

//Join hover
GSType.hover.JoinHover = function() {
    this.schoolName = null;
    this.loadOnExitUrl = null;
    this.onSubmitCallback = null;
    this.onFacebookSubmitCallback = null;

    this.undoSimpleMssFields = function() {
        // show first name
        jQuery('#joinHover div.joinHover_firstName').show();
        // hide email label (short)
        jQuery('#joinHover div.joinLabel label.shortLabel').hide();
        // show email label (long)
        jQuery('#joinHover div.joinLabel label.longLabel').show();
        // hide confirm email
        jQuery('#joinHover div.joinHover_confirmEmail').hide();
        // show username
        jQuery('#joinHover div.joinHover_username').show();
        // show password
        jQuery('#joinHover div.joinHover_password').show();
        // show confirm password
        jQuery('#joinHover div.joinHover_confirmPassword').show();
        // show terms
        jQuery('#joinHover div.joinHover_terms').show();
        // formatting changes
        jQuery('#joinHover div.separator').show();
        jQuery('#joinHover div.separatorMss').hide();
        jQuery('#joinHover div.formHelperWrapper').show();
        jQuery('#joinHover div.formHelperSpacer').show();
        jQuery('#joinHover div.btstips').removeClass('size1of1').addClass('size15of19');

        jQuery('#joinHover div.joinSubmitconfirm').hide();
        jQuery('#joinHover div.joinSubmit').show();
        jQuery('#joinHover div.joinSubmit button').text('Join now'); // instead of Join now
        jQuery('#joinHover div.joinSubmit .lastUnit').show(); // instead of Join now
        // update partners text
        jQuery('#joinHover div.joinHover_partners label[for="opt3"]').html(
            'Send me offers to save on family activities and special ' +
                'promotions from our carefully chosen partners.');
    };
    this.showSimpleMssFields = function() {
        // hide first name
        jQuery('#joinHover div.joinHover_firstName').hide();
        // show email label (short)
        jQuery('#joinHover div.joinLabel label.shortLabel').show();
        // hide email label (long)
        jQuery('#joinHover div.joinLabel label.longLabel').hide();
        // show confirm email
        jQuery('#joinHover div.joinHover_confirmEmail').show();
        // hide username
        jQuery('#joinHover div.joinHover_username').hide();
        // hide password
        jQuery('#joinHover div.joinHover_password').hide();
        // hide confirm password
        jQuery('#joinHover div.joinHover_confirmPassword').hide();
        // hide terms
        jQuery('#joinHover div.joinHover_terms').hide();
        // formatting changes
        jQuery('#joinHover div.separator').hide();
        jQuery('#joinHover div.separatorMss').show();
        jQuery('#joinHover div.formHelperWrapper').hide();
        jQuery('#joinHover div.formHelperSpacer').hide();
        jQuery('#joinHover div.btstips').removeClass('size15of19').addClass('size1of1');

        jQuery('#joinHover div.joinSubmitconfirm').show();
        jQuery('#joinHover div.joinSubmit').hide();

        jQuery('#joinHover div.joinSubmitconfirm button').text('Sign up'); // instead of Join now
        jQuery('#joinHover div.joinSubmitconfirm .lastUnit').hide(); // instead of Join now
        // update partners text
        jQuery('#joinHover div.joinHover_partners label[for="opt3"]').html(
            'Send me offers to save on family activities and special ' +
                'promotions from GreatSchools and our carefully chosen partners.');
    };
    this.baseFields = function() {
        $(GS.FACEBOOK_BUTTON_IN_JOIN_SELECTOR).hide();
        // hide city and state inputs
        jQuery('#joinHover .joinHover_location').hide();
        // hide nth / MSS
        jQuery('#joinHover div.grades2').hide();
        //jQuery('#joinHover div .grades ul').hide();
        // hide LD newsletter
        jQuery('#joinHover div.joinHover_ld').hide();
        // hide BTS tip
        jQuery('#joinHover div.joinHover_btstip').hide();
        //check checkbox for greatnews
        jQuery('#joinHover #opt1').prop('checked', true);
        this.undoSimpleMssFields();
    };
    //sets a notification message on the join form - can be used to explain why this hover was launched
    this.addMessage = function(text) {
        jQuery('#joinHover .message').html(text).show();
    };
    //method is plural to remain consistent with other hovers. Should always get called when hover closes
    this.clearMessages = function() {
        jQuery('#joinHover .valid').hide();
        jQuery('#joinHover .invalid').hide();
        jQuery('#joinHover .alert').hide();
    };
    this.setJoinHoverType = function(type) {
        jQuery('#joinHover form#joinGS input#joinHoverType').val(type);
    };
    this.setTitle = function(title) {
        jQuery('#joinHover').find('.js-smallSpacingTitle').html(title);
    };
    this.setSubTitle = function(subTitle) {
        jQuery('#joinHover').find('.js-joinSubtitle').html(subTitle);
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
            // GS-11161
            jQuery('#joinHover div.grades2').show();
        }

        jQuery('#joinHover div.grades label[for="opt1"]').html(labelTextPrefix + labelPhrases);
    };
    // GS-11161
    this.configAndShowEmailTipsMssLabelNew = function()
    {
        var labelTextPrefix = "Sign me up for";
        var labelPhrases = " the <em>GreatSchools Weekly</em> &ndash; full of practical tips and grade-by-grade " +
            "information to help you support your child's education.";

        jQuery('#joinHover div.grades label[for="opt1"]').html(labelTextPrefix + labelPhrases);
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
        // TODO-10568
        //GSType.hover.joinHover.dialogByWidth();
        var joinHover = jQuery('#joinHover');
        joinHover.find('.redirect_field').val(window.location.href);
    };
    this.loadOnExit = function(url) {
        GSType.hover.joinHover.loadOnExitUrl = url;
        var joinHover = jQuery('#joinHover');
        joinHover.find('.redirect_field').val(url);
        joinHover.bind('dialogclose', function() {
            //alert('dialogclose #joinHover');
            if (GSType.hover.joinHover.loadOnExitUrl) {
                window.location = GSType.hover.joinHover.loadOnExitUrl;
            }
        });
    };
    this.executeOnExit = function(f) {
        var joinHover = jQuery('#joinHover');
        joinHover.bind('dialogclose', function() {
            //alert('dialogclose executeOnExit #joinHover');
            f();
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
        var joinEmail = jQuery('#joinHover #jemail').val();
        var joinRedirect = jQuery('#joinHover .redirect_field').val();
        GSType.hover.joinHover.hide();
        GSType.hover.signInHover.showHover(joinEmail,
            joinRedirect,
            GSType.hover.signInHover.showJoinFunction,
            GSType.hover.joinHover.onSubmitCallback);
        return false;
    };
    this.showMssAutoHoverOnExit = function(schoolName, schoolId, schoolState) {
        GSType.hover.sendMeUpdates.configureSchoolInfo('', schoolName, schoolId, schoolState);
        this.showHoverOnExit(GSType.hover.sendMeUpdates.showSendUpdates);
    };
    this.showNthHoverOnExit = function() {
        this.showHoverOnExit(GSType.hover.joinHover.showJoinTrackGradeAuto);
    };
    this.showHoverOnExit = function(showHoverFunction) {
        var arr = GS.getElementsByCondition(
            function(el) {
                if (el.tagName == "A") {
                    if (el.parentNode.parentNode.parentNode.className === 'gsTabs') { // meh, what's best way to ignore the links that are part of profile tabs?
                        return false;
                    }
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
                        var threeMinuteDuration = getCookieExpiresDate(0,0,3);
                        createCookieWithExpiresDate('seenHoverOnExitRecently','1',threeMinuteDuration);
                        window.destUrl = gRedirectAnchor.href;
                        // show hover
                        GSType.hover.joinHover.loadOnExit(gRedirectAnchor.href);
                        GSType.hover.sendMeUpdates.configureSchoolInfo(gRedirectAnchor.href);
                        showHoverFunction();
                        return false;
                    }
                } catch (e) {
                }
                return true;
            };
        }
    };

    this.defaultPostInterruptCallback = function($anchor) {
        //alert('default callback');
        if (GSType.hover.joinHover.loadOnExitUrl) {
            window.location = GSType.hover.joinHover.loadOnExitUrl;
        }
    };

    // just a newer version of showMssAutoHoverOnExit, built for new Profile, and uses new showInterruptHoverOnPageExit()
    this.showMssAutoHoverOnPageExit = function(schoolName, schoolId, schoolState, postInterruptCallback) {
        GSType.hover.sendMeUpdates.configureSchoolInfo(undefined, schoolName, schoolId, schoolState);
        this.showInterruptHoverOnPageExit(GSType.hover.sendMeUpdates.showSendUpdates, postInterruptCallback);
    };
    // just a newer version of showNthHoverOnExit, built for new Profile, and uses new showInterruptHoverOnPageExit()
    this.showNthHoverOnPageExit = function(postInterruptCallback) {
        this.showInterruptHoverOnPageExit(GSType.hover.joinHover.showJoinTrackGradeAuto, postInterruptCallback);
    };
    // new version of showHoverOnExit
    this.showInterruptHoverOnPageExit = function(showHoverFunction, postInterruptCallback) {
        var self = this;
        // automatically ignore any links with class no_interrupt
        // assumes this is the first event executed when 'click' is triggered

        $('a:not(.no_interrupt)').bindFirst('click', function(event) {
            var $this = $(this);
            var href = $this.attr('href');

            if (!(href && href !== '' && href !== '#' && href !== (window.location.href+'#'))) {
                return true;
            } else if ($this.attr('target') || $this.attr('onclick')){
                return true;
            }

            //the reason this is hardcoded to mssAutoHover is because a new hover was added that requires exactly
            //the same functionality as existing "mss auto hover on exit", but displays depending on school type
            //Therefore, use the same cookie and don't mess too much with existing code at this time.
            if (mssAutoHoverInterceptor.shouldIntercept('mssAutoHover')) {
                var threeMinuteDuration = getCookieExpiresDate(0,0,3);
                createCookieWithExpiresDate('seenHoverOnExitRecently','1',threeMinuteDuration);
                window.destUrl = href;
                GSType.hover.joinHover.loadOnExitUrl = href;
                $('#joinHover .redirect_field').val(href);
                GSType.hover.sendMeUpdates.configureSchoolInfo(href);
                //alert(href);

                GSType.hover.joinHover.executeOnExit(function() {
                    $this.trigger('click');
                });

                // deferreds will be used by modals that dont support the GSType.hover.joinHover.executeOnExit method above
                var deferred = showHoverFunction();
                if (deferred && deferred.hasOwnProperty('always')) {
                    deferred.always(function() {
                        $this.trigger('click');
                    });
                }

                event.stopImmediatePropagation();
                return false;
            } else {
                return true;
            }
        });
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
        GS.log("test");
        jQuery('.joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        GSType.hover.joinHover.configureForMss(schoolName, schoolId, schoolState);
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Send me updates");
        // GS-11161
        GSType.hover.joinHover.setSubTitle("Get timely updates for " + GSType.hover.joinHover.schoolName +
            ", including performance data and recently posted user reviews.");
        // show nth / MSS
        // GS-11161
        //GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, true);
//        GSType.hover.joinHover.configAndShowEmailTipsMssLabelNew();

//        GSType.hover.joinHover.showSimpleMssFields();

        GSType.hover.joinHover.setJoinHoverType("Auto");

        GSType.hover.joinHover.configureOmniture('MSS Join Hover', 'Hovers,Join,MSS Join Hover');

        GSType.hover.signInHover.showJoinFunction = GSType.hover.sendMeUpdates.showSendUpdates;
        GSType.hover.joinHover.show();
    };
    this.showSchoolReviewJoin = function(onSubmitCallback) {
        jQuery('.joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        GSType.hover.joinHover.baseFields();
        if (onSubmitCallback) {
            GSType.hover.joinHover.onSubmitCallback = onSubmitCallback;
        }
        GSType.hover.joinHover.setTitle("Almost done!");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools to submit your review. Once you verify your email address, your review will be posted, provided it meets our guidelines.");

        // set label for weekly updates opt-in
//        if (GSType.hover.joinHover.schoolName) {
//            GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, true);
//        } else {
//            GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);
//        }

        GSType.hover.joinHover.setJoinHoverType("SchoolReview");

        GSType.hover.joinHover.configureOmniture('School Reviews Join Hover', 'Hovers,Join,School Reviews Join Hover');

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showSchoolReviewJoin;
        GSType.hover.joinHover.show();
    };
    this.showLearningDifficultiesNewsletter = function() {
        jQuery('.joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        GSType.hover.joinHover.onSubmitCallback = null;
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Special Education newsletter");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools to get the resources you need to support your child with a learning difficulty or attention problem");
        // show nth / MSS
//        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);
        // show LD newsletter
        jQuery('#joinHover .joinHover_ld').show();

        //set up checkboxes
        jQuery('#joinHover #opt2').prop('checked', true);

        GSType.hover.joinHover.setJoinHoverType("LearningDifficultiesNewsletter");

        GSType.hover.joinHover.configureOmniture('Special Ed NL Join Hover', 'Hovers,Join,Special Ed NL Join Hover');

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showLearningDifficultiesNewsletter;
        GSType.hover.joinHover.show();
    };
    this.showBackToSchoolTipOfTheDay = function() {
        jQuery('.joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        GSType.hover.joinHover.onSubmitCallback = null;
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Back-to-School Tip of the Day");
        GSType.hover.joinHover.setSubTitle("Join GreatSchools to get Back-to-School tips delivered straight to your inbox!");
        // show nth / MSS
//        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);
        // show BTS tip
        jQuery('#joinHover .joinHover_btstip').show();
        // hide partners
        jQuery('#joinHover .joinHover_partners').hide();

        //set up checkboxes
        jQuery('#joinHover #opt4').prop('checked', true);

        GSType.hover.joinHover.setJoinHoverType("BTSTip");

        GSType.hover.joinHover.configureOmniture('Back to School Tips Join Hover', 'Hovers,Join,Back to School Tips Join Hover');

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showBackToSchoolTipOfTheDay;
        GSType.hover.joinHover.show();
    };
    this.showJoinPostComment = function() {
        jQuery('.joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        GSType.hover.joinHover.onSubmitCallback = null;
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Join GreatSchools");
        GSType.hover.joinHover.setSubTitle("Create an account to participate in the parent community and other discussions on our site");
        // show nth / MSS
//        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);

        jQuery(GS.FACEBOOK_BUTTON_IN_JOIN_SELECTOR).off('click.joinHover');
        jQuery(GS.FACEBOOK_BUTTON_IN_JOIN_SELECTOR).on('click.joinHover', function() {
            ModalManager.hideModal({layerId:'joinHover'});
            GS.facebook.login().done(function(data) {
                if (GSType.hover.joinHover.onSubmitCallback) {
                    GSType.hover.joinHover.onSubmitCallback(data.email);
                }
            });
        });

        GSType.hover.joinHover.setJoinHoverType("PostComment");

        GSType.hover.joinHover.configureOmniture('Community Join Hover', 'Hovers,Join,Community Join Hover');

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinPostComment;
        $(GS.FACEBOOK_BUTTON_IN_JOIN_SELECTOR).show();
        GSType.hover.joinHover.show();
    };
    this.showJoinTrackGrade = function() {
        GSType.hover.joinHover.setJoinHoverType("TrackGrade");
        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinTrackGrade;
        GSType.hover.joinHover.showJoinNth();
    };
    this.showJoinTrackGradeAuto = function() {
        GSType.hover.joinHover.setJoinHoverType("TrackGradeAuto");
        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinTrackGradeAuto;
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
    // WARNING: do not call showJoinNth directly without first calling setJoinHoverType with a valid value from
    // RegistrationHoverCommand.JoinHoverType enum and modifying RegistrationHoverController.joinTypeToHow()
    // see GS-13450
    this.showJoinNth = function() {
        jQuery('.joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        jQuery(GS.FACEBOOK_BUTTON_IN_JOIN_SELECTOR).off('click.joinHover');
        jQuery(GS.FACEBOOK_BUTTON_IN_JOIN_SELECTOR).on('click.joinHover', function() {
            ModalManager.hideModal({layerId:'joinHover'});
            GS.facebook.login().done(function(data) {
                if (GSType.hover.joinHover.onSubmitCallback) {
                    GSType.hover.joinHover.onSubmitCallback(data.email);
                }
            });
        });
        GSType.hover.joinHover.onSubmitCallback = null;
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Join GreatSchools");
        GSType.hover.joinHover.setSubTitle("Sign in with Facebook or create a new account.");
        // show nth / MSS
//        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);

        GSType.hover.joinHover.configureOmniture('Weekly NL Join Hover', 'Hovers,Join,Weekly NL Join Hover');

        GSType.hover.joinHover.show();
        $(GS.FACEBOOK_BUTTON_IN_JOIN_SELECTOR).show();
    };
    this.showJoinMsl = function() {
        jQuery('.joinBtn').click(GSType.hover.joinHover.clickSubmitHandler);
        jQuery(GS.FACEBOOK_BUTTON_IN_JOIN_SELECTOR).off('click.joinHover');
        jQuery(GS.FACEBOOK_BUTTON_IN_JOIN_SELECTOR).on('click.joinHover', function() {
            ModalManager.hideModal({layerId:'joinHover'});
            GS.facebook.login().done(function(data) {
                GSType.hover.joinHover.onFacebookSubmitCallback(data);
            });
        });
        $(GS.FACEBOOK_BUTTON_IN_JOIN_SELECTOR).show();
//        GSType.hover.joinHover.configureForMss(schoolName, schoolId, schoolState);
        GSType.hover.joinHover.baseFields();
        GSType.hover.joinHover.setTitle("Join GreatSchools");
        GSType.hover.joinHover.setSubTitle("Create an account to save one or more schools to your personalized list.");
        // show nth / MSS
//        GSType.hover.joinHover.configAndShowEmailTipsMssLabel(true, true, false);

        GSType.hover.joinHover.setJoinHoverType("MSL");

        GSType.hover.joinHover.configureOmniture('MSL Join Hover', 'Hovers,Join,MSL Join Hover');

        GSType.hover.signInHover.showJoinFunction = GSType.hover.joinHover.showJoinMsl;
        GSType.hover.joinHover.show();
        $(GS.FACEBOOK_BUTTON_IN_JOIN_SELECTOR).show();
    };
//    this.validateFirstName = function() {
//        jQuery.getJSON(
//            GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
//            {firstName:jQuery('#joinGS #fName').val(), field:'firstName'},
//            function(data) {
//                GSType.hover.joinHover.validateFieldResponse('#joinGS .joinHover_firstName .errors', 'firstName', data);
//            });
//    };
    this.validateEmail = function() {
        jQuery.getJSON(
            GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
            {email:jQuery('#joinGS #jemail').val(), field:'email', simpleMss: (jQuery('#joinHoverType').val() === 'Auto')},
            function(data) {
                GSType.hover.joinHover.validateFieldResponse('#joinGS .joinHover_email .errors', 'email', data);
            });
    };
//    this.validateConfirmEmail = function() {
//        jQuery.getJSON(
//            GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
//            {email:jQuery('#joinGS #jemail').val(), confirmEmail:jQuery('#joinGS #jcemail').val(), field:'confirmEmail', simpleMss: (jQuery('#joinHoverType').val() === 'Auto')},
//            function(data) {
//                GSType.hover.joinHover.validateFieldResponse('#joinGS .joinHover_confirmEmail .errors', 'confirmEmail', data);
//            });
//    };
//    this.validateUsername = function() {
//        jQuery.getJSON(
//            GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
//            {screenName:jQuery('#joinGS #uName').val(), email:jQuery('#joinGS #jemail').val(), field:'username'},
//            function(data) {
//                GSType.hover.joinHover.validateFieldResponse('#joinGS .joinHover_username .errors', 'screenName', data);
//            });
//    };
    this.validatePassword = function() {
        jQuery.getJSON(
            GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
            {password:jQuery('#joinGS #jpword').val(), confirmPassword:jQuery('#joinGS #cpword').val(), field:'password'},
            function(data) {
                GSType.hover.joinHover.validateFieldResponse('#joinGS .joinHover_password .errors', 'password', data);
            });
        GSType.hover.joinHover.validateConfirmPassword();
    };
    this.validateConfirmPassword = function() {
        jQuery.getJSON(
            GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
            {password:jQuery('#joinGS #jpword').val(), confirmPassword:jQuery('#joinGS #cpword').val(), field:'confirmPassword'},
            function(data) {
                GSType.hover.joinHover.validateFieldResponse('#joinGS .joinHover_confirmPassword .errors', 'confirmPassword', data);
            });
    };
    this.validateFieldResponse = function(fieldSelector, fieldName, data) {
        var errorIcon ='<span class="iconx16 i-16-alert "><!-- do not collapse --></span> ';
        var fieldError = jQuery(fieldSelector + ' .invalid');
        var fieldValid = jQuery(fieldSelector + ' .valid');
        fieldError.hide();
        fieldValid.hide();
        if (data && data[fieldName]) {
            fieldError.html(errorIcon+data[fieldName]);
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
//    jQuery('.js_closeJoinHover').click(function() {
//        GSType.hover.joinHover.hide();
//        return false;
//    });
    this.clickSubmitHandler = function() {
        GS.log('entered clickSubmitHAndler');
        var params = jQuery('#joinGS').serialize();
        jQuery('.joinBtn').prop('disabled', true);


        //if - Choose city - is selected, just remove this from the form, as if no city was given
//        if (jQuery('#joinCity').val() == '- Choose city -') {
//            params = params.replace(/&city=([^&]+)/, "");
//        }

        var first = true;
        var newsletters = [];
        jQuery('#joinGS [name="grades"]').each(function() {
            if (jQuery(this).prop('checked')) {
                newsletters.push(encodeURIComponent(jQuery(this).val()));
            }
        });

        params += "&grades=" + newsletters.join(',');

        var mssHover = false;
        if(jQuery('#joinHoverType').val() === 'Auto') {
            mssHover = true;
        }

        if (mssHover) {
            params += "&simpleMss=" + mssHover;
        } else {
            params += "&simple=true";
        }

        GS.log('before if(MssHover)');
        if(mssHover) {
            jQuery.getJSON(GS.uri.Uri.getBaseHostname() + "/community/registrationValidationAjax.page", params, function(data) {
                GS.joinHover_checkValidationResponse(data, true);
            });
        }
        else {
            jQuery.getJSON(GS.uri.Uri.getBaseHostname() + "/community/registrationValidationAjax.page", params, function(data) {
                GS.joinHover_checkValidationResponse(data, false);
            });
        }
        GS.log('returning from clickSubmitHandler');
        return false;
    };
};
GSType.hover.JoinHover.prototype = new GSType.hover.HoverDialog('joinHover',555);

//SignInHover hover
GSType.hover.SignInHover = function() {
    this.showJoinFunction = GSType.hover.joinHover.showJoinTrackGrade;
    this.loadOnExitUrl = null;
    this.onSubmitCallback = null;
    this.onFacebookSubmitCallback = null;
    this.loadDialog = function() {
        this.pageName='Sign In Hover';
        this.hier1='Hovers,Sign In,Sign In Hover';
        //this.dialogByWidth();
        var signInHover = jQuery('#signInHover');
        signInHover.find('.redirect_field').val(window.location.href);
    };
    this.addMessage = function(text) {
        jQuery('#signInHover .messages').append('<p><span>\u00BB</span> ' + text + '</p>');
    };
    this.clearMessages = function() {
        var signInHover = jQuery('#signInHover');
        signInHover.find('.messages').empty();
        signInHover.find('.errors .error').hide();
    };
    this.setEmail = function(email) {
        jQuery('#signInHover .setemail').val(email);
    };
    this.setRedirect = function(redirect) {
        var signInHover = jQuery('#signInHover');
        signInHover.find('.redirect_field').val(redirect);
    };
    this.loadOnExit = function(url) {
        GSType.hover.signInHover.loadOnExitUrl = url;
        GSType.hover.signInHover.setRedirect(url);
        jQuery('#signInHover').bind('dialogclose', function() {
            //alert('dialogclose for #signInHover executed');
            if (GSType.hover.signInHover.loadOnExitUrl) {
                window.location = GSType.hover.signInHover.loadOnExitUrl;
            }
        });
    };
    this.cancelLoadOnExit = function() {
        //alert('unbinding... #signInHover and #fullPageOverlay');
        GSType.hover.signInHover.loadOnExitUrl = null;
        jQuery('#signInHover').unbind('dialogclose');
         };
    this.validateFields = function() {

        jQuery('#signInHover .errors .error').hide();

        var params = {
            email: jQuery('#semail').val(),
            password: jQuery('#spword').val()
        };

        jQuery.getJSON(GS.uri.Uri.getBaseHostname() + '/community/registration/popup/loginValidationAjax.page', params,
            GSType.hover.signInHover.loginValidatorHandler);

        return false;
    };
    this.loginValidatorHandler = function(data) {
        var errorIcon = '<span class="iconx16 i-16-alert "><!-- do not collapse --></span> ';
        jQuery('#signInHover .errors .error').hide();
        if (data.noSuchUser) {
            jQuery('#signInHover .errors .error').html(errorIcon + data.noSuchUser).show();
        } else if (data.userNoPassword) {
            GSType.hover.signInHover.clearMessages();
            GSType.hover.signInHover.hide();
            GSType.hover.joinHover.showJoinGlobalHeader();
            GSType.hover.joinHover.addMessage(errorIcon + data.userNoPassword);
        } else if (data.userNotValidated) {
            GSType.hover.emailNotValidated.setEmail(jQuery('#semail').val()); // must happen before hide
            GSType.hover.signInHover.clearMessages();
            GSType.hover.signInHover.hide();
            GSType.hover.emailNotValidated.show();
        } else if (data.email) {
            jQuery('#signInHover .errors .error').html(errorIcon + data.email).show();
        } else if (data.userDeactivated) {
            jQuery('#signInHover .errors .error').html(errorIcon + data.userDeactivated).show();
        } else if (data.passwordMismatch) {
            jQuery('#signInHover .errors .error2').html(errorIcon + data.passwordMismatch).show();
        } else if (data.facebookUser) {
            jQuery('#signInHover .errors .error2').html(errorIcon + data.facebookUser).show();
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
//        $(".js-facebookSignInModule").removeClass("dn");
        GSType.hover.signInHover.clearMessages();
        GSType.hover.signInHover.setEmail(email);
        GSType.hover.signInHover.setRedirect(redirect);
        if (showJoinFunction) {
            GSType.hover.signInHover.showJoinFunction = showJoinFunction;
        }
        jQuery('#signinBtn').click(GSType.hover.signInHover.validateFields);

        jQuery(GS.FACEBOOK_BUTTON_IN_SIGNIN_SELECTOR).off('click.signinHover');
        jQuery(GS.FACEBOOK_BUTTON_IN_SIGNIN_SELECTOR).on('click.signinHover', function() {
            ModalManager.hideModal({layerId:'signInHover'});
            GS.facebook.login().done(function(data) {
                if (GSType.hover.signInHover.onSubmitCallback) {
                    GSType.hover.signInHover.onSubmitCallback(data.email);
                }
            });
        });

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
GSType.hover.SignInHover.prototype = new GSType.hover.HoverDialog('signInHover',590);

//ValidateEditEmail Hover
GSType.hover.ValidateEditEmail = function() {
    this.loadDialog = function() {
        this.pageName='Verify Change Email Hover';
        this.hier1='Hovers,Verification,Verify Change Email Hover';
        //this.dialogByWidth();
    }
};
GSType.hover.ValidateEditEmail.prototype = new GSType.hover.HoverDialog('valEditEmail',640);

//PrintSchoolChooser Hover
GSType.hover.PrintSchoolChooser = function() {
    this.loadDialog = function() {
    }
    this.populateModal = function () {

        var container = $('#js-pyoc');
        container.empty();

        $('#js-schoolListData .standard_1-1').each(function () {
            var $this = $(this);
            var stateID = $this.find('.compare_checkbox').val();
            var output = [stateID.slice(0, 2), ',', stateID.slice(2)].join('');
            var cityState = $this.find('.js-pyocCityState').html().split(' ',2);
            var city = cityState[0];
            var state = cityState[1];
            var pyocSchoolData = '<div class="schoolSelectList pam clearfix">' +
                '<span class="fl ttc"><span>' + $this.find('.js-pyocSchoolName').html() + '</span><br/>' +
                '<span class="small">' + city +' '+ state + '. ' + $this.find('.js-pyocSchoolType').html() + ', '+ $this.find('.js-pyocGradeRange').html() + '</span>' +
                '</span>' +
                '<span class="fr">' +
                '<input type="checkbox" checked="checked" value="' + output + '" class="js-stateId"/>' +
                '</span>' +
                '</div>';
            container.append(pyocSchoolData);
        });
    };

    this.getCheckCount = function (){
        $('.js-pyocCountCheck, .js-stateId, #pyocDeselect').click(function() {
            var pyocCounter = $('#js-pyoc').find('input.js-stateId:checked').length;
            if(pyocCounter == 1){
                $('.pyocCount').html(pyocCounter + ' school ');
            }
            else{
                $('.pyocCount').html(pyocCounter + ' schools ');
            }
            var pageCount = (Math.ceil(pyocCounter/3));
            if(pageCount == 1 ){
                $('.pageCount').html('(' + pageCount + ' page)');
            }
            else{
                $('.pageCount').html('(' + pageCount + ' pages)');
            }
        });
    }
    this.printSchoolList = function () {
        $('.downloadPYOC').unbind('click').click(function() {
            var state = [];
            var schoolID = [];
            var count = 0;
            $('#js-pyoc input:checked').each(function () {
                count++;
                var selected = $(this).attr('value').split(",");
                state.push(selected[0]);
                schoolID.push(selected[1]);
            });
            if (count == 0) {
                $('.js-showError').show();
                return false;
            }
            else {
                $('.js-showError').hide();
            }

            $('#selectedState').val(state.join(','));
            $('#selectedId').val(schoolID.join(','));
            if ($('#js-pyocChecklist input:checked')) {
                $('#js-appendCheckList').val('yes');
            }
            else {
                $('#js-appendCheckList').val('no');
            }

            if (s.tl) {
                s.tl(true, 'o', 'MSL_download_printable');
            }

            $('#js-printSchoolChooserSubmit').submit();
        });
    }

    this.deselectSchool = function () {
        $('#pyocDeselect').click(function () {
            $('#js-pyoc input').each(function () {
                $(this).attr('checked', false);
            });
        });
    }
    this.showHover = function() {
        this.populateModal();
        this.printSchoolList();
        this.deselectSchool();
        this.getCheckCount();
        GSType.hover.printSchoolChooser.show();
    };
};
GSType.hover.PrintSchoolChooser.prototype = new GSType.hover.HoverDialog('printSchoolChooser',640);

//ValidateEmailHover Hover
GSType.hover.ValidateEmailHover = function() {
    this.loadDialog = function() {
        this.pageName='Verify Email Hover';
        this.hier1='Hovers,Verification,Verify Email Hover';
        //this.dialogByWidth();
    }
};
GSType.hover.ValidateEmailHover.prototype = new GSType.hover.HoverDialog('valEmail',640);

//ValidateEmailSchoolReviewHover Hover
GSType.hover.ValidateEmailSchoolReviewHover = function() {
    this.loadDialog = function() {
        this.pageName='School Reviews Verify Email Hover';
        this.hier1='Hovers,Verification,School Reviews Verify Email Hover';
        //this.dialogByWidth();
    }
};
GSType.hover.ValidateEmailSchoolReviewHover.prototype = new GSType.hover.HoverDialog('valEmailSchoolReview',640);


//ValidateLinkExpired hover
GSType.hover.ValidateLinkExpired = function() {
    this.email = '';
    this.loadDialog = function() {
        this.pageName='Email Verification Link Expired Hover';
        this.hier1='Hovers,Verification,Email Verification Link Expired Hover';
        //this.dialogByWidth();
    };
    this.setEmail = function(email) {
        GSType.hover.validateLinkExpired.email = email;
    }
};
GSType.hover.ValidateLinkExpired.prototype = new GSType.hover.HoverDialog('expVer',640);

//EmailNotValidated hover
GSType.hover.EmailNotValidated = function() {
    this.email = '';
    this.emailRedirect = '';
    this.loadDialog = function() {
        this.pageName='Email Not Verified Hover';
        this.hier1='Hovers,Verification,Email Not Verified Hover';
        //this.dialogByWidth();
    };
    this.setEmail = function(email) {
        GSType.hover.emailNotValidated.email = email;
    };

    this.setEmailRedirect = function (emailRedirect) {
        GSType.hover.emailNotValidated.emailRedirect = emailRedirect;
    };
};
GSType.hover.EmailNotValidated.prototype = new GSType.hover.HoverDialog('valNewEmail',640);

//Base School Review Thank You Hover
GSType.hover.SchoolReviewThankYou = function() {
    this.pageName = "";
    this.hierarchy = "";

    this.loadDialog = function() {
        //this.dialogByWidth();
    };
    this.showHover = function() {
        this.setTitle("Thank you for submitting a review");
        this.setBody(this.body());
        jQuery('#schoolReviewThankYou').bind('dialogclose', this.onClose.gs_bind(this));
        this.sendOmnitureTrackingInfo();
        this.show();
        // GS-13832
        window.google_conversion_id = 1004863430;
        window.google_conversion_language = "en";
        window.google_conversion_format = "3";
        window.google_conversion_color = "ffffff";
        window.google_conversion_label = "Gw-FCKr6qQQQxv-T3wM";
        window.google_conversion_value = 0;
        jQuery.getScript('http://www.googleadservices.com/pagead/conversion.js');
    };
    this.sendOmnitureTrackingInfo = function() {
        pageTracking.pageName =  this.getPageName();
        pageTracking.hierarchy = this.getHierarchy();
        pageTracking.server = "www.greatschools.org";
        pageTracking.send();
    };
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
        jQuery('#schoolReviewThankYou p').html(body);
    };
};
GSType.hover.SchoolReviewThankYou.prototype = new GSType.hover.HoverDialog("schoolReviewThankYou",640);

//School Review Posted Thank You
GSType.hover.SchoolReviewPostedThankYou = function() {
    this.body = function() {
        return "Your review has been posted to GreatSchools.";
    };
    this.onClose = function() {
        //window.location.reload();
    };
    this.getHierarchy = function() {
        return "Hovers,Success,School Reviews Success Hover";
    };
    this.getPageName = function() {
        return "School Reviews Success Hover";
    };
};
GSType.hover.SchoolReviewPostedThankYou.prototype = new GSType.hover.SchoolReviewThankYou();

//School Review Not Posted Thank You
GSType.hover.SchoolReviewNotPostedThankYou = function() {
    this.body = function() {
        return "Please note that it can take up to 48 hours for your review to be posted to our site.";
    };
    this.getHierarchy = function() {
        return "Hovers,Success,Moderated School Reviews Success Hover";
    };
    this.getPageName = function() {
        return "Moderated School Reviews Success Hover";
    };
};
GSType.hover.SchoolReviewNotPostedThankYou.prototype = new GSType.hover.SchoolReviewThankYou();

// Email Validated Review Hover
GSType.hover.EmailValidatedSchoolReview = function() {
    this.loadDialog = function() {
        //this.dialogByWidth();
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
GSType.hover.EmailValidatedSchoolReview.prototype = new GSType.hover.HoverDialog("emailValidatedSchoolReviewHover",640);

//ESP Thank You Hover
GSType.hover.SchoolEspThankYou = function() {
    this.loadDialog = function() {
    };
    this.showHover = function() {
        GSType.hover.schoolEspThankYou.show();
    };

    this.onClose = function() {
    };
};

GSType.hover.SchoolEspThankYou.prototype = new GSType.hover.HoverDialog("schoolEspThankYou",640);

GSType.hover.EspAccountVerified = function() {
    this.loadDialog = function() {

    };
    this.showHover = function() {
        GSType.hover.espAccountVerified.show();
    };
    this.onClose = function() {};
};

GSType.hover.EspAccountVerified.prototype = new GSType.hover.HoverDialog("espAccountVerified",640);

// Confirm ESP Save hover
GSType.hover.ConfirmEspSave = function() {
    this.loadDialog = function() {
    };
    this.showHover = function() {
        GSType.hover.confirmEspSave.show();
    };

    this.onClose = function() {
    };
};

GSType.hover.ConfirmEspSave.prototype = new GSType.hover.HoverDialog("confirmEspSaveHover",640);


//Hover to re-send the ESP pre-approval email.
GSType.hover.EspPreApprovalEmail = function() {
    this.email = '';
    this.schoolName = '';
    this.loadDialog = function() {
    };
    this.setEmail = function(email) {
        GSType.hover.espPreApprovalEmail.email = email;
    };
    this.setSchoolName = function(schoolName) {
        GSType.hover.espPreApprovalEmail.schoolName = schoolName;
    };
};

GSType.hover.EspPreApprovalEmail.prototype = new GSType.hover.HoverDialog("js_espPreApprovalEmailHover",640);

// Hover to inform user there changes are provisional
GSType.hover.EspProvisionalReminder = function() {
    this.loadDialog = function() {
    };
};

GSType.hover.EspProvisionalReminder.prototype = new GSType.hover.HoverDialog("js_espProvisionalReminderHover",640);

//Email to a friend hover
GSType.hover.EmailToFriend = function() {
    this.loadDialog = function() {
        //this.dialogByWidth();
    };
};
GSType.hover.EmailToFriend.prototype = new GSType.hover.HoverDialog("emailToFriendHover",640);

GSType.hover.NlSubscription = function() {
    this.loadDialog = function() {
    };
    this.showHover = function () {
        subCookie.createAllHoverCookie('showNLHoverOnArticles',1,1);
        $j('#nlSubEmail_error').hide();
        $j('#nlSubEmail_error_alreadySub').hide();
        GSType.hover.nlSubscription.show();
        $j("#nlSubEmail").blur();
        $j("#removeNlSubscriptionFromPage").remove();
    };
    this.validateEmail = function() {
        var isEmailValid = false;
        jQuery.ajax({
            type: 'POST',
            url: GS.uri.Uri.getBaseHostname() + '/util/isValidEmail.page',
            data: {email:jQuery('#hover_nlSubscription #nlSubEmail').val()},
            dataType: 'text',
            async: false,
            success: function(data) {
                isEmailValid = data == 'true' ? true : false;
            }
        });
        return isEmailValid;
    };

    this.subscribeUser = function() {
        jQuery.ajax({
            type: 'POST',
            url: GS.uri.Uri.getBaseHostname() + '/content/cms/nlSubscription.page',
            data: {email:jQuery('#hover_nlSubscription #nlSubEmail').val(),
                partnerNewsletter:jQuery('#hover_nlSubscription #partnerNewsletter').is(':checked'),ajaxRequest:true},
            dataType: 'json',
            async: false,
            success: function(data) {
                if (data.userAlreadySubscribed === true) {
                    $j('#nlSubEmail_error_alreadySub').show();
                } else {
                    GSType.hover.nlSubscription.hide();
                    if (data.thankYouMsg != '' && data.thankYouMsg != null) {
                        jQuery('#hover_nlSubscriptionThankYou #thankYouMsg').append("<p class='make-2b2b2b'>" + data.thankYouMsg + "</p>");
                    }
                    GSType.hover.nlSubscriptionThankYou.show();
                    s.linkTrackVars = "events";
                    s.linkTrackEvents = "event52";
                    pageTracking.successEvents = 'event52';
                    pageTracking.send();
                }
            }
        });
    };
}
GSType.hover.NlSubscription.prototype = new GSType.hover.HoverDialog("hover_nlSubscription",540);

GSType.hover.NlSubscriptionThankYou = function() {
    this.loadDialog = function() {
    };
    this.showHover = function () {
        GSType.hover.nlSubscriptionThankYou.show();
    };
}
GSType.hover.NlSubscriptionThankYou.prototype = new GSType.hover.HoverDialog("hover_nlSubscriptionThankYou",540);

GSType.hover.InterruptSurvey = function() {
    var surveyUrl = '';
    this.loadDialog = function() { };
    this.showHover = function (hier1Last, pageType, url) {
        if (pageType == "Overview" && hasCookie("seenHoverOnExitRecently")) {
            return;
        } else if (!hasCookie("survey_hover_seen")) {
            createCookie("survey_hover_seen", "1", 15);
            if (pageType == "Overview") {
                createCookie("survey_hover_seen_this_session", "1");
            }
            jQuery('#interruptSurvey .takeSurveyButton').click(GSType.hover.interruptSurvey.clickSubmitHandler);
            this.pageName = 'Survey Interrupt Hover ' + pageType;
            this.hier1 = 'Surveys,Auto Hover,' + hier1Last;
            surveyUrl = url;
            GSType.hover.interruptSurvey.show();
        }
    };
    this.clickSubmitHandler = function() {
        window.open(surveyUrl);
        GSType.hover.interruptSurvey.hide();
        return false;
    }
};
GSType.hover.InterruptSurvey.prototype = new GSType.hover.HoverDialog('interruptSurvey',465);

GSType.hover.PrincipalConfirmation = function() {
    this.loadDialog = function() {
        //this.dialogByWidth();
    };
    this.show = function(id, state) {
//        if (!this.initialized) {
//            this.dialogByWidth();
//            this.initialized = true;
//        }
        jQuery('#principalConfirmationForm #hdnSchoolId').val(id);
        jQuery('#principalConfirmationForm #hdnSchoolState').val(state);
        jQuery('#principalConfirmationForm').attr("action", "/school/principalComments.page");
//        jQuery('#' + this.hoverId).showModal();
//        jQuery('#' + this.hoverId).showModal();
        // TODO: this is not working scope issue
        GSType.hover.principalConfirmation.showModal();

        return false;
    };
};
GSType.hover.PrincipalConfirmation.prototype = new GSType.hover.HoverDialog("principalConfirmationHover",640);

GSType.hover.PrincipalReviewSubmitted = function() {
    this.loadDialog = function() {
        jQuery('#principalReviewSubmittedHover').bind('dialogclose', this.onClose.gs_bind(this));
        //this.dialogByWidth();
    };

    this.onClose = function() {
        window.location.reload();
    };
};
GSType.hover.PrincipalReviewSubmitted.prototype = new GSType.hover.HoverDialog("principalReviewSubmittedHover",640);

// May only compare 8 schools hover
GSType.hover.CompareSchoolsLimitReached = function() {
    this.loadDialog = function() {};
    this.schoolList = '';
    this.source = '';
    this.uncheckAllCallback = null;
    this.showCompare = function() {
        window.location = '/school-comparison-tool/results.page?schools=' + this.schoolList + '&source=' + this.source;
    };
    this.uncheckAll = function() {
        if (this.uncheckAllCallback) {
            this.uncheckAllCallback();
        }
        this.hide();
    };
    this.show = function(schoolList, source, uncheckAllCallback) {
        this.schoolList = schoolList;
        this.source = source;
        if (uncheckAllCallback) {
            this.uncheckAllCallback = uncheckAllCallback;
            jQuery('.jq_compareSchoolsLimitReachedHover_uncheckAll_div').show();
            jQuery('.jq_compareSchoolsLimitReachedHover_uncheckAll').bind('click', this.uncheckAll.gs_bind(this));
        } else {
            this.uncheckAllCallback = null;
            jQuery('.jq_compareSchoolsLimitReachedHover_uncheckAll_div').hide();
        }
        if (!this.initialized) {
            this.dialogByWidth();
            this.initialized = true;
            jQuery('.jq_compareSchoolsLimitReachedHover_edit').bind('click', this.hide.gs_bind(this));
            jQuery('.jq_compareSchoolsLimitReachedHover_continue').bind('click', this.showCompare.gs_bind(this));
        }
        pageTracking.clear();
        pageTracking.pageName = 'Compare_8Maximum_Hover';
        pageTracking.hierarchy = 'Compare,8Maximum_Hover';
        pageTracking.send();

        // TODO: this doesn't work scope is off
        GSType.hover.compareSchoolsLimitReached.showModal();
//        jQuery('#' + this.hoverId).dialog('open');
        return false;
    };
};
GSType.hover.CompareSchoolsLimitReached.prototype = new GSType.hover.HoverDialog("compareSchoolsLimitReachedHover", 640);


GSType.hover.ReportContentHover = function() {
    this.loadDialog = function() {};
    this.show = function(linkId) {
        GSType.hover.reportContentHover.showModal();

        var textarea = jQuery('textarea[name=reason]:visible');
        textarea.val('');
        textarea.focus();

        jQuery('.reportCancelButton:visible').on('click', function() {
            GSType.hover.reportContentHover.hide();
            return false;
        });

        jQuery('.reportSendButton:visible').on('click', function() {
            var reason = jQuery('textarea[name=reason]:visible').val();
            var type = jQuery('input[name=reportContentType]').val();
            if (reason == null || reason.length == 0) {
                alert("Please enter a description for your report.");
                return false;
            }
            GSType.hover.reportContentHover.hide();

            var params = {type: type,
                contentId: jQuery('input[name=reportContentId]').val(),
                reporterId: jQuery('input[name=reportReporterId]').val(),
                reason: reason
            };

            jQuery.post(GS.uri.Uri.getBaseHostname() + '/community/reportContent.page', params);

            $('#' + linkId).parent().addClass('reported');
            var contentTypeNormalized = params.type;
            if (contentTypeNormalized == 'discussion') {
                contentTypeNormalized = 'conversation';
            } else if (contentTypeNormalized == 'schoolReview') {
                contentTypeNormalized = 'review';
            } else if (contentTypeNormalized == 'schoolMedia') {
                contentTypeNormalized = 'photo';
                $('#' + linkId).parent().addClass('small_bold tar bottom make-d61');
            }
            $('#' + linkId).replaceWith("You've reported this " + contentTypeNormalized + ".");

            return false;
        });

        pageTracking.clear();
        pageTracking.pageName = "Report Item Hover";
        pageTracking.hierarchy = "Community,Actions,Report Item Hover";
        pageTracking.send();

        return false;
    };
};
GSType.hover.ReportContentHover.prototype = new GSType.hover.HoverDialog("reportContentHover", 624);

//Mini state launcher hover
GSType.hover.MiniStateLauncher = function() {
    this.loadDialog = function () {
        this.pageName='State Selection Hover';
        this.hier1='Search,State select page,Hover';
    }
};
GSType.hover.MiniStateLauncher.prototype = new GSType.hover.HoverDialog('miniStateLauncherHover',250);

//GS-12508.
GSType.hover.SchoolReviewPosted = function() {
    this.loadDialog = function() {
    };
    this.showHover = function() {
        GSType.hover.schoolReviewPosted.show();
    };
    this.show = function() {
        this.showModal();
        // GS-13832
        window.google_conversion_id = 1004863430;
        window.google_conversion_language = "en";
        window.google_conversion_format = "3";
        window.google_conversion_color = "ffffff";
        window.google_conversion_label = "Gw-FCKr6qQQQxv-T3wM";
        window.google_conversion_value = 0;
        jQuery.getScript('http://www.googleadservices.com/pagead/conversion.js');
    };
    this.onClose = function() {};
    this.pageName = 'School Reviews Success Hover';
    this.hier1 = 'Hovers,Success,School Reviews Success Hover';
};
GSType.hover.SchoolReviewPosted.prototype = new GSType.hover.HoverDialog("schoolReviewPosted",640);

//GS-13761
//GSType.hover.ClickToReviewYourSchool = function() {
//    this.loadDialog = function() {
//        jQuery('#js_clickToReviewYourSchoolHover_goToReviewForm').click(function() {
//            GSType.hover.clickToReviewYourSchool.cancelLoadOnExit();
//            if (!$.support.leadingWhitespace) {
//                //IE7 and 8 stuff
//                GSType.hover.clickToReviewYourSchool.hide();
//                function getPathFromUrl(url) {
//                    return url.split("?")[0];
//                }
//                var linkToStr = getPathFromUrl(window.location.href) + "?tab=reviews#schoolReviewSubmitForm";
//                window.location.href = linkToStr;
//            }
//        });
//    };
//    this.showHover = function() {
//        GSType.hover.clickToReviewYourSchool.show();
//    };
//    this.pageName = 'Review Your School Hover';
//    this.hier1 = 'Hovers,Crowdsourcing,Review Your School Hover';
//    this.executeOnExit = function(f) {
//        jQuery('#' + GSType.hover.clickToReviewYourSchool.hoverId).on('dialogclose', function() {
//            f();
//        });
//    };
//    this.cancelLoadOnExit = function() {
//        jQuery('#' + GSType.hover.clickToReviewYourSchool.hoverId).off('dialogclose');
//    };
//
//    this.showInterruptHoverOnPageExit = function(showHoverFunction) {
//        // automatically ignore any links with class no_interrupt
//        // assumes this is the first event executed when 'click' is triggered
//        $('a:not(.no_interrupt)').bindFirst('click', function(event) {
//            var $this = $(this);
//            var href = $this.attr('href');
//
//            if (!(href && href !== '' && href !== '#' && href !== (window.location.href+'#'))) {
//                return true;
//            } else if ($this.attr('target') || $this.attr('onclick')){
//                return true;
//            }
//
//            //the reason this is hardcoded to mssAutoHover is because a new hover was added that requires exactly
//            //the same functionality as existing "mss auto hover on exit", but displays depending on number of reviews
//            //Therefore, use the same cookie and don't mess too much with existing code at this time. GS-13761
//            if (mssAutoHoverInterceptor.shouldIntercept('mssAutoHover')) {
//                // set cookie to avoid hover repeating
//                var threeMinuteDuration = getCookieExpiresDate(0,0,3);
//                createCookieWithExpiresDate('seenHoverOnExitRecently','1',threeMinuteDuration);
//                // cause whatever link triggered this event to be triggered by closing the hover
//                GSType.hover.clickToReviewYourSchool.executeOnExit(function() {
//                    var foo = $this.triggerHandler('click');
//                    if (foo) {
//                        window.location.href = href;
//                    }
//                });
//
//                showHoverFunction();
//
//                event.stopImmediatePropagation();
//                return false;
//            } else {
//                return true;
//            }
//        });
//    };
//};
//GSType.hover.ClickToReviewYourSchool.prototype = new GSType.hover.HoverDialog("clickToReviewYourSchoolHover",640);

GSType.hover.forgotPassword = new GSType.hover.ForgotPasswordHover();
GSType.hover.emailValidated = new GSType.hover.EmailValidated();
GSType.hover.editEmailValidated = new GSType.hover.EditEmailValidated();
GSType.hover.subscriptionEmailValidated = new GSType.hover.SubscriptionEmailValidated();
GSType.hover.emailNotValidated = new GSType.hover.EmailNotValidated();
GSType.hover.validateEmail = new GSType.hover.ValidateEmailHover();
GSType.hover.validateEmailSchoolReview = new GSType.hover.ValidateEmailSchoolReviewHover();
GSType.hover.joinHover = new GSType.hover.JoinHover();
GSType.hover.signInHover = new GSType.hover.SignInHover();
GSType.hover.validateEditEmail = new GSType.hover.ValidateEditEmail();
GSType.hover.printSchoolChooser = new GSType.hover.PrintSchoolChooser();
GSType.hover.validateLinkExpired = new GSType.hover.ValidateLinkExpired();

GSType.hover.schoolReviewPostedThankYou = new GSType.hover.SchoolReviewPostedThankYou();
GSType.hover.schoolReviewNotPostedThankYou = new GSType.hover.SchoolReviewNotPostedThankYou();
GSType.hover.emailValidatedSchoolReview = new GSType.hover.EmailValidatedSchoolReview();
GSType.hover.schoolEspThankYou = new GSType.hover.SchoolEspThankYou();
GSType.hover.espAccountVerified = new GSType.hover.EspAccountVerified();
GSType.hover.confirmEspSave = new GSType.hover.ConfirmEspSave();
GSType.hover.espPreApprovalEmail = new GSType.hover.EspPreApprovalEmail();
GSType.hover.emailToFriend = new GSType.hover.EmailToFriend();
GSType.hover.interruptSurvey = new GSType.hover.InterruptSurvey();
GSType.hover.nlSubscription = new GSType.hover.NlSubscription();
GSType.hover.nlSubscriptionThankYou = new GSType.hover.NlSubscriptionThankYou();

GSType.hover.principalConfirmation = new GSType.hover.PrincipalConfirmation();
GSType.hover.principalReviewSubmitted = new GSType.hover.PrincipalReviewSubmitted();

GSType.hover.compareSchoolsLimitReached = new GSType.hover.CompareSchoolsLimitReached();

GSType.hover.reportContentHover = new GSType.hover.ReportContentHover();

GSType.hover.miniStateLauncher = new GSType.hover.MiniStateLauncher();
GSType.hover.schoolReviewPosted = new GSType.hover.SchoolReviewPosted();
//GSType.hover.clickToReviewYourSchool = new GSType.hover.ClickToReviewYourSchool();

GSType.hover.espProvisionalReminder = new GSType.hover.EspProvisionalReminder();

GS.forgotPasswordHover_checkValidationResponse = function(data) {
    GSType.hover.forgotPassword.clearMessages();

    // special-case error messages for OSP signin page
    var isOspSignIn = window.location.pathname === '/official-school-profile/signin.page';
    if (isOspSignIn && data !== undefined && data.errorCode !== undefined) {
        var espRegistrationUrl = data.ESP_REGISTRATION_URL;
        if (data.errorCode === 'NO_SUCH_ACCOUNT') {
            GSType.hover.forgotPassword.addMessage('There is no school official\'s account associated with this email address. Please <a href="' + espRegistrationUrl + '">register to gain access</a>.');
        } else if (data.errorCode === 'EMAIL_ONLY_ACCOUNT') {
            GSType.hover.forgotPassword.addMessage('You have an email address on file, but still need to <a href="' + espRegistrationUrl + '">create a school official\'s profile</a>.');
        }
        return;
    } else if (data.errorMsg) {
        GSType.hover.forgotPassword.addMessage(data.errorMsg);
        return;
    }

    jQuery.post(GS.uri.Uri.getBaseHostname() + '/community/forgotPassword.page', jQuery('#hover_forgotPasswordForm').serialize());
    var email = jQuery('#fpemail').val();

    GSType.hover.forgotPassword.addMessage('An email has been sent to ' + email +
        ' with instructions for selecting a new password. ' + '<a href="javascript:void(0)" onclick="GSType.hover.forgotPassword.hide();GSType.hover.signInHover.showHover(\'' + email + '\');return false;">Sign in</a>');
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
    } else if (location.hostname.match(/dev\.|dev$|\.office\.|cmsqa|localhost|127\.0\.0\.1|macbook|qaapp-1\.|qaapp-2\.|qaadmin-1\.|qa\.|qa-preview\./)) {
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

GS.showSendMeUpdates = function(redirect, schoolName, schoolId, schoolState) {
    if (GS.isSignedIn()) {
        return true; // signed in users go straight to destination
    } else {
        GSType.hover.sendMeUpdates.configureSchoolInfo(redirect, schoolName, schoolId, schoolState);
        GSType.hover.signInHover.setRedirect(redirect);
        if (GS.isMember()) {
            GS.log("isMember");
            GSType.hover.signInHover.showHover('', redirect, GSType.hover.sendMeUpdates.showSendUpdates);
        } else {
            GS.log("showSendUpdates");
            GSType.hover.sendMeUpdates.showSendUpdates();
        }
    }
    return false;
};

GS.showAddMslJoinHover = function(omniturePageName, schoolName, schoolId, schoolState, elem, mapSearch) {
    if (omniturePageName && s.tl) {
        s.tl(true, 'o', 'Add_to_MSL_Link_' + omniturePageName);
    }

    var statePlusId = schoolState + schoolId;
    var mslHelper = new GS.community.MySchoolListHelper();
    if (GS.isSignedIn()) {
        if (omniturePageName) {
            mslHelper.addSchool(schoolState, schoolId, function() {
                var addToMsl = jQuery('.js-add-msl-' + statePlusId);
                // We already have the html on search map results page, so just use show and hide instead of writing html.
                if(mapSearch) {
                    addToMsl.find('.js-notInMsl').hide();
                    addToMsl.find('.js-existsInMsl').show();
                }
                else {
                    addToMsl.find('.js-msl-text').html("Added to <a href=\"/mySchoolList.page\">My School List</a>");
                    addToMsl.find('.js-add-msl-success-icon').attr("class", "iconx16 i-16-success");
                }
            }, function() {});
        } else {
            return true;
        }
    } else {
        var redirect = window.location.href;
        if (!omniturePageName && elem) {
            redirect = elem.href;
        }
        var mslSuccessCallback = function(email, formId) {
            GSType.hover.signInHover.setRedirect(redirect);
            mslHelper.addSchool(schoolState, schoolId, function() {jQuery('#' + formId).submit();}, function() {jQuery('#' + formId).submit();}, email);
        };
//        GSType.hover.joinHover.configureForMss(schoolName, schoolId, schoolState);
        GSType.hover.joinHover.onSubmitCallback = mslSuccessCallback;
        GSType.hover.joinHover.onFacebookSubmitCallback = function(data) {
            mslHelper.addSchool(schoolState, schoolId, function() {}, function() {}, data.email);
        };
        if (GS.isMember()) {
            GSType.hover.signInHover.showHover('', redirect, GSType.hover.joinHover.showJoinMsl, mslSuccessCallback);
        } else {
            GSType.hover.joinHover.showJoinMsl();
        }
    }
    return false;
};

GS.showAddMslJoinHoverAllSchools = function(schoolIdList, schoolState) {
    if (s.tl) {
        s.tl(true, 'o', 'Add_to_MSL_Link_Compare_Allschools');
    }

    var mslHelper = new GS.community.MySchoolListHelper();
    if (GS.isSignedIn()) {
        var idsArr = schoolIdList.split(",");
        var counter;
        for (counter=0; counter < idsArr.length; counter++) {
            (function() {
                var myId = idsArr[counter];
                var myStatePlusId = schoolState + myId;
                mslHelper.addSchool(schoolState, myId, function() {
                    jQuery('.js-add-msl-' + myStatePlusId).find('.js-msl-text').html("Added to <a href=\"/mySchoolList.page\">My School List</a>");
                    jQuery('.js-add-msl-' + myStatePlusId).find('.sprite').attr("class", "sprite i-checkmark-sm img");
                }, function() {});
            }());
        }
        jQuery('.js-add-all-msl').find('.js-msl-text').html("Added all schools to <a href=\"/mySchoolList.page\">My School List</a>");
        jQuery('.js-add-all-msl').find('.sprite').attr("class", "sprite i-checkmark-sm img");
    } else {
        var redirect = window.location.href;
        var mslSuccessCallback = function(email, formId) {
            var idsArr = schoolIdList.split(",");
            var counter;
            for (counter=0; counter < idsArr.length; counter++) {
                mslHelper.addSchool(schoolState, idsArr[counter], function() {}, function() {}, email);
            }
            GSType.hover.signInHover.setRedirect(redirect);
            window.setTimeout(function() {jQuery('#' + formId).submit()}, 100); // give MSL time to commit
        };
        GSType.hover.joinHover.onSubmitCallback = mslSuccessCallback;
        GSType.hover.joinHover.onFacebookSubmitCallback = function(data) {
            var idsArr = schoolIdList.split(",");
            var counter;
            for (counter=0; counter < idsArr.length; counter++) {
                mslHelper.addSchool(schoolState, idsArr[counter], function() {}, function() {}, data.email);
            }
        };
        if (GS.isMember()) {
            GSType.hover.signInHover.showHover('', redirect, GSType.hover.joinHover.showJoinMsl, mslSuccessCallback);
        } else {
            GSType.hover.joinHover.showJoinMsl();
        }
    }
    return false;
};

GS.joinHover_checkValidationResponse2 = function(data) {

    jQuery('#joinGS').submit();
    jQuery('#joinGS').submit(function() {
        return false; // prevent multiple submits
    });
    GSType.hover.joinHover.hide();

    jQuery('.joinBtn').prop('disabled', false);
};

GS.joinHover_checkValidationResponse = function(data, sendTracking) {
    if (GS.joinHover_passesValidationResponse(data)) {
        if (GSType.hover.joinHover.loadOnExitUrl) {
            GSType.hover.joinHover.cancelLoadOnExit();
        }

        if(sendTracking) {
            pageTracking.clear();
            pageTracking.successEvents = "event5";
            pageTracking.pageName = "MSS Hover";
            pageTracking.send();
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
    jQuery('.joinBtn').prop('disabled', false);
};

GS.joinHover_passesValidationResponse = function(data) {
    GS.log('in joinHover_passesValidationResponse', data);
    var errorIcon = '<span class="iconx16 i-16-alert"><!--not empty--></span> ';
    var emailError = jQuery('#joinGS .joinHover_email .invalid');
    var passwordError = jQuery('#joinGS .joinHover_password .invalid');
    var confirmPasswordError = jQuery('#joinGS .joinHover_confirmPassword .invalid');

    emailError.hide();
    passwordError.hide();
    confirmPasswordError.hide();

    var objCount = 0;
    for (_obj in data) objCount++;

    if (objCount > 0) {
        GS.log('joinHover_passesValidationResponse fail');
        jQuery('#joinGS #js_ProcessError').show("fast");
        jQuery('#joinGS #process_error').show("fast");

        if (data.email) {
            emailError.html(errorIcon+data.email).show();
            jQuery('#joinGS .joinHover_email .invalid a.launchSignInHover').click(function() {
                GSType.hover.joinHover.showSignin();
                return false;
            });
        }

        if (data.password) {
            passwordError.html(errorIcon+data.password).show();
        }

        if (data.confirmPassword) {
            confirmPasswordError.html(errorIcon+data.confirmPassword).show();
        }

        return false;
    } else {
        return true;
    }
};

GS.community.MySchoolListHelper = function() {

    this.addSchool = function(state, id, successCallback, failCallback, email) {
        var url = GS.uri.Uri.getBaseHostname() + "/mySchoolListAjax.page";
        var data = {};
        data.schoolDatabaseState = state;
        data.schoolId = id;
        if (email) {
            data.email = email;
            data.redirectUrl = '';
        }

        jQuery.post(url, data, function(data) {
            if (data.success !== undefined && data.success === true) {
                successCallback();
                this.incrementCountInHeader();
            } else {
                failCallback();
            }
        }.gs_bind(this), "json");
    };

    this.incrementCountInHeader = function() {
        var mslCountInHeader = jQuery('#utilLinks .last a');
        var mslCount = Number(mslCountInHeader.html().replace(/[^0-9]/g,''));
        mslCount = mslCount + 1;
        mslCountInHeader.html("My School List (" + mslCount + ")");
    };
};

GS.getElementsByCondition = function(condition,container) {
    container = container||document;
    var all = container.all||container.getElementsByTagName('*');

    var arr = [];
    for(var k=0;k<all.length;k++)
    {
        var elm = all[k];
        if(condition(elm,k)) {
            arr[arr.length] = elm
        }
    }
    all = null;
    return arr;
};


jQuery(function() {
    GSType.hover.subscriptionEmailValidated.loadDialog();
    GSType.hover.editEmailValidated.loadDialog();
    GSType.hover.emailNotValidated.loadDialog();
    GSType.hover.emailValidated.loadDialog();
    GSType.hover.forgotPassword.loadDialog();
    GSType.hover.joinHover.loadDialog();
    GSType.hover.signInHover.loadDialog();
    GSType.hover.validateEditEmail.loadDialog();
    GSType.hover.printSchoolChooser.loadDialog();
    GSType.hover.validateEmail.loadDialog();
    GSType.hover.validateEmailSchoolReview.loadDialog();
    GSType.hover.validateLinkExpired.loadDialog();

    GSType.hover.schoolReviewPostedThankYou.loadDialog();
    GSType.hover.schoolReviewNotPostedThankYou.loadDialog();
    GSType.hover.emailValidatedSchoolReview.loadDialog();
    GSType.hover.schoolEspThankYou.loadDialog();
    GSType.hover.espAccountVerified.loadDialog();
    GSType.hover.espPreApprovalEmail.loadDialog();
    GSType.hover.emailToFriend.loadDialog();
    GSType.hover.interruptSurvey.loadDialog();
    GSType.hover.nlSubscription.loadDialog();
    GSType.hover.nlSubscriptionThankYou.loadDialog();
    GSType.hover.principalConfirmation.loadDialog();
    GSType.hover.principalReviewSubmitted.loadDialog();

    GSType.hover.compareSchoolsLimitReached.loadDialog();
    GSType.hover.reportContentHover.loadDialog();
    GSType.hover.miniStateLauncher.loadDialog();
    GSType.hover.schoolReviewPosted.loadDialog();
//    GSType.hover.clickToReviewYourSchool.loadDialog();

    GSType.hover.espProvisionalReminder.loadDialog();

    jQuery('#hover_forgotPasswordSubmit').click(function() {
        jQuery.getJSON(GS.uri.Uri.getBaseHostname() + '/community/forgotPasswordValidator.page',
            jQuery('#hover_forgotPasswordForm').serialize(),
            GS.forgotPasswordHover_checkValidationResponse);

        return false;
    });

    var initForContentType = function(idStr, contentType) {
        jQuery('input[name=reportContentType]').val(contentType);
        var reportContentId = idStr.substring(("report_" + contentType + "_").length);
        jQuery('input[name=reportContentId]').val(reportContentId);
        var contentTypeNormalized = contentType;
        if (contentTypeNormalized == 'discussion') {
            contentTypeNormalized = 'conversation';
        } else if (contentTypeNormalized == 'schoolReview') {
            contentTypeNormalized = 'review';
        } else if (contentTypeNormalized == 'schoolMedia') {
            contentTypeNormalized = 'photo';
        }
        jQuery('span.reportHoverIntroContentType').html(contentTypeNormalized);
        jQuery('span.reportHoverHeader').html(contentTypeNormalized);
    };

    jQuery('.reportContent').on('click', function(){
        if (GS.showJoinHover(jQuery('#frmPRModule-email').val(),window.location.href,GSType.hover.joinHover.showJoinPostComment)) {
            var linkId = jQuery(this).attr('id');
            if (linkId.indexOf('reply') > -1) {
                initForContentType(linkId, 'reply');
            } else if (linkId.indexOf('discussion') > -1) {
                initForContentType(linkId, 'discussion');
            } else if (linkId.indexOf('member') > -1) {
                initForContentType(linkId, 'member');
            } else if (linkId.indexOf('schoolReview') > -1) {
                initForContentType(linkId, 'schoolReview');
            } else if (linkId.indexOf('schoolMedia') > -1) {
                initForContentType(linkId, 'schoolMedia');
            }

            if (linkId.indexOf('schoolReview') > -1) {
                jQuery('.communityGuidelines').hide();
                jQuery('.schoolReviewGuidelines').show();
            } else {
                jQuery('.communityGuidelines').show();
                jQuery('.schoolReviewGuidelines').hide();
            }

            GSType.hover.reportContentHover.show(linkId);
        };
        return false;
    });

    jQuery('#hover_nlSubscriptionSubmit').on("click", function() {
        var validEmail = GSType.hover.nlSubscription.validateEmail();
        if(!validEmail || $j('#nlSubEmail').val() === ''){
            $j('#nlSubEmail_error').show();
        }else if (validEmail) {
            GSType.hover.nlSubscription.subscribeUser();
        }
    });

    jQuery('#js_closeValNewEmail').click(function() {
        var params = {
            email: GSType.hover.emailNotValidated.email,
            emailRedirect: GSType.hover.emailNotValidated.emailRedirect
        };
        jQuery.get(GS.uri.Uri.getBaseHostname() + '/community/requestEmailValidation.page', params);

        GSType.hover.emailNotValidated.hide();
    });

    jQuery('#js_closeExpVer').click(function() {
        var params = {
            email: GSType.hover.validateLinkExpired.email
        };
        jQuery.get(GS.uri.Uri.getBaseHostname() + '/community/requestEmailValidation.page', params);

        GSType.hover.validateLinkExpired.hide();
    });


    jQuery('#js_sendEspPreApprovalEmail').click(function() {
        var params = {
            email: GSType.hover.espPreApprovalEmail.email,
            schoolName:GSType.hover.espPreApprovalEmail.schoolName
        };

        jQuery.get(GS.uri.Uri.getBaseHostname() + '/official-school-profile/espPreApprovalEmail.page', params);

        GSType.hover.espPreApprovalEmail.hide();
    });

    jQuery('#joinState').change(GSType.hover.joinHover.loadCities);

//    jQuery('.js_closeJoinHover').click(function() {
//        GSType.hover.joinHover.hide();
//        return false;
//    });

//    jQuery('#joinHover #fName').blur(GSType.hover.joinHover.validateFirstName);
    jQuery('#joinHover #jemail').blur(GSType.hover.joinHover.validateEmail);
//    jQuery('#joinHover #jcemail').blur(GSType.hover.joinHover.validateConfirmEmail);
//    jQuery('#joinHover #uName').blur(GSType.hover.joinHover.validateUsername);
    jQuery('#joinHover #jpword').blur(GSType.hover.joinHover.validatePassword);
    jQuery('#joinHover #cpword').blur(GSType.hover.joinHover.validateConfirmPassword);
//    jQuery('#joinHover .js_closeJoinHover').click(GSType.hover.joinHover.hide);

    jQuery('#joinHover').bind('dialogclose', function() {

        jQuery('#joinGS .error').hide();
        GSType.hover.joinHover.clearMessages();
    });

    jQuery('#joinHover #lnchSignin').click(GSType.hover.joinHover.showSignin);
    jQuery('#js_espLaunchSignin').click(GSType.hover.joinHover.showSignin);

    jQuery('#hover_forgotPassword').bind('dialogclose', function() {
        GSType.hover.forgotPassword.clearMessages();
    });

    GSType.hover.forgotPassword.clearMessages();

    // TODO-10568
    /*
     jQuery('.signInHoverLink').click(function() {
     GSType.hover.signInHover.show();
     });
     */


    jQuery('#signInHover').bind('dialogclose', GSType.hover.signInHover.clearMessages);

    jQuery('#signInHover_launchJoin').click(GSType.hover.signInHover.showJoin);
    jQuery('#signInHover_launchForgotPassword').click(GSType.hover.signInHover.showForgotPassword);
    jQuery('#js_espLaunchForgotPassword').click(GSType.hover.signInHover.showForgotPassword);

    jQuery('#signin').attr("action", "/community/loginOrRegister.page");
// TODO-10568
    /*
     jQuery('.joinAutoHover_showHover').click(function() {
     GSType.hover.joinHover.showJoinAuto('Alameda High School', 1, 'CA');
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
     */
   /* jQuery('.js_subscriptionEmailValidated').click(function() {
        GSType.hover.subscriptionEmailValidated.hide();
        return false;
    });
    jQuery('.js_closeForgotPassword').click(function() {
        GSType.hover.forgotPassword.hide();
        return false;
    });
    jQuery('.js_closeSignInHover').click(function() {
        GSType.hover.signInHover.hide();
        return false;
    });
    jQuery('.js_closeJoinHover').click(function() {
        GSType.hover.joinHover.hide();
        return false;
    });
     */
    jQuery('.js_chooseEnableDisable').on("click",function( ) {
        if(jQuery(this).is(':checked')){
            jQuery('.js_ShowHideGrades').removeClass('disabled_field');
        }
        else{
            jQuery(".js_showGradeSelect").hide('fast');
            jQuery('.js_ShowHideGrades').html("Choose grades &#187;");
            jQuery('.js_ShowHideGrades').addClass('disabled_field');
        }
    })

    jQuery('.js_ShowHideGrades').on("click", function( event) {
        event.preventDefault();
        if(!jQuery('.js_ShowHideGrades').hasClass('disabled_field')){
            if(jQuery(".js_showGradeSelect").css("display") == "none"){

                jQuery(".js_showGradeSelect").show('fast');
                jQuery('.js_ShowHideGrades').html("&#171; Hide Grade Chooser");
            }
            else{
                jQuery(".js_showGradeSelect").hide('fast');
                jQuery('.js_ShowHideGrades').html("Choose grades &#187;");
            }
        }
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
    } else if (showHover == "modalUspRegistration") {
        GSType.hover.modalUspRegistration.show();
    } else if (showHover == "modalUspSignIn") {
        GSType.hover.modalUspSignIn.show();
    } else if (showHover == "reviewLandingPageInformational") {
        GSType.hover.reviewLandingPageInformational.show();
    } else if (showHover == "verifyYourEmailAddressUSP") {
        GSType.hover.verifyYourEmailAddressUSP.show();
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
    } else if (showHover == "subscriptionEmailValidated") {
        GSType.hover.subscriptionEmailValidated.show();
    } else if (showHover == "principalReviewSubmitted") {

        pageTracking.pageName =  "School Officials Comment Thanks Hover";
        pageTracking.hierarchy = "ESP,School Comment Thanks Hover";
        pageTracking.server = "www.greatschools.org";
        pageTracking.send();

        GSType.hover.principalReviewSubmitted.show();
    } else if (showHover == "schoolEspThankYou") {
        GSType.hover.schoolEspThankYou.show();
    } else if (showHover == "espAccountVerified") {
        GSType.hover.espAccountVerified.show();
    } else if (showHover == "confirmEspSave") {
        GSType.hover.confirmEspSave.show();
    } else if (showHover == "espPreApprovalEmail") {
        GSType.hover.espPreApprovalEmail.show();
    }else if (showHover == "schoolReviewPosted") {
        GSType.hover.schoolReviewPosted.show();
    } else if (showHover == "espProvisionalReminder") {
        GSType.hover.espProvisionalReminder.show();
    }else if (showHover == "YoutubeVideoLightbox") {
        GSType.hover.YoutubeVideoLightbox.show();
// else if (showHover == "clickToReviewYourSchool") {
//        GSType.hover.clickToReviewYourSchool.show();
//    }
    }
    subCookie.deleteObjectProperty("site_pref", "showHover");

    //Omniture tracking for facebook share button on school review hovers.GS-12508

    jQuery('.js_fbshare_schoolReviewPosted').on('click', function() {
        if (s) {
            pageTracking.clear();
            pageTracking.successEvents = "event61";
            pageTracking.send();
        }
        GSType.hover.schoolReviewPosted.hide();
    });

});


GSType.hover.ModalUspRegistration = function() {};
GSType.hover.ModalUspRegistration.prototype = new GSType.hover.HoverDialog('js-modalUspRegistration');
GSType.hover.modalUspRegistration = new GSType.hover.ModalUspRegistration();

GSType.hover.ModalUspSignIn = function() {};
GSType.hover.ModalUspSignIn.prototype = new GSType.hover.HoverDialog('js-modalUspSignIn');
GSType.hover.modalUspSignIn = new GSType.hover.ModalUspSignIn();


GSType.hover.ReviewLandingPageInformational = function() {};
GSType.hover.ReviewLandingPageInformational.prototype = new GSType.hover.HoverDialog('js-reviewLandingPageInformational');
GSType.hover.reviewLandingPageInformational = new GSType.hover.ReviewLandingPageInformational();

GSType.hover.VerifyYourEmailAddressUSP = function() {};
GSType.hover.VerifyYourEmailAddressUSP.prototype = new GSType.hover.HoverDialog('js-verifyYourEmailAddressUSP');
GSType.hover.verifyYourEmailAddressUSP = new GSType.hover.VerifyYourEmailAddressUSP();

GSType.hover.YoutubeVideoLightbox = function() {};
GSType.hover.YoutubeVideoLightbox.prototype = new GSType.hover.HoverDialog('js-youtubeVideoLightbox');
GSType.hover.youtubeVideoLightbox = new GSType.hover.YoutubeVideoLightbox();


GSType.hover.SendMeUpdates = function() {
    this.configureOmniture = function(pageName, hier1) {
        GSType.hover.sendMeUpdates.pageName=pageName;
        GSType.hover.sendMeUpdates.hier1=hier1;
    };
    this.configureSchoolInfo = function(redirect, schoolName, schoolId, schoolState) {
        var redirectField = jQuery('#js-sendMeUpdates').find('.redirect_field');
        // if the field hasn't been set, then default to the current location.
        // if the field HAS been set, assume that is valid unless explicitly overridden.
        if (redirectField.val() == '') {
            redirect = redirect || window.location.href;
        }
        if (redirect) {
            redirectField.val(redirect);
        }
        if (schoolName) {
            GSType.hover.sendMeUpdates.schoolName = schoolName;
        }
        if (schoolId) {
            jQuery('#js-sendMeUpdates .school_id').val(schoolId);
        }
        if (schoolState) {
            jQuery('#js-sendMeUpdates .school_state').val(schoolState);
        }
    };
    this.validateEmail = function(emailAddress) {
        var emailPattern = /^[a-zA-Z0-9._-]+[+a-zA-Z0-9._-]*@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
        return emailPattern.test(emailAddress);
    };
    this.clickSubmitHandler = function() {
        GS.log('entered clickSubmitHAndler');
        var emailField = $("#jemail").val();
        var emailConfirmField = $("#jcemail").val();
        GS.log('emailField:', emailField);
        GS.log('emailConfirmField:', emailConfirmField);
        $(".js-sMU-emailMalformed").addClass("dn");
        $(".js-sMU-emailsDontMatch").addClass("dn");
//        var params = jQuery('#js-sMU').serialize();
        //validate
        if(GSType.hover.sendMeUpdates.validateEmail(emailField) && (emailField == emailConfirmField)){
            return true;
        }
        if(!GSType.hover.sendMeUpdates.validateEmail(emailField)){
           // show email formed wrong
            $(".js-sMU-emailMalformed").removeClass("dn");
        }
        if(emailField != emailConfirmField){
            // show emails don't match
            $(".js-sMU-emailsDontMatch").removeClass("dn");

        }
        return false;
    };
    this.showSendUpdates = function(schoolName, schoolId, schoolState) {
        // the deferred is used by showInterruptHoverOnPageExit
        var deferred = $.Deferred();
        jQuery('.js-signUp').on("click", GSType.hover.sendMeUpdates.clickSubmitHandler);
        GSType.hover.sendMeUpdates.configureSchoolInfo(undefined, schoolName, schoolId, schoolState);
        var $sendMeUpdates = $('#js-sendMeUpdates');
        $sendMeUpdates.unbind('dialogclose');
        $sendMeUpdates.bind('dialogclose', function() {
           deferred.resolve();
        });
        GSType.hover.sendMeUpdates.configureOmniture('MSS Join Hover', 'Hovers,Join,MSS Join Hover');
        GSType.hover.sendMeUpdates.show();
        return deferred;
    };
};
GSType.hover.SendMeUpdates.prototype = new GSType.hover.HoverDialog('js-sendMeUpdates');
GSType.hover.sendMeUpdates = new GSType.hover.SendMeUpdates();
