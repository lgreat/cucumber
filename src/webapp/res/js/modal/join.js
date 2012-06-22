/**
 * Created with IntelliJ IDEA.
 * User: mseltzer
 * Date: 6/22/12
 * Time: 10:02 AM
 * To change this template use File | Settings | File Templates.
 */
var JoinModal = (function($){
    var layerId = "JoinModal";
    var schoolName = null;
    var loadOnExitUrl = null;
    var onSubmitCallback = null;

    return{
        hideModal : function ( ){
            ModalManager.hideModal({
                'layerId' : layerId
            });
        },
        showModal : function( ){
            // alert("TEST");
//
            /*     if (onSubmitCallback) {
             this.onSubmitCallback = onSubmitCallback;
             } else {
             this.onSubmitCallback = null;
             }
             this.setEmail(email);
             this.setRedirect(redirect);
             if (showJoinFunction) {
             this.showJoinFunction = showJoinFunction;
             }
             $('#'+submitButton).click(this.validateFields);
             */
            // alert(layerId);
            console.log("showModal");
            ModalManager.showModal({
                'layerId' : layerId
            });
        },
//Join hover
//this = function() {
//    this.schoolName = null;
//    this.loadOnExitUrl = null;
//    this.onSubmitCallback = null;

        undoSimpleMssFields : function() {
            console.log("undoSimpleMssFields");
            // show first name
            $('#'+layerId+' div.'+layerId+'_firstName').show();
            // hide email label (short)
            $('#'+layerId+' div.joinLabel label.shortLabel').hide();
            // show email label (long)
            $('#'+layerId+' div.joinLabel label.longLabel').show();
            // hide confirm email
            $('#'+layerId+' div.'+layerId+'_confirmEmail').hide();
            // show username
            $('#'+layerId+' div.'+layerId+'_username').show();
            // show password
            $('#'+layerId+' div.'+layerId+'_password').show();
            // show confirm password
            $('#'+layerId+' div.'+layerId+'_confirmPassword').show();
            // show terms
            $('#'+layerId+' div.'+layerId+'_terms').show();
            // formatting changes
            $('#'+layerId+' div.separator').show();
            $('#'+layerId+' div.separatorMss').hide();
            $('#'+layerId+' div.formHelperWrapper').show();
            $('#'+layerId+' div.formHelperSpacer').show();
            $('#'+layerId+' div.btstips').removeClass('size1of1').addClass('size15of19');
            // move join button to bottom
            $('#'+layerId+' div.joinSubmit').insertAfter('#'+layerId+' div.bottomHalf');
            $('#'+layerId+' div.joinSubmit button').text('Join now'); // instead of Join now
            $('#'+layerId+' div.joinSubmit .lastUnit').show(); // instead of Join now
            // update partners text
            $('#'+layerId+' div.'+layerId+'_partners label[for="opt3"]').html(
                'Send me offers to save on family activities and special ' +
                    'promotions from our carefully chosen partners.');
        },
        showSimpleMssFields : function() {
            console.log("showSimpleMssFields");
            // hide first name
            $('#'+layerId+' div.'+layerId+'_firstName').hide();
            // show email label (short)
            $('#'+layerId+' div.joinLabel label.shortLabel').show();
            // hide email label (long)
            $('#'+layerId+' div.joinLabel label.longLabel').hide();
            // show confirm email
            $('#'+layerId+' div.'+layerId+'_confirmEmail').show();
            // hide username
            $('#'+layerId+' div.'+layerId+'_username').hide();
            // hide password
            $('#'+layerId+' div.'+layerId+'_password').hide();
            // hide confirm password
            $('#'+layerId+' div.'+layerId+'_confirmPassword').hide();
            // hide terms
            $('#'+layerId+' div.'+layerId+'_terms').hide();
            // formatting changes
            $('#'+layerId+' div.separator').hide();
            $('#'+layerId+' div.separatorMss').show();
            $('#'+layerId+' div.formHelperWrapper').hide();
            $('#'+layerId+' div.formHelperSpacer').hide();
            $('#'+layerId+' div.btstips').removeClass('size15of19').addClass('size1of1');
            // move join button to below confirm email
            $('#'+layerId+' div.joinSubmit').insertAfter('#'+layerId+' div.'+layerId+'_confirmEmail');
            $('#'+layerId+' div.joinSubmit button').text('Sign up'); // instead of Join now
            $('#'+layerId+' div.joinSubmit .lastUnit').hide(); // instead of Join now
            // update partners text
            $('#'+layerId+' div.'+layerId+'_partners label[for="opt3"]').html(
                'Send me offers to save on family activities and special ' +
                    'promotions from GreatSchools and our carefully chosen partners.');
        },
        baseFields : function() {
            console.log("baseFields");
            // hide city and state inputs
            $('#'+layerId+' .joinHover_location').hide();
            // hide nth / MSS
            $('#'+layerId+' div.grades2').hide();
            //$('#'+layerId+' div .grades ul').hide();
            // hide LD newsletter
            $('#'+layerId+' div.joinHover_ld').hide();
            // hide BTS tip
            $('#'+layerId+' div.joinHover_btstip').hide();
            //check checkbox for greatnews
            $('#'+layerId+' #opt1').prop('checked', true);
            this.undoSimpleMssFields();
        },
        //sets a notification message on the join form - can be used to explain why this hover was launched
        addMessage : function(text) {
            $('#'+layerId+' .message').html(text).show();
        },
        //method is plural to remain consistent with other hovers. Should always get called when hover closes
        clearMessages : function() {
            $('#'+layerId+' .message').empty();
            $('#'+layerId+' .message').hide();
        },
        setJoinHoverType : function(type) {
            $('#'+layerId+' form#joinGS input#joinHoverType').val(type);
        },
        setTitle : function(title) {
            $('#'+layerId+' div.hoverTitle h3').html(title);
        },
        setSubTitle : function(subTitle, subTitleText) {
            // GS-11161
            /*
             $('#'+layerId+' .introTxt h3').html(subTitle);
             $('#'+layerId+' .introTxt p').html(subTitleText);
             */
            $('#'+layerId+' .introTxt span.title').html(subTitle);
            if (subTitleText && subTitleText.charAt(0) != ',') {
                subTitleText = " " + subTitleText;
            }
            $('#'+layerId+' .introTxt span.subtitle').html(subTitleText);
        },
        configAndShowEmailTipsMssLabel : function(includeWeeklyEmails, includeTips, includeMss) {
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
                labelPhrases += " periodic updates about <strong>" + this.schoolName + "</strong>";
            }
            labelPhrases += ".";

            //choose whether to display nth grader checkboxes flyout
            if (includeTips) {
                // GS-11161
                $('#'+layerId+' div.grades2').show();
            }

            $('#'+layerId+' div.grades label[for="opt1"]').html(labelTextPrefix + labelPhrases);
        },
        // GS-11161
        configAndShowEmailTipsMssLabelNew : function() {
            var labelTextPrefix = "Sign me up for";
            var labelPhrases = " the <em>GreatSchools Weekly</em> &ndash; full of practical tips and grade-by-grade " +
                "information to help you support your child's education.";

            $('#'+layerId+' div.grades label[for="opt1"]').html(labelTextPrefix + labelPhrases);
        },
        parseCities : function(data) {
            var citySelect = $('#'+layerId+' #joinCity');
            if (data.cities) {
                citySelect.empty();
                for (var x = 0; x < data.cities.length; x++) {
                    var city = data.cities[x];
                    if (city.name) {
                        citySelect.append("<option value=\"" + city.name + "\">" + city.name + "</option>");
                    }
                }
            }
        },
        loadCities : function() {
            var state = $('#'+layerId+' #joinState').val();
            var url = "/community/registrationAjax.page";

            $('#'+layerId+' #joinCity').html("<option>Loading...</option>");

            $.getJSON(url, {state:state, format:'json', type:'city'}, this.parseCities);
        },
        loadDialog : function() {
            // TODO-10568
            //this.dialogByWidth();
            //var '+layerId+' = $('#'+layerId+'');
            '+layerId+'.find('.redirect_field').val(window.location.href);
        },
        loadOnExit : function(url) {
            this.loadOnExitUrl = url;
           // var '+layerId+' = $('#'+layerId+'');
            '+layerId+'.find('.redirect_field').val(url);
            '+layerId+'.bind('dialogclose', function() {
                window.location = this.loadOnExitUrl;
            });
        },
        cancelLoadOnExit : function() {
            this.loadOnExitUrl = null;
            $('#'+layerId+'').unbind('dialogclose');
        },
        showSignin : function() {
            if (this.loadOnExitUrl) {
                SignInModal.loadOnExit(this.loadOnExitUrl);
                this.cancelLoadOnExit();
            }
            this.hide();
            SignInModal.showHover($('#'+layerId+' #jemail').val(),
                $('#'+layerId+' .redirect_field').val(),
                SignInModal.showJoinFunction,
                this.onSubmitCallback);
            return false;
        },
        showMssAutoHoverOnExit : function(schoolName, schoolId, schoolState) {
            this.configureForMss(schoolName, schoolId, schoolState);
            this.showHoverOnExit(this.showJoinAuto);
        },
        showNthHoverOnExit : function() {
            this.showHoverOnExit(this.showJoinNth);
        },
        showHoverOnExit : function(showHoverFunction) {
            var arr = GS.getElementsByCondition(
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
                            var threeMinuteDuration = getCookieExpiresDate(0,0,3);
                            createCookieWithExpiresDate('seenHoverOnExitRecently','1',threeMinuteDuration);
                            window.destUrl = gRedirectAnchor.href;
                            // show hover
                            this.loadOnExit(gRedirectAnchor.href);
                            showHoverFunction();
                            return false;
                        }
                    } catch (e) {
                    }
                    return true;
                };
            }
        },
        configureForMss : function(schoolName, schoolId, schoolState) {
            if (schoolName) {
                this.schoolName = schoolName;
            }
            if (schoolId) {
                $('#'+layerId+' .school_id').val(schoolId);
            }
            if (schoolState) {
                $('#'+layerId+' .school_state').val(schoolState);
            }
        },
        configureOmniture : function(pageName, hier1) {
            this.pageName=pageName;
            this.hier1=hier1;
        },
        showJoinAuto : function(schoolName, schoolId, schoolState) {
            $('#joinBtn').click(this.clickSubmitHandler);
            this.configureForMss(schoolName, schoolId, schoolState);
            this.baseFields();
            this.setTitle("Send me updates");
            // GS-11161
            this.setSubTitle("Get timely updates for " + this.schoolName,
                ", including performance data and recently posted user reviews.");
            // show nth / MSS
            // GS-11161
            //this.configAndShowEmailTipsMssLabel(true, true, true);
            this.configAndShowEmailTipsMssLabelNew();

            this.showSimpleMssFields();

            this.setJoinHoverType("Auto");

            this.configureOmniture('MSS Join Hover', 'Hovers,Join,MSS Join Hover');

            SignInModal.showJoinFunction = this.showJoinAuto;
            this.showModal();
        },
        showSchoolReviewJoin : function(onSubmitCallback) {
            $('#joinBtn').click(this.clickSubmitHandler);
            this.baseFields();
            if (onSubmitCallback) {
                this.onSubmitCallback = onSubmitCallback;
            }
            this.setTitle("Almost done!");
            this.setSubTitle("Join GreatSchools",
                " to submit your review. Once you verify your email address, your review will be posted, provided it meets our guidelines.");

            // set label for weekly updates opt-in
            if (this.schoolName) {
                this.configAndShowEmailTipsMssLabel(true, true, true);
            } else {
                this.configAndShowEmailTipsMssLabel(true, true, false);
            }

            this.setJoinHoverType("SchoolReview");
            $('#joinHover_cancel').hide();

            this.configureOmniture('School Reviews Join Hover', 'Hovers,Join,School Reviews Join Hover');

            SignInModal.showJoinFunction = this.showSchoolReviewJoin;
            this.showModal();
        },
        showLearningDifficultiesNewsletter : function() {
            $('#joinBtn').click(this.clickSubmitHandler);
            this.onSubmitCallback = null;
            this.baseFields();
            this.setTitle("Special Education newsletter");
            this.setSubTitle("Join GreatSchools",
                "to get the resources you need to support your child with a learning difficulty or attention problem");
            // show nth / MSS
            this.configAndShowEmailTipsMssLabel(true, true, false);
            // show LD newsletter
            $('#joinHover .joinHover_ld').show();

            //set up checkboxes
            $('#joinHover #opt2').prop('checked', true);

            this.setJoinHoverType("LearningDifficultiesNewsletter");

            this.configureOmniture('Special Ed NL Join Hover', 'Hovers,Join,Special Ed NL Join Hover');

            SignInModal.showJoinFunction = this.showLearningDifficultiesNewsletter;
            this.showModal();
        },
        showBackToSchoolTipOfTheDay : function() {
            $('#joinBtn').click(this.clickSubmitHandler);
            this.onSubmitCallback = null;
            this.baseFields();
            this.setTitle("Back-to-School Tip of the Day");
            this.setSubTitle("Join GreatSchools",
                "to get Back-to-School tips delivered straight to your inbox!");
            // show nth / MSS
            this.configAndShowEmailTipsMssLabel(true, true, false);
            // show BTS tip
            $('#joinHover .joinHover_btstip').show();
            // hide partners
            $('#joinHover .joinHover_partners').hide();

            //set up checkboxes
            $('#joinHover #opt4').prop('checked', true);

            this.setJoinHoverType("BTSTip");

            this.configureOmniture('Back to School Tips Join Hover', 'Hovers,Join,Back to School Tips Join Hover');

            SignInModal.showJoinFunction = this.showBackToSchoolTipOfTheDay;
            this.showModal();
        },
        showJoinPostComment : function() {
            $('#joinBtn').click(this.clickSubmitHandler);
            this.onSubmitCallback = null;
            this.baseFields();
            this.setTitle("Speak your mind");
            this.setSubTitle("Join GreatSchools",
                "to participate in the parent community and other discussions on our site");
            // show nth / MSS
            this.configAndShowEmailTipsMssLabel(true, true, false);

            this.setJoinHoverType("PostComment");

            this.configureOmniture('Community Join Hover', 'Hovers,Join,Community Join Hover');

            SignInModal.showJoinFunction = this.showJoinPostComment;
            this.showModal();
        },
        showJoinTrackGrade : function() {
            this.setJoinHoverType("TrackGrade");
            SignInModal.showJoinFunction = this.showJoinTrackGrade;
            this.showJoinNth();
        },
        showJoinGlobalHeader : function() {
            this.setJoinHoverType("GlobalHeader");
            SignInModal.showJoinFunction = this.showJoinGlobalHeader;
            this.showJoinNth();
        },
        showJoinFooterNewsletter : function() {
            this.setJoinHoverType("FooterNewsletter");
            SignInModal.showJoinFunction = this.showJoinFooterNewsletter;
            this.showJoinNth();
        },
        showJoinNth : function() {
            $('#joinBtn').click(this.clickSubmitHandler);
            this.onSubmitCallback = null;
            this.baseFields();
            this.setTitle("Is your child on track?");
            this.setSubTitle("Join GreatSchools",
                "to get grade-by-grade tips and practical advice to help you guide your child to educational success.");
            // show nth / MSS
            this.configAndShowEmailTipsMssLabel(true, true, false);

            this.configureOmniture('Weekly NL Join Hover', 'Hovers,Join,Weekly NL Join Hover');

            this.showModal();
        },
        showJoinMsl : function() {
            $('#joinBtn').click(this.clickSubmitHandler);
    //        this.configureForMss(schoolName, schoolId, schoolState);
            this.baseFields();
            this.setTitle("Welcome to My School List");
            this.setSubTitle("Join GreatSchools",
                "to save one or more schools of interest to your personalized list.");
            // show nth / MSS
            this.configAndShowEmailTipsMssLabel(true, true, false);

            this.setJoinHoverType("MSL");

            this.configureOmniture('MSL Join Hover', 'Hovers,Join,MSL Join Hover');

            SignInModal.showJoinFunction = this.showJoinMsl;
            this.showModal();
        },
        validateFirstName : function() {
            $.getJSON(
                GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
                {firstName:$('#joinGS #fName').val(), field:'firstName'},
                function(data) {
                    this.validateFieldResponse('#joinGS .joinHover_firstName .errors', 'firstName', data);
                });
        },
        validateEmail : function() {
            $.getJSON(
                GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
                {email:$('#joinGS #jemail').val(), field:'email', simpleMss: ($('#joinHoverType').val() === 'Auto')},
                function(data) {
                    this.validateFieldResponse('#joinGS .joinHover_email .errors', 'email', data);
                });
        },
        validateConfirmEmail : function() {
            $.getJSON(
                GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
                {email:$('#joinGS #jemail').val(), confirmEmail:$('#joinGS #jcemail').val(), field:'confirmEmail', simpleMss: ($('#joinHoverType').val() === 'Auto')},
                function(data) {
                    this.validateFieldResponse('#joinGS .joinHover_confirmEmail .errors', 'confirmEmail', data);
                });
        },
        validateUsername : function() {
            $.getJSON(
                GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
                {screenName:$('#joinGS #uName').val(), email:$('#joinGS #jemail').val(), field:'username'},
                function(data) {
                    this.validateFieldResponse('#joinGS .joinHover_username .errors', 'screenName', data);
                });
        },
        validatePassword : function() {
            $.getJSON(
                GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
                {password:$('#joinGS #jpword').val(), confirmPassword:$('#joinGS #cpword').val(), field:'password'},
                function(data) {
                    this.validateFieldResponse('#joinGS .joinHover_password .errors', 'password', data);
                });
            this.validateConfirmPassword();
        },
        validateConfirmPassword : function() {
            $.getJSON(
                GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
                {password:$('#joinGS #jpword').val(), confirmPassword:$('#joinGS #cpword').val(), field:'confirmPassword'},
                function(data) {
                    this.validateFieldResponse('#joinGS .joinHover_confirmPassword .errors', 'confirmPassword', data);
                });
        },
        validateFieldResponse : function(fieldSelector, fieldName, data) {
            var fieldError = $(fieldSelector + ' .invalid');
            var fieldValid = $(fieldSelector + ' .valid');
            fieldError.hide();
            fieldValid.hide();
            if (data && data[fieldName]) {
                fieldError.html(data[fieldName]);
                fieldError.show();
                if (fieldName == 'email') {
                    $('#joinGS .joinHover_email .invalid a.launchSignInHover').click(function() {
                        this.showSignin();
                        return false;
                    });
                }
            } else {
                fieldValid.show();
            }
        },
        clickSubmitHandler : function() {
            var params = $('#joinGS').serialize();
            $('#joinBtn').prop('disabled', true);


            //if - Choose city - is selected, just remove this from the form, as if no city was given
            if ($('#joinCity').val() == '- Choose city -') {
                params = params.replace(/&city=([^&]+)/, "");
            }

            var first = true;
            var newsletters = [];
            $('#joinGS [name="grades"]').each(function() {
                if ($(this).prop('checked')) {
                    newsletters.push(encodeURIComponent($(this).val()));
                }
            });

            params += "&grades=" + newsletters.join(',');

            params += "&simpleMss=" + ($('#joinHoverType').val() === 'Auto');

            $.getJSON(GS.uri.Uri.getBaseHostname() + "/community/registrationValidationAjax.page", params, GS.joinHover_checkValidationResponse);
            return false;
        }
    }
})(jQuery);

