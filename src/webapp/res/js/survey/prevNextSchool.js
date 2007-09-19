//requires yui:connection manager
function updateSchool(level, city, sltSchoolId, sltStateId, schoolNotListedId) {
    var callback = {
        success: function(o) {
            var sltSchool = document.getElementById(sltSchoolId);
            var jsonResponse = o.responseText.parseJSON();
            var schools = jsonResponse.schools;
            sltSchool.options.length = 0;
            for (var i = 0; i < schools.length; i++) {
                var option = document.createElement("OPTION");
                sltSchool[i] = new Option(schools[i].name, schools[i].id);
            }
            sltSchool[sltSchool.length] = new Option("My child's school is not listed.", schoolNotListedId);
        },
        failure: function(o) {
            tempSelectMsg(document.getElementById(sltSchoolId), 'Error Retrieving schools.');
        }
    };
    var sltState = document.getElementById(sltStateId);
    var state = sltState.options[sltState.selectedIndex].value;
    tempSelectMsg(document.getElementById(sltSchoolId), 'Retrieving schools');
    var url = "/school/ajaxFindSchoolAndCity.page?filter=school&level=" + level + "&state=" + state + "&city=" + city;
    YAHOO.util.Connect.asyncRequest('GET', url, callback, "");
    return 0;
}

function updateCity(state, sltCityId) {
    var callback = {
        success: function(o) {
            var sltCity = document.getElementById(sltCityId);
            var jsonResponse = o.responseText.parseJSON();
            var cities = jsonResponse.cities;
            sltCity.options.length = 0;
            for (var i = 0; i < cities.length; i++) {
                var option = document.createElement("OPTION");
                sltCity[i] = new Option(cities[i].name, cities[i].name);
            }
        },
        failure: function(o) {
            tempSelectMsg(document.getElementById(sltCityId), 'Error Retrieving cities.');
        }
    };

    tempSelectMsg(document.getElementById(sltCityId), 'Retrieving cities');

    var url = "/school/ajaxFindSchoolAndCity.page?filter=city&state=" + state;
    YAHOO.util.Connect.asyncRequest('GET', url, callback, "");
    return 0;
}

function clearSchool(sltId) {
    var sltSchool = document.getElementById(sltId);
    sltSchool.selectedIndex = 0;
    sltSchool.innerHTML = "";
    return 0;
}

function tempSelectMsg(select, msg) {
    select.options.length = 0;
    select.innerHTML = "<option value=\"\">" + msg + "</option>";
    return 0;
}

function hideAndShow(hide, show1, show2) {
    document.getElementById(hide).style.display = 'none';
    document.getElementById(show1).style.display = 'inline';
    document.getElementById(show2).style.display = 'inline';
    return 0;
}