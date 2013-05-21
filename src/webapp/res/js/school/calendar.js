var GS = GS || {};
GS.school = GS.school || {};

// module to manage the List and Calendar views of the Tandem Calendar on the school profile culture tab
// does not include overview tile code. look for profilePage.js for that
// Revealing module pattern:
GS.school.calendar =  (function($) {
    "use strict";

    var API_BASE_URL = "/school/calendar.page";

    var eventTableRowTemplateSelector = "#js-calendar-list-event-template";
    var listModuleSelector = "#js-school-calendar-list";
    var listEventsLoadingSelector = ".js-school-events-loading";
    var listNoSchoolEventsSelector = ".js-no-school-events";
    var eventTableRowTemplate;
    var $listModule;

    $(function() {
        var templateHtml = $(eventTableRowTemplateSelector).html();
        if (templateHtml !== undefined) {
            eventTableRowTemplate = Hogan.compile($(eventTableRowTemplateSelector).html());
            $(eventTableRowTemplateSelector).find('li').hide();
            $listModule = $(listModuleSelector);
            $('#js-export-school-calendar').on('change', function() {
                var $select = $(this);
                var format = $select.val();
                var schoolName = $select.data('gs-school-name');
                var ncesCode = $select.data('gs-school-nces-code');
                exportCalendar(ncesCode, format, schoolName);
            });
        }
    });


    /**
     * A function to parse the XCAL format date into an object
     */
    var parseDate = function(xCalDate) {

        parseDate.months = parseDate.months || ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

        var year = xCalDate.substring(0,4);
        var month = xCalDate.substring(5,7);
        var monthName = parseDate.months[xCalDate.substring(5,7)-1];
        var day = xCalDate.substr(8,10);
        var superscript;

        if (day === 1 || day === 21 || day === 31) {
            superscript = "st";
        } else if (day === 2 || day === 22) {
            superscript = "nd";
        } else if (day === 3 || day === 23) {
            superscript = "rd";
        } else {
            superscript = "th";
        }

        var prettyDate = function() {
            return monthName + " " + parseInt(day, 10) + superscript + " " + year;
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
            var dateStart = $this.find('dtstart').text().trim();
            var dateEnd = $this.find('dtend').text().trim();
            var dateStartObj = parseDate(dateStart);
            var dateEndObj = parseDate(dateEnd);
            var summary = $this.find('summary').text().trim();
            var description = $this.find('description').text().trim();

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
                log("getEventsViaAjax done success");
            events = parseXCalData(data);
            getEventsViaAjax.cache[ncesCode] = events;
            deferred.resolve(events);
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

            showListEvents();
        }).fail(function() {
            showListNoEvents();
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
                $('#js-tandemOverviewTileTitle').html(event.dateStart.prettyDate);
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


    /**
     * Style table so that it shows the events for a school
     */
    var showListEvents = function() {
        $listModule.find(listNoSchoolEventsSelector).hide();
        $listModule.find('.bd').show();
        $listModule.find('.ft').show();
        $listModule.find(listEventsLoadingSelector).hide();
    };

    /**
     * Style table so it displays a message that there are no events
     */
    var showListNoEvents = function() {
        $listModule.find(listNoSchoolEventsSelector).show();
        $listModule.find('.bd').hide();
        $listModule.find('.ft').hide();
        $listModule.find(listEventsLoadingSelector).hide();
    };

    /**
     * Style table so that it shows a "Loading" state
     */
    var showListLoading = function() {
        $listModule.find(listNoSchoolEventsSelector).show();
        $listModule.find('.bd').hide();
        $listModule.find('.ft').hide();
        $listModule.find(listEventsLoadingSelector).show();
    };

    var show = function() {
        $listModule.show();
    };

    var hide = function() {
        $listModule.hide();
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

    var exportCalendar = function(ncesCode, format, schoolName) {
        var href = "/school/calendar/ical.page?ncesCode=" + encodeURIComponent(ncesCode) + "&format=" +
                encodeURIComponent(format) + "&schoolName=" + encodeURIComponent(schoolName);
        window.location.href = href;
    };

    var log = function() {
        if (window.location.search.indexOf('logging=true') > -1) {
            console.log(arguments);
        }
    };


    return {
        getEventsAndUpdateListUI: getEventsAndUpdateListUI,
        show: show,
        hide: hide,
        exportCalendar: exportCalendar
    };
})(jQuery);
