package gs.web.authorization;


import gs.data.util.CmsUtil;
import gs.web.util.CookieUtil;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

public class Facebook {

    public static final String SIGNED_REQUEST_PARAM = "fbSignedRequest";

    public static FacebookRequestData getRequestData(HttpServletRequest request) {
        FacebookRequestData facebookRequestData = new FacebookRequestData();
        if (request.getParameter(SIGNED_REQUEST_PARAM) != null) {
            facebookRequestData = parseSignedRequest(request.getParameter(SIGNED_REQUEST_PARAM));
        }
        return facebookRequestData;
    }

    public static FacebookRequestData parseSignedRequest(String signedRequest) {
        boolean valid = false;

        String facebookSecretKey = CmsUtil.getFacebookSecret();
        String facebookAppId = CmsUtil.getFacebookAppId();
        FacebookRequestData requestData = new FacebookRequestData();

        try {
            //parse signed_request

            //it is important to enable url-safe mode for Base64 encoder
            Base64 base64 = new Base64();

            //split request into signature and data
            String[] signedRequestParts = signedRequest.split("\\.", 2);

            //parse signature
            String sig =  decode(signedRequestParts[0]);
            //parse data and convert to json object
            String jsonData = decode(signedRequestParts[1]);

            // For some reason, need to add end curly bracket. Supposedly upgrading to commons-codec 1.6 will solve this problem
            JSONObject data = (JSONObject) JSONSerializer.toJSON(jsonData.trim() + "}");

            //check signature algorithm
            if(!data.getString("algorithm").equals("HMAC-SHA256")) {
                //unknown algorithm is used
                valid = false;
            }

            //check if data is signed correctly
            if(hmacSHA256(signedRequestParts[1], facebookSecretKey).equals(signedRequestParts[0])) {
                if (data.has("code")) {
                    requestData.setAuthorizationCode(data.getString("code"));
                }
                requestData.setUserId(data.getString("user_id"));
            } else {
                //signature is not correct, possibly the data was tampered with
                valid = false;
            }

        } catch (Exception e) {
            requestData = new FacebookRequestData();
        }

        return requestData;
    }

    public static FacebookRequestData getFacebookDataFromCookie(HttpServletRequest request) {
        FacebookRequestData facebookRequestData = new FacebookRequestData();
        Cookie cookie = CookieUtil.getCookie(request, "fbsr_" + CmsUtil.getFacebookAppId());

        if (cookie != null) {
            String signedRequest = cookie.getValue();
            facebookRequestData = parseSignedRequest(signedRequest);
        }

        return facebookRequestData;
    }

    static private String decode(String data) throws DecoderException, UnsupportedEncodingException {
        String result = new String(Base64.decodeBase64(data.replaceAll("-_", "+/").getBytes("UTF-8")));
        return result;
    }

    //HmacSHA256 implementation
    private static String hmacSHA256(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] hmacData = mac.doFinal(data.getBytes("UTF-8"));
        String result = new String(Base64.encodeBase64(hmacData), "UTF-8");
        result = result.replaceAll("\\+","-").replaceAll("/", "_");
        result = result.substring(0,result.length()-1);
        return result;
    }


}
