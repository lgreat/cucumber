package gs.web.search;

import gs.data.geo.ILocation;
import gs.data.geo.LatLon;
import gs.data.state.State;
import gs.data.util.Address;

public interface ISchoolSearchResult extends ILocation {

    public Integer getId();

    public State getDatabaseState();

    public String getName();

    public Address getAddress();

    public String getPhone();

    public LatLon getLatLon();

    public String getLevelCode();

    public String getSchoolType();

    public Integer getGreatSchoolsRating();

    public Integer getParentRating();

}
