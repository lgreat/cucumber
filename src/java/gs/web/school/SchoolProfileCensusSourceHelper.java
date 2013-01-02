package gs.web.school;

import gs.data.school.census.CensusDescription;
import gs.data.state.State;
import gs.data.util.Pair;
import java.io.Serializable;

import java.util.*;

/**
 * @author aroy@greatschools.org
 */
public class SchoolProfileCensusSourceHelper implements Serializable {
    private final static CensusDescription MANUAL_OVERRIDE =
            new CensusDescription(-1, State.CA, -1, "Manually entered by school official",
                    "Manually entered by school official", null);
    private Map<String, Integer> _sourceKeyToIndexMap = new HashMap<String, Integer>();
    private List<Pair<CensusDescription, Integer>> _sources = new ArrayList<Pair<CensusDescription, Integer>>();

    public List<Pair<CensusDescription, Integer>> getCensusSources() {
        return _sources;
    }

    public int recordSource(SchoolProfileStatsDisplayRow row) {
        String key = getKey(row);
        if (key == null) {
            return 0;
        }
        if (_sourceKeyToIndexMap.get(key) == null) {
            _sourceKeyToIndexMap.put(key, _sourceKeyToIndexMap.size() + 1);
            if (row.isManualOverride()) {
                _sources.add(new Pair<CensusDescription, Integer>(MANUAL_OVERRIDE, null));
            } else {
                _sources.add(new Pair<CensusDescription, Integer>(getSourceFromRow(row), row.getYear()));
            }

        }
        if (_sources.size() < 2) {
            return 0;
        }
        return _sourceKeyToIndexMap.get(key);
    }

    public void clear() {
        _sourceKeyToIndexMap.clear();
        _sources.clear();
    }

    public static String getKey(SchoolProfileStatsDisplayRow row) {
        if (row.isManualOverride()) {
            return "Manually entered by school official";
        }
        else {
            CensusDescription source = getSourceFromRow(row);
            if (source == null) {
                return null;
            }
            return source.getSource() + row.getYear();
        }
    }

    public static CensusDescription getSourceFromRow(SchoolProfileStatsDisplayRow row) {
        Set<CensusDescription> sources = row.getCensusDescriptions();
        if (sources != null && !sources.isEmpty()) {
            return sources.iterator().next();
        }
        return null;
    }
}
