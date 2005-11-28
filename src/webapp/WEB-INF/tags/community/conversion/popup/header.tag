<jsp:directive.tag body-content="scriptless"/>
<jsp:directive.attribute name="title" required="true"/>
<table id="header" width="100%">
<tr>
 <td class="header" valign="bottom"> <img src="/res/img/community/conversion/popup/insider_popup_title.gif" width="200" height="20" border="0">
     <hr noshade size="1" width="225" align="left"/>${title}<br/><br/>
     <span class="orangeheader">
         <jsp:doBody/>
     </span>
 </td>
 <td class="header" valign="bottom" align="right">
     <img src="/res/img/community/conversion/popup/gs_bug.gif" alt="GreatSchools.net" width="223" height="73" hspace="5" border="0"/>
 </td>
</tr>
</table>