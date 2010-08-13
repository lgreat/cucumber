package gs.web.email;

import gs.data.geo.City;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.school.School;
import gs.data.community.Student;
import gs.data.community.SubscriptionProduct;

import java.util.List;

/**
 * Command backing the email management form.
 */
public class ManagementCommand {
    protected final Log _log = LogFactory.getLog(getClass());

    private String _unsubPostTitle;
    private boolean _unsubCommunityNotificationsForAllPosts = false;

    private StateManager stateManager;
    private int userId;
    private String email;

    private String firstName;

    // your location
    private State _userState;
    private String _userCity;
    private List<City> _userCityList;

    //checkbox value representing if it is checked
    private boolean greatnews;
    private int greatnewsId;
    //hidden value representing if they had it already
    private boolean hasGreatNews;

    //checkbox values representing if they are checked
    private boolean mypk;
    private boolean myk;
    private boolean my1;
    private boolean my2;
    private boolean my3;
    private boolean my4;
    private boolean my5;
    private boolean myms;
    private boolean myhs;

    //id of row to be used if a my nth grader needs to be deleted
    private int mypkId;
    private int mykId;
    private int my1Id;
    private int my2Id;
    private int my3Id;
    private int my4Id;
    private int my5Id;
    private int mymsId;
    private int myhsId;

    //this may not be used
    private List<Student> myNthList;

    //checkbox values representing a school is checked
    private boolean school1;
    private boolean school2;
    private boolean school3;
    private boolean school4;

    //name of school in My School Stats for use when giving a delete message on success view
    private String name1;
    private String name2;
    private String name3;
    private String name4;

    //id of row to be used if we need to delete the school in My School Stats
    private int id1;
    private int id2;
    private int id3;
    private int id4;

    //variables to add a My School Stats
    private State stateAdd;
    private List cityList;
    private String city;
    private int school;
    private String schoolName;
    private List<School> currentMySchoolStats;
    private List myStatIds;

    //variables used for seasonal newsletter
    private String startweek;
    private int seasonalId;
    private boolean seasonal;

    //variable for learning disabilities
    private boolean learning_dis;
    private int learning_disId;

    //variables for chooser pack
    private boolean chooser;
    private boolean chooserpack_p;
    private boolean chooserpack_e;
    private boolean chooserpack_m;
    private boolean chooserpack_h;

    private int chooserpack_pId;
    private int chooserpack_eId;
    private int chooserpack_mId;
    private int chooserpack_hId;

    //variable for sponsors
    private boolean sponsor;
    private int sponsorId;

    //variable for pledge
    private boolean pledge;
    private int pledgeId;

    //variables for bts tips
    private boolean btsTip;
    private String btsTipVersion;
    private int btsTip_eId;
    private int btsTip_mId;
    private int btsTip_hId;

    private boolean tooManySchoolsError = false;

    //variable for community posts
    private boolean _repliesToCommunityPosts;

    public boolean isRepliesToCommunityPosts() {
        return _repliesToCommunityPosts;
    }

    public void setRepliesToCommunityPosts(boolean repliesToCommunityPosts) {
        this._repliesToCommunityPosts = repliesToCommunityPosts;
    }

    public boolean isTooManySchoolsError() {
        return tooManySchoolsError;
    }

    public void setTooManySchoolsError(boolean tooManySchoolsError) {
        this.tooManySchoolsError = tooManySchoolsError;
    }

    public StateManager getStateManager() {
        return stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getGreatnews() {
        return greatnews;
    }

    public void setGreatnews(boolean greatnews) {
        this.greatnews = greatnews;
    }

    public int getGreatnewsId() {
        return greatnewsId;
    }

    public void setGreatnewsId(int greatnewsId) {
        this.greatnewsId = greatnewsId;
    }

    public boolean isSchool1() {
        return school1;
    }

    public void setSchool1(boolean school1) {
        this.school1 = school1;
    }

    public boolean isSchool2() {
        return school2;
    }

    public void setSchool2(boolean school2) {
        this.school2 = school2;
    }

    public boolean isSchool3() {
        return school3;
    }

    public void setSchool3(boolean school3) {
        this.school3 = school3;
    }

    public boolean isSchool4() {
        return school4;
    }

    public void setSchool4(boolean school4) {
        this.school4 = school4;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
    }

    public String getName4() {
        return name4;
    }

    public void setName4(String name4) {
        this.name4 = name4;
    }

    public int getId1() {
        return id1;
    }

    public void setId1(int id1) {
        this.id1 = id1;
    }

    public int getId2() {
        return id2;
    }

    public void setId2(int id2) {
        this.id2 = id2;
    }

    public int getId3() {
        return id3;
    }

    public void setId3(int id3) {
        this.id3 = id3;
    }

    public int getId4() {
        return id4;
    }

    public void setId4(int id4) {
        this.id4 = id4;
    }

    public boolean isMypk() {
        return mypk;
    }

    public void setMypk(boolean mypk) {
        this.mypk = mypk;
    }

    public boolean isMyk() {
        return myk;
    }

    public void setMyk(boolean myk) {
        this.myk = myk;
    }

    public boolean isMy1() {
        return my1;
    }

    public void setMy1(boolean my1) {
        this.my1 = my1;
    }

    public boolean isMy2() {
        return my2;
    }

    public void setMy2(boolean my2) {
        this.my2 = my2;
    }

    public boolean isMy3() {
        return my3;
    }

    public void setMy3(boolean my3) {
        this.my3 = my3;
    }

    public boolean isMy4() {
        return my4;
    }

    public void setMy4(boolean my4) {
        this.my4 = my4;
    }

    public boolean isMy5() {
        return my5;
    }

    public void setMy5(boolean my5) {
        this.my5 = my5;
    }

    public boolean isMyms() {
        return myms;
    }

    public void setMyms(boolean myms) {
        this.myms = myms;
    }

    public boolean isMyhs() {
        return myhs;
    }

    public void setMyhs(boolean myhs) {
        this.myhs = myhs;
    }

    public boolean isHasGreatNews() {
        return hasGreatNews;
    }

    public void setHasGreatNews(boolean hasGreatNews) {
        this.hasGreatNews = hasGreatNews;
    }

    public int getMypkId() {
        return mypkId;
    }

    public void setMypkId(int mypkId) {
        this.mypkId = mypkId;
    }

    public int getMykId() {
        return mykId;
    }

    public void setMykId(int mykId) {
        this.mykId = mykId;
    }

    public int getMy1Id() {
        return my1Id;
    }

    public void setMy1Id(int my1Id) {
        this.my1Id = my1Id;
    }

    public int getMy2Id() {
        return my2Id;
    }

    public void setMy2Id(int my2Id) {
        this.my2Id = my2Id;
    }

    public int getMy3Id() {
        return my3Id;
    }

    public void setMy3Id(int my3Id) {
        this.my3Id = my3Id;
    }

    public int getMy4Id() {
        return my4Id;
    }

    public void setMy4Id(int my4Id) {
        this.my4Id = my4Id;
    }

    public int getMy5Id() {
        return my5Id;
    }

    public void setMy5Id(int my5Id) {
        this.my5Id = my5Id;
    }

    public int getMymsId() {
        return mymsId;
    }

    public void setMymsId(int mymsId) {
        this.mymsId = mymsId;
    }

    public int getMyhsId() {
        return myhsId;
    }

    public void setMyhsId(int myhsId) {
        this.myhsId = myhsId;
    }

    public List<Student> getMyNthList() {
        return myNthList;
    }

    public void setMyNthList(List<Student> myNthList) {
        this.myNthList = myNthList;
    }

    public State getStateAdd() {
        return stateAdd;
    }

    public void setStateAdd(State stateAdd) {
        this.stateAdd = stateAdd;
    }

    public List getCityList() {
        return cityList;
    }

    public void setCityList(List cityList) {
        this.cityList = cityList;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getSchool() {
        return school;
    }

    public void setSchool(int school) {
        this.school = school;
    }

    public List<School> getCurrentMySchoolStats() {
        return currentMySchoolStats;
    }

    public void setCurrentMySchoolStats(List<School> currentMySchoolStats) {
        this.currentMySchoolStats = currentMySchoolStats;
    }

    public List getMyStatIds() {
        return myStatIds;
    }

    public void setMyStatIds(List myStatIds) {
        this.myStatIds = myStatIds;
    }

    public String getStartweek() {
        return startweek;
    }

    public void setStartweek(String startweek) {
        this.startweek = startweek;
    }

    public int getSeasonalId() {
        return seasonalId;
    }

    public void setSeasonalId(int seasonalId) {
        this.seasonalId = seasonalId;
    }
    
    public boolean isSeasonal() {
        return seasonal;
    }

    public void setSeasonal(boolean seasonal) {
        this.seasonal = seasonal;
    }

    public boolean isLearning_dis() {
        return learning_dis;
    }

    public void setLearning_dis(boolean learning_dis) {
        this.learning_dis = learning_dis;
    }

    public int getLearning_disId() {
        return learning_disId;
    }

    public void setLearning_disId(int learning_disId) {
        this.learning_disId = learning_disId;
    }

    public boolean isChooser() {
        return chooser;
    }

    public void setChooser(boolean chooser) {
        this.chooser = chooser;
    }

    public boolean isChooserpack_p() {
        return chooserpack_p;
    }

    public void setChooserpack_p(boolean chooserpack_p) {
        this.chooserpack_p = chooserpack_p;
    }

    public boolean isChooserpack_e() {
        return chooserpack_e;
    }

    public void setChooserpack_e(boolean chooserpack_e) {
        this.chooserpack_e = chooserpack_e;
    }

    public boolean isChooserpack_m() {
        return chooserpack_m;
    }

    public void setChooserpack_m(boolean chooserpack_m) {
        this.chooserpack_m = chooserpack_m;
    }

    public boolean isChooserpack_h() {
        return chooserpack_h;
    }

    public void setChooserpack_h(boolean chooserpack_h) {
        this.chooserpack_h = chooserpack_h;
    }

    public int getChooserpack_pId() {
        return chooserpack_pId;
    }

    public void setChooserpack_pId(int chooserpack_pId) {
        this.chooserpack_pId = chooserpack_pId;
    }

    public int getChooserpack_eId() {
        return chooserpack_eId;
    }

    public void setChooserpack_eId(int chooserpack_eId) {
        this.chooserpack_eId = chooserpack_eId;
    }

    public int getChooserpack_mId() {
        return chooserpack_mId;
    }

    public void setChooserpack_mId(int chooserpack_mId) {
        this.chooserpack_mId = chooserpack_mId;
    }

    public int getChooserpack_hId() {
        return chooserpack_hId;
    }

    public void setChooserpack_hId(int chooserpack_hId) {
        this.chooserpack_hId = chooserpack_hId;
    }

    public boolean isSponsor() {
        return sponsor;
    }

    public void setSponsor(boolean sponsor) {
        this.sponsor = sponsor;
    }

    public int getSponsorId() {
        return sponsorId;
    }

    public void setSponsorId(int sponsorId) {
        this.sponsorId = sponsorId;
    }

    public boolean isPledge() {
        return pledge;
    }

    public void setPledge(boolean pledge) {
        this.pledge = pledge;
    }

    public int getPledgeId() {
        return pledgeId;
    }

    public void setPledgeId(int pledgeId) {
        this.pledgeId = pledgeId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public State getUserState() {
        return _userState;
    }

    public void setUserState(State userState) {
        _userState = userState;
    }

    public String getUserCity() {
        return _userCity;
    }

    public void setUserCity(String userCity) {
        _userCity = userCity;
    }

    public List<City> getUserCityList() {
        return _userCityList;
    }

    public void setUserCityList(List<City> userCityList) {
        _userCityList = userCityList;
    }

    public List<State> getStateList() {
        stateManager = new StateManager();
        return stateManager.getListByAbbreviations();
    }

    public boolean isNthChecked() {
        return mypk || myk || my1 || my2 || my3 || my4 || my5 || myms || myhs;
    }

    public boolean checkedBox(SubscriptionProduct myNth){
        if(myNth.equals(SubscriptionProduct.MY_PRESCHOOLER)){
            return mypk;
        }
        if(myNth.equals(SubscriptionProduct.MY_KINDERGARTNER)){
            return myk;
        }
        if(myNth.equals(SubscriptionProduct.MY_FIRST_GRADER)){
            return my1;
        }
        if(myNth.equals(SubscriptionProduct.MY_SECOND_GRADER)){
            return my2;
        }
        if(myNth.equals(SubscriptionProduct.MY_THIRD_GRADER)){
            return my3;
        }
        if(myNth.equals(SubscriptionProduct.MY_FOURTH_GRADER)){
            return my4;
        }
        if(myNth.equals(SubscriptionProduct.MY_FIFTH_GRADER)){
            return my5;
        }
        if(myNth.equals(SubscriptionProduct.MY_MS)){
            return myms;
        }
        if(myNth.equals(SubscriptionProduct.MY_HS)){
            return myhs;
        }
        return false;
    }

    public int getMyNthId(SubscriptionProduct myNth){
        if(myNth.equals(SubscriptionProduct.MY_PRESCHOOLER)){
            return mypkId;
        }
        if(myNth.equals(SubscriptionProduct.MY_KINDERGARTNER)){
            return mykId;
        }
        if(myNth.equals(SubscriptionProduct.MY_FIRST_GRADER)){
            return my1Id;
        }
        if(myNth.equals(SubscriptionProduct.MY_SECOND_GRADER)){
            return my2Id;
        }
        if(myNth.equals(SubscriptionProduct.MY_THIRD_GRADER)){
            return my3Id;
        }
        if(myNth.equals(SubscriptionProduct.MY_FOURTH_GRADER)){
            return my4Id;
        }
        if(myNth.equals(SubscriptionProduct.MY_FIFTH_GRADER)){
            return my5Id;
        }
        if(myNth.equals(SubscriptionProduct.MY_MS)){
            return mymsId;
        }
        if(myNth.equals(SubscriptionProduct.MY_HS)){
            return myhsId;
        }
        return 0;
    }

    public boolean isBtsTip() {
        return btsTip;
    }

    public void setBtsTip(boolean btsTip) {
        this.btsTip = btsTip;
    }

    public String getBtsTipVersion() {
        return btsTipVersion;
    }

    public void setBtsTipVersion(String btsTipVersion) {
        this.btsTipVersion = btsTipVersion;
    }

    public int getBtsTip_eId() {
        return btsTip_eId;
    }

    public void setBtsTip_eId(int btsTip_eId) {
        this.btsTip_eId = btsTip_eId;
    }

    public int getBtsTip_mId() {
        return btsTip_mId;
    }

    public void setBtsTip_mId(int btsTip_mId) {
        this.btsTip_mId = btsTip_mId;
    }

    public int getBtsTip_hId() {
        return btsTip_hId;
    }

    public void setBtsTip_hId(int btsTip_hId) {
        this.btsTip_hId = btsTip_hId;
    }

    public String getUnsubPostTitle() {
        return _unsubPostTitle;
    }

    public void setUnsubPostTitle(String unsubPostTitle) {
        _unsubPostTitle = unsubPostTitle;
    }

    public boolean isUnsubCommunityNotificationsForAllPosts() {
        return _unsubCommunityNotificationsForAllPosts;
    }

    public void setUnsubCommunityNotificationsForAllPosts(boolean unsubCommunityNotificationsForAllPosts) {
        _unsubCommunityNotificationsForAllPosts = unsubCommunityNotificationsForAllPosts;
    }
}
