<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:c="http://java.sun.com/jsp/jstl/core">
    <c:if test="${!sessionScope.context.adFree}">
        <c:if test='${!sessionScope.context.yahooCobrand}'>
            <div id="googleAds" class="ad">
                <script type="text/javascript">
                    google_ad_client = "pub-9662012843341888";
                    google_ad_width = 728;
                    google_ad_height = 90;
                    google_ad_format = "728x90_as";
                    google_ad_channel = "";
                    google_color_border = "B4D0DC";
                    google_color_bg = "ECF8FF";
                    google_color_link = "0000CC";
                    google_color_url = "008000";
                    google_color_text = "6F6F6F";
                </script>
                <![CDATA[
                <script
                        type="text/javascript"
                        src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
                </script>
                ]]>
            </div>
        </c:if>
    </c:if>
</jsp:root>