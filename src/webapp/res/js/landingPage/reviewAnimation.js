/**
 * Created with IntelliJ IDEA.
 * User: mitch
 * Date: 3/6/13
 * Time: 10:07 AM
 * To change this template use File | Settings | File Templates.
 */


$(document).ready(function() {
    $('#js_testClickAnimation').on("click",function() {
        $('#imageBgScale').fadeOut('slow', function() {
            $('#imageBgScale').fadeIn('slow', function() {
                // Animation complete
            });
        });

    });
//    $('#footertab').toggle(function() {
//        $('#footer').animate({
//            bottom: '-=120'
//        }, 1000);
//    },function() {
//        $('#footer').animate({
//            bottom: '+=120'
//        }, 1000);
//    })
});