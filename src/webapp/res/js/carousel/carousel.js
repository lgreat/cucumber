// JavaScript Document

$(document).ready(function() { 
	
	// Initialize state
	var i = 1;
	var MINIMUMI = 1;
	var CURRENTI = $('.images img').size();
	var MAXIMUMI = 8;
	var fadeTime = 2000;
	var holdTime = 4000;
	var swapTime = 2000;
	var holdID = null;
	var swapID = null;
	$('.bg').animate({"opacity" : .9},0);
	$('.inner h3, .inner p, .images img').hide();
	if (CURRENTI>MAXIMUMI){
		CURRENTI = MAXIMUMI;
	}
	// Run animation
	
	// Initially, image 1 fadeIn over 2 secs
	// image 1 holds for 4 secs
	var fadeInSlide = function() {
		$('.images img:eq('+(i-1)+')').fadeIn(fadeTime);
		holdID = setTimeout(fadeOutSlide,holdTime);
		//console.log('holdID = '+holdID);
	}
	// image 1 fadeOut over 2 secs
	// blank holds for 2 secs
	var fadeOutSlide = function() {
		clearTimeout(holdID);
		$('.images img:eq('+(i-1)+')').fadeOut(fadeTime);
		swapID = setTimeout(nextSlide,swapTime);
		//console.log('swapID = '+swapID);
	}
	// text 1 hides
	// nav goes to 2
	// text 2 shows
	var nextSlide = function() {
		clearTimeout(swapID);
		if (i >= CURRENTI) {
			i = MINIMUMI;
		} else {
			i++;
		}
		setText(i);
		setButtonState(i);
		fadeInSlide();
	}
	// image 2 fadeIn over 2 secs
	// ...
	
	// Stop animation
  	if (CURRENTI>MINIMUMI){
        $('#carousel').mouseover(function() {
            // Pause animation on mouseover
            clearTimeout(holdID);
            clearTimeout(swapID);
            setImage(i);
            setText(i);
            setButtonState(i);
        });

        $('#carousel').mouseout(function() {
            // Restart animation on mouseout
            holdID = setTimeout(fadeOutSlide,holdTime);
        });
    }
	
	// Navigation functions
	var setText = function(slideNumber) {
		$('.inner h3').hide();
		$('.inner h3:eq('+(slideNumber-1)+')').show();
		$('.inner p').hide();
		$('.inner p:eq('+(slideNumber-1)+')').show();
	}
	
	var setImage = function(slideNumber) {
		$('.images img').hide();
		$('.images img:eq('+(slideNumber-1)+')').show();
	}
	
	var setButtonState = function(slideNumber) {
		$('.num').attr('src', function() {
			return this.src.replace('on.gif','off.gif');
		});
		$('.num:eq('+(slideNumber-1)+')').attr('src', function() {
			return this.src.replace('off.gif','on.gif');
		});
	}
	// Navigation buttons
	// - numbered buttons
	$('.num').click(function() {
		var src = $(this).attr('src');
		i = src.replace(/^.*num(\d+)-.*$/,'$1');
		setImage(i);
		setText(i);
		setButtonState(i);
	});
	// - arrow right
	$('#ar').click(function() {
		if (i >= CURRENTI) {
			i = MINIMUMI;
		} else {
			i++;
		}
		//console.log(i);
		setImage(i);
		setText(i);
		setButtonState(i);
	});
	// - arrow left
	$('#al').click(function() {
		if (i <= MINIMUMI) {
			i = CURRENTI;
		} else {
			i--;
		}
		//console.log(i);
		setImage(i);
		setText(i);
		setButtonState(i);
	});
	
	// start animation
	if (CURRENTI>MINIMUMI){
        setText(i);
	    setButtonState(i);
	    fadeInSlide();
  } else {
        setText(i);
        setImage(i);
	}
	// EOF
});