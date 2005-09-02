<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib
    uri="http://www.opensymphony.com/sitemesh/decorator"
    prefix="decorator" %>
<html>
  <head>
    <title>
      My Site -
      <decorator:title default="Welcome!" />
    </title>
    <base href="http://localhost:8080/gs-web/" ></base>
<%--        <base href="http://tmauder.dev.greatschools.net/testview/" ></base>--%>
    <link rel="stylesheet" href="res/css/spp2.css"></link>
    <decorator:head />
  </head>
  <body>

 <table id="header" width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td><img src="images/conv_top_sm.jpg" alt="Moving? Choosing a new School? GreatSchools Insider can help." width="380" height="70" /></td>
    <td width="300" align="right" valign="middle"><img src="images/GS_logo.gif" alt="GreatSchools.net logo" hspace="15" border="0" /></td>
  </tr>
</table>

 <div class="wizard"><div class="wizard2"><div class="wizard3"><div class="wizard4"><div class="pad">

<decorator:body />

</div></div></div></div></div>

<h5 class="ncopy">GreatSchools.net
Elementary, middle and high school information for public, private and charter schools nationwide.</h5>
<h6>&#169;1998-2005 GreatSchools Inc. All Rights Reserved. <a href="/">Home</a>
| <a href="http://greatschools.net/cgi-bin/static/terms.html/ca">Terms of Use</a></h6>
    <p>
     <a href="http://validator.w3.org/check?uri=referer"><img
          src="http://www.w3.org/Icons/valid-xhtml10"
          alt="Valid XHTML 1.0!" height="31" width="88"  border="0" /></a> <a href="http://www.springframework.org/"
title="Spring - java/j2ee Application Framework"><img
src="http://www.springframework.org/buttons/spring_80x15.png"
width="80" height="15" border="0" alt="Spring Framework" /></a>
</p>
  </body>
</html>

