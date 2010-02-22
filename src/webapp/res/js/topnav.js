// JavaScript Document

jQuery(document).ready(function() {

    /* Subnav menus */
    var srchArtTab = jQuery('#srch2').hasClass('active');
    if(srchArtTab){
        jQuery('#qNew').attr('style','width:318px')
        jQuery('#stateSelector').attr('style','display:none');
    }
    jQuery('#topnav_menusubnav > .nav_menu').hover(function() {
        jQuery(this).toggleClass('over');
        jQuery(this).children('.nav_group_heading:not(#GlobalNav_CommunityButton)').toggleClass('over');
    }, function() {
        jQuery(this).toggleClass('over');
        jQuery(this).children('.nav_group_heading:not(#GlobalNav_CommunityButton)').toggleClass('over');
    });

    jQuery('#topnav_menusubnav li.nav_menu ul.nav_group_items li').hover(function() {
        jQuery(this).toggleClass('over');
    }, function() {
        jQuery(this).removeClass('over');
    });

    /* My Account menu */

    jQuery('#utilLinks > .nav_menu').hover(function() {
        jQuery(this).toggleClass('over');
    }, function() {
        jQuery(this).toggleClass('over');
    });
    /* search tab click functions */
    jQuery('#topnav_search .radLabel').click(function(){
        jQuery(this).parent('li').removeClass('inactive').addClass('active');
        jQuery(this).parent('li').siblings().removeClass('active').addClass('inactive');
    });
});