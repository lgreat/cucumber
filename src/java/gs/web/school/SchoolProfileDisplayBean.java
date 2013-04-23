package gs.web.school;

import gs.data.school.EspResponse;
import org.apache.commons.lang.WordUtils;

import java.util.*;

/**
 * Bean used to control setting up the display of school profile information.
 *
 * The overall display consists of tabs that displays information about specific topics.
 * The display page (tab) consists of sections which are major subject areas.
 * Within each section are data rows where the dat comes from the esp_response
 * table with a response_key that contains a specified valueEspResponseKey.
 *
 * There will be one instance of this bean for every row on the school profiles page
 *
 * This bean includes a lot of information about how each row is to be displayed.  Including the following features:
 * 1. Tab, section and row information
 * 2. A list of data elements to be displayed for this row.  Each entry is in the tabAbbrev/SectionAbbrev/DbElementName
 *    format.  The data elements are put in the data model (name ProfileData) that is passed to the page.
 * 3. An optional list of DbResponse values valid for this row.
 * 4. How the data is to be formatted on the page (contained in enum DisplayFormat)
 * 5. How to update the row title using DB data (optional, contained in _titleMatch and _titleModelKey)
 * 6. A few rows need special handling to form the result for the page.  Support data for these rows is in
 *    _supportEspResponseKeys and _supportModelKeys.
 * 7. Some of the DB results for a row can have multiple values including "None".  In some cases "None" is to be
 *    shown and other cases it is hidden.  In one case "None" is to be shown if it is the only value.
 *    This is controlled by the enum NoneHandling.  These values are applied in SchoolProfileProgramsController.applyUniqueDataRules
 * 8. Optional CMS article ID that provides a further description of the _rowTitle.  This link always has the text
 *    "Don't understand these terms?" and opens the provided link in a new window.
 * User: rraker
 * Date: 6/14/12
 * Time: 10:30 AM
 */
public class SchoolProfileDisplayBean {

    public enum  DisplayFormat { BASIC, TWO_COL, URL }   // The possible display formats
    public enum NoneHandling{
        /** If the only value is none show it otherwise remove it.  This is the default action */
        REMOVE_NONE_IF_NOT_ONLY_VALUE,
        /** This means no changes to the values returned from the backend */
        NO_NONE_PROCESSING,
        //* Remove none no mater how many values are in the list and if there is only one the list will become empty */
        REMOVE_NONE_ALWAYS
    }

    private String _tabAbbreviation;            // abbreviation for a tab
    private String _sectionAbbreviation;        // abbreviation for a section
    private String _sectionTitle;               // this is the title displayed for this section
    private String _rowTitle;                   // this is the row description text
    private Set<String> _espResponseKeys = new HashSet<String>();       // these are the esp_response.response_key values to be used for this row
    private String _espResponseKeyWithAllowed;  // This is the esp_response_key that has an "allowed" set of values
    private Set _allowedResponseValues;         // only this set of values is allowed on this row
    private String _modelKey;                   // the result data is stored under this key
    private DisplayFormat _displayFormat;       // how the data should be formatted
    private List<String> _urlDescription;       // For row displayed as a URL this is the Description
    private List<String> _urlValue;             // For row displayed as a URL this is the url
    private List<String> _titleMatch;           // string in row title to find for substitution
    private List<String> _titleModelKey;        // modelKey to value for this replacement
    //private boolean _requiresNoneHandling;      // indicator that special "None" value handling is needed
    private NoneHandling _noneMode;             // How to treat None values
    private List<AdditionalData> _additionalData;    // A place to store additional data to retrieve from the ESP database and map to the model
    private String _rowTitleCmsArticleId;       // If the row title has a link to an explanation of the data then this is the CMS article ID
    private String _rowTitleCmsArticleAnchor;   // If the row title has a link to an explanation of the data then this is anchor for the CMS article ID
    private String _rowTitleCmsArticleOmniture; // Optional Omniture tracking code for clicking on the link to the CMS article

    // Don't allow default construction
    private SchoolProfileDisplayBean(){}

    /**
     * Create a SchoolProfileDisplayBean that includes a list of allowed values
     * @param tabAbbreviation       tab abbreviation
     * @param sectionAbbreviation   section abbreviation
     * @param sectionTitle          section title
     * @param rowTitle              row title
     * @param espResponseKey        database response key for this row.
     * @param allowedResponseValues list of allowed values from the database for this row
     */
    public SchoolProfileDisplayBean( String tabAbbreviation, String sectionAbbreviation, String sectionTitle,
                                     String rowTitle, String espResponseKey, String[] allowedResponseValues ){
        _tabAbbreviation = tabAbbreviation;
        _sectionAbbreviation = sectionAbbreviation;
        _sectionTitle = sectionTitle;
        _rowTitle = rowTitle;
        if( espResponseKey != null && espResponseKey.length() > 0 ) {
            _espResponseKeys.add( espResponseKey );
            // Compute the model key
            _modelKey = calcModelKey( espResponseKey );
        }
        if( allowedResponseValues != null ) {
            _allowedResponseValues = new HashSet<String>(Arrays.asList(allowedResponseValues));
            _espResponseKeyWithAllowed = espResponseKey;
        }
        else {
            _allowedResponseValues = null;
        }
        _displayFormat = DisplayFormat.BASIC;
        _noneMode = NoneHandling.REMOVE_NONE_IF_NOT_ONLY_VALUE;
    }

    /**
     * Create a SchoolProfileDisplayBean that doesn't include a list of allowed values
     * @param tabAbbreviation       tab abbreviation
     * @param sectionAbbreviation   section abbreviation
     * @param sectionTitle          section title
     * @param rowTitle              row title
     * @param espResponseKey        database response key for this row.
     */
    public SchoolProfileDisplayBean( String tabAbbreviation, String sectionAbbreviation, String sectionTitle,
                                     String rowTitle, String espResponseKey ){
        this( tabAbbreviation, sectionAbbreviation, sectionTitle, rowTitle, espResponseKey, null );
    }

    /**
     * Create a SchoolProfileDisplayBean that doesn't include an espResponseKey.
     * This is an incomplete definition and at least one more add... method needs to be called to display and data
     * @param tabAbbreviation       tab abbreviation
     * @param sectionAbbreviation   section abbreviation
     * @param sectionTitle          section title
     * @param rowTitle              row title
     */
    public SchoolProfileDisplayBean( String tabAbbreviation, String sectionAbbreviation, String sectionTitle,
                                     String rowTitle ){
        this( tabAbbreviation, sectionAbbreviation, sectionTitle, rowTitle, null, null );
    }

    public String getTabAbbreviation() {
        return _tabAbbreviation;
    }
    public String getSectionAbbreviation() {
        return _sectionAbbreviation;
    }

    public void setSectionAbbreviation(String sectionAbbreviation) {
        _sectionAbbreviation = sectionAbbreviation;
    }

    public String getSectionTitle() {
        return _sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        _sectionTitle = sectionTitle;
    }

    public String getRowTitle() {
        return _rowTitle;
    }

    public Set<String> getEspResponseKeys() {
        return _espResponseKeys;
    }

    public String getEspResponseKeyWithAllowed() {
        return _espResponseKeyWithAllowed;
    }

    public Set<String> getAllowedResponseValues() {
        return _allowedResponseValues;
    }

    public String getModelKey() {
        return _modelKey;
    }

    public void setDisplayFormat( DisplayFormat displayFormat ) {
        _displayFormat = displayFormat;
    }

    public DisplayFormat getDisplayFormat() {
        return _displayFormat;
    }

    /**
     * Add another DB key for retrieval.  Note - when adding additional keys there is no way to restrict the values
     * @param espResponseKey  the response key to add
     */
    public void addKey( String espResponseKey ) {
        _espResponseKeys.add(espResponseKey);
    }

    public void addRowTitleCmsArticleIdAndAnchor( String id, String anchor, String omnitureTrackingCode ) {
        _rowTitleCmsArticleId = id;
        _rowTitleCmsArticleAnchor = anchor;
        _rowTitleCmsArticleOmniture = omnitureTrackingCode;
    }

    public String getRowTitleCmsArticleId() {
        return _rowTitleCmsArticleId;
    }

    public String getRowTitleCmsArticleAnchor() {
        return _rowTitleCmsArticleAnchor;
    }

    public String getRowTitleCmsArticleOmniture() {
        return _rowTitleCmsArticleOmniture;
    }

    /**
     * Specify a URL descriptionEspResponseKey and valueEspResponseKey for the display.
     * @param descriptionEspResponseKey  The epsResponseKey for this description
     * @param urlEspResponseKey  The espResponseKey for this url
     */
    public void addUrl( String descriptionEspResponseKey, String urlEspResponseKey) {
        // To support this requirement the following data structures need to be built:
        // 1. The values passed in, converted to modelKeys, are stored in _urlDescription and _urlValue.  These are used by the jspx.
        // 2. Both of the input values need to be added to the _espResponseKeys list so the data will be retrieved from the ESP database
        // 3. An esp to model key list is neeeded so the ESP data can be mapped to ModelKeys.  This is done in _additionalData.

        if( _urlDescription == null ) {
            _urlDescription = new ArrayList<String>(5);
            _urlValue = new ArrayList<String>(5);
        }

        // Create additionalData list if needed
        if( _additionalData == null ) {
            _additionalData = new ArrayList<AdditionalData>();
        }

        if( descriptionEspResponseKey != null && descriptionEspResponseKey.length() > 0 ) {
            String modelKey = calcModelKey(descriptionEspResponseKey);
            _urlDescription.add( modelKey );
            _additionalData.add( new AdditionalData( descriptionEspResponseKey ) );
            _espResponseKeys.add(descriptionEspResponseKey);        // Need to retrieve data for this key
        }
        else {
            _urlDescription.add("");
        }

        if( urlEspResponseKey != null && urlEspResponseKey.length() > 0 ) {
            String modelKey = calcModelKey(urlEspResponseKey);
            _urlValue.add( modelKey );
            _additionalData.add( new AdditionalData( urlEspResponseKey ) );
            _espResponseKeys.add(urlEspResponseKey);        // Need to retrieve data for this key
        }
        else {
            _urlValue.add("");
        }

        _displayFormat = DisplayFormat.URL;     // Display format must be URL
    }

    /**
     * Returns the modelKey of the descriptionEspResponseKey as an array of strings
     * @return modelKey
     */

    public String [] getUrlDescModelKeys(  ) {
        if( _urlDescription == null )
            return new String[0];
       else {
            return _urlDescription.toArray(new String[0]);
        }
    }

    /**
     * Returns the modelKey of the (url) valueEspResponseKey as an array of strings
     * @return modelKey
     */
    public String [] getUrlValueModelKeys() {
        if( _urlValue == null )
            return new String[0];
        else {
            return _urlValue.toArray(new String[0]);
        }
    }

    /**
     * If the Result value is none is returned should we show any data for this row
      */
    public void setShowNone( NoneHandling none ) {
        _noneMode = none;
    }

    public NoneHandling getShowNone() {
        return _noneMode;
    }

    /**
     * If the row title has replaceable values in it that need to be replaced with values from the database
     * then add that information here
     * @param titleMatch     the replaceable value in the title
     * @param espResponseKey the key in the ProfileData model where to get the data
     */
    public void addRowTitleSubstitution( String titleMatch, String espResponseKey) {
        if( titleMatch == null || titleMatch.length() == 0 || espResponseKey == null || espResponseKey.length() == 0 ) {
            return;     // Need to non empty values to continue
        }
        if( _titleMatch == null ) {
            _titleMatch = new ArrayList<String>(2);
        }
        _titleMatch.add( titleMatch );

        if( _titleModelKey == null ) {
            _titleModelKey = new ArrayList<String>(2);
        }
        _titleModelKey.add( calcModelKey(espResponseKey) );
        _espResponseKeys.add(espResponseKey);
    }

    /**
     * Return the title string to match
     * @param i the entry to return
     * @return the string that is to be matched
     */
    public String getTitleMatch( int i ) {
        if( _titleMatch == null || i < 0 || i >= _titleMatch.size() ) {
            return "";
        }
        return _titleMatch.get( i );
    }

    /**
     * Return the title substitution modelKey
     * @param i the entry to return
     * @return the key to the data in the data model
     */
    public String getTitleModelKey( int i ) {
        if( _titleModelKey == null || i < 0 || i >= _titleModelKey.size() ) {
            return "";
        }
        return _titleModelKey.get( i );
    }

    public List<AdditionalData> getAdditionalData() {
        return _additionalData;
    }

    /**
     * A few rows need supporting data that is more specific that just additional results.
     * For instance, the "School-run shuttle" row includes a field for the route information.
     * @param espResponseKey
     */
    public void addSupportInfo( String espResponseKey ) {
        if( espResponseKey != null && espResponseKey.length() > 0 ) {
            // Create additionalData list if needed
            if( _additionalData == null ) {
                _additionalData = new ArrayList<AdditionalData>();
            }

            _additionalData.add( new AdditionalData( espResponseKey ) );
        }
    }

    /**
     * Builds a row title by replacing replaceable values with the corresponding data
     * @param displayBean  the displayBean to get the title from
     * @param profileData  the data
     * @return  the final row title
     */
    public static String buildRowTitle( SchoolProfileDisplayBean displayBean, Map<String, List<String>> profileData ) {
        // Check entry conditions
        if( displayBean == null || profileData == null || displayBean.getTitleMatch(0).length() == 0 ) {
            return displayBean.getRowTitle();
        }

        // Perform substitutions
        StringBuilder title = new StringBuilder( displayBean.getRowTitle() );
        for( int i = 0; displayBean.getTitleMatch(i).length() > 0; i++ ) {
            String toMatch = displayBean.getTitleMatch(i);
            int pos = title.indexOf( toMatch );
            if( pos >= 0 ) {
                String key = displayBean.getTitleModelKey( i );
                String value = profileData.get( key ).get( 0 );
                title.replace( pos, pos+toMatch.length(), value );
            }
        }
        return title.toString();
    }

    @Override
    public String toString() {
        return "SchoolProfileDisplayBean{" +
                "_tabAbbreviation='" + _tabAbbreviation + '\'' +
                ", _sectionAbbreviation='" + _sectionAbbreviation + '\'' +
                ", modelKey-'" + _modelKey + '\'' +
                ", _sectionTitle='" + _sectionTitle + '\'' +
                ", _rowTitle='" + _rowTitle + '\'' +
                ", _responseKey='" + _espResponseKeys + '\'' +
                ", _allowedResponseValues=" + (_allowedResponseValues == null ? null : Arrays.asList(_allowedResponseValues)) +
                '}';
    }

    private String calcModelKey( String espResponseKey ) {
        if( espResponseKey != null && espResponseKey.length() > 0 ) {
            return _tabAbbreviation + '/' + _sectionAbbreviation + '/' + espResponseKey;
        }
        else {
            return "";
        }
    }

    // This is a simple inner class to keep track of additional ESP data to retrieve and how to map that to modelKeys
    public class AdditionalData {
        String _espResponseKey;
        String _modelKey;

        private AdditionalData() {}

        public AdditionalData(String espResponseKey) {
            _espResponseKey = espResponseKey;
            _modelKey = calcModelKey( espResponseKey );
        }

        public String getEspResponseKey() {
            return _espResponseKey;
        }

        public String getModelKey() {
            return _modelKey;
        }
    }
}

