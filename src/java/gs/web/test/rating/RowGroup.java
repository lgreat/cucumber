package gs.web.test.rating;

import gs.data.test.rating.IRatingsConfig;

import java.util.List;
import java.util.ArrayList;

/**
 * @author thuss
 */
public class RowGroup implements IRatingsDisplay.IRowGroup {

    final String _label;
    final List _rows;

    RowGroup(IRatingsConfig.IRowGroupConfig rowGroupConfig) {
        _label = rowGroupConfig.getLabel();
        _rows = new ArrayList();
    }

    RowGroup(String label, List rows) {
        _label = label;
        _rows = rows;
    }

    public String getLabel() {
        return _label;
    }

    public int getNumRows() {
        return _rows.size();
    }

    public List getRows() {
        return _rows;
    }

    public void add(IRatingsDisplay.IRowGroup.IRow row) {
        _rows.add(row);
    }
}
