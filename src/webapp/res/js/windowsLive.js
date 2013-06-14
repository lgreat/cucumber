var GS = GS || {};

// requires jQuery
GS.windowsLive = GS.windowsLive || (function () {
    // Permissions that GS.org will ask for
    var permissions = 'wl.calendars_update,wl.events_create';

    // Resolved on first successful login; never rejected
    var successfulLoginDeferred = $.Deferred();

    /*
    WL.init({
        client_id: clientId,
        redirect_uri: redirectUri
    });
    */

    var init = function() {

    };

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

    var getLoginDeferred = function () {
        return successfulLoginDeferred.promise();
    };

    var createCalendar = function(calendarName) {
        var deferred = $.Deferred();

        WL.login({
            scope: "wl.calendars_update"
        }).then(
            function(response) {
                WL.api({
                    path: "me/calendars",
                    method: "POST",
                    body: {
                        name: calendarName
                    }
                }).then(
                    function (response) {
                        deferred.resolve(response);
                    },
                    function (response) {
                        deferred.reject(response);
                    }
                );
            },
            function(response) {
                deferred.reject(response);
            }
        );

        return deferred.promise();
    };

    var createEvent = function(calendarId, eventName, eventDescription, startTime, endTime, location, isAllDayEvent) {
        var deferred = $.Deferred();

        isAllDayEvent = isAllDayEvent || true;

        WL.api({
            path: "/" + calendarId + "/events",
            method: "POST",
            body: {
                name: eventName,
                description: eventDescription,
                start_time: startTime,
                end_time: endTime,
                is_all_day_event: isAllDayEvent,
                //availability: "busy",
                visibility: "private"
            }
        }).then(
            function(response) {
                deferred.resolve(response);
            },
            function(response) {
                deferred.reject(response);
            }
        );

        return deferred.promise();
    };

    var login = function () {
        var deferred = $.Deferred();

        WL.login({
            scope: permissions
        }).then(
            function(response) {
                deferred.resolve(response);
            },
            function(response) {
                deferred.reject(response);
            }
        );

        return deferred.promise();
    };

    return {
        status: status,
        login: login,
        getLoginDeferred: getLoginDeferred,
        createCalendar: createCalendar,
        createEvent: createEvent,
        init: init
    };
})();
