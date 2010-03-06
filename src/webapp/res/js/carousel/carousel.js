// JavaScript Document
var $j = jQuery;

$j(document).ready(function() {
    //
    // extend the core jQuery with disableSelection
    jQuery.fn.extend({
        disableSelection : function() {
            this.each(function() {
                this.onselectstart = function() {
                    return false;
                };
                this.unselectable = "on";
                jQuery(this).css('-moz-user-select', 'none');
            });
        }
    });
    //
    // disable selection of grade by grade
    $j('#carousel').disableSelection();

    // initialize state
    $j('#banner').animate({"opacity" : .9}, 0);
    $j('#info h2, #info p, #image img,#buttons li,#next,#prev').hide();
    var MINI = 1; // at least one slide must be set in the carousel
    var MAXI = 8; // no more than 8 slides can be set in the carousel
    var i = 1;
    var images = $j('#image img');
    var SETI = (images ? images.size() : 0);
    var fadeTime = 2000;
    var holdTime = 4000;
    var swapTime = 2000;
    var holdID = null;
    var swapID = null;
    if (SETI > MAXI) {
        SETI = MAXI;
    }
    if (SETI < MINI) {
        SETI = MINI;
    }

    // Run animation

    // Initially, image 1 fadeIn over 2 secs
    // image 1 holds for 4 secs
    var fadeInSlide = function() {
        $j('#image img:eq(' + (i - 1) + ')').fadeIn(fadeTime);
        holdID = setTimeout(fadeOutSlide, holdTime);
    }
    // image 1 fadeOut over 2 secs
    // blank holds for 2 secs
    var fadeOutSlide = function() {
        clearTimeout(holdID);
        $j('#image img:eq(' + (i - 1) + ')').fadeOut(fadeTime);
        swapID = setTimeout(nextSlide, swapTime);
    }
    // text 1 hides
    // nav goes to 2
    // text 2 shows
    var nextSlide = function() {
        clearTimeout(swapID);
        if (i >= SETI) {
            i = MINI;
        } else {
            i++;
        }
        setText(i);
        setButtonState(i);
        fadeInSlide();
    }
    // image 2 fadeIn over 2 secs

    // Stop animation
    if (SETI > MINI) {
        $j('#carousel').mouseover(function() {
            // Pause animation on mouseover
            clearTimeout(holdID);
            clearTimeout(swapID);
            setImage(i);
            setText(i);
            setButtonState(i);
        });

        $j('#carousel').mouseout(function() {
            // Restart animation on mouseout
            holdID = setTimeout(fadeOutSlide, holdTime);
        });
    }

    // set state
    var setText = function(slideNumber) {
        $j('#info h2').hide();
        $j('#info h2:eq(' + (slideNumber - 1) + ')').show();
        $j('#info p').hide();
        $j('#info p:eq(' + (slideNumber - 1) + ')').show();
    }

    var setImage = function(slideNumber) {
        $j('#image img').hide();
        $j('#image img:eq(' + (slideNumber - 1) + ')').show();
    }

    var setButtonState = function(slideNumber) {
        $j('#buttons li').removeClass('on');
        $j('#buttons li').addClass('off');
        $j('#buttons li:eq(' + (slideNumber - 1) + ')').removeClass('off');
        $j('#buttons li:eq(' + (slideNumber - 1) + ')').addClass('on');
    }

    var showButtons = function() {
        if (SETI > MINI) {
            $j('#next').show();
            $j('#prev').show();
            $j('#buttons li:lt(' + (SETI) + ')').show();
        }
    }

    // change button state
    $j('#buttons li').click(function() {
        i = $j(this).attr('id');
        $j('#buttons li').removeClass('on');
        $j('#buttons li').addClass('off');
        $j(this).removeClass('off');
        $j(this).addClass('on');
        setImage(i);
        setText(i);
    });

    // next button
    $j('#next').click(function() {
        if (i >= SETI) {
            i = MINI;
        } else {
            i++;
        }
        setImage(i);
        setText(i);
        setButtonState(i);
    });
    // previous button
    $j('#prev').click(function() {
        if (i <= MINI) {
            i = SETI;
        } else {
            i--;
        }
        setImage(i);
        setText(i);
        setButtonState(i);
    });

    // start animation
    if (SETI > MINI) {
        showButtons();
        setText(i);
        setImage(i);
        setButtonState(i);
        holdID = setTimeout(fadeOutSlide, holdTime);
    } else {
        setText(i);
        setImage(i);
    }

});
// EOF
