package gs.web.hub;

import gs.web.util.list.Anchor;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 10/1/13
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class EduCommunityModel {

    private String  _heading;

    private String   _description;

    private String _logoLocation;

    private ArrayList<Anchor> _links;

    public EduCommunityModel(final String heading, final String description, final String logoLocation, final ArrayList<Anchor> links){
               _heading= heading;
               _description= description;
               _logoLocation= logoLocation;
               _links=   links;
    }

    public String getHeading() {
        return _heading;
    }

    public void setHeading(final String heading) {
        this._heading = heading;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        this._description = description;
    }

    public String getLogoLocation() {
        return _logoLocation;
    }

    public void setLogoLocation(final String logoLocation) {
        this._logoLocation = logoLocation;
    }

    public ArrayList<Anchor> getLinks() {
        return _links;
    }

    public void setLinks(final ArrayList<Anchor> links) {
        this._links = links;
    }
}
