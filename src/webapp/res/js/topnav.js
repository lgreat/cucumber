// JavaScript Document
// required to avoid "$j" collisions with Prototype.js
jQuery.noConflict();
var $j = jQuery;

$j(document).ready(function() {

    $j('#topnav_menusubnav > .nav_menu').hover(function() {
        $j(this).children('.nav_group_heading').css('background-color','#66ccdd');
        $j(this).children('.nav_group_heading.arrow').css('backgroundImage','url('+'/res/img/nav/down_arrow_selected.png'+')');
        $j(this).children('.nav_group_heading').css('color','#fff');
        $j(this).children('.nav_group_items').show();
    }, function() {
        $j(this).children('.nav_group_heading').css('background-color','#fff');
        $j(this).children('.nav_group_heading.arrow').css('backgroundImage','url('+'/res/img/nav/down_arrow.png'+')');
        $j(this).children('.nav_group_heading').css('color','#3399aa');
        $j(this).children('.nav_group_heading.topnav_subnav_active').css('color','#444');
        $j(this).children('.nav_group_items').hide();
    });

    $j('#topnav_menusubnav li.nav_menu ul.nav_group_items li').hover(function() {
        $j(this).css('background-color','#66ccdd');
        $j(this).children('a, a:hover').css('background-color','#66ccdd');
        $j(this).children('a, a:hover').css('color','#fff');
    }, function() {
        $j(this).css('background-color','#fff');
        $j(this).children('a, a:hover').css('background-color','#fff');
        $j(this).children('a, a:hover').css('color','#3399aa');
    });
});