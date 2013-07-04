var GS = GS || {};

// requires jQuery
GS.facebook = GS.facebook || (function () {
    var registrationAndLoginUrl = "/community/registration/socialRegistrationAndLogin.json";

    // JQuery selector for FB login button in the right rail on city browse (search result) pages for pilot cities.
    // It's a class selector so might have been introduced on other pages
    var loginSelector = ".js-facebook-login";
    var logoutSelector = ".js-facebook-logout";


    // Facebook permissions that GS.org will ask for during FB.login()
    var facebookPermissions = 'email,user_likes,friends_likes,friends_education_history,user_education_history';


    // FQL query that should return all your friends
    var userFriendsQuery = "SELECT uid, name, pic_square, profile_url FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me())";


    // FQL query to get Education-based page_fan (an association table) rows for your friends
    var friendsThatAreFansOfSchoolsQuery = "SELECT uid, page_id, type FROM page_fan WHERE uid IN (SELECT uid FROM #userFriendsQuery) AND (type = 'School' or type = 'Education' or type = 'Public School' or type = 'Private School' or type = 'Charter School' or type = 'Elementary School' or type = 'Middle School' or type = 'High School')";


    // FQL query to get Page objects that match page_fan rows fetched previously
    var schoolPageDetailsQuery = "SELECT page_id, name, location, page_url FROM page WHERE (location.state != '' OR website != '') AND page_id IN (SELECT page_id FROM #friendsThatAreFansOfSchoolsQuery)";


    // Resolved on first successful FB login; never rejected
    var successfulLoginDeferred = $.Deferred();


    // Resolved immediately on load only if user is already logged in, otherwise rejected
    // added to help with Omniture requirement, might not be needed in the future
    var statusOnLoadDeferred = $.Deferred();


    // If the user ever logged in, they're probably logged in. But, their session could have expired
    var mightBeLoggedIn = function () {
        return successfulLoginDeferred.isResolved();
    };


    // TODO: move these UI methods
    var firstLinkSelector = "#utilLinks a:eq(0)";
    var secondLinkSelector = "#utilLinks a:eq(1)";
    var thirdLinkSelector = "#utilLinks a:eq(2)";
    var getSignOutLink = function (email, userId) {
        return "/cgi-bin/logout/CA/?email=" + encodeURIComponent(email) + "&mid=" + userId;
    };
    var getSignOutLinkHtml = function (email, userId) {
        var html = '<a class="js-log-out" href="' + getSignOutLink(email, userId) + '">Sign Out</a>';
        return html;
    };
    var getWelcomeHtml = function (firstName) {
        var html = '<li>Welcome, <a class="nav_group_heading" href="/account/">' + firstName + '</a></li>';
        return html;
    };
    var getMySchoolListHtml = function (numberMSLItems) {
        var html = '<a rel="nofollow" href="/mySchoolList.page">My School List (' + numberMSLItems + ')</a>';
        return html;
    };
    var updateUIForLogin = function (userId, email, firstName, numberMSLItems) {
        $(firstLinkSelector).parent().replaceWith(getWelcomeHtml(firstName));
        $(secondLinkSelector).replaceWith(getSignOutLinkHtml(email, userId));
        $(thirdLinkSelector).replaceWith(getMySchoolListHtml(numberMSLItems));
    };


    /**
     * Asks FB JS JDK for User's login status. Executes callbacks based on result
     *
     * @param options
     *      connected: callback that's called if user is connected
     *      notConnected: callback that's called if user is not connected
     */
    var status = function (options) {
        FB.getLoginStatus(function (response) {
            if (response.status === 'connected') {
                if (options && options.connected) {
                    options.connected();
                }
                // connected
            } else if (response.status === 'not_authorized') {
                // not_authorized
                if (options && options.notConnected) {
                    options.notConnected();
                }
            } else {
                if (options && options.notConnected) {
                    options.notConnected();
                }
                // not_logged_in
            }
        });
    };

    // GS-13920
    var trackLoginClicked = function () {
        omnitureEventNotifier.clear();
        omnitureEventNotifier.successEvents = "event79;";
        omnitureEventNotifier.send();
    };

    var trackGSAccountCreated = function() {
        s.tl(true,'o', 'GS_FB_account_created');
    };

    var getLoginDeferred = function () {
        return successfulLoginDeferred.promise();
    };
    var getStatusOnLoadDeferred = function () {
        return statusOnLoadDeferred.promise();
    };


    // Meant to be fired right after FB JS has downloaded / executed
    // Sets up click event for Log In button(s)
    // Sets up default behavior for login deferreds
    var init = function () {
        // We probably need to handle logout on every page, so call it here
        // rather than leaving it up to the individual page
        initLogoutBehavior();

        $(function () {
            // this flag will control whether onClick code is executed, used to prevent double-triggering on rapid clicks
            var loginDisabled = false;

            // we'll set up click handler for login buttons here
            $(loginSelector).on('click', function () {
                if (!loginDisabled) {
                    // even though we don't know if the user is logged in or not, disable login right away.
                    // if we were to put it inside the callback, there would be a delay before the login button is disabled
                    loginDisabled = true;

                    status({
                        notConnected: function () {
                            trackLoginClicked();
                            login().always(function() {
                                loginDisabled = false;
                            });
                        }
                    });
                }
            });

            // Call status() right away, and if user is logged in, resolve loginDeferred and statusOnLoadDeferred
            status({
                connected: function () {
                    statusOnLoadDeferred.resolve();
                    successfulLoginDeferred.resolve();
                },
                notConnected: function () {
                    statusOnLoadDeferred.reject();
                }
            });
        });
    };

    // Set up click handler on logout links. Need to synchronize FB and GS logout behavior
    var initLogoutBehavior = function () {
        $('#utilLinks').on('click', '.js-log-out', function (e) {
            // 1. get the href the user clicked
            // 2. If possibly logged in, tell FB to log out
            // 3. Stop default link behavior
            // 4. After logged out, send them to where they wanted to go

            var $this = $(this);
            var href = $this.attr('href');

            FB.getLoginStatus(function(response){
                if (response.status === "connected") {
                    FB.logout(function (response) {
                        logout();
                    });
                } else {
                    logout();
                }
            });
            e.preventDefault();
            return false;
        });
        $(logoutSelector).click(function() {
            FB.getLoginStatus(function(response){
                if (response.status === "connected") {
                    FB.logout(function (response) {
                        logout();
                        window.location.reload();
                    });
                } else {
                    logout();
                    window.location.reload();
                }
            });
        });
    };


    // When a user logs out, we may or may not need to redirect them away from the current page. If user is on a
    // member-only page (like the Account page) then we redirect to home page.
    var getLogoutRedirectUrl = function() {
        var redirectUrl = "/index.page";

        var currentUrl = window.location.href;

        if (currentUrl.match("mySchoolList\\.page|resetPassword\\.page|changeEmail\\.page|accountInformation\\.page|interstitial\\.page")) {
            redirectUrl = "/index.page";
        } else if (currentUrl.match("community\\.") && currentUrl.match("dashboard|members.*profile|members.*awards|recommend\\-content")) {
            redirectUrl = "/index.page";
        } else if (!currentUrl.match("^http")) {
            redirectUrl = "/index.page";
        } else {
            redirectUrl = currentUrl;
        }

        return redirectUrl;
    };

    // Logs out of GS. Deletes the appropriate cookies and redirects if needed
    var logout = function() {
        var hostname = window.location.hostname;
        var redirectUrl = getLogoutRedirectUrl();

        deleteCookie("MEMBER"); // subscriber login
        deleteCookie("MEMID"); // MSL
        deleteCookie("SESSION_CACHE"); //

        var communityCookieName = "community_www";

        // calculate correct community cookie
        // TODO: do we need this? probably not since the existing logic isn't up-to-date
        if (hostname.match("staging\\.") || hostname.match("staging$")) {
            communityCookieName = "community_staging";
        } else if (hostname.match("dev\\.|dev$|clone\\.|localhost|samson")) {
            communityCookieName = "community_dev";
        } else {
            communityCookieName = "community_www";
        }

        deleteCookie(communityCookieName);

        window.location.href = redirectUrl;
    };

    var deleteCookie = function(name) {
        var dayLength = 24 * 60 * 60 * 1000;
        var date = new Date();
        date.setTime(date.getTime() - dayLength);
        var expires = "; expires=" + date.toGMTString();
        document.cookie = name + "=" + expires + "; path=/";
    };

    // should log user into FB and GS (backend creates GS account if none exists)
    // Does not currently do refresh after logging in, just updates site header
    // resolves deferreds and updates login flags
    var login = function () {

        // any time a login call completes successfully, resolve the single loginDeferred for this module.
        var loginAttemptDeferred = $.Deferred().done(function() {
            successfulLoginDeferred.resolve();
        });

        FB.login(function (response) {
            if (response.authResponse) {
                FB.api('/me', function (data) {
                    if (!data || data.error) {
                        // problem occurred
                        loginAttemptDeferred.reject();
                    } else {
                        var obj = {
                            email: data.email,
                            firstName: data.first_name,
                            lastName: data.last_name,
                            how: "facebook",
                            facebookId: data.id,
                            terms: true,
                            fbSignedRequest: response.authResponse.signedRequest
                        };
                        // Handle GS reg/login
                        // Backed out from r226
                        $.post(registrationAndLoginUrl, obj).done(function (regLoginResponse) {
                            if (regLoginResponse !== undefined && regLoginResponse.success && regLoginResponse.success === 'true') {
                                if (regLoginResponse.GSAccountCreated) {
                                    trackGSAccountCreated();
                                }
                                updateUIForLogin(regLoginResponse.userId, regLoginResponse.email, regLoginResponse.firstName, regLoginResponse.numberMSLItems);
                            }
                            loginAttemptDeferred.resolve(data);
                        }).fail(function() {
                            loginAttemptDeferred.reject();
                        });
                    }
                });
            } else {
                loginAttemptDeferred.reject();
            }
        }, {
            scope: facebookPermissions,
            response_type: "token"
        });

        return loginAttemptDeferred;
    };

    // Generate a unique string to identify a school by name/city/state
    var createSchoolHash = function (schoolName, city, state) {
        return schoolName.toLowerCase() + "|" + city.toLowerCase() + "|" + state.toLowerCase();
    };

    // Using FQL queries, get user's friends and friends' Likes, where Liked pages are schools
    // Multiple resultsets come back from FB. Join them together.
    // Combine all your friends who Liked a school, into a Set on that school object
    // Send data off to DOM manipulation handler
    var getUserFriendsSchoolPageData = function (facebookDataHandler) {
        if (getUserFriendsSchoolPageData.schoolPagesByUrl && getUserFriendsSchoolPageData.schoolPagesBySchoolHash) {
            facebookDataHandler(getUserFriendsSchoolPageData.schoolPagesByUrl, getUserFriendsSchoolPageData.schoolPagesBySchoolHash);
            return;
        }

        FB.api({
            method: 'fql.multiquery',
            queries: {
                userFriendsQuery: userFriendsQuery,
                friendsThatAreFansOfSchoolsQuery: friendsThatAreFansOfSchoolsQuery,
                schoolPageDetailsQuery: schoolPageDetailsQuery
            }
        }, function (response) {
            var i, item;

            // friends
            var userFriendsResults = response[0]['fql_result_set'];

            // associations
            var friendsThatAreFansOSchoolsResults = response[1]['fql_result_set'];

            // school pages
            var schoolPageDetailsResults = response[2]['fql_result_set'];

            // it's easier to reference the objects we need when they're indexed by a key rather than in an array
            var userFriendsMap = {};
            i = userFriendsResults.length;
            while (i--) {
                item = userFriendsResults[i];
                userFriendsMap[item.uid] = item;
            }

            var schoolPageMap = {};
            i = schoolPageDetailsResults.length;
            while (i--) {
                item = schoolPageDetailsResults[i];
                schoolPageMap[item.page_id] = item;
            }

            // A lookup - Url of school's FB page to school page object
            var schoolPagesByUrl = {};

            // A lookup - A unique String (name/city/state) to school page object
            var schoolPagesBySchoolHash = {};

            i = friendsThatAreFansOSchoolsResults.length;

            // iterate through friend<-->school page associates. Build up maps of Key-->School Page..friends[]
            while (i--) {
                item = friendsThatAreFansOSchoolsResults[i];
                var pageId = item.page_id;
                var uid = item.uid;
                var schoolPage = null;
                var friend = null;
                var pageUrl = null;
                if (schoolPageMap.hasOwnProperty(pageId)) {
                    schoolPage = schoolPageMap[pageId];
                }
                if (userFriendsMap.hasOwnProperty(uid)) {
                    friend = userFriendsMap[uid];
                }
                if (schoolPage == null || friend == null) {
                    continue;
                }

                schoolPage.fans = schoolPage.fans || [];

                pageUrl = schoolPage.page_url.replace("https:","http:");
                if (!schoolPagesByUrl.hasOwnProperty(pageUrl)) {
                    schoolPagesByUrl[pageUrl] = schoolPage;
                }

                if (schoolPage.hasOwnProperty('location') && schoolPage.location.hasOwnProperty('city') && schoolPage.location.hasOwnProperty('state')) {
                    var schoolHash = createSchoolHash(schoolPage.name, schoolPage.location.city, schoolPage.location.state);

                    if (!schoolPagesBySchoolHash.hasOwnProperty(schoolHash)) {
                        schoolPagesBySchoolHash[schoolHash] = schoolPage;
                    }
                }

                schoolPage.fans.push(friend);
            }

            getUserFriendsSchoolPageData.schoolPagesByUrl = schoolPagesByUrl;
            getUserFriendsSchoolPageData.schoolPagesBySchoolHash = schoolPagesBySchoolHash;

            // now that we have two maps of data, send it off to UI manipulating
            facebookDataHandler(schoolPagesByUrl, schoolPagesBySchoolHash);
        });
    };

    // Launch a FB dialog to allow user to post School (url, name, desc) to their Feed
    var postToFeed = function (link, pictureUrl, name, caption, description) {
        postToFeed.disabled = postToFeed.disabled || false;
        if (!postToFeed.disabled) {

            // disable this method's functionality until FB dialog has been closed
            postToFeed.disabled = true;

            var obj = {
                method: 'feed',
                redirect_uri: 'www.greatschools.org',
                link: link,
                picture: pictureUrl,
                name: name,
                caption: caption,
                description: description
            };

            var callback = function (response) {
                // re-enable postToFeed
                postToFeed.disabled = false;
            };

            FB.ui(obj, callback);
        }
    };

    var debugStatus = function() {
        FB.getLoginStatus(function(response){
           console.log(response);
        });
    };

    return {
        status: status,
        debugStatus: debugStatus,
        login: login,
        getUserFriendsSchoolPageData: getUserFriendsSchoolPageData,
        createSchoolHash: createSchoolHash,
        getLoginDeferred: getLoginDeferred,
        getStatusOnLoadDeferred: getStatusOnLoadDeferred,
        postToFeed: postToFeed,
        mightBeLoggedIn: mightBeLoggedIn,
        init: init,
        updateUIForLogin: updateUIForLogin,
        logout: logout,
        deleteCookie: deleteCookie
    };
})();
