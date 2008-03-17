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
    if(omnitureEntity == undefined || omnitureEntity.length < 1 || value == undefined || value.length < 1) alert("capture: " + omnitureEntity + ", " + value);
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

    if (this.dataObject[variableType + index] == undefined){
        return '';
    }


    return this.dataObject[variableType + index];
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
 }

 var evar5ClickEventHandler = function(e) {
     var obj = getNode(e);
     clickCapture.capture("eVar5", s.pageName + ' ' +  obj.id);
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
     var links = document.links;
     //var debugLinks = "";
     for(var i = 0; i < links.length; i++) {
         var res = null;

         // CI9
         if (links[i].className.indexOf("GS_CI9_") > -1){
             res = registerEventHandler(links[i],"click", customInsight9ClickEventHandler);
             //debugLinks += "\n" + "GS_CI9_\t" + links[i].className + "\t";
         }

         // eVar 5
         if (links[i].className.indexOf("GS_EV5_") > -1){
             res = registerEventHandler(links[i],"click", evar5ClickEventHandler);
             //debugLinks += "\n" +"GS_EV5_\t" + links[i].className + " " + res + "\t";
         }

     }
     //alert(debugLinks);
 }


/*
 * create an instance of ClickCapture
 */
var clickCapture = new ClickCapture();










