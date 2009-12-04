/* Script by: www.jtricks.com
 * Version: 20071127
 * Latest version:
 * www.jtricks.com/javascript/navigation/fixed_menu.html
 * 
 * Heavily modified by aroy@greatschools.org:
 * - Added upper and lower bounds
 * - Remove left/right code so element is fixed inside another element (i.e. right column)
 */
var fixedMenu =
{
};

fixedMenu.computeShifts = function()
{
    fixedMenu.shiftY = fixedMenu.hasInner
        ? pageYOffset
        : fixedMenu.hasElement
          ? document.documentElement.scrollTop
          : document.body.scrollTop;
    // don't scroll element higher than top of right column
    fixedMenu.shiftY = fixedMenu.shiftY - fixedMenu.objectTop(document.getElementById(fixedMenu.offsetTopElementId));
    if (fixedMenu.shiftY < 0) {
        fixedMenu.shiftY = 0;
    }
    if (fixedMenu.targetTop > 0) {
        fixedMenu.shiftY += fixedMenu.targetTop;
    }
    else
    {
        fixedMenu.shiftY +=
            (fixedMenu.hasElement
            ? document.documentElement.clientHeight
            : fixedMenu.hasInner
              ? window.innerHeight - 20
              : document.body.clientHeight)
            - fixedMenu.targetBottom
            - fixedMenu.menu.offsetHeight;
    }

    // don't scroll element lower than bottom of main column
    var mainHeight;
    var mainDiv = document.getElementById(fixedMenu.offsetBottomElementId);
    mainHeight = (navigator.userAgent.toLowerCase().indexOf('opera') == -1) ?
                 mainDiv.scrollHeight : mainDiv.offsetHeight;
    // decrease height of scrollable area by the distance from top of right column that this module appears
    mainHeight = mainHeight - document.getElementById(fixedMenu.offsetTopElementId).offsetTop;
    var myHeight = (navigator.userAgent.toLowerCase().indexOf('opera') == -1) ?
                 fixedMenu.menu.scrollHeight : fixedMenu.menu.offsetHeight;
    mainHeight = mainHeight - myHeight;
    if (fixedMenu.shiftY > mainHeight) {
        fixedMenu.shiftY = fixedMenu.currentY;
    }
};

fixedMenu.moveMenu = function()
{
    fixedMenu.computeShifts();

    if (fixedMenu.currentY != fixedMenu.shiftY)
    {
        fixedMenu.currentY = fixedMenu.shiftY;

        if (document.layers)
        {
            fixedMenu.menu.top = fixedMenu.currentY;
        }
        else
        {
            fixedMenu.menu.style.top = fixedMenu.currentY + 'px';
        }
    }
};

fixedMenu.floatMenu = function()
{
    fixedMenu.moveMenu();
    setTimeout('fixedMenu.floatMenu()', 20);
};

// addEvent designed by Aaron Moore
fixedMenu.addEvent = function(element, listener, handler)
{
    if(typeof element[listener] != 'function' ||
       typeof element[listener + '_num'] == 'undefined')
    {
        element[listener + '_num'] = 0;
        if (typeof element[listener] == 'function')
        {
            element[listener + 0] = element[listener];
            element[listener + '_num']++;
        }
        element[listener] = function(e)
        {
            var r = true;
            e = (e) ? e : window.event;
            for(var i = 0; i < element[listener + '_num']; i++)
                if(element[listener + i](e) === false)
                    r = false;
            return r;
        };
    }

    //if handler is not already stored, assign it
    for(var i = 0; i < element[listener + '_num']; i++)
        if(element[listener + i] == handler)
            return;
    element[listener + element[listener + '_num']] = handler;
    element[listener + '_num']++;
};

fixedMenu.objectTop = function(obj)
{
    var curtop = 0;
    if (obj.offsetParent) {
        do {
            curtop += obj.offsetTop;
        } while (obj = obj.offsetParent);
    }
    return curtop;
};

/*
 * fixedMenuId = id of element to have fixed
 * offsetTopElementId = id of element used to calculate max top offset
 * offsetBottomElementId = id of element used to calculate max bottom offset
 */
fixedMenu.init = function(fixedMenuId, offsetTopElementId, offsetBottomElementId)
{
    fixedMenu.hasInner = typeof(window.innerWidth) == 'number';
    fixedMenu.hasElement = document.documentElement != null
       && document.documentElement.clientWidth;

    fixedMenu.menu = document.getElementById
        ? document.getElementById(fixedMenuId)
        : document.all
          ? document.all[fixedMenuId]
          : document.layers[fixedMenuId];
    fixedMenu.offsetTopElementId=offsetTopElementId;
    fixedMenu.offsetBottomElementId=offsetBottomElementId;

    var ob =
        document.layers
        ? fixedMenu.menu
        : fixedMenu.menu.style;

    fixedMenu.targetLeft = parseInt(ob.left);
    fixedMenu.targetTop = parseInt(ob.top);
    fixedMenu.targetRight = parseInt(ob.right);
    fixedMenu.targetBottom = parseInt(ob.bottom);

    if (document.layers)
    {
        fixedMenu.menu.left = 0;
        fixedMenu.menu.top = 0;
    }

    fixedMenu.addEvent(window, 'onscroll', fixedMenu.moveMenu);
    fixedMenu.floatMenu();
};

