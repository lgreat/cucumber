package gs.web.hub;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 9/9/13
 * Time: 9:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class AdditionalResourcesModel {
    private String  _label;

    private String  _url;

    private String  _type;

    private String   _description;

    private String   _columnHeading;

    private String   _columnNo;





    public AdditionalResourcesModel(final String label , final String url , final String type, final String columnNo, final String columnHeading){
        this._label= label;
        this._url= url;
        this._type= type;
        this._columnHeading= columnHeading;
        this._columnNo= columnNo;

    }



    public AdditionalResourcesModel(final String label , final String url, final String type, final String columnNo){
        this._label= label;
        this._url= url;
        this._type= type;
        this._columnNo= columnNo;


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

    public String getType() {
        return _type;
    }

    public void setType(final String type) {
        this._type = type;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        this._description = description;
    }


    public String getColumnHeading() {
        return _columnHeading;
    }

    public void setColumnHeading(final String columnHeading) {
        this._columnHeading = columnHeading;
    }

    public String getColumnNo() {
        return _columnNo;
    }

    public void setColumnNo(final String columnNo) {
        this._columnNo = columnNo;
    }
}
