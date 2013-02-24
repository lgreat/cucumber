package gs.web.school;

import org.apache.commons.lang.StringUtils;

import java.util.*;


public interface IGroupOfStudentTeacherViewRows  {


    public Long getGroupId();

    public CensusGroup getCensusGroup();

    public Map<String, String> getSchoolValueMap();

}
