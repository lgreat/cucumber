package gs.web.school;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
* Created with IntelliJ IDEA.
* User: samson
* Date: 3/1/13
* Time: 7:31 PM
* To change this template use File | Settings | File Templates.
*/

public class GroupOfStudentTeacherViewRows implements List<SchoolProfileStatsDisplayRow>, Serializable {
    private CensusGroup _censusGroup;
    private ArrayList<SchoolProfileStatsDisplayRow> _rows;

    public GroupOfStudentTeacherViewRows(CensusGroup group, ArrayList<SchoolProfileStatsDisplayRow> rows) {
        _censusGroup = group;
        _rows = rows;
    }

    public boolean isAnySchoolData() {
        boolean result = false;
        for (SchoolProfileStatsDisplayRow row : _rows) {
            result = result || censusValueNotEmpty(row.getSchoolValue());
        }
        return result;
    }

    public boolean isAnyDistrictData() {
        boolean result = false;
        for (SchoolProfileStatsDisplayRow row : _rows) {
            result = result || censusValueNotEmpty(row.getDistrictValue());
        }
        return result;
    }

    public boolean isAnyStateData() {
        boolean result = false;
        for (SchoolProfileStatsDisplayRow row : _rows) {
            result = result || censusValueNotEmpty(row.getStateValue());
        }
        return result;
    }

    protected boolean censusValueNotEmpty(String value) {
        return !StringUtils.isEmpty(value) && !"N/A".equalsIgnoreCase(value);
    }

    public Long getGroupId() {
        return _censusGroup.getId();
    }

    public CensusGroup getCensusGroup() {
        return _censusGroup;
    }

    public Map<String, String> getSchoolValueMap() {
        Map<String, String> map = new LinkedHashMap<String,String>();
        for (SchoolProfileStatsDisplayRow row : _rows) {
            if (StringUtils.isNotEmpty(row.getSchoolValue())) {
                map.put(row.getText(), row.getSchoolValue());
            }
        }
        return map;
    }

    public int size() {
        return _rows.size();
    }

    public boolean isEmpty() {
        return _rows.isEmpty();
    }

    public boolean contains(Object o) {
        return _rows.contains(o);
    }

    public Iterator<SchoolProfileStatsDisplayRow> iterator() {
        return _rows.iterator();
    }

    public Object[] toArray() {
        return _rows.toArray();
    }

    public <T> T[] toArray(T[] ts) {
        return _rows.toArray(ts);
    }

    public boolean add(SchoolProfileStatsDisplayRow schoolProfileStatsDisplayRow) {
        return _rows.add(schoolProfileStatsDisplayRow);
    }

    public boolean remove(Object o) {
        return _rows.remove(o);
    }

    public boolean containsAll(Collection<?> objects) {
        return _rows.containsAll(objects);
    }

    public boolean addAll(Collection<? extends SchoolProfileStatsDisplayRow> schoolProfileStatsDisplayRows) {
        return _rows.addAll(schoolProfileStatsDisplayRows);
    }

    public boolean addAll(int i, Collection<? extends SchoolProfileStatsDisplayRow> schoolProfileStatsDisplayRows) {
        return _rows.addAll(i, schoolProfileStatsDisplayRows);
    }

    public boolean removeAll(Collection<?> objects) {
        return _rows.removeAll(objects);
    }

    public boolean retainAll(Collection<?> objects) {
        return _rows.retainAll(objects);
    }

    public void clear() {
        _rows.clear();
    }

    @Override
    public boolean equals(Object o) {
        return _rows.equals(o);
    }

    @Override
    public int hashCode() {
        return _rows.hashCode();
    }

    public SchoolProfileStatsDisplayRow get(int i) {
        return _rows.get(i);
    }

    public SchoolProfileStatsDisplayRow set(int i, SchoolProfileStatsDisplayRow schoolProfileStatsDisplayRow) {
        return _rows.set(i, schoolProfileStatsDisplayRow);
    }

    public void add(int i, SchoolProfileStatsDisplayRow schoolProfileStatsDisplayRow) {
        _rows.add(i, schoolProfileStatsDisplayRow);
    }

    public SchoolProfileStatsDisplayRow remove(int i) {
        return _rows.remove(i);
    }

    public int indexOf(Object o) {
        return _rows.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return _rows.lastIndexOf(o);
    }

    public ListIterator<SchoolProfileStatsDisplayRow> listIterator() {
        return _rows.listIterator();
    }

    public ListIterator<SchoolProfileStatsDisplayRow> listIterator(int i) {
        return _rows.listIterator(i);
    }

    public List<SchoolProfileStatsDisplayRow> subList(int i, int i2) {
        return _rows.subList(i, i2);
    }
}
