<jsp:directive.tag body-content="empty"/>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!sessionScope.context.adFree}">
    <div id="googleAds" class="ad">
        <script type="text/javascript">
            <!--
            google_ad_client = "pub-9662012843341888";
            google_ad_width = 120;
            google_ad_height = 600;
            google_ad_format = "120x600_as";
            google_color_border = "B4D0DC";
            google_color_bg = "ECF8FF";
            google_color_link = "0000CC";
            google_color_url = "008000";
            google_color_text = "6F6F6F";
            //-->
        </script>
        <script type="text/javascript"
                src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
        </script>
    </div>
</c:if>