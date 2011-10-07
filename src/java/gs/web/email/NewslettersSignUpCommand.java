package gs.web.email;

import gs.data.school.School;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class NewslettersSignUpCommand {
    private String _email;
    private int userId;

    private boolean dailytip;
    private int dailytipId;

    //checkbox value representing if it is checked
    private boolean greatnews;
    private int greatnewsId;


    //variables to add a My School Stats
    private State stateAdd;
    private List cityList;
    private String city;
    private int school;
    private String schoolName;
    private List<School> currentMySchoolStats;
    private List myStatIds;

    //checkbox values representing a school is checked for My School  Stats
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


    //variable for sponsors
    private boolean sponsor;
    private int sponsorId;

    private boolean tooManySchoolsError = false;

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        if (StringUtils.isNotBlank(email)) {
            _email = email.trim();
        } else {
            _email = null;
        }
    }

     public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

     public boolean isDailytip() {
        return dailytip;
    }

    public void setDailytip(boolean dailytip) {
        dailytip = dailytip;
    }

    public int getDailytipId() {
        return dailytipId;
    }

    public void setDailytipId(int dailytipId) {
        dailytipId = dailytipId;
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

    public boolean isTooManySchoolsError() {
        return tooManySchoolsError;
    }

    public void setTooManySchoolsError(boolean tooManySchoolsError) {
        this.tooManySchoolsError = tooManySchoolsError;
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
}