package gs.web.util;

import gs.data.state.State;
import gs.data.school.SchoolType;
import gs.data.school.LevelCode;
import gs.web.school.SchoolsController;
import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Sep 24, 2008
 * Time: 5:05:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryStructureUrlFactory {
    public static String createNewCityBrowseURIRoot(State state, String cityName) {
        if (state == null || StringUtils.isBlank(cityName)) {
            throw new IllegalArgumentException("Must specify state and city");
        }
        String stateNameForUrl = state.getLongName().toLowerCase().replaceAll(" ", "-");
        String cityNameForUrl = cityName.toLowerCase().replaceAll("-", "_").replaceAll(" ", "-");

        return "/" + stateNameForUrl + "/" + cityNameForUrl + "/";
    }

    public static String createNewCityBrowseURISchoolTypeLabel(Set<SchoolType> schoolTypes) {
        if (schoolTypes == null || (schoolTypes.size() > 3)) {
            throw new IllegalArgumentException("Must specify a set of no more than 3 school types");
        }

        StringBuilder label = new StringBuilder();

        SchoolType firstType = null;
        SchoolType secondType = null;
        if (schoolTypes.size() == 1) {
            firstType = schoolTypes.toArray(new SchoolType[schoolTypes.size()])[0];
        } else if (schoolTypes.size() == 2) {
            if (schoolTypes.contains(SchoolType.PUBLIC)) {
                firstType = SchoolType.PUBLIC;
            } else if (schoolTypes.contains(SchoolType.PRIVATE)) {
                firstType = SchoolType.PRIVATE;
            } else if (schoolTypes.contains(SchoolType.CHARTER)) {
                firstType = SchoolType.CHARTER;
            }

            Set otherSchoolTypes = new HashSet<SchoolType>(schoolTypes);
            otherSchoolTypes.remove(firstType);

            if (otherSchoolTypes.contains(SchoolType.PUBLIC)) {
                // this should never happen
                secondType = SchoolType.PUBLIC;
            } else if (otherSchoolTypes.contains(SchoolType.PRIVATE)) {
                secondType = SchoolType.PRIVATE;
            } else if (otherSchoolTypes.contains(SchoolType.CHARTER)) {
                secondType = SchoolType.CHARTER;
            }
        }

        if (firstType != null) {
            label.append(firstType.getSchoolTypeName());
        }
        if (secondType != null) {
            label.append("-");
            label.append(secondType.getSchoolTypeName());
        }

        return label.toString();
    }

    public static String createNewCityBrowseURI(State state, String cityName, Set<SchoolType> schoolTypes, LevelCode levelCode) {
        if (state == null || StringUtils.isBlank(cityName) || (schoolTypes == null || schoolTypes.size() > 3)) {
            StringBuilder s = new StringBuilder("Must specify state, city, level code, and a set of no more than 3 school types. Provided: ");
            s.append("state = ");
            s.append(state != null ? state.getAbbreviation() : "null");
            s.append(", city = " + cityName);
            s.append(", schoolTypes = ");
            if (schoolTypes == null) {
                s.append("null");
            } else {
                s.append("{");
                SchoolType[] types = schoolTypes.toArray(new SchoolType[]{});
                for (int i = 0; i < types.length; i++) {
                    if (i > 0) {
                        s.append(",");
                    }
                    s.append(types[i].getSchoolTypeName());
                }
                s.append("}");
            }

            s.append(", levelCode = ");
            s.append(levelCode != null ? levelCode.getCommaSeparatedString() : "null");

            throw new IllegalArgumentException(s.toString());
        }

        StringBuilder url = new StringBuilder(createNewCityBrowseURIRoot(state, cityName));

        String schoolTypeLabel = createNewCityBrowseURISchoolTypeLabel(schoolTypes);
        if (!StringUtils.isBlank(schoolTypeLabel)) {
            url.append(schoolTypeLabel);
            url.append("/");
        }

        url.append(createNewCityBrowseURILevelLabel(levelCode));
        url.append("/");

        return url.toString();
    }

    public static String createNewCityBrowseURILevelLabel(LevelCode levelCode) {
        if (LevelCode.PRESCHOOL.equals(levelCode)) {
            return SchoolsController.LEVEL_LABEL_PRESCHOOLS;
        } else if (LevelCode.ELEMENTARY.equals(levelCode)) {
            return SchoolsController.LEVEL_LABEL_ELEMENTARY_SCHOOLS;
        } else if (LevelCode.MIDDLE.equals(levelCode)) {
            return SchoolsController.LEVEL_LABEL_MIDDLE_SCHOOLS;
        } else if (LevelCode.HIGH.equals(levelCode)) {
            return SchoolsController.LEVEL_LABEL_HIGH_SCHOOLS;
        } else {
            // all others not supported
            return SchoolsController.LEVEL_LABEL_SCHOOLS;
        }
    }
}
