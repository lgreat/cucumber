Event.observe(window, "load", function() {

    // valid options:
    // frequency: second delay after typing stops before lookup occurs
    // minChars: minimum number of characters before lookup can occur
    // indicator: some sort of element(?) that is shown during lookup (distinct from anything displayed
    //            by the CSS)
    // widthOffset: The popup containing the list of school matches has width equal to the width
    //              of the parent text field plus this offset value (in pixels). Defaults to 0.
    var autoAssistOptions = {frequency:0.01, minChars:3, widthOffset:100};
    // instead of a for-loop, I have to use an anonymous function because of a complicated scoping issue.
    // the loop variable in a for-loop is scoped up to the top of this function (Event.observe), and is passed by reference
    // to the function in AutoAssist. So each function is pointing to the same loop variable, and thus the same
    // value.
    // By using an anonymous function, the variable "i" is scoped only to the top of that function (loop(i)), and thus is
    // unique for each run through the function.
    (function loop(i) {
        if (i < 12) {
            new AutoAssist("school" + i, function () {
                var stateSelector = $("state" + i);
                var state = stateSelector.options[stateSelector.selectedIndex].value;
                return "http://apeterson.dev.greatschools.net/cgi-bin/ajax_autocomplete.pl?type=school&param3=" + i +
                       "&fn=setKidsSchool&q=" + this.text.value + "&state=" + state;
            }, autoAssistOptions);
            loop(i+1);
        }
    })(0);


});

// callback function, called by the external script that does the search for school names
function setKidsSchool(schoolId, school, num) {
    $("schoolId" + num).value=schoolId;
    $("school" + num).value=school;
}
