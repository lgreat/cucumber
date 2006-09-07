/*  AutoAssist - Ajax AutoComplete Component, Version 0.9
 *  (c) Cheng Guangnan <chenggn@capxous.com>
 *  For details, see http://capxous.com/autoassist  */
var isKHTML = navigator.appVersion.match(/Konqueror|Safari|KHTML/);
var isOpera = navigator.userAgent.indexOf('Opera') > -1;
var isIE = !isOpera && navigator.userAgent.indexOf('MSIE') > 1;
var isMoz = !isOpera && !isKHTML && navigator.userAgent.indexOf('Mozilla/5.') == 0;
Object.extend(Event, {KEY_PAGE_UP:33,KEY_PAGE_DOWN:34,KEY_END:35,KEY_HOME:36,KEY_INSERT:45,KEY_SHIFT:16,KEY_CTRL:17,KEY_ALT:18});
var CAPXOUS = new Object();
CAPXOUS = {SEL:'onSelect',INX:'_index',inst:new Array(),name:'',key:'',sixty_four:function(d) {
    var b64 = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
    var h = b64.substr(d & 63, 1);
    while (d > 63) {
        d >>= 6;
        h = b64.substr(d & 63, 1) + h;
    }
    ;
    return h;
},registeredTo:function(n) {
    CAPXOUS.name = n;
    return CAPXOUS;
},byLicenseKey:function(k) {
    CAPXOUS.key = k;
},getWindowHeight:function() {
    var h = 0;
    if (typeof(window.innerHeight) == 'number') {
        h = window.innerHeight;
    } else if (document.documentElement && document.documentElement.clientHeight) {
        h = document.documentElement.clientHeight;
    } else if (document.body && document.body.clientHeight) {
        h = document.body.clientHeight;
    }
    ;
    return parseInt(h);
},getStyle:function(e) {
    if (!isKHTML && document.defaultView && document.defaultView.getComputedStyle) {
        return document.defaultView.getComputedStyle(e, null);
    } else if (e.currentStyle) {
        return e.currentStyle;
    } else {
        return e.style;
    }
},getInt:function(s) {
    var i = parseInt(s);
    return isNaN(i)?0:i;
},style:{highlight:'aa_highlight',wait:'aa_wait'},findPopup:function(v) {
    var e = Event.element(v);
    while (e.parentNode && !e.CAPX)e = e.parentNode;
    return e.parentNode?e:null;
},isSelectable:function(e) {
    return(e.nodeType == 1) && (e.getAttribute(CAPXOUS.SEL));
    return false;
},findSelectable:function(v, p) {
    var e = Event.element(v);
    while (e.parentNode && (e != p) && (!CAPXOUS.isSelectable(e))) {
        e = e.parentNode;
    }
    ;
    return(e.parentNode && (e != p))?e:null;
},click:function(v) {
    var e = Event.element(v);
    var p = CAPXOUS.findPopup(v);
    if (p) {
        var s = CAPXOUS.findSelectable(v, p);
        if (s) {
            p.CAPX.i = s.getAttribute(CAPXOUS.INX);
            p.CAPX.select();
        } else {
            while (e.parentNode && (e != p) && (!e.tagName || e.tagName.toUpperCase() != 'A'))e = e.parentNode;
            if (e.parentNode && (e != p)) {
                p.CAPX.request(e.getAttribute("ajaxHref"));
                Event.stop(v);
            }
        }
    } else {
        CAPXOUS.inst.each(function(i) {
            if (i.text != e && i.pop != e)setTimeout(i.hide.bind(i), 10);
        });
    }
},mouseover:function(v) {
    var p = CAPXOUS.findPopup(v);
    if (p) {
        var s = CAPXOUS.findSelectable(v, p);
        if (s) {
            p.CAPX.highlight(s.getAttribute(CAPXOUS.INX));
        }
        ;
    }
},init:function() {
    var p = document.createElement('div');
    p.className = CAPXOUS.style.wait;
    var s = p.style;
    s.display = 'inline';
    s.position = 'absolute';
    s.width = s.height = '0px';
    document.body.appendChild(p);
},dispose:function() {
    CAPXOUS.inst.each(function(i) {
        i.dispose();
    });
    CAPXOUS.inst = null;
}};
Event.observe(window, 'load', CAPXOUS.init);
Event.observe(window, 'unload', CAPXOUS.dispose);
CAPXOUS.AutoComplete = Class.create();
CAPXOUS.AutoComplete.prototype = {visible:false,complete:false,initialize:function(text, f, options) {
    text = $(text);
    if ((text == null) || (f == null) || (typeof f != 'function'))return;
    text.setAttribute('autocomplete', 'off');
    this.txtBox = this.text = text;
    this.keydownX = this.keydown.bindAsEventListener(this);
    this.prepareX = this.prepare.bind(this);
    Event.observe(this.text, 'keydown', this.keydownX);
    Event.observe(this.text, 'dblclick', this.prepareX);
    this.options = options || {};
    this.options.frequency = this.options.frequency || 0.4;
    this.options.minChars = this.options.minChars || 1;
    // added by aroy: pixel value added to width of parent text field when sizing div
    this.options.widthOffset = this.options.widthOffset || 0;
    this.timeout = 0;
    this.getURL = f;
    this.buf = document.createElement('div');
    var p = document.createElement('div');
    p.CAPX = this;
    Element.addClassName(p, 'aa');
    var ps = p.style;
    ps.position = 'absolute';
    ps.top = '-999px';
    ps.height = 'auto';
    Element.hide(p);
    document.body.appendChild(p);
    this.pop = p;
    this.i = -1;
    CAPXOUS.inst.push(this);
    if (CAPXOUS.inst.length == 1) {
        Event.observe(document, 'click', CAPXOUS.click, true);
        Event.observe(document, 'mouseover', CAPXOUS.mouseover);
    }
},dispose:function() {
    Event.stopObserving(this.text, 'keydown', this.keydownX);
    Event.stopObserving(this.text, 'dblclick', this.prepareX);
    this.children = this.req = this.buf = this.iefix = this.pop = this.pop.CAPX = this.text = this.getURL = null;
    CAPXOUS.inst = CAPXOUS.inst.without(this);
    if (!CAPXOUS.inst.length) {
        Event.stopObserving(document, 'click', CAPXOUS.click, true);
        Event.stopObserving(document, 'mouseover', CAPXOUS.mouseover);
    }
},keydown:function(event) {
    var keyCode = event.keyCode;
    if ((keyCode == Event.KEY_UP) || (keyCode == Event.KEY_DOWN)) {
        if (this.complete) {
            (keyCode == Event.KEY_UP)?this.up():this.down();
            this.show();
        }
        ;
        Event.stop(event);
        return;
    }
    ;
    switch (keyCode) {case Event.KEY_TAB:case Event.KEY_LEFT:case Event.KEY_RIGHT:case Event.KEY_PAGE_UP:case Event.KEY_PAGE_DOWN:case Event.KEY_END:case Event.KEY_HOME:case Event.KEY_INSERT:case Event.KEY_SHIFT:case Event.KEY_CTRL:case Event.KEY_ALT:return;case Event.KEY_ESC:this.hide();return;case Event.KEY_RETURN:if (this.visible) {
        this.select();
        return;
    };default:if (this.timeout != 0)clearTimeout(this.timeout);this.timeout = setTimeout(this.prepare.bind(this), this.options.frequency * 1000);this.hide();}
},select:function() {
    if (this.getCurrentEntry()) {
        var stat = this.getCurrentEntry().getAttribute(CAPXOUS.SEL);
        try {
            eval(stat);
        } catch(e) {
        }
        ;
        this.hide();
    }
},getCurrentEntry:function() {
    return this.children?this.children[this.i]:null;
},highlight:function(i) {
    if (!this.complete)return;
    Element.removeClassName(this.getCurrentEntry(), CAPXOUS.style.highlight);
    this.i = i;
    Element.addClassName(this.getCurrentEntry(), CAPXOUS.style.highlight);
},up:function() {
    if (this.i > -1)this.highlight(this.i - 1);
},down:function() {
    if (this.i < this.children.length)this.highlight(this.i + 1);
},preRequest:function() {
    return this.text.value.length >= this.options.minChars;
},prepare:function() {
    this.request();
},request:function(url) {
    if (this.preRequest()) {
        if (url) {
            if (url.charAt(0) == "&")url = this.getURL() + url;
            this.onLoading(true);
        } else {
            url = this.getURL();
            this.onLoading();
        }
        ;
        url = encodeURI(url);
        this.profiling = new Date(); // added by aroy for profiling
        this.req = new Ajax.Updater(this.buf, url, {method:'get',onComplete:this.onComplete.bind(this),onFailure:this.onFailure.bind(this)});
    }
},onFailure:function(transport) {
},onLoading:function() {
    this.complete = false;
    this.i = -1;
    if (!arguments[0]) {
        this.hide();
        this.pop.innerHTML = '';
    }
    ;
    this.startIndicator();
},onComplete:function() {
    setTimeout(this.updateContent.bind(this, arguments[0]), 10);
},updateContent:function() {
    var t = ((this.req == null) || (this.req.transport == arguments[0]));
    if (t) {
        this.complete = true;
        this.pop.innerHTML = this.buf.innerHTML;
        this.buf.innerHTML = '';
        this.i = -1;
        this.children = new Array();
        var totalTime = (new Date()).getTime() - this.profiling.getTime(); // added by aroy for profiling
        var timeSeconds = totalTime/1000;
        var c = '\x3C\x64\x69\x76\x20\x73\x74\x79\x6C\x65\x3D\x22\x63\x6C\x65\x61\x72\x3A\x62\x6F\x74\x68\x3B\x70\x61\x64\x64\x69\x6E\x67\x3A\x32\x70\x78\x3B\x74\x65\x78\x74\x2D\x61\x6C\x69\x67\x6E\x3A\x63\x65\x6E\x74\x65\x72\x3B\x22\x3E\x3C\x61\x20\x68\x72\x65\x66\x3D\x22\x68\x74\x74\x70\x3A\x2F\x2F\x63\x61\x70\x78\x6F\x75\x73\x2E\x63\x6F\x6D\x2F\x22\x20\x73\x74\x79\x6C\x65\x3D\x22\x66\x6F\x6E\x74\x2D\x73\x69\x7A\x65\x3A\x31\x30\x70\x78\x22\x3E\x50\x6F\x77\x65\x72\x65\x64\x26\x6E\x62\x73\x70\x3B\x62\x79\x26\x6E\x62\x73\x70\x3B\x43\x61\x70\x78\x6F\x75\x73\x2E\x63\x6F\x6D\x3C\x2F\x61\x3E' + ' (' + timeSeconds + 's)' + '\x3C\x2F\x64\x69\x76\x3E';
        var s = 0;
        for (var i = 0; i < CAPXOUS.name.length; i++)s += CAPXOUS.name.charCodeAt(i);
        var j = CAPXOUS.key.indexOf(CAPXOUS.sixty_four(s));
        this.pop.innerHTML += (!j && j != CAPXOUS.length)?'':c;
        $A(this.pop.getElementsByTagName('*')).inject([], function(es, c) {
            if (CAPXOUS.isSelectable(c)) {
                c.setAttribute(CAPXOUS.INX, this.children.length);
                Element.addClassName(c, 'row');
                this.children.push(c);
            }
        }.bind(this));
        this.down();
        this.show();
        this.stopIndicator();
    }
},offset:function() {
    var o = 0;
    if (isMoz || isKHTML || (isIE && (document.compatMode != 'BackCompat'))) {
        var bl = 'border-left-width';
        var br = 'border-right-width';
        var pl = 'padding-left';
        var pr = 'padding-right';
        var f = new Function('e', 'p', 'return CAPXOUS.getInt(Element.getStyle(e, p));');
        o = f(this.pop, bl) + f(this.pop, br) + f(this.pop, pl) + f(this.pop, pr);
    }
    ;
    return o;
},fixIEOverlapping:function() {
    var f;
    if (!(f = this.iefix)) {
        f = document.createElement('iframe');
        f.src = 'javascript:false;';
        var fs = f.style;
        fs.position = 'absolute';
        fs.margin = fs.padding = '0px';
        Element.hide(f);
        document.body.appendChild(f);
        this.iefix = f;
    }
    ;
    Position.clone(this.pop, f);
    f.style.zIndex = 1;
    this.pop.style.zIndex = 2;
    Element.show(f);
},show:function() {
    Element.show(this.pop);
    var ph = this.pop.offsetHeight;
    Element.hide(this.pop);
    var pos = Position.cumulativeOffset(this.text);
    var tt = pos[1];
    var th = this.text.offsetHeight;
    var tl = pos[0];
    // added by aroy: add the widthOffset option into width of div
    var tw = this.text.offsetWidth + this.options.widthOffset;
    var wh = CAPXOUS.getWindowHeight();
    var pt;
    var of;
    if ((Position.page(this.text)[1] + th + ph <= wh) || (tt - ph < 0)) {
        pt = tt + th;
        of = th;
    } else {
        pt = tt - ph;
        of = -ph;
    }
    ;
    tw = tw - this.offset();
    Element.setStyle(this.pop, {top:pt + 'px',left:tl + 'px',width:tw + 'px',height:'auto'});
    Element.show(this.pop);
    if (isIE)this.fixIEOverlapping();
    this.visible = true;
},hide:function() {
    if (this.visible) {
        Element.hide(this.pop);
        if (isIE)Element.hide(this.iefix);
        this.visible = false;
    }
},startIndicator:function() {
    Element.addClassName(this.text, CAPXOUS.style.wait);
    if (this.options.indicator)Element.show(this.options.indicator);
},stopIndicator:function() {
    Element.removeClassName(this.text, CAPXOUS.style.wait);
    if (this.options.indicator)Element.hide(this.options.indicator);
}};
var AutoAssist = CAPXOUS.AutoComplete;