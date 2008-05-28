package gs.web.community;

/**
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: May 27, 2008
 * Time: 3:42:42 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ICaptchaCommand {

    public String getChallenge();
    public void setChallenge(String v);
    public String getResponse();
    public void setResponse(String v);
}
