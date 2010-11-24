package gs.web.compare;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Nanditha Patury <mailto:npatury@greatschools.net>
 */
public class CensusStruct {
    private boolean isHeaderCell = false;
    private boolean isSimpleCell = false;
    private String headerText;
    private String extraInfo;
    private Map breakdownMap;
    private String value;


    public boolean getIsHeaderCell() {
        return isHeaderCell;
    }

    public void setIsHeaderCell(boolean headerCell) {
        isHeaderCell = headerCell;
    }

    public boolean getIsSimpleCell() {
        return isSimpleCell;
    }

    public void setIsSimpleCell(boolean simpleCell) {
        isSimpleCell = simpleCell;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public Map getBreakdownMap() {
        return breakdownMap;
    }

    public void setBreakdownMap(Map breakdownMap) {
        this.breakdownMap = breakdownMap;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}