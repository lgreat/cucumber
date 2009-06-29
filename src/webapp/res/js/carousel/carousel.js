// JavaScript Document
// required to avoid "$j" collisions with Prototype.js
jQuery.noConflict();
var $j = jQuery;

$j(document).ready(function() { 
	
	// Initialize state
	var i = 1;
	var MINIMUMI = 1;
	var images = $j('.images img');
	var CURRENTI = (images ? images.size() : 0);
	var MAXIMUMI = 8;
	var fadeTime = 2000;
	var holdTime = 4000;
	var swapTime = 2000;
	var holdID = null;
	var swapID = null;
	$j('.bg').animate({"opacity" : .9},0);
	$j('.inner h3, .inner p, .images img,').hide();
	if (CURRENTI>MAXIMUMI){
		CURRENTI = MAXIMUMI;
	}
	// Run animation
	
	// Initially, image 1 fadeIn over 2 secs
	// image 1 holds for 4 secs
	var fadeInSlide = function() {
		$j('.images img:eq('+(i-1)+')').fadeIn(fadeTime);
		holdID = setTimeout(fadeOutSlide,holdTime);
		//console.log('holdID = '+holdID);
	}
	// image 1 fadeOut over 2 secs
	// blank holds for 2 secs
	var fadeOutSlide = function() {
		clearTimeout(holdID);
		$j('.images img:eq('+(i-1)+')').fadeOut(fadeTime);
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
	//
	
	// Stop animation
	if (CURRENTI>MINIMUMI){
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
			holdID = setTimeout(fadeOutSlide,holdTime);
		});
	}
	
	// Navigation functions
	var setText = function(slideNumber) {
		$j('.inner h3').hide();
		$j('.inner h3:eq('+(slideNumber-1)+')').show();
		$j('.inner p').hide();
		$j('.inner p:eq('+(slideNumber-1)+')').show();
	}
	
	var setImage = function(slideNumber) {
		$j('.images img').hide();
		$j('.images img:eq('+(slideNumber-1)+')').show();
	}
	
	var setButtonState = function(slideNumber) {
		$j('.num').attr('src', function() {
			return this.src.replace('on.gif','off.gif');
		});
		$j('.num:eq('+(slideNumber-1)+')').attr('src', function() {
			return this.src.replace('off.gif','on.gif');
		});
	}
	var showButtons = function() {
		$j('.num:lt('+(CURRENTI)+')').show();
		$j('.num:gt('+(MAXIMUMI-1)+')').hide(function(){
			$j(this).parent().hide();
		});
	}
	// Navigation buttons
	// - numbered buttons
	$j('.num').click(function() {
		var src = $j(this).attr('src');
		i = src.replace(/^.*num(\d+)-.*$/,'$1');
		setImage(i);
		setText(i);
		setButtonState(i);
	});
	// - arrow right
	$j('#ar').click(function() {
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
	$j('#al').click(function() {
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
		setImage(i);
		setButtonState(i);
		//fadeInSlide();
		showButtons();
		holdID = setTimeout(fadeOutSlide,holdTime);
	} else {
		setText(i);
		setImage(i);
	}
	// EOF
});