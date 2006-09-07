var MAX_KIDS = 11;

// valid options:
// frequency: second delay after typing stops before lookup occurs
// minChars: minimum number of characters before lookup can occur
// indicator: some sort of element(?) that is shown during lookup (distinct from anything displayed
//            by the CSS)
// widthOffset: The popup containing the list of school matches has width equal to the width
//              of the parent text field plus this offset value (in pixels). Defaults to 0.
var autoAssistOptions = {frequency:0.2, minChars:1, widthOffset:100};

// run the following function on window load
Event.observe(window, "load", function() {

    // the inside of this for-loop MUST BE a function call, otherwise because of javascript
    // scoping issues the AutoAssist calls will end up pointing to the same variable and the page
    // will not function right
    for (var i=1; i < (MAX_KIDS + 1); i++) {
        declareStudentListener(i);
    }

    declarePreviousSchoolListener(1);
    declarePreviousSchoolListener(2);
    declarePreviousSchoolListener(3);

});

function declareStudentListener(i) {
    // this call registers a listener on a text field that fires off an AJAX call when the user
    // types text. The result is a popup box with context-sensitive choices based on what the user
    // is typing.
    // the first parameter is the name of the field to register the listener on
    // the second parameter is a function that should return the URL to make the AJAX request to.
    // it is a function so you can query other fields if necessary (in this case, the state selector)
    // the final paramter is an options map (see comment on the options map declaration above).
    new AutoAssist("school" + i, function () {
        var stateSelector = $("state" + i);
        var state = stateSelector.options[stateSelector.selectedIndex].value;
        return "/cgi-bin/ajax_autocomplete.pl?type=school&param3=" + i +
               "&fn=setKidsSchool&q=" + this.text.value + "&state=" + state;
    }, autoAssistOptions);
}

function declarePreviousSchoolListener(i) {
    // see comment under declareStudentListener above
    new AutoAssist("previousSchool" + i, function () {
        var stateSelector = $("previousState" + i);
        var state = stateSelector.options[stateSelector.selectedIndex].value;
        return "/cgi-bin/ajax_autocomplete.pl?type=school&param3=" + i +
               "&fn=setPreviousSchool&q=" + this.text.value + "&state=" + state;
    }, autoAssistOptions);
}

// callback function, called by the external script that does the search for school names
function setKidsSchool(schoolId, school, num) {
    $("schoolId" + num).value=schoolId;
    $("school" + num).value=school;
}

// callback function, called by the external script that does the search for school names
function setPreviousSchool(schoolId, school, num) {
    $("previousSchoolId" + num).value=schoolId;
    $("previousSchool" + num).value=school;
}