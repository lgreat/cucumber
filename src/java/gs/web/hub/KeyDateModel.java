package gs.web.hub;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 9/30/13
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class KeyDateModel {

    private String  _date;

    private String   _description;

    public KeyDateModel(final String date , final String description){
        _date= date;
        _description= description;
    }


    public String getDate() {
        return _date;
    }

    public void setDate(final String date) {
        this._date = date;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        this._description = description;
    }

}
