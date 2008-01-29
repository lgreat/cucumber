package gs.web.util;

import gs.data.admin.IPropertyDao;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.StringTokenizer;

/**
 *
 * How to configure the A/B variant
 *
 * Common configurations:
 *   A/B 50/50:      1/1
 *   A/B 80/20:      4/1
 *   A/B/C 70/15/15: 14/3/3
 *
 *   TO DISABLE (A ONLY): 1
 *
 * Configuration is set as a ratio as follows
 * Each number represents the ratio of that version occurring out of the sum of all versions.
 * EXAMPLE:
 *   14/3/3
 *   This sums to 20. Version A will occur 14/20 (70%) of the time, version B 3/20 (15%), version C 3/20
 *   Thus 14/3/3 is equivalent to A/B/C 70/15/15
 *
 * RESTRICTIONS:
 *   - Each value must be in the range 1-99
 *   - The sum of all values must be less than or equal to 100
 *   - There can be no more than 26 values
 *   - There can be no fewer than 1 value (1 value DISABLES A/B testing -- users will only see A)
 *
 * @see gs.web.util.CookieInterceptor
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class VariantConfiguration {
    protected static String _lastConfiguration = "";
    protected static int[] _abCutoffs = new int[] {1};
    protected static int _cutoffTotal = 1;
    private static final Log _log = LogFactory.getLog(VariantConfiguration.class);

    /**
     * This converts an AB configuration string as read from file into a static array that can
     * be used elsewhere to determine the AB version. If there are any errors, this sets _abCutoffs
     * to null.
     * @param abConfiguration string in the form a/b/c
     */
    protected static void convertABConfigToArray(String abConfiguration){
        if (StringUtils.isEmpty(abConfiguration)) {
            return;
        }
        try {
            // split on forward slash '/'
            StringTokenizer tok = new StringTokenizer(abConfiguration, "/");

            // Restrict number of tokens to range 1-26
            if (tok.countTokens() > 0 && tok.countTokens() < 27) {
                _cutoffTotal = 0;
                _abCutoffs = new int[tok.countTokens()];
                int tokenNum = 0;
                // place the tokens in the array _abCutoffs and set _cutoffTotal to the sum of the tokens
                while (tok.hasMoreTokens()) {
                    int num = Integer.valueOf(tok.nextToken());
                    // check for invalid values
                    if (num < 1 || num > 99) {
                        _log.error("Invalid value " + num + " found in AB configuration: " + abConfiguration);
                        _abCutoffs = null;
                        break;
                    }
                    _cutoffTotal += num;
                    // check for invalid values
                    if (_cutoffTotal > 100) {
                        _log.error("Invalid period " + _cutoffTotal + " found in AB configuration: " + abConfiguration);
                        _abCutoffs = null;
                        break;
                    }
                    _abCutoffs[tokenNum++] = num;
                }
                // cache this configuration
                _lastConfiguration = abConfiguration;
            } else {
                _log.error("Invalid number of tokens " + tok.countTokens() + " found in AB configuration: " +
                        abConfiguration);
            }
        } catch (NumberFormatException nfe) {
            _log.error("Invalid number found in AB configuration: " + abConfiguration);
            _abCutoffs = null;
        }

        // fail-safe default to A-only
        if (_abCutoffs == null) {
            _abCutoffs = new int[] {1};
            _cutoffTotal = 1;
        }
    }

    protected static String getVariant(long secondsSinceEpoch, IPropertyDao propertyDao) {
        checkConfiguration(propertyDao.getProperty(IPropertyDao.VARIANT_CONFIGURATION));
        char abVersion = 'a';
        int runningTotal = 0;
        // Check each cutoff to see if the trno falls into it, if so assign the ab version appropriately.
        // AB version starts at 'a' and goes up the alphabet from there, one letter per cutoff.
        // EXAMPLE:
        // cutoffs = [1, 1]; total = 2;
        // First, set runningTotal to 1 (value of first cutoff).
        // Check if secondsSinceEpoch % 2 < 1 (this has 50% chance of being true)
        // if so set version to 'a' otherwise continue
        // Next iteration, set runningTotal to 2 (previous value plus second cutoff)
        // Check if secondsSinceEpoch % 2 < 2 (always true)
        // set version to 'b'
        for (int num: _abCutoffs) {
            runningTotal += num;
            if ( (secondsSinceEpoch % _cutoffTotal) < runningTotal) {
                return Character.toString(abVersion);
            }
            // increment ab version (e.g. from 'a' to 'b')
            abVersion++;
        }
        // default to a if loop doesn't determine config for some reason
        return Character.toString('a');
    }

    protected static void checkConfiguration(String config) {
        if (StringUtils.equals(_lastConfiguration, config)) {
            return;
        }
        convertABConfigToArray(config);
    }

    public static String convertABConfigurationToString() {
        StringBuffer title = new StringBuffer();
        StringBuffer percents = new StringBuffer();

        char abVersion = 'a';
        for (int i = 0; i < _abCutoffs.length; i++) {
            int num = _abCutoffs[i];
            title.append(Character.toUpperCase(abVersion));
            float percent = ((float) num) / ((float) _cutoffTotal) * 100;
            percents.append((int) percent);
            abVersion++;
            if (i < _abCutoffs.length - 1) {
                title.append('/');
                percents.append('/');
            }
        }

        return title.toString() + ": " + percents.toString();
    }
}
