package gs.web.search;

import gs.data.geo.ILocation;
import gs.data.geo.LatLon;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.data.util.Address;

import java.util.List;
import java.util.Map;

public interface ISchoolSearchResult extends ILocation {

    public Integer getId();

    public State getDatabaseState();

    public String getName();

    public Address getAddress();

    public String getPhone();

    public LatLon getLatLon();

    public LevelCode getLevelCode();

    public SchoolType getSchoolType();

    public Integer getGreatSchoolsRating();

    public Integer getParentRating();

    public Map<String,Object> toMap();

}
