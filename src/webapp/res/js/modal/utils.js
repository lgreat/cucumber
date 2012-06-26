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

GS.showAddMslJoinHover = function(omniturePageName, schoolName, schoolId, schoolState, elem) {
    if (omniturePageName && s.tl) {
        s.tl(true, 'o', 'Add_to_MSL_Link_' + omniturePageName);
    }

    var statePlusId = schoolState + schoolId;
    var mslHelper = new GS.community.MySchoolListHelper();
    if (GS.isSignedIn()) {
        if (omniturePageName) {
            mslHelper.addSchool(schoolState, schoolId, function() {
                jQuery('.js-add-msl-' + statePlusId).find('.js-msl-text').html("Added to <a href=\"/mySchoolList.page\">My School List</a>");
                jQuery('.js-add-msl-' + statePlusId).find('.sprite').attr("class", "sprite i-checkmark-sm img");
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
            mslHelper.addSchool(schoolState, schoolId, function() {}, function() {}, email);
            GSType.hover.signInHover.setRedirect(redirect);
            jQuery('#' + formId).submit();
        };
//        GSType.hover.joinHover.configureForMss(schoolName, schoolId, schoolState);
        GSType.hover.joinHover.onSubmitCallback = mslSuccessCallback;
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
        if (GS.isMember()) {
            GSType.hover.signInHover.showHover('', redirect, GSType.hover.joinHover.showJoinMsl, mslSuccessCallback);
        } else {
            GSType.hover.joinHover.showJoinMsl();
        }
    }
    return false;
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


