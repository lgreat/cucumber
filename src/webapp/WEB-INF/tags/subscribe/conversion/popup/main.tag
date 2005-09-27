<jsp:directive.tag body-content="scriptless"/>
<jsp:directive.attribute name="imageName" required="true"/>
<jsp:directive.attribute name="imageWidth" required="true"/>
<jsp:directive.attribute name="imageHeight" required="true"/>
<table id="main" width="100%">
<tr>
    <td class="copy" valign="top" width="50%"><br/>
        <jsp:doBody/>
    </td>
    <td width="50%">
        <table cellpadding="0" cellspacing="0" border="0" bgcolor="#dbe4e2" width="223">
            <tr>
                <td><img src="/res/img/subscribe/conversion/popup/grey_300top.gif" width="300" height="10" border="0"/></td>
            </tr>
            <tr>
                <td><img src="/res/img/subscribe/conversion/popup/${imageName}" width="${imageWidth}" height="${imageHeight}" border="0"/></td>
            </tr>
            <tr>
                <td><img src="/res/img/subscribe/conversion/popup/grey_300btm.gif" width="300" height="10" border="0"/></td>
            </tr>
        </table>
    </td>
</tr>
</table>