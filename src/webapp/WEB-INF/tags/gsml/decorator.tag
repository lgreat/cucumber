<jsp:directive.tag body-content="scriptless"/>
<jsp:directive.attribute name="tagclass" required="true"/>

<table class="${tagclass}" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td class="TL"></td>
        <td class="T"></td>
        <td class="TR"></td>
    </tr>
    <tr>
        <td class="L"></td>
        <td class="C">

            <jsp:doBody/>

        </td>
        <td class="R"></td>
    </tr>
    <tr>
        <td class="BL"></td>
        <td class="B"></td>
        <td class="BR"></td>
    </tr>
</table>


