package gs.web.school;

import gs.data.geo.City;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;

public class SavvySourceHelper {
    public static String getSavvyCityPageUrl(City city) {
        if (city != null) {
            return getSavvyCityPageUrl(city.getState(), city.getName());
        } else {
            throw new IllegalArgumentException("City must not be null");
        }
    }

    // hyphens, spaces, apostrophes
    final private static String SPECIAL_CHARACTERS = "['\\-\\s]";

    public static String getSavvyCityPageUrl(State state, String cityName) {
        if (state == null || StringUtils.isBlank(cityName)) {
            throw new IllegalArgumentException("State and city name must not be null");
        }

        String normalizedCityName =
                cityName.replaceAll(SPECIAL_CHARACTERS, "_").toLowerCase();
        return "http://www.savvysource.com/preschools/c_preschools_in_" +
                normalizedCityName + "_" + state.getAbbreviationLowerCase();
    }

    public static String getSavvyStatePageUrl(State state) {
        if (state == null) {
            throw new IllegalArgumentException("State must not be null");
        }

        String stateLongName;
        if (State.DC.equals(state)) {
            stateLongName = "Washington";
        } else {
            stateLongName = state.getLongName();
        }

        String normalizedStateName =
                stateLongName.replaceAll(SPECIAL_CHARACTERS, "_").toLowerCase();
        return "http://www.savvysource.com/preschools/s_preschools_in_" +
                normalizedStateName + "_" + state.getAbbreviationLowerCase();
    }
}
