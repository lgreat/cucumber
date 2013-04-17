var GS = GS || {};

// requires jQuery
GS.facebook = GS.facebook || (function() {
    var loginSelector = ".js-facebook-login";
    var facebookPermissions =
        'email,user_likes,' +
        'friends_likes,' +
        'friends_education_history,' +
        'user_education_history';

    var userFriendsQuery = "SELECT uid, name, pic_square, profile_url FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me())";
    var friendsThatAreFansOfSchoolsQuery = "SELECT uid, page_id, type FROM page_fan WHERE uid IN (SELECT uid FROM #userFriendsQuery) AND (type = 'School' or type = 'Education' or type = 'Public School' or type = 'Private School' or type = 'Charter School' or type = 'Elementary School' or type = 'Middle School' or type = 'High School')";
    var schoolPageDetailsQuery = "SELECT page_id, name, location, page_url FROM page WHERE (location.state != '' OR website != '') AND page_id IN (SELECT page_id FROM #friendsThatAreFansOfSchoolsQuery)";

    var loginDeferred = $.Deferred();

    var status = function(options) {
        FB.getLoginStatus(function(response) {
            if (response.status === 'connected') {
                if (options && options.connected) {
                    options.connected();
                }
                // connected
            } else if (response.status === 'not_authorized') {
                // not_authorized
            } else {
                if (options && options.notConnected) {
                    options.notConnected();
                }
                // not_logged_in
            }
        });
    };

    var getLoginDeferred = function() {
        return loginDeferred.promise();
    };

    var init = function() {
        $(function() {
            $(loginSelector).on('click', function() {
                status({
                    notConnected: function() {
                        login();
                    }
                });
            });

            status({
                connected: function() {
                    loginDeferred.resolve();
                }
            });
        });
    };

    var login = function() {
        FB.login(
            function(response) {
                if (response.authResponse) {
                    loginDeferred.resolve();
                    // connected
                } else {
                    loginDeferred.reject();
                    // cancelled
                }
            }, {
                scope: facebookPermissions
            }
        );
    };

    var createSchoolHash = function(schoolName, city, state) {
        return schoolName.toLowerCase() + "|" + city.toLowerCase() +  "|" + state.toLowerCase();
    };

    var getUserFriendsSchoolPageData = function(facebookDataHandler) {
        if (getUserFriendsSchoolPageData.schoolPagesByUrl && getUserFriendsSchoolPageData.schoolPagesBySchoolHash) {
            facebookDataHandler(getUserFriendsSchoolPageData.schoolPagesByUrl,getUserFriendsSchoolPageData.schoolPagesBySchoolHash);
            return;
        }

        FB.api({
            method: 'fql.multiquery',
            queries: {
                userFriendsQuery: userFriendsQuery,
                friendsThatAreFansOfSchoolsQuery: friendsThatAreFansOfSchoolsQuery,
                schoolPageDetailsQuery: schoolPageDetailsQuery
            }
        }, function(response){
            var i, item;
            var userFriendsResults = response[0]['fql_result_set'];
            var friendsThatAreFansOSchoolsResults = response[1]['fql_result_set'];
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

            // for each friend that is a fan of a school,
            // create a map of facebook page_url to page object, and add the "friend" object as a property to page
            var schoolPagesByUrl = {};
            var schoolPagesBySchoolHash = {};
            i = friendsThatAreFansOSchoolsResults.length;
            while (i--) {
                item = friendsThatAreFansOSchoolsResults[i];
                var pageId = item.page_id;
                var uid = item.uid;
                var schoolPage = null;
                var friend = null;
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

                if (!schoolPagesByUrl.hasOwnProperty(schoolPage.page_url)) {
                    schoolPagesByUrl[schoolPage.page_url] = schoolPage;
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
            facebookDataHandler(schoolPagesByUrl, schoolPagesBySchoolHash);
        });
    };

    var postToFeed = function(link, pictureUrl, name, caption, description) {

        var obj = {
            method: 'feed',
            redirect_uri: 'www.greatschools.org',
            link: link,
            picture: pictureUrl,
            name: name,
            caption: caption,
            description: description
        };

        var callback = function(response) {
            console.log('got response for feed post: ', response);
            //document.getElementById('msg').innerHTML = "Post ID: " + response['post_id'];
        };

        FB.ui(obj, callback);
    };

    return {
        status:status,
        login:login,
        getUserFriendsSchoolPageData:getUserFriendsSchoolPageData,
        createSchoolHash:createSchoolHash,
        getLoginDeferred:getLoginDeferred,
        postToFeed:postToFeed,
        init:init
    }
})();
