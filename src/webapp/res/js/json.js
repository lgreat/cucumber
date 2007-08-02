
if(!Object.prototype.toJSONString){(function(s){var m={'\b':'\\b','\t':'\\t','\n':'\\n','\f':'\\f','\r':'\\r','"':'\\"','\\':'\\\\'};s.parseJSON=function(filter){var j;function walk(k,v){var i;if(v&&typeof v==='object'){for(i in v){if(v.hasOwnProperty(i)){v[i]=walk(i,v[i]);}}}
return filter(k,v);}
if(/^[,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]*$/.test(this.replace(/\\./g,'@').replace(/"[^"\\\n\r]*"/g,''))){j=eval('('+this+')');return typeof filter==='function'?walk('',j):j;}
throw new SyntaxError('parseGSJSON');};s.toJSONString=function(){if(/["\\\x00-\x1f]/.test(this)){return'"'+this.replace(/([\x00-\x1f\\"])/g,function(a,b){var c=m[b];if(c){return c;}
c=b.charCodeAt();return'\\u00'+
Math.floor(c/16).toString(16)+
(c%16).toString(16);})+'"';}
return'"'+this+'"';};})(String.prototype);}