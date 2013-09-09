package gs.web.hub;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 9/6/13
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Model for Feature on Steps on  City Hub Choose  Pages.
 * @author sarora@greatschools.org Shomi Arora.
 */
public class FeaturedResourcesModel {

    private String  _label;

    private String  _url;

    private String   _description;

    private String   _type;


    public FeaturedResourcesModel(final String label, final String url,  final String description, final String type){
            this._label= label;
            this._url= url;
            this._type= type;
            this._description= description;

    }


    public FeaturedResourcesModel(final String label, final String url, final String type){
        this._label= label;
        this._url= url;
        this._type= type;


    }


    public String getLabel() {
        return _label;
    }

    public void setLabel(final String label) {
        this._label = label;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(final String url) {
        this._url = url;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        this._description = description;
    }


    public String getType() {
        return _type;
    }

    public void setType(final String type) {
        this._type = type;
    }
}
