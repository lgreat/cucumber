package gs.web.hub;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 9/30/13
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */

import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.web.util.list.Anchor;

import java.util.ArrayList;

/**
 * Model for Steps on  City Hub Enrollment   Pages.
 * @author sarora@greatschools.org Shomi Arora.
 */
public class EnrollmentModel {


    private String  _tabName;

    private LevelCode   _levelCode;

    private SchoolType _schoolType;

    private String  _schoolTypeDescription;

    private String   _description;

    private ArrayList<String> _tipsInfoModel;

    private Anchor  _browseLink;



    private MoreInformationModel _moreInfo;




    public EnrollmentModel(final String  tabName, final LevelCode levelCode, final SchoolType schoolType , final String  schoolTypeDescription, final String  description,final Anchor browseLinkInfo) {
        _tabName = tabName;
        _levelCode= levelCode;
        _schoolType= schoolType;
        _schoolTypeDescription= schoolTypeDescription;
        _description= description;
        _browseLink= browseLinkInfo;

    }

    public EnrollmentModel(final String  tabName, final LevelCode levelCode, final SchoolType schoolType , final String  description, final Anchor browseLinkInfo) {
        _tabName = tabName;
        _levelCode= levelCode;
        _schoolType= schoolType;
        _description= description;
        _browseLink= browseLinkInfo;

    }

    public EnrollmentModel(final String  tabName, final LevelCode levelCode, final SchoolType schoolType , final String  description) {
        _tabName = tabName;
        _levelCode= levelCode;
        _schoolType= schoolType;
        _schoolTypeDescription= description;


    }




    public String getTabName() {
        return _tabName;
    }

    public void setTabName(final String tabName) {
        this._tabName = tabName;
    }

    public LevelCode getLevelCode() {
        return _levelCode;
    }

    public void setLevelCode(LevelCode _levelCode) {
        this._levelCode = _levelCode;
    }

    public SchoolType getSchoolType() {
        return _schoolType;
    }

    public void setSchoolType(SchoolType _schoolType) {
        this._schoolType = _schoolType;
    }

    public String getSchoolTypeDescription() {
        return _schoolTypeDescription;
    }

    public void setSchoolTypeDescription(final String schoolTypeDescription) {
        this._schoolTypeDescription = schoolTypeDescription;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        this._description = description;
    }



    public ArrayList<String> getTipsInfoModel() {
        return _tipsInfoModel;
    }

    public void setTipsInfoModel(ArrayList<String> tipsInfoModel) {
        this._tipsInfoModel = tipsInfoModel;
    }

    public MoreInformationModel getMoreInfo() {
        return _moreInfo;
    }

    public void setMoreInfo(MoreInformationModel moreInfo) {
        this._moreInfo = moreInfo;
    }

    public Anchor getBrowseLink() {
        return _browseLink;
    }

    public void setBrowseLink(Anchor browseLink) {
        this._browseLink = browseLink;
    }

}
