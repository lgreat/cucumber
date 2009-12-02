// JavaScript Document

jQuery(document).ready(function() {

    jQuery('#topnav_menusubnav > .nav_menu').hover(function() {
        jQuery(this).children('.nav_group_heading:not(#GlobalNav_CommunityButton)').css('background-color','#66ccdd');
        jQuery(this).children('.nav_group_heading.arrow:not(#GlobalNav_CommunityButton)').css('backgroundImage','url('+'/res/img/nav/down_arrow_selected.png'+')');
        jQuery(this).children('.nav_group_heading:not(#GlobalNav_CommunityButton)').css('color','#fff');
        jQuery(this).children('.nav_group_items').show();
    }, function() {
        jQuery(this).children('.nav_group_heading').css('background-color','#fff');
        jQuery(this).children('.nav_group_heading.arrow').css('backgroundImage','url('+'/res/img/nav/down_arrow.png'+')');
        jQuery(this).children('.nav_group_heading').css('color','#3399aa');
        jQuery(this).children('.nav_group_heading.topnav_subnav_active').css('color','#444');
        jQuery(this).children('.nav_group_items').hide();
    });

    jQuery('#topnav_menusubnav li.nav_menu ul.nav_group_items li').hover(function() {
        jQuery(this).css('background-color','#66ccdd');
        jQuery(this).find('a').css('background-color','#66ccdd');
        jQuery(this).find('a').css('color','#fff');
    }, function() {
        jQuery(this).css('background-color','#fff');
        jQuery(this).children('a').css('background-color','#fff');
        jQuery(this).children('a').css('color','#3399aa');
    });
});