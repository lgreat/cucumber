<%@ taglib
    uri="http://www.opensymphony.com/sitemesh/decorator"
    prefix="decorator" %>
<html>
  <head>
    <title>
      My Site -
      <decorator:title default="Welcome!" />
    </title>
    <base href="http://localhost:8080/gs-web/" />
    <link rel="stylesheet" href="css/gs.css">
    <decorator:head />
  </head>
  <body>
    <table>
      <tr>
        <td>
              		<table cellspacing="0" cellpadding="0" width="750" align="left" valign="top" border="0">
              			<tr>
              				<td width="750" height="80" align="left" valign="top"><img src="images/header_logo.gif" width="201" height="80" border="0" alt="GreatSchools.net"><img src="images/pixel.gif" width="304" height="80" border="0" alt="">
              				</td>
              			</tr>

              			<tr>
              				<td bgcolor="#003399" width="750" height="19" align="left" valign="top"><img src="images/pixel.gif" width=1 height=19 border=0 alt=""></td>
              			</tr>
              		</table>

        </td>
      </tr>
      <tr>
        <td><decorator:body /></td>
      </tr>
      <tr>
        <td>
<h5 class="ncopy">GreatSchools.net
Elementary, middle and high school information for public, private and charter schools nationwide.</h5>

	&#169;1998-2005 GreatSchools Inc. All Rights Reserved. <a href="/">Home</a> | <a href="/cgi-bin/static/terms.html/ca">Terms of Use</a><br>
	<img src="images/pixel.gif" width="1" height="24" border="0" alt=""><br>
</td>
      </tr>
    </table>
  </body>
</html>

