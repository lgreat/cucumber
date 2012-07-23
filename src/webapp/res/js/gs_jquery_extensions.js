// JavaScript Document

/*
Description: List of custom jQuery plugins built fot the GreatSchools website
Author: Randall Cox
Version: 1.0
.
General comments:
http://docs.jquery.com/Plugins/Authoring
http://docs.jquery.com/Tutorials#Plugin_Development
.

[Table Of Contents]

=0 template
=1 disableSelection (disable text selection)
=1a disableSelection - register CSS class list of website wide items with text selection disabled
=2 popup (informational popup bubbles)
=3 alternateRowColors (table stripping plugin)
=3a alternateRowColors - register table stripping plugin
=4 debug (console.log or alert messaging)
=5 characterCounter (textarea character count)
=6 infiniteCarousel
=6 infiniteCarousel - registration
=7 tabs (create tabs using the styles in tabs.css)
*/

/* =0 template
-------------------------------------------------------------------------------------------*/

/*
// create closure
(function($){

    //Attach this new method to jQuery
    $.fn.extend({

        //This is where you write your plugin's name
        pluginname: function() {

            //Iterate over the current set of matched elements
            return this.each(function() {

                //code to be inserted here

            });
        }
    });

// end of closure
})(jQuery);
*/

/* =1 disableSelection (disable text selection)
-------------------------------------------------------------------------------------------*/

(function($) {
    // extend the core jQuery with disableSelection
    $.fn.extend({
        disableSelection : function() {
            return this.each(function() {
                // for IE
                this.onselectstart = function() {
                    return false;
                };
                this.unselectable = "on";
                // for normal browsers
                var obj = $(this);
                obj.css('-moz-user-select', 'none');
                obj.css('-webkit-user-select', 'none');
                obj.css('-khtml-user-select', 'none');
            });
        }
    });
})(jQuery);

/* =1a disableSelection
-------------------------------------------------------------------------------------------*/

jQuery(document).ready(function() {
    // disable selection of button-1 class
    jQuery('.button-1').disableSelection();
    // disable selection of button-1-inactive class
    jQuery('.button-1-inactive').disableSelection();
    // disable selection of button-2 class
    jQuery('.button-2').disableSelection();
    // disable selection of button-2-inactive class
    jQuery('.button-2-inactive').disableSelection();
    // disable selection of button-3 class
    jQuery('.button-3').disableSelection();
    // disable selection of button-3-inactive class
    jQuery('.button-3-inactive').disableSelection();
    // disable selection of button-3 class
    jQuery('.button-4').disableSelection();
    // disable selection of button-3-inactive class
    jQuery('.button-4-inactive').disableSelection();
    // disable selection of button-3 class
    jQuery('.button-5').disableSelection();
    // disable selection of button-3-inactive class
    jQuery('.button-5-inactive').disableSelection();
    // disable selection of button-3 class
    jQuery('.button-6').disableSelection();
    // disable selection of button-3-inactive class
    jQuery('.button-6-inactive').disableSelection();
    // disable selection of text in primary navigation
    jQuery('#primary').disableSelection();
    // disable selection of text in secondary navigation
    jQuery('#secondary').disableSelection();
    // disable selection of text on Office School Profile mahjong tiles
    jQuery('#js_espFormNav').disableSelection();
    // disable selection of text on topic center tabs
    jQuery('.tabNavigation').disableSelection();
    // disable selection of forward button on infinite carousel
    jQuery('.infiniteCarousel3').disableSelection();
    // disable selection of back button on infinite carousel
    jQuery('.infiniteCarousel4').disableSelection();
    // disable selection of Photo-gallery on school overview page
    jQuery('#photo-gallery').disableSelection();
    // disable selection of Photo-gallery on articles page
    jQuery('#article-photo-gallery').disableSelection();

});

/* =2 popup (informational popup bubbles)
-------------------------------------------------------------------------------------------*/

//You need an anonymous function to wrap around your function to avoid conflict
(function ($) {

  //Attach this new method to jQuery
  $.fn.extend({

    //This is where you write your plugin's name
    //plugin name - popup
    popup: function(options) {

      //Set the default values, use comma to separate the settings
      //Settings list and the default values
      var defaults = {
        distance: 10,
        time: 250,
        hideDelay: 500,
        appear: 'left',// left=appears on the left, right=appears on the right, above=appears above, below=appears below
        leftPosition: 0,
        topPosition: 0
      };

      var options = $.extend(defaults, options);

      return this.each(function() {
        // options
        var o = options;
        //console.log(o.appear);
        var obj = $(this);

        var hideDelayTimer = null;
        var hDir = null;
        var vDir = null;

        // tracker
        var beingShown = false;
        var shown = false;

        var trigger = $('.js-trigger', obj);
        var popup = $('.js-popup', obj).css({
          'opacity': 0
        });
        var popupWidth = popup.outerWidth();
        var popupHeight = popup.outerHeight();
        var popupOffset = popup.offset();
        var popupOffsetLeft = popupOffset.left;
        var triggerWidth = trigger.outerWidth();
        var triggerOffset = trigger.offset();
        var triggerOffsetLeft = triggerOffset.left;

        var topPos = null;
        var leftPos = null;

        // set the mouseover and mouseout on both element
        $([trigger.get(0), popup.get(0)]).mouseover(function () {
          //Add a higher z-index value so this image stays on top
          obj.css({'z-index' : '1000'});
          popup.css({'z-index' : '501'});
          // get the location of the popup and set its horizonal and vertical directions
          switch (true) {
            case (o.appear==='above'):
              leftPos = o.leftPosition;
              topPos = o.topPosition;
              hDir = 0;
              vDir = -1;
              break;
            case (o.appear==='right'):
              leftPos = o.leftPosition;
              topPos = o.topPosition;
              hDir = 1;
              vDir = 0;
              break;
            case (o.appear==='below'):
              leftPos = o.leftPosition;
              topPos = o.topPosition;
              hDir = 0;
              vDir = 1;
              break;
            case (o.appear==='left'):
              leftPos = o.leftPosition;
              topPos = o.topPosition;
              hDir = -1;
              vDir = 0;
              break;
            default:
              alert("A valid popup appearance position has not been set.");
          }
          // stops the hide event if we move from the trigger to the popup element
          if (hideDelayTimer) clearTimeout(hideDelayTimer);

          // don't trigger the animation again if we're being shown, or already visible
          if (beingShown || shown) {
            return;
          } else {
            beingShown = true;

            // reset position of popup box
            popup.css({
              top: topPos,
              left: leftPos,
              display: 'block' // brings the popup back in to view
            })

            // (we're using chaining on the popup) now animate it's opacity and position
        .   animate({
              left: '+=' + (o.distance * hDir) + 'px',
              top: '+=' + (o.distance * vDir) + 'px',
              opacity: 1
            }, o.time, 'swing', function () {
              // once the animation is complete, set the tracker variables
              beingShown = false;
              shown = true;
            });

          }
        }).mouseout(function () {
          //Add a higher z-index value so this image stays on top
          obj.css({'z-index' : '1'});
          popup.css({'z-index' : '1'});
          // reset the timer if we get fired again - avoids double animations
          if (hideDelayTimer) clearTimeout(hideDelayTimer);

          // store the timer so that it can be cleared in the mouseover if required
          hideDelayTimer = setTimeout(function () {
            hideDelayTimer = null;
            popup.animate({
              left: '+=' + (o.distance * hDir) + 'px',
              top: '+=' + (o.distance * vDir) + 'px',
              opacity: 0
            }, o.time, 'swing', function () {
              // once the animate is complete, set the tracker variables
              shown = false;
              // hide the popup entirely after the effect (opacity alone doesn't do the job)
              popup.css('display', 'none');
            });
          }, o.hideDelay);
        });
      });

    }

  });

//pass jQuery to the function for closure,
})(jQuery);

/* =3 alternateRowColors (table stripping plugin)
---------------------------------------------------------------------------*/

(function($) {

    //Attach this new method to jQuery
    $.fn.extend({

        //This is where you write your plugin's name
        alternateRowColors: function() {

            //Iterate over the current set of matched elements
            return this.each(function() {
                $('tr:odd', this)
                        .removeClass('odd').addClass('even');
                $('tr:even', this)
                        .removeClass('even').addClass('odd');
                return this;

            });
        }
    });

// end of closure
})(jQuery);

/* =3a alternateRowColors (table stripping plugin)
 ---------------------------------------------------------------------------*/
jQuery(document).ready(function() {
    // disable selection of button-1 class
    jQuery('.tableType1 tbody.striped').alternateRowColors();
});

/* =4 debug messaging
---------------------------------------------------------------------------*/

function debug(what) {
    if (window.console && window.console.firebug) {
        console.log(what);
    }
    else {
        alert(what);
    }
}

/* =5 characterCounter (textarea character count)
---------------------------------------------------------------------------*/

(function($){

    //Attach this new method to jQuery
    $.fn.extend({

        //This is where you write your plugin's name
        characterCounter: function(options) {

            var defaults = {
                charLimit: 250
            };

            var options = $.extend(defaults, options);

            //Iterate over the current set of matched elements
            return this.each(function() {
                // options
                var o = options;
                var obj = $(this);

                // controls character input/counter
                obj.bind('keyup paste mouseup click input', function() {
                    var charLimit = o.charLimit;
                    var charLength = obj.val().length;
                    var charFeedback = $('#js-charCount');
                    var plural = (((charLimit - charLength)===1) || ((charLength - charLimit)===1)) ? '' : 's';
                    // Alerts when character limit is reached
                    if(obj.val().length > charLimit){
                        charFeedback.html('<span class="formErrorText">' + (charLength - charLimit) + ' character' + plural + ' over limit</span>');
                        $('> span', charFeedback).show();
                    } else {
                        // Displays count
                        charFeedback.html((charLimit - charLength) + ' character' + plural + ' remaining');
                    }
                });

            });
        }
    });

// end of closure
})(jQuery);

/* =6 infiniteCarousel
---------------------------------------------------------------------------*/

// create closure
(function($) {

    //Attach this new method to jQuery
    $.fn.extend({

        //This is where you write your plugin's name
        infiniteCarousel: function(options) {

            //Set the default values, use comma to separate the settings
            //Settings list and the default values
            var defaults = {
                showCounter: false
            };

            var options = $.extend(defaults, options);

            function repeat(str, num) {
                return new Array(num + 1).join(str);
            }

            //Iterate over the current set of matched elements
            return this.each(function() {
                var o = options;
                var $wrapper = $('> .wrapper', this).css('overflow', 'hidden'),
                    $slider = $wrapper.find('> ul'),
                    $items = $slider.find('> li'),
                    $single = $items.filter(':first'),
                    $counterDiv = $('> div.carouselCounter', this),
                    $photoNum = $('> input.js_photoNum', this),
                    viewWidth = $wrapper.innerWidth(),
                    singleWidth = $single.outerWidth(),
                    visible = Math.ceil(viewWidth / singleWidth),// note: does not include padding or border
                    currentPage = 1,
                    pages = Math.ceil($items.length / visible),
                    totalItems = $items.length;

//                console.log('viewWidth: '+viewWidth);
//                console.log('singleWidth: '+singleWidth);
//                console.log('visible: '+visible);
//                console.log('pages: '+pages);
//                console.log('length of all items: '+$items.length);
//                console.log('(length of all items % visible): '+($items.length % visible));

                // 1. Pad so that 'visible' number will always be seen, otherwise create empty items
                if (($items.length % visible) != 0) {
                    $slider.append(repeat('<li class="empty" />', visible - ($items.length % visible)));
                    $items = $slider.find('> li');
                }

                // 2. Top and tail the list with 'visible' number of items, top has the last section, and tail has the first
                $items.filter(':first').before($items.slice(-visible).clone().addClass('cloned'));
                $items.filter(':last').after($items.slice(0, visible).clone().addClass('cloned'));
                $items = $slider.find('> li'); // reselect

                // 3. Set the left position to the first 'real' item
                $wrapper.scrollLeft(singleWidth * visible);
//                console.log('first item position: '+(singleWidth * visible));

                // 4. paging function
                function gotoPage(page) {
//                    console.log('Current Page: '+currentPage);
                    var newScrollLeft = page * singleWidth * visible;
                    var scrollLeftDelta = newScrollLeft - $wrapper.scrollLeft();

                    $wrapper.filter(':not(:animated)').animate({
                            scrollLeft : '+=' + scrollLeftDelta
                        }, 500, function () {
                        if (page == 0) {
                            $wrapper.scrollLeft(singleWidth * visible * pages);
                            page = pages;
                        } else if (page > pages) {
                            $wrapper.scrollLeft(singleWidth * visible);
                            // reset back to start position
                            page = 1;
                        }

                        currentPage = page;
                        if (o.showCounter === true && $counterDiv !== undefined) {
                            $counterDiv.text(currentPage + ' of ' + totalItems);
                            if ($photoNum !== undefined) {
                                $photoNum.val(currentPage);
                            }
                        }

                    });

                    return false;
                }

                pages > 1 ? $wrapper.after('<a class="arrow back">&lsaquo;</a><a class="arrow forward">&rsaquo;</a>') : $wrapper;

                // 5. Bind to the forward and back buttons
                $('a.back', this).click(function () {
                    return gotoPage(currentPage - 1);
                });

                $('a.forward', this).click(function () {
                    return gotoPage(currentPage + 1);
                });

                // Bind a custom event that listens for item selections from outside the carousel
                $slider.on('itemSelected', 'li', function() {
                    var me = $(this);
                    if (me.hasClass('cloned')) { // don't consider any events triggered off our fake items
                        return false;
                    }
                    var myPage = Math.floor(me.index() / visible); // first cloned page is page 0
                    if (myPage != currentPage && myPage > 0 && myPage <= pages) {
                        gotoPage(myPage);
                    }
                });

                // Bind a custom event that listens for this carousel being shown (after being hidden)
                $(this).on('shown', function() {
                    // re-calculate scrollLeft in case being hidden reset it to 0
                    $wrapper.scrollLeft(currentPage * singleWidth * visible);
                });

                // create a public interface to move to a specific page
                $(this).bind('goto', function (event, page) {
                    gotoPage(page);
                });

            });
        }
    });

// end of closure
})(jQuery);

/* =6 infiniteCarousel - registration
-------------------------------------------------------------------------------------------*/
jQuery(document).ready(function () {
    jQuery('.infiniteCarousel9').infiniteCarousel();
});

/* =7 tabs (create tabs using the styles in tabs.css)
-------------------------------------------------------------------------------------------*/
 /*
(function($) {
    // extend the core jQuery with gsTabs
    $.fn.extend({
        gsTabs : function() {
            return this.each(function() {
                var tab = $(this);
                tab.children('div').hide(); // Hide all content divs
                var tabNav = tab.find('ul:first'); // get only the first ul not all of the descendents
                tab.children('div:first').show(); // Show the first div
                tabNav.find('li:first a').addClass('selected'); // Set the class of the first link to active
                tabNav.find('li').each(function(){
                    $(this).find('a').click(function(){ //When any link is clicked
                        tab.children('div').hide(); // hide all layers
                        var tabNum = tabNav.find('li').index($(this).parent());// find reference to the content
                        tab.children('div').eq(tabNum).show();// show the content
                        tabNav.find('li a').removeClass('selected');// turn all of them off
                        $(this).addClass('selected');// turn selected on
                        return false;
                    });
                });
            });
        }
    });
})(jQuery);

jQuery(document).ready(function() {
    jQuery('.gsTabs').gsTabs();
});
   */
/* =7 tabs (create tabs using the styles in tabs.css)
 -------------------------------------------------------------------------------------------*/

(function($) {
    // extend the core jQuery with gsTabs
    $.fn.extend({
        gsTabs : function() {
            var changeHistory = function(title, url) {
                if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
                    window.History.pushState(null, title, url);
                }
            }
            return this.each(function() {
                var tab = $(this);
                var tabNav = tab.find('ul:first'); // get only the first ul not all of the descendents
                var showHome = tabNav.find('.selected').length;
                if(!showHome) {
                   tabNav.find('li:first a').addClass('selected').siblings().addClass('selected');
                   tab.children('div:first').show();
                }
                tabNav.find('li').each(function(){
                    $(this).find('a').click(function(){ //When any link is clicked
                        changeHistory($(this).attr('title'), $(this).attr('href') );
                        tab.children('div').hide(); // hide all layers
                        var tabNum = tabNav.find('li').index($(this).parent());// find reference to the content
                        tab.children('div').eq(tabNum).show();// show the content
                        tabNav.find('li a').removeClass('selected');// turn all of them off
                        tabNav.find('li #arrowdiv').removeClass('selected');// turn all of them off
                        $(this).addClass('selected');// turn selected on
                        $(this).siblings().addClass('selected');// turn selected on
                        return false;
                    });
                });
            });
        }
    });
})(jQuery);

jQuery(document).ready(function() {
    jQuery('.gsTabs').gsTabs();
});