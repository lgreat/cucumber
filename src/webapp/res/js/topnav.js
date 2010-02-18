// JavaScript Document

jQuery(document).ready(function() {

    /* Subnav menus */

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
        jQuery(this).parent('li').toggleClass('active', true);
        jQuery(this).parent('li').siblings().toggleClass('active', false);
    });
});