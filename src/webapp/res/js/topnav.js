GS_loadSubnav = function() {
    /* Subnav menus */

    var stateValue = function (selectedState) {
        jQuery("#topnav_search_school .showStateHeader").text(selectedState === "" ? "State" : selectedState);
    };
    jQuery("#topnav_search_school #stateSelector").change(function () {
        stateValue(jQuery(this).val());
    }).trigger("change");

    jQuery("#topnav_search_school #stateSelector").keyup(function () {
        stateValue(jQuery(this).val());
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