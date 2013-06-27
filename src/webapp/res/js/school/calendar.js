var GS = GS || {};
GS.school = GS.school || {};

// module to manage the List and Calendar views of the Tandem Calendar on the school profile culture tab
// does not include overview tile code. look for profilePage.js for that
// Revealing module pattern:

GS.school.calendar =  (function($) {
    "use strict";

    var monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];


    var mapCalNames = {
        'Microsoft Outlook': 'Outlook',
        'Outlook.com': 'Outlook.com',
        'iCal Format': 'iCal',
        'Google Calendar': 'Google'
    };
//    var mapCalNames = [];
//    mapCalNames["Microsoft Outlook"] = "Outlook";
//    mapCalNames["iCal Format"] = "iCal";
//    mapCalNames["Google Calendar"] = "Google";

    var API_BASE_URL = "/school/calendar.page";

    var eventTableRowTemplateSelector = "#js-calendar-list-event-template";
    var listModuleSelector = "#js-school-calendar-list";
    var listEventsLoadingSelector = ".js-school-events-loading";
    var listNoSchoolEventsSelector = ".js-no-school-events";
    var eventTableRowTemplate;
    var $listModule;





    $(function () {
        GS.school.tandem.setTandemBranded(hasTandemBranding);
        initializeCustomSelect("js-export-school-calendar", selectCallbackTandemCalFile);
        var templateHtml = $(eventTableRowTemplateSelector).html();
        if (templateHtml !== undefined) {
            eventTableRowTemplate = Hogan.compile($(eventTableRowTemplateSelector).html());
            $(eventTableRowTemplateSelector).find('li').hide();
            $listModule = $(listModuleSelector);
        }
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

     var initializeCustomSelect = function(layerContainer, callbackFunction){
        var selectContainer = $("#"+layerContainer); //notify
        var selectBox = selectContainer.find(".js-selectBox");
        var selectDropDownBox = selectContainer.find(".js-selectDropDown");
        var selectDropDownItem = selectContainer.find(".js-ddValues");
        var selectBoxText = selectContainer.find(".js-selectBoxText");

        selectBox.on("click", showSelect);

        selectDropDownBox.on("click", function(event) {
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
    var selectCallbackTandemCalFile = function(selectValue){
        if(selectValue === "Print Calendar"){
            s.tl(true, 'o', 'Tandem_Print');
            setTimeout(function() {
                print();
            }, 500);
        }
        else{
            var $select = $("#js-export-school-calendar");
            var sv = $.trim(selectValue);
            var format = mapCalNames[sv];
            var schoolName = $select.data('gs-school-name');
            var ncesCode = $select.data('gs-school-nces-code');
            exportCalendar(ncesCode, format, schoolName);
        }
    };


    /**
     * A function to parse the XCAL format date into an object
     */
    var parseDate = function(xCalDate) {

        parseDate.months = parseDate.months || ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

        var year = xCalDate.substring(0,4);
        var month = xCalDate.substring(5,7);
        var monthName = parseDate.months[xCalDate.substring(5,7)-1];
        var day = xCalDate.substr(8,10);

        var prettyDate = function() {
            return monthName + " " + parseInt(day, 10) +  ", " + year;
        };

        return {
            'year': year,
            'month': month,
            'monthName': monthName,
            'day': day,
            'prettyDate': prettyDate
        };
    };


    /**
     * converts an xcal XML document and converts it into a map of events
     * Returns map of year + month number to array of events, such as:
     *
     * {
     *   "20131": [
     *       {
     *           dateStart: {year:2013, month:01, day:30},
     *           dateEnd: {year:2013, month:01, day:30},
     *           summary: "summary text",
     *           description: "description text"
     *         }
     *     ]
     * }
     *
     *
     */
    var parseXCalData = function(xcal) {
        var $xml = $(xcal);
        var events = [];

        $xml.find('vevent').each(function() {
            var $this = $(this);
            var dateStart = $.trim($this.find('dtstart').text());
            var dateEnd = $.trim($this.find('dtend').text());
            var dateStartObj = parseDate(dateStart);
            var dateEndObj = parseDate(dateEnd);
            var summary = $.trim($this.find('summary').text());
            var description = $.trim($this.find('description').text());

            var obj = {
                dateStart: dateStartObj,
                dateEnd: dateEndObj,
                summary: summary,
                description: description
            };

            var yearAndMonth = obj.dateStart.year + parseInt(obj.dateStart.month,10);

            events[yearAndMonth] = events[yearAndMonth] || [];
            events[yearAndMonth].push(obj);
        });

        return events;
    };


    /**
     * Makes an AJAX call to server to get xcal document for the given school nces code
     */
    var getEventsViaAjax = function(ncesCode) {
        log("getEventsViaAjax beginning");
        var deferred = $.Deferred();

        getEventsViaAjax.cache = getEventsViaAjax.cache || {};

        // events are cached in case user tabs through calendar.
        // if cached events found, return right away
        var events = getEventsViaAjax.cache[ncesCode];
        if (typeof events !== 'undefined') {
            deferred.resolve(events);
            return deferred.promise();
        }

        $.ajax({
            url: API_BASE_URL,
            type: "GET",
            contentType: "application/xml",
            dataType: 'xml',
            data: {
                ncesCode: ncesCode
            }
        }).done(function(data) {
            log("getEventsViaAjax done success", data);
            if(data === undefined || data === null || data === "" || data.length === 0) {
                deferred.reject();
            } else {
                events = parseXCalData(data);
                getEventsViaAjax.cache[ncesCode] = events;
                deferred.resolve(events);
            }
        }).fail(function() {
            log("getEventsViaAjax done failure");
            deferred.reject();
        });

        log("getEventsViaAjax returning");
        return deferred.promise();
    };


    /**
     * should be called when user interacts with calendar's List module. Get events, then call UI methods
     */
    var getEventsAndUpdateListUI = function(ncesCode, year, month) {
        log("beginnging getEventsAndUpdateListUI", arguments);

        var promise = getEventsViaAjax(ncesCode).done(function(events) {
            log("getEventsViaAjax promise was resolved", events);
            // var today = new Date();
            // var todayYear = year || today.getFullYear();
            // var todayMonth = month || today.getMonth() + 1;

            events = filterEvents(events);

            clearEventsList();
            fillCalendarList(events);
            GS.school.tandem.handleTandemAd(true);
            show();
        }).fail(function() {
            GS.school.tandem.handleTandemAd(false);
            hide();
        });

        log("returning getEventsAndUpdateListUI", promise);
        return promise;
    };



    /**
     * Filters out events that are in the past. Could be changed to filter out events that aren't in a particular year/month
     */
    var filterEvents = function(events) {
        log("filterEvents beginning", events);

        var today = new Date();
        var currentYear = today.getFullYear();
        var currentMonth = today.getMonth() + 1;
        var currentDay = today.getDate();
        var i,
            monthEvents,
            ml,
            event;

        var filteredEvents = [];

        for (var key in events) {
            if (events.hasOwnProperty(key)) {

                monthEvents = events[key];
                ml = monthEvents.length;

                for (i = 0; i < ml; i++) {
                    event = monthEvents[i];
                    if (event.dateStart.year == currentYear && parseInt(event.dateStart.month) >= parseInt(currentMonth)) {
                        if (!(parseInt(event.dateStart.month) === parseInt(currentMonth) && parseInt(event.dateStart.day) < parseInt(currentDay))) {
                            filteredEvents.push(event);
                        }
                    }
                }
            }
        }

        log("filterEvents returning");
        return filteredEvents;
    };

    /**
     * Filters out events that are in the past. Could be changed to filter out events that aren't in a particular year/month
     */
    var formatEvents = function(events) {
        log("filterEvents beginning", events);

        var i,
                monthEvents,
                ml,
                event;

        var filteredEvents = [];

        for (var key in events) {
            if (events.hasOwnProperty(key)) {

                monthEvents = events[key];
                ml = monthEvents.length;

                for (i = 0; i < ml; i++) {
                    event = monthEvents[i];
                    filteredEvents.push(event);
                }
            }
        }

        log("filterEvents returning");
        return filteredEvents;
    };


    /**
     * Modifies the DOM. Gets the table that contains the calendar list view, and populates it
     */
    var fillCalendarList = function(events) {
        log("fillCalendarList beginning", events);

        var $ul = $(eventTableRowTemplateSelector);

        var eventsLength = events.length;
        var i,
            event,
            html;

        // temporary hide table so that it isn't redrawn every time we add a row
        $ul.hide();

        for (i = 0; i < eventsLength; i++) {

            event = events[i];

            if (i === 0) {
                $('#js-tandemOverviewTileEvent').html(event.summary);
                $('#js-tandemOverviewTileMonth').html(monthNames[event.dateStart.month-1]);
                $('#js-tandemOverviewTileDay').html(parseInt(event.dateStart.day));
            }

            html = getEventTableRowHtml(event);
            if (html !== undefined) {
                $ul.append(html);
            }
        }

        $listModule.find('.js-school-calendar-year').html(events[0].dateStart.year);

        $ul.show();

        log("fillCalendarList returning");
    };


    /**
     * Clear any events in the events list table. Make sure not to remove the hidden hogan template
     */
    var clearEventsList = function() {
        log("clearEventsList beginning");
        var $tbody = $listModule.find('tbody');
        $tbody.find('tr:not(' + eventTableRowTemplateSelector + ')').remove();
        log("clearEventsList returning");
    };



    var show = function() {
        if($listModule != undefined){
            $listModule.show();
        }
    };

    var hide = function() {
        if($listModule != undefined){
            $listModule.hide();
        }
    };

    /**
     * Returns the HTML necessary to display one event in list format
     */
    var getEventTableRowHtml = function(event) {
        if (eventTableRowTemplate === undefined) {
            return undefined;
        }

        var html = eventTableRowTemplate.render({
            date: event.dateStart.month + '/' + event.dateStart.day,
            title: event.summary,
            time: ""
        });

        return html;
    };

    // Send browser to download calendar
    var exportCalendar = function(ncesCode, format, schoolName) {
        var customLink = "";
        // formats:  Outlook, iCal, Google
        if (format === 'Outlook') {
            customLink = 'Tandem_Outlook';
        } else if (format === 'iCal') {
            customLink = 'Tandem_iCal';
        } else if (format === 'Google') {
            customLink = 'Tandem_GoogleCal';
        } else if (format === 'Outlook.com') {
            customLink = 'Tandem_Outlook_com';
        }

        s.tl(true, 'o', customLink);

        if (customLink !== "") {
            if (format === 'Outlook.com') {
                uploadToOutlook(ncesCode, schoolName);
            } else {
                // without setTimeout, omniture request was being canceled by ical.page request
                setTimeout(function() {
                    var href = "/school/calendar/ical.page?ncesCode=" + encodeURIComponent(ncesCode) + "&format=" +
                            encodeURIComponent(format) + "&schoolName=" + encodeURIComponent(schoolName);
                    window.location.href = href;
                }, 500);
            }
        }
    };

    // hides/shows DOM nodes to let the user know the school Calendar is being uploaded to their Outlook.com account
    var styleAsUploadingToOutlook = function() {
        var $spinner = $('#js-outlook-uploading');
        $spinner.show();
    };

    // hides/shows DOM nodes to let the user know the school Calendar is done uploading to their Outlook.com account
    var styleAsUploadingDone = function() {
        var $spinner = $('#js-outlook-uploading');
        $spinner.hide();
    };

    var uploadToOutlook = function(ncesCode, schoolName) {
        var date = '';
        var endDate = '';
        var event;
        var calendarName = schoolName + ' Calendar';

        // login() call returns a promise
        GS.windowsLive.login().done(function() {
            styleAsUploadingToOutlook();

            // now create an Outlook.com calendar for the user
            GS.windowsLive.createCalendar(calendarName).done(function(createCalendarResponse) {

                // getEventsViaAjax also returns a promise, but should already be resolved
                var promise = getEventsViaAjax(ncesCode).done(function(events) {
                    events = filterEvents(events);
                    var numberOfEvents = events.length;
                    var i = 0;

                    // we'll need to recursively call this function
                    function uploadEvent(i) {
                        event = events[i];
                        date = event.dateStart.year + '-' + event.dateStart.month + '-' + event.dateStart.day + "T00:00:00" + getTimezoneOffset();
                        endDate = event.dateStart.year + '-' + event.dateStart.month + '-' + event.dateStart.day + "T23:59:59" + getTimezoneOffset();

                        // createEvent returns a promise
                        GS.windowsLive.createEvent(
                            createCalendarResponse.id,
                            event.summary,
                            'Description:' + event.summary,
                            date,
                            endDate
                        ).done(function() {
                            i = i + 1;
                            if (i < numberOfEvents) {
                                uploadEvent(i);
                            } else {
                                styleAsUploadingDone();
                            }
                        }).fail(function(response) {
                            // event creation failed
                            log('failed event:', response);
                        });
                    }
                    uploadEvent(i);
                }).fail(function() {
                    // could not get the Tandem events
                    alert('Upload to Outlook.com could not be completed.');
                });
            }).fail(function(response) {
                // failed
                alert('Upload to Outlook.com could not be completed.');
                log(response);
            });

        }).fail(function() {
            // could not log in to WindowsLive
            alert("We're sorry, we were not able to connect to Outlook.com to upload the school calendar. You might not have given permissions to access your Outlook.com calendar.");
        });
    };

    var getTimezoneOffset = function() {
        // The windowsLive API doesn't properly handle dates unless I specify UTC offset. Currently all school events
        // have no actual time (they're all day events) so it shouldn't even matter. But we need to specify a time
        // for the windows live API to work.
        return '-0' + (((new Date()).getTimezoneOffset() / 60) * 100);
    };

    var print = function() {
        var $select = $("#js-export-school-calendar");
        var ncesCode = $select.data('gs-school-nces-code');
        var schoolName = $select.data('gs-school-name');
        var url = "/school/calendar/printTandemCalendar.page";

        getEventsViaAjax(ncesCode).done(function(events) {

            var formattedEvents = filterEvents(events);
            var serializedData = JSON.stringify(formattedEvents);

            $('body').append($('<form/>')
                    .attr({'action': url, 'method': 'post', 'id': 'printCalendarForm', 'target': '_blank'})
                    .append($('<input/>')
                            .attr({'type': 'hidden', 'name': 'data', 'value': serializedData})
                    )
                    .append($('<input/>')
                            .attr({'type': 'hidden', 'name': 'schoolName', 'value': schoolName})
                    )
            ).find('#printCalendarForm').submit();
        });
    };

    // Looks for a JSON string in a div node, reads that, and uses that to draw a table
    var loadEventsFromDomAndFillCalendarList = function() {
        var selector = "#js-calendardata";
        var stringifiedJson = $(selector).html();
        var json = JSON.parse(stringifiedJson);

        fillCalendarList(json);
    };

    var log = function() {
        if (window.location.search.indexOf('logging=true') > -1) {
            console.log(arguments);
        }
    };

    var pad = function(number, width, z) {
        z = z || '0';
        number = number + '';
        return number.length >= width ? number : new Array(width - number.length + 1).join(z) + number;
    };

    return {
        getEventsAndUpdateListUI: getEventsAndUpdateListUI,
        show: show,
        hide: hide,
        exportCalendar: exportCalendar,
        uploadToOutlook: uploadToOutlook,
        print: print,
        formatEvents : formatEvents,
        loadEventsFromDomAndFillCalendarList: loadEventsFromDomAndFillCalendarList,
        getTimezoneOffset: getTimezoneOffset
    };
})(jQuery);

GS.school.tandem =  (function($) {
    var tandemAd = {
        'returned': false,
        'active': false,
        'showAd': false,
        'whichAd': {
            'positive': null,
            'positiveLayer': null,
            'negative': null,
            'negativeLayer': null
        },
        'tabname': 'overview',
        'branding': 'false'
    };

    // set value in js within the tagx.   Microsoft value in db
    var isTandemBranded = function () {
        return tandemAd.branding;
    }
    var setTandemBranded = function(val){
        tandemAd.branding = val;
    }

    var isTandemActive = function () {
        return tandemAd.active;
    }
    var setTandemActive = function(val){
        tandemAd.active = val;
    }

    // this is set to true once it returns
    // used by profilePage.js to display ad slot
    var isTandemReturned = function () {
        return tandemAd.returned;
    }
    var setTandemReturned = function(val){
        tandemAd.returned = val;
    }

    // This is set when tandom has not returned yet.
    var getTandemTabName = function () {
        return tandemAd.tabname;
    }
    var setTandemTabName = function(val){
        tandemAd.tabname = val;
    }

    // This is set when tandom has not returned yet.
    var isTandemShowAd = function () {
        return tandemAd.showAd;
    }
    var setTandemShowAd = function(val){
        tandemAd.showAd = val;
    }

    // need an array of the slot to fill in the positive and the negative
    // ad will be called in refreshAds
    var tandemWhichAdPositive = function () {
        return tandemAd.whichAd.positive;
    }
    var tandemWhichAdPositiveLayerId = function () {
        return tandemAd.whichAd.positiveLayerId;
    }
    var tandemWhichAdNegative = function () {
        return tandemAd.whichAd.negative;
    }
    var tandemWhichAdNegativeLayerId = function () {
        return tandemAd.whichAd.negativeLayerId;
    }
    var setTandemWhichAd = function(p, pl, n, nl){
        tandemAd.whichAd.positive = p;
        tandemAd.whichAd.positiveLayerId = pl;
        tandemAd.whichAd.negative = n;
        tandemAd.whichAd.negativeLayerId = nl;
    }
    var handleTandemAd = function(val){
        setTandemReturned(true);
        setTandemActive(val);
        // it had not returned when called so now it needs to show ads
        if(isTandemShowAd()){
            if(isTandemBranded() == 'true'){
                if(isTandemActive()){
                    if(tandemWhichAdPositive() != null && tandemWhichAdPositive() != ""){
                        //in profilePage.js
                        GS.profile.refreshSingleAd(getTandemTabName(), [tandemWhichAdPositive()], tandemWhichAdPositiveLayerId());
                    }
                }
                else{
                    if(tandemWhichAdNegative() != null && tandemWhichAdNegative() != ""){
                        GS.profile.refreshSingleAd(getTandemTabName(), [tandemWhichAdNegative()],tandemWhichAdNegativeLayerId());
                    }
                }
            }
            else{
                if(tandemWhichAdNegative() != null && tandemWhichAdNegative() != ""){
                    GS.profile.refreshSingleAd(getTandemTabName(), [tandemWhichAdNegative()], tandemWhichAdNegativeLayerId());
                }
            }
        }
    }
    return {
        handleTandemAd:handleTandemAd,
        setTandemBranded: setTandemBranded,
        isTandemBranded: isTandemBranded,
        isTandemReturned:isTandemReturned,
        setTandemActive : setTandemActive,
        setTandemWhichAd: setTandemWhichAd,
        setTandemShowAd : setTandemShowAd,
        setTandemTabName: setTandemTabName,
        isTandemActive: isTandemActive
    };
})(jQuery);