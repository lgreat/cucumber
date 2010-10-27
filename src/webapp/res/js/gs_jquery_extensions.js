// JavaScript Document

/*
Description: List of custom jQuery plugins built fot the GreatSchools website
Author: Randall Cox
Version: 1.0
.
General comments:
.

[Table Of Contents]

=0 disableSelection - for disabling text selection
=0a disableSelection - CSS class list of website wide items with text selection disabled
=1 popup - for informational popup bubbles
*/

/* =0 disableSelection
-------------------------------------------------------------------------------------------*/

(function() {
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
                jQuery(this).css('-webkit-user-select', 'none');
                jQuery(this).css('-khtml-user-select', 'none');
            });
        }
    });
})(jQuery);

/* =0a disableSelection
-------------------------------------------------------------------------------------------*/

jQuery(document).ready(function() {
    // disable selection of button-1 class
    jQuery('.button-1').disableSelection();
    // disable selection of button-1-inactive class
    jQuery('.button-1-inactive').disableSelection();
});

/* =1 popup
-------------------------------------------------------------------------------------------*/

//You need an anonymous function to wrap around your function to avoid conflict
(function () {

  //Attach this new method to jQuery
  jQuery.fn.extend({

    //This is where you write your plugin's name
    //plugin name - popup
    popup: function(options) {

      //Set the default values, use comma to separate the settings
      //Settings list and the default values
      var defaults = {
        distance: 10,
        time: 250,
        hideDelay: 500,
        appear: 'left'// left=on the left, right=on the right, top=above, bottom=below
      };

      var options = jQuery.extend(defaults, options);

      return this.each(function() {
        // options
        var o = options;
        //console.log(o.appear);
        var obj = jQuery(this);

        var hideDelayTimer = null;
        var hDir = null;
        var vDir = null;

        // tracker
        var beingShown = false;
        var shown = false;

        var trigger = jQuery('.js-trigger', obj);
        var popup = jQuery('.js-popup', obj).css({
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
        jQuery([trigger.get(0), popup.get(0)]).mouseover(function () {
          // get the location of the popup and set its horizonal and vertical directions
          switch (true) {
            case (o.appear==='top'):
              topPos = popupHeight*-1+10;
              leftPos = (((popupWidth/2)*-1)+(triggerWidth/2));
              hDir = 0;
              vDir = -1;
              break;
            case (o.appear==='right'):
              topPos = -18;
              leftPos = popupOffsetLeft+8;
              hDir = 1;
              vDir = 0;
              break;
            case (o.appear==='bottom'):
              topPos = 8;
              leftPos = (((popupWidth/2)*-1)+(triggerWidth/2));
              hDir = 0;
              vDir = 1;
              break;
            case (o.appear==='left'):
              topPos = -18;
              leftPos = popupOffsetLeft - popupWidth + 10;
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

//pass jQuery to the function,
//So that we will able to use any valid Javascript variable name

})(jQuery);
