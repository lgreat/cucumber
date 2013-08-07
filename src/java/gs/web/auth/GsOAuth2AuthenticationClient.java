package gs.web.auth;

/*import org.apache.amber.oauth2.client.OAuthClient;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.GrantType;*/

public class GsOAuth2AuthenticationClient {

    private String _authorizationLocation;
    private String _accessTokenLocation;
    private String _clientId;
    private String _redirectUri;
    private String _applicationSecret;

    /*public String getAccessToken(String authorizationCode) throws OAuthSystemException, OAuthProblemException {
        OAuthClientRequest oAuthClientRequest = createAccessTokenRequest(authorizationCode);

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        OAuthJSONAccessTokenResponse tokenResponse = oAuthClient.accessToken(oAuthClientRequest);

        return tokenResponse.getAccessToken();
    }

    public OAuthClientRequest createAccessTokenRequest(String authorizationCode) throws OAuthSystemException {
        OAuthClientRequest request = OAuthClientRequest
            .tokenLocation(_accessTokenLocation)
            .setGrantType(GrantType.AUTHORIZATION_CODE)
            .setClientId(_clientId)
            .setClientSecret(_applicationSecret)
            .setRedirectURI(_redirectUri)
            .setCode(authorizationCode)
            .buildBodyMessage();

        return request;
    }

    public OAuthClientRequest createAuthorizationTokenRequest() throws OAuthSystemException {
        OAuthClientRequest request = OAuthClientRequest
            .authorizationLocation(_authorizationLocation)
            .setClientId(_clientId)
            .setRedirectURI(_redirectUri)
            .buildQueryMessage();
        return request;
    }*/
}
