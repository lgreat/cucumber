/*
 *  GS-5574
 *
 *  depends on global.js
 */

/*
 * ClickCapture object constructor
 */
function ClickCapture(){

    //alert("ClickCapture()");

    this.getData();
    this.clearData();
    //this.debug();

}

ClickCapture.prototype.cookieName = "OmnitureTracking"   ;

/*
 *  capture()
 *
 *  Saves the values to the cookie.
 *
 *  Note that multiple handlers may be associated to one event, so there may be multiple calls
 *  to this method.
 */
ClickCapture.prototype.capture = function (omnitureEntity, value) {
    //alert("capture(" + omnitureEntity + ", " + value + ")");
    //if(omnitureEntity == undefined || omnitureEntity.length < 1 || value == undefined || value.length < 1) alert("capture: " + omnitureEntity + ", " + value);
    subCookie.setObjectProperty(this.cookieName, omnitureEntity, value);
};

/*
 * getData()
 *
 * Gets the captured info from the this.cookie and clears the this.cookie
 */
ClickCapture.prototype.getData = function (){
    //alert("TrackItLoadFromCookie()");
    this.dataObject = subCookie.getObject(this.cookieName);
} ;

/*
 *  clearData
 *
 *  Clears this.cookie
 */
ClickCapture.prototype.clearData = function (){
    //alert("clearData()");
    subCookie.setObject(this.cookieName, new Array()) ;
};


/*
 *  debug
 *
 *  takes the captured data and passes it along to omniture
 */
ClickCapture.prototype.debug = function ()  {
    //alert("debug");
    var debugStr = "ClickCapture Debug";
    for(var propName in this.dataObject){
        debugStr += "\n" + propName + " = " + this.dataObject[propName];
    }
    alert(debugStr) ;
} ;

ClickCapture.prototype.getVariable = function(variableType, index, overrideValue){

    if (overrideValue != undefined && overrideValue.length > 0){
        return overrideValue;
    }

    if (this.dataObject == undefined){
        return '';
    }

    var value = '';
    if (this.dataObject[variableType + index]) {
        value =  this.dataObject[variableType + index];
    }

    if (value != 'undefined' ){
        return value;
    }else{
        return '';
    }

} ;

ClickCapture.prototype.getProp = function( index, overrideValue){
    return this.getVariable("prop", index, overrideValue);
} ;

ClickCapture.prototype.getEVar = function( index, overrideValue){
    return this.getVariable("eVar", index, overrideValue);
}

ClickCapture.prototype.getEvents = function(pageEvents){

    // if pageEvents isn't null, merge it into the ones captured

    
} ;

var customInsight9ClickEventHandler = function (e) {
     var obj = getNode(e);
     clickCapture.capture("prop9",  obj.id);
     //removed the truncating of anything past the most significant part of the obj.id
     //var id = obj.id;
     //var idParts = id.split("__");
     //var mostSignificantPart = idParts[0];
     //clickCapture.capture("prop9",  mostSignificantPart);
 }

 var evar5ClickEventHandler = function(e) {
     var obj = getNode(e);
     var id = obj.id;
     var idParts = id.split("__");
     var mostSignificantPart = idParts[0];
     clickCapture.capture("eVar5",   mostSignificantPart);
 }


 function getNode(e){

     var obj = eventTrigger(e);

     // if obj is an image get it's parent (expected to be a link)
     if (obj.tagName.toLowerCase() == 'img'){
         obj = obj.parentNode;
     }

     return obj;
 }

 //register the object click handlers...
 function registerMyEventHandlers() {

     registerClickHandlers(customInsight9ClickEventHandler,'GS_CI9_');
     registerClickHandlers(evar5ClickEventHandler,'GS_EV5_');
 }

/*
 * registers click handler, customClickHandler, to each element that has a class with className in it
 */
function registerClickHandlers(customClickHandler, className){
    //alert("registerClickHandlers");
    var elements = document.getElementsByClassName(className);

    for(var i = 0; i < elements.length; i++) {
        var res = null;

        if (elements[i].id != undefined && elements[i].id.length > 0) {
             res = registerEventHandler(elements[i],"click", customClickHandler);
         }
    }
}

/*
 * from http://javascript.about.com/library/bldom08.htm
 */
document.getElementsByClassName = function(cl) {
    var retnode = [];
    var myclass = new RegExp('\\b'+cl+'\\b');
    var elem = this.getElementsByTagName('*');
    for (var i = 0; i < elem.length; i++) {
        var classes = elem[i].className;
        if (myclass.test(classes)) retnode.push(elem[i]);
    }
    return retnode;
};
/*
 * create an instance of ClickCapture
 */
var clickCapture = new ClickCapture();










