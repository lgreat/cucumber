package gs.web.compare;

import java.util.List;

/**
 * @author Nanditha Patury <mailto:npatury@greatschools.net>
 */
public class CensusStruct {
    private boolean _isHeaderCell = false;
    private boolean _isSimpleCell = false;
    private String _headerText;
    private String _breakdownText;
    private String _extraInfo;
    private List<BreakdownNameValue> _breakdownList;
    private String _value;
    private int _year;
    /** Remove any values lower than this from _breakdownList (display requirement) */
    private int _breakdownValueMinimum = 0;

    // following signatures are helpful for JSTL readability
    public boolean getIsHeaderCell() {
        return _isHeaderCell;
    }

    public void setIsHeaderCell(boolean headerCell) {
        _isHeaderCell = headerCell;
    }

    public boolean getIsSimpleCell() {
        return _isSimpleCell;
    }

    public void setIsSimpleCell(boolean simpleCell) {
        _isSimpleCell = simpleCell;
    }

    // following two signatures are helpful for Java readability
    public boolean isHeaderCell() {
        return _isHeaderCell;
    }

    public boolean isSimpleCell() {
        return _isSimpleCell;
    }

    public String getHeaderText() {
        return _headerText;
    }

    public void setHeaderText(String headerText) {
        _headerText = headerText;
    }

    public String getBreakdownText() {
        return _breakdownText;
    }

    public void setBreakdownText(String breakdownText) {
        _breakdownText = breakdownText;
    }

    public String getExtraInfo() {
        return _extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        _extraInfo = extraInfo;
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    public List<BreakdownNameValue> getBreakdownList() {
        return _breakdownList;
    }

    public void setBreakdownList(List<BreakdownNameValue> breakdownList) {
        _breakdownList = breakdownList;
    }

    public int getYear() {
        return _year;
    }

    public void setYear(int year) {
        _year = year;
    }

    public int getBreakdownValueMinimum() {
        return _breakdownValueMinimum;
    }

    public void setBreakdownValueMinimum(int breakdownValueMinimum) {
        _breakdownValueMinimum = breakdownValueMinimum;
    }
}