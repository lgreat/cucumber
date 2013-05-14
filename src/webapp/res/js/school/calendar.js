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
        eventTableRowTemplate = Hogan.compile($(eventTableRowTemplateSelector).html());
        $listModule = $(listModuleSelector);
        $('#js-export-school-calendar').on('change', function() {
            exportCalendar("");
        });
    });


    /**
     * A function to parse the XCAL format date into an object
     */
    var parseDate = function(xCalDate) {

        parseDate.months = parseDate.months || ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

        return {
            'year': xCalDate.substring(0,4),
            'month': xCalDate.substring(5,7),
            'monthName': parseDate.months[xCalDate.substring(5,7)-1],
            'day': xCalDate.substr(8,10)
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
            events = parseXCalData(data);
            getEventsViaAjax.cache[ncesCode] = events;
            deferred.resolve(events);
        }).fail(function() {
            deferred.reject();
        });

        return deferred.promise();
    };


    /**
     * should be called when user interacts with calendar's List module. Get events, then call UI methods
     */
    var getEventsAndUpdateListUI = function(ncesCode, year, month) {

        showListLoading();
        getEventsViaAjax(ncesCode).done(function(events) {

            var today = new Date();
            var todayYear = year || today.getFullYear();
            var todayMonth = month || today.getMonth() + 1;

            clearEventsList();
            fillCalendarList(events, todayYear, todayMonth);

            showListEvents();
        }).fail(function() {
            showListNoEvents();
        });
    };


    /**
     * Modifies the DOM. Gets the table that contains the calendar list view, and populates it
     */
    var fillCalendarList = function(events, year, month) {

        var $tbody = $listModule.find('tbody');

        var todayYearAndMonth = "" + year + month;
        var monthEvents = events[todayYearAndMonth];
        var today = new Date();

        var i;
        if (monthEvents !== undefined) {

            // temporary hide table so that it isn't redrawn every time we add a row
            $tbody.hide();

            i = monthEvents.length;
            while (i--) {
                var event = monthEvents[i];
                if (event.dateStart.day >= today.getDate()) {
                    var html = getEventTableRowHtml(event);
                    $tbody.prepend(html);
                }
            }

            $listModule.find('.js-school-calendar-year').html(monthEvents[0].dateStart.year);
            $listModule.find('.js-school-calendar-month').html(monthEvents[0].dateStart.monthName);

            $tbody.show();
        }
    };


    /**
     * Clear any events in the events list table. Make sure not to remove the hidden hogan template
     */
    var clearEventsList = function() {
        var $tbody = $listModule.find('tbody');
        $tbody.find('tr:not(' + eventTableRowTemplateSelector + ')').remove();
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

        var html = eventTableRowTemplate.render({
            date: event.dateStart.month + '/' + event.dateStart.day,
            title: event.summary,
            time: ""
        });

        return '<tr>' + html + '</tr>';
    };

    var exportCalendar = function(format) {
        var selector = "#js-export-school-calendar";
        window.location.href = "http://samson.greatschools.org:8080/school/calendar/ical.page?ncesCode=181281002036";
    };


    return {
        getEventsAndUpdateListUI: getEventsAndUpdateListUI,
        show: show,
        hide: hide,
        exportCalendar: exportCalendar
    };
})(jQuery);
