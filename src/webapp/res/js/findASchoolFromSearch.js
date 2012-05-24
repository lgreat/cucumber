GS = GS || {};
GS.findASchool = GS.findASchool || {};

// also in customizeSchoolSearchWidget.js
// http://stackoverflow.com/questions/237104/javascript-array-containsobj
Array.prototype.contains = function(obj) {
  var i = this.length;
  while (i--) {
    if (this[i] === obj) {
      return true;
    }
  }
  return false;
};


$(function() {


/*

    jQuery('#js-radius input').click(function() {
        GS.findASchool.setDistanceRadius();
        
        GS.findASchool.filterTracking.track(jQuery(this).attr('id'));
    });

    // custom links for editorial module
    $(".js_editorialModule ul li a").click(function() {
        if (s.tl) {
            s.tl(this,'o',this.href);
        }
    });


    var stateValue = function(selectedState) {
        $(".showState").text(selectedState === "" ? "Select State" : selectedState);
    };

    $("#js-findByNameStateSelect").change(function () {
        stateValue($(this).val());
    }).trigger("change");

    $("#js-findByNameStateSelect").keyup(function () {
        stateValue($(this).val());
    });*/


});