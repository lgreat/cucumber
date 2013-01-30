GS_loadSubnav = function() {
    /* Subnav menus */

//    $(".selectSchoolArticle").onchange(){
//        var test = jQuery('.selectSchoolArticle').text();
//        alert(test);
//        if(".selectSchoolArticle"){
//
//        }
//    }

    var stateValue = function (selectedState) {
        $("#topnav_search .showState").text(selectedState === "" ? "State" : selectedState);
    };
    $("#topnav_search #stateSelector").change(function () {
        stateValue($(this).val());
    }).trigger("change");

   $("#topnav_search #stateSelector").keyup(function () {
        stateValue($(this).val());
    });

    var schoolArticleSelect = function (selectedSchoolArticle) {
        $("#topnav_search .showSchoolArticle").text(selectedSchoolArticle === "" ? "State" : selectedSchoolArticle);
    };
    $("#topnav_search .selectSchoolArticle").change(function () {
        schoolArticleSelect($(this).val());
    }).trigger("change");

    $("#topnav_search .selectSchoolArticle").keyup(function () {
        schoolArticleSelect($(this).val());
    });

    var srchArtTab = jQuery('#srch2').hasClass('active');
    if(srchArtTab){
        jQuery('#qNew').attr('style','width:300px')
    }
    jQuery('#topnav_menusubnav > .nav_menu').hover(function() {
        jQuery(this).addClass('over');
        jQuery(this).children('.nav_group_heading').addClass('over');
    }, function() {
        jQuery(this).removeClass('over');
        jQuery(this).children('.nav_group_heading').removeClass('over');
    });

    jQuery('#topnav_menusubnav li.nav_menu ul.nav_group_items li').hover(function() {
        jQuery(this).addClass('over');
    }, function() {
        jQuery(this).removeClass('over');
    });

    jQuery('#topnav_search .radLabel').click(function(){
        jQuery(this).removeClass('inactive').addClass('active');
        jQuery(this).siblings().removeClass('active').addClass('inactive');
    });
};