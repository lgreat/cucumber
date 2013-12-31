/**
 * Created with IntelliJ IDEA.
 * User: mseltzer
 * Date: 6/22/12
 * Time: 10:02 AM
 * To change this template use File | Settings | File Templates.
 */
var JoinModal = (function($){
    var layerId = "JoinModal";
    var containerId = "fullPageOverlay";
    var formId = "joinGS";
    var formAccess = "#"+containerId+" #"+formId;
    var schoolName = null;
    var loadOnExitUrl = null;
    var onSubmitCallback = null;




//    $('#'+layerId+'_cancel').click(function() {
//        this.hideModal();
//        return false;
//    });


//    $('#'+layerId+' #jemail').blur(this.validateEmail);
//    $('#'+layerId+' #jcemail').blur(this.validateConfirmEmail);
//    $('#'+layerId+' #uName').blur(this.validateUsername);
//    $('#'+layerId+' #jpword').blur(this.validatePassword);
//    $('#'+layerId+' #cpword').blur(this.validateConfirmPassword);

//    $('#'+layerId).bind('dialogclose', function() {
//        $('#joinGS .error').hide();
//        this.clearMessages();
//    });

    var checkValidationResponse = function(data) {
        console.log("checkValidationResponse");

        if (passesValidationResponse(data)) {
            console.log("passesValidationResponse -- true");
            if (JoinModal.loadOnExitUrl) {
                console.log("JoinModal.loadOnExitUrl -- true");
                JoinModal.cancelLoadOnExit();
            }
            if (JoinModal.onSubmitCallback) {
                console.log("JoinModal.onSubmitCallback -- true");
                JoinModal.onSubmitCallback($(formAccess+"  #jemail").val(), "joinGS");
            } else {
//                $(formAccess).submit();
//                $(formAccess).submit(function() {
//                    return false; // prevent multiple submits
//                });
                console.log("JoinModal.onSubmitCallback -- false");
                JoinModal.hideModal();
            }
        }else {
            $(formAccess+' #joinBtn').prop('disabled', false);
        console.log("passesValidationResponse -- false");
       // $(formAccess+' #joinBtn').prop('disabled', false);
        }
    };

    var passesValidationResponse = function(data) {
        console.log("passesValidationResponse");
        var firstNameError = $(formAccess+' .joinHover_firstName .invalid');
        var emailError = $(formAccess+' .joinHover_email .invalid');
        var usernameError = $(formAccess+' .joinHover_username .invalid');
        var usernameValid = $(formAccess+' .joinHover_username .valid');
        var passwordError = $(formAccess+' .joinHover_password .invalid');
        var confirmPasswordError = $(formAccess+' .joinHover_confirmPassword .invalid');
        // GS-11161
        //var termsError = jQuery('#joinGS #joinHover_termsNotChecked');
        var locationError = $(formAccess+' #joinHover_chooseLocation');

        firstNameError.hide();
        emailError.hide();
        usernameError.hide();
        usernameValid.hide();
        passwordError.hide();
        confirmPasswordError.hide();
        // GS-11161
        //termsError.hide();
        locationError.hide();

        var objCount = 0;
        for (_obj in data) objCount++;

        if (objCount > 0) {
            $(formAccess+' #process_error').show("fast");

            /*
             // GS-11161
             if (data.terms) {
             termsError.html(data.terms).show();
             }
             */

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
                $(formAccess+' .joinHover_email .invalid a.launchSignInHover').click(function() {
                    JoinModal.showSignin();
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

    return{
        hideModal : function ( ){
            console.log("hideModal");
            ModalManager.hideModal({
                'layerId' : layerId
            });
        },
        showModal : function( ){
            // alert("TEST");
//
//           if (onSubmitCallback) {
//             this.onSubmitCallback = onSubmitCallback;
//             } else {
//             this.onSubmitCallback = null;
//             }
//             this.setEmail(email);
//             this.setRedirect(redirect);
//             if (showJoinFunction) {
//             this.showJoinFunction = showJoinFunction;
//             }
//             $('#'+submitButton).click(this.validateFields);
//            console.log("showModal");
            ModalManager.showModal({
                'layerId' : layerId
            });

            $(formAccess+' #fName').blur(this.validateFirstName);
            $(formAccess+' #jemail').blur(this.validateEmail);
            $(formAccess+' #jcemail').blur(this.validateConfirmEmail);
            $(formAccess+' #uName').blur(this.validateUsername);
            $(formAccess+' #jpword').blur(this.validatePassword);
            $(formAccess+' #cpword').blur(this.validateConfirmPassword);
            $(formAccess+' #joinState').change(this.loadCities);
            $(formAccess+' #joinBtn').click(this.clickSubmitHandler);
            this.loadCities;
        },

        undoSimpleMssFields : function() {
            console.log("undoSimpleMssFields");
            // show first name
            $(formAccess+' div.'+layerId+'_firstName').show();
            // hide email label (short)
            $(formAccess+' div.joinLabel label.shortLabel').hide();
            // show email label (long)
            $(formAccess+' div.joinLabel label.longLabel').show();
            // hide confirm email
            $(formAccess+' div.'+layerId+'_confirmEmail').hide();
            // show username
            $(formAccess+' div.'+layerId+'_username').show();
            // show password
            $('#'+layerId+' div.'+layerId+'_password').show();
            // show confirm password
            $(formAccess+' div.'+layerId+'_confirmPassword').show();
            // show terms
            $(formAccess+' div.'+layerId+'_terms').show();
            // formatting changes
            $(formAccess+' div.separator').show();
            $(formAccess+' div.separatorMss').hide();
            $(formAccess+' div.formHelperWrapper').show();
            $(formAccess+' div.formHelperSpacer').show();
            $(formAccess+' div.btstips').removeClass('size1of1').addClass('size15of19');
            // move join button to bottom
            $(formAccess+' div.joinSubmit').insertAfter('#'+layerId+' div.bottomHalf');
            $(formAccess+') div.joinSubmit button').text('Join now'); // instead of Join now
            $(formAccess+' div.joinSubmit .lastUnit').show(); // instead of Join now
            // update partners text
            $(formAccess+' div.'+layerId+'_partners label[for="opt3"]').html(
                'Send me offers to save on family activities and special ' +
                    'promotions from our carefully chosen partners.');
        },
        showSimpleMssFields : function() {
            console.log("showSimpleMssFields");
            // hide first name
            $(formAccess+' div.'+layerId+'_firstName').hide();
            // show email label (short)
            $(formAccess+' div.joinLabel label.shortLabel').show();
            // hide email label (long)
            $(formAccess+' div.joinLabel label.longLabel').hide();
            // show confirm email
            $(formAccess+' div.'+layerId+'_confirmEmail').show();
            // hide username
            $(formAccess+' div.'+layerId+'_username').hide();
            // hide password
            $(formAccess+' div.'+layerId+'_password').hide();
            // hide confirm password
            $(formAccess+' div.'+layerId+'_confirmPassword').hide();
            // hide terms
            $(formAccess+' div.'+layerId+'_terms').hide();
            // formatting changes
            $(formAccess+' div.separator').hide();
            $(formAccess+' div.separatorMss').show();
            $(formAccess+' div.formHelperWrapper').hide();
            $(formAccess+' div.formHelperSpacer').hide();
            $(formAccess+' div.btstips').removeClass('size15of19').addClass('size1of1');
            // move join button to below confirm email
            $(formAccess+' div.joinSubmit').insertAfter(formAccess+' div.'+layerId+'_confirmEmail');
            $(formAccess+' div.joinSubmit button').text('Sign up'); // instead of Join now
            $(formAccess+' div.joinSubmit .lastUnit').hide(); // instead of Join now
            // update partners text
            $(formAccess+' div.'+layerId+'_partners label[for="opt3"]').html(
                'Send me offers to save on family activities and special ' +
                    'promotions from GreatSchools and our carefully chosen partners.');
        },
        baseFields : function() {
            console.log("baseFields");
            // hide city and state inputs
            $(formAccess+' .joinHover_location').hide();
            // hide nth / MSS
            $(formAccess+' div.grades2').hide();
            //$(formAccess+' div .grades ul').hide();
            // hide LD newsletter
            $(formAccess+' div.joinHover_ld').hide();
            // hide BTS tip
            $(formAccess+' div.joinHover_btstip').hide();
            //check checkbox for greatnews
            $(formAccess+' #opt1').prop('checked', true);
            this.undoSimpleMssFields();
        },
        //sets a notification message on the join form - can be used to explain why this hover was launched
        addMessage : function(text) {
            console.log("addMessage");
            $(formAccess+' .message').html(text).show();
        },
        //method is plural to remain consistent with other hovers. Should always get called when hover closes
        clearMessages : function() {
            console.log("clearMessages");
            $(formAccess+' .message').empty();
            $(formAccess+' .message').hide();
        },
        setJoinHoverType : function(type) {
            console.log("setJoinHoverType");
            $(formAccess+' #joinHoverType').val(type);
        },
        ///
        setTitle : function(title) {
            console.log("setTitle");
            $(formAccess+' div.hoverTitle h3').html(title);
        },
        /////
        setSubTitle : function(subTitle, subTitleText) {
            console.log("setSubTitle");
            // GS-11161
            /*
             $(formAccess+' .introTxt h3').html(subTitle);
             $(formAccess+' .introTxt p').html(subTitleText);
             */
            $(formAccess+' .introTxt span.title').html(subTitle);
            if (subTitleText && subTitleText.charAt(0) != ',') {
                subTitleText = " " + subTitleText;
            }
            $(formAccess+' .introTxt span.subtitle').html(subTitleText);
        },
        configAndShowEmailTipsMssLabel : function(includeWeeklyEmails, includeTips, includeMss) {
            console.log("configAndShowEmailTipsMssLabel");
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
                $(formAccess+' div.grades2').show();
            }

            $(formAccess+' div.grades label[for="opt1"]').html(labelTextPrefix + labelPhrases);
        },
        // GS-11161

        configAndShowEmailTipsMssLabelNew : function() {
            console.log("configAndShowEmailTipsMssLabelNew");
            var labelTextPrefix = "Sign me up for";
            var labelPhrases = " the <em>GreatSchools Weekly</em> &ndash; full of practical tips and grade-by-grade " +
                "information to help you support your child's education.";

            $(formAccess+' div.grades label[for="opt1"]').html(labelTextPrefix + labelPhrases);
        },
        parseCities : function(data) {

            console.log("parseCities");
            var citySelect = $(formAccess+' #joinCity');
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


            var state = $(formAccess+' #joinState').val();
            var url = "/community/registrationAjax.page";

            $(formAccess+' #joinCity').html("<option>Loading...</option>");
            console.log("loadCities");
            $.getJSON(url, {state:state, format:'json', type:'city'}, JoinModal.parseCities);
        },
        loadDialog : function() {
            console.log("loadDialog");
            // TODO-10568
            //this.dialogByWidth();
            $(formAccess).find('.redirect_field').val(window.location.href);
        },
        loadOnExit : function(url) {
            console.log("loadOnExit");
            this.loadOnExitUrl = url;
            $(formAccess).find('.redirect_field').val(url);
        },
        cancelLoadOnExit : function() {
            console.log("cancelLoadOnExit");
            this.loadOnExitUrl = null;
            $(formAccess).unbind('dialogclose');
        },
        signIn : function() {
            console.log("signIn");
            if (this.loadOnExitUrl) {
                SignInModal.loadOnExit(this.loadOnExitUrl);
                this.cancelLoadOnExit();
            }
            this.hideModal();
            SignInModal.showModal($(formAccess+' #jemail').val(),
                $(formAccess+' .redirect_field').val(),
                SignInModal.showJoinFunction,
                this.onSubmitCallback);
            return false;
        },
        showMssAutoHoverOnExit : function(schoolName, schoolId, schoolState) {
            console.log("showMssAutoHoverOnExit");
            this.configureForMss(schoolName, schoolId, schoolState);
            this.showHoverOnExit(this.showJoinAuto);
        },
        showNthHoverOnExit : function() {
            console.log("showNthHoverOnExit");
            this.showHoverOnExit(this.showJoinNth);
        },
        showHoverOnExit : function(showHoverFunction) {
            console.log("showHoverOnExit");
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
            console.log("configureForMss");
            if (schoolName) {
                this.schoolName = schoolName;
            }
            if (schoolId) {
                $(formAccess+' .school_id').val(schoolId);
            }
            if (schoolState) {
                $(formAccess+' .school_state').val(schoolState);
            }
        },
        configureOmniture : function(pageName, hier1) {
            console.log("configureOmniture");
            this.pageName=pageName;
            this.hier1=hier1;
        },
        showJoinAuto : function(schoolName, schoolId, schoolState) {
            console.log("showJoinAuto");
           // $(formAccess+' #joinBtn').click(this.clickSubmitHandler);
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
            console.log("showSchoolReviewJoin");
           // $(formAccess+' #joinBtn').click(this.clickSubmitHandler);
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
            //$(formAccess+' #joinHover_cancel').hide();

            this.configureOmniture('School Reviews Join Hover', 'Hovers,Join,School Reviews Join Hover');

            SignInModal.showJoinFunction = this.showSchoolReviewJoin;
            this.showModal();
        },
        showLearningDifficultiesNewsletter : function() {
            console.log("showLearningDifficultiesNewsletter");
          //  $(formAccess+' #joinBtn').click(this.clickSubmitHandler);
            this.onSubmitCallback = null;
            this.baseFields();
            this.setTitle("Special Education newsletter");
            this.setSubTitle("Join GreatSchools",
                "to get the resources you need to support your child with a learning difficulty or attention problem");
            // show nth / MSS
            this.configAndShowEmailTipsMssLabel(true, true, false);
            // show LD newsletter
            $(formAccess+' .joinHover_ld').show();

            //set up checkboxes
            $(formAccess+' #opt2').prop('checked', true);

            this.setJoinHoverType("LearningDifficultiesNewsletter");

            this.configureOmniture('Special Ed NL Join Hover', 'Hovers,Join,Special Ed NL Join Hover');

            SignInModal.showJoinFunction = this.showLearningDifficultiesNewsletter;
            this.showModal();
        },
        showBackToSchoolTipOfTheDay : function() {
            console.log("showBackToSchoolTipOfTheDay");
           // $(formAccess+' #joinBtn').click(this.clickSubmitHandler);
            this.onSubmitCallback = null;
            this.baseFields();
            this.setTitle("Back-to-School Tip of the Day");
            this.setSubTitle("Join GreatSchools",
                "to get Back-to-School tips delivered straight to your inbox!");
            // show nth / MSS
            this.configAndShowEmailTipsMssLabel(true, true, false);
            // show BTS tip
            $(formAccess+'  .joinHover_btstip').show();
            // hide partners
            $(formAccess+'  .joinHover_partners').hide();

            //set up checkboxes
            $(formAccess+'  #opt4').prop('checked', true);

            this.setJoinHoverType("BTSTip");

            this.configureOmniture('Back to School Tips Join Hover', 'Hovers,Join,Back to School Tips Join Hover');

            SignInModal.showJoinFunction = this.showBackToSchoolTipOfTheDay;
            this.showModal();
        },
        showJoinPostComment : function() {
            console.log("showJoinPostComment");
           // $(formAccess+' #joinBtn').click(this.clickSubmitHandler);
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
            console.log("showJoinTrackGrade");
            this.setJoinHoverType("TrackGrade");
            SignInModal.showJoinFunction = this.showJoinTrackGrade;
            this.showJoinNth();
        },
        showJoinGlobalHeader : function() {
            console.log("showJoinGlobalHeader");
            this.setJoinHoverType("GlobalHeader");
            SignInModal.showJoinFunction = this.showJoinGlobalHeader;
            this.showJoinNth();
        },
        showJoinFooterNewsletter : function() {
            console.log("showJoinFooterNewsletter");
            this.setJoinHoverType("FooterNewsletter");
            SignInModal.showJoinFunction = this.showJoinFooterNewsletter;
            this.showJoinNth();
        },
        showJoinNth : function() {
            console.log("showJoinNth");
//            $(formAccess+' #joinBtn').click(this.clickSubmitHandler);
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
            console.log("showJoinMsl");
            $(formAccess+' #joinBtn').click(this.clickSubmitHandler);
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
            console.log("validateFirstName:"+ GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page');
            $.getJSON(
                GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
                {firstName:$(formAccess+'  #fName').val(), field:'firstName'},
                $.proxy(function(data) {
                    JoinModal.validateFieldResponse(formAccess+' .joinHover_firstName .errors', 'firstName', data);
                }));
        },
        validateEmail : function() {

            console.log("validateEmail:"+ GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page');
            $.getJSON(
                GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
                {email:$(formAccess+'  #jemail').val(), field:'email', simpleMss: ($(formAccess+' #joinHoverType').val() === 'Auto')},
                $.proxy(function(data){
                    JoinModal.validateFieldResponse(formAccess+'  .joinHover_email .errors', 'email', data);
                }));
        },
        validateConfirmEmail : function() {
            console.log("validateConfirmEmail");
            $.getJSON(
                GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
                {email:$(formAccess+'  #jemail').val(), confirmEmail:$(formAccess+'  #jcemail').val(), field:'confirmEmail', simpleMss: ($(formAccess+' #joinHoverType').val() === 'Auto')},
                $.proxy(function(data) {
                    JoinModal.validateFieldResponse(formAccess+'  .joinHover_confirmEmail .errors', 'confirmEmail', data);
                }));
        },
        validateUsername : function() {
            console.log("validateUsername");
            $.getJSON(
                GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
                {screenName:$(formAccess+'  #uName').val(), email:$(formAccess+'  #jemail').val(), field:'username'},
                $.proxy(function(data) {
                    JoinModal.validateFieldResponse(formAccess+' .joinHover_username .errors', 'screenName', data);
                }));
        },
        validatePassword : function() {
            console.log("validatePassword");
            $.getJSON(
                GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
                {password:$(formAccess+'  #jpword').val(), confirmPassword:$(formAccess+'  #cpword').val(), field:'password'},
                $.proxy(function(data) {
                    JoinModal.validateFieldResponse(formAccess+'  .joinHover_password .errors', 'password', data);
                }));
            JoinModal.validateConfirmPassword();
        },
        validateConfirmPassword : function() {
            console.log("validateConfirmPassword");
            $.getJSON(
                GS.uri.Uri.getBaseHostname() + '/community/registrationValidationAjax.page',
                {password:$(formAccess+'  #jpword').val(), confirmPassword:$(formAccess+'  #cpword').val(), field:'confirmPassword'},
                $.proxy(function(data) {
                    JoinModal.validateFieldResponse(formAccess+'  .joinHover_confirmPassword .errors', 'confirmPassword', data);
                }));
        },
        validateFieldResponse : function(fieldSelector, fieldName, data) {
            console.log("validateFieldResponse");
            var fieldError = $(fieldSelector + ' .invalid');
            var fieldValid = $(fieldSelector + ' .valid');
            fieldError.hide();
            fieldValid.hide();
            if (data && data[fieldName]) {
                fieldError.html(data[fieldName]);
                fieldError.show();
                if (fieldName == 'email') {
                    $(formAccess+'  .joinHover_email .invalid a.launchSignInHover').click(function() {
                        this.showSignin();
                        return false;
                    });
                }
            } else {
                fieldValid.show();
            }
        },
        clickSubmitHandler : function() {
            console.log("clickSubmitHandler");
            var params = $(formAccess).serialize();

            console.log(params);
            $(formAccess+' #joinBtn').prop('disabled', true);


            //if - Choose city - is selected, just remove this from the form, as if no city was given
            if ($(formAccess+' #joinCity').val() == '- Choose city -') {
                params = params.replace(/&city=([^&]+)/, "");
            }

            var first = true;
            var newsletters = [];
            $(formAccess+'  [name="grades"]').each(function() {
                if ($(this).prop('checked')) {
                    newsletters.push(encodeURIComponent($(this).val()));
                }
            });

            params += "&grades=" + newsletters.join(',');

            params += "&simpleMss=" + ($(formAccess+' #joinHoverType').val() === 'Auto');
            console.log("registrationValidationAjax1");
            $.getJSON(GS.uri.Uri.getBaseHostname() + "/community/registrationValidationAjax.page", params, checkValidationResponse);
            console.log("registrationValidationAjax2");
            return false;
        }
    }
//    $('#joinGS #fName').bind("blur",
//        function(){ console.log("TEST fname"); }
//    );


//    $(document).ready(function() {
//        console.log("joinGS #fName:"+ $('#joinGS #fName'));
//        $('#joinGS #fName').blur(
//            //validateFirstName
//            //
//            function(){ console.log("TEST fname"); }
//        );
//
//    });

})(jQuery);


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
    } else if (location.hostname.match("dev\.|dev$|alpha.*\\.|alpha$|\.office\.|cmsqa|localhost|127\.0\.0\.1|macbook|qaapp-1\.|qaapp-2\.|qaadmin-1\.|qa\.|qa-preview\.")) {
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
