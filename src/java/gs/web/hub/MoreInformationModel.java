package gs.web.hub;

import gs.web.util.list.Anchor;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 9/30/13
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoreInformationModel {

    private String _address;

    private ArrayList<Anchor> _link;

    public MoreInformationModel(final String address){
         _address = address;

    }


    public MoreInformationModel(final ArrayList<Anchor> link){
         _link= link;

    }

    public MoreInformationModel(final String address, final ArrayList<Anchor> link){
        _address = address;
        _link= link;

    }
}
