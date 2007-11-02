package gs.web.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.StringTokenizer;

import gs.web.util.context.SessionContext;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class VariantConfiguration {
    public static final String AB_CONFIGURATION_FILE_CLASSPATH = "/gs/web/util/abConfig.txt";
    protected static int[] _abCutoffs = null;
    protected static int _cutoffTotal = 0;
    private static final Log _log = LogFactory.getLog(VariantConfiguration.class);

    static {
        readABConfiguration();
    }

    /**
     * Reads AB configuration from file and attempts to parse it. In the event of any error or
     * invalid configuration, this defaults to 50/50 testing.
     * This method should only be called once in the lifetime of the JVM, to populate the static
     * array of configuration data _abCutoffs.
     */
    protected static void readABConfiguration() {
        Resource resource = new ClassPathResource(AB_CONFIGURATION_FILE_CLASSPATH);
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String line = reader.readLine();
            while (StringUtils.isNotEmpty(line)) {
                if (line.startsWith("#")) {
                    // comment line, ignore
                } else {
                    buffer.append(line);
                }
                line = reader.readLine();
            }
            convertABConfigToArray(buffer.toString());
        } catch (Exception e) {
            _log.error("Exception generated parsing AB configuration: " + e.toString());
            _abCutoffs = null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        if (_abCutoffs == null) {
            _log.error("Couldn't determine ab version from file " + AB_CONFIGURATION_FILE_CLASSPATH);
            _abCutoffs = new int[] {1};
            _cutoffTotal = 1;
        }
    }

    /**
     * This converts an AB configuration string as read from file into a static array that can
     * be used elsewhere to determine the AB version. If there are any errors, this sets _abCutoffs
     * to null.
     * @param abConfiguration read from file
     * @throws NumberFormatException if the configuration has non-numeric values
     */
    protected static void convertABConfigToArray(String abConfiguration) throws NumberFormatException {
        if (StringUtils.isEmpty(abConfiguration)) {
            return;
        }
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
                    _log.error("Invalid value " + num + " found in AB configuration file");
                    _abCutoffs = null;
                    break;
                }
                _cutoffTotal += num;
                // check for invalid values
                if (_cutoffTotal > 100) {
                    _log.error("Invalid period " + _cutoffTotal + " found in AB configuration file");
                    _abCutoffs = null;
                    break;
                }
                _abCutoffs[tokenNum++] = num;
            }
        } else {
            _log.error("Invalid number of tokens " + tok.countTokens() + " found in AB configuration file");
        }
    }

    protected static void determineVariantFromConfiguration(long trnoSecondsSinceEpoch, SessionContext sessionContext) {
        char abVersion = 'a';
        int runningTotal = 0;
        // Check each cutoff to see if the trno falls into it, if so assign the ab version appropriately.
        // AB version starts at 'a' and goes up the alphabet from there, one letter per cutoff.
        // EXAMPLE:
        // cutoffs = [1, 1]; total = 2;
        // First, set runningTotal to 1 (value of first cutoff).
        // Check if trnoSecondsSinceEpoch % 2 < 1 (this has 50% chance of being true)
        // if so set version to 'a' otherwise continue
        // Next iteration, set runningTotal to 2 (previous value plus second cutoff)
        // Check if trnoSecondsSinceEpoch % 2 < 2 (always true)
        // set version to 'b'
        for (int num: _abCutoffs) {
            runningTotal += num;
            if ( (trnoSecondsSinceEpoch % _cutoffTotal) < runningTotal) {
                sessionContext.setAbVersion(Character.toString(abVersion));
                break;
            }
            // increment ab version (e.g. from 'a' to 'b')
            abVersion++;
        }
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
