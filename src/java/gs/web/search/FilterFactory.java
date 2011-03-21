package gs.web.search;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class FilterFactory {

    Logger _log = Logger.getLogger(FilterFactory.class);

    public<T extends FieldFilter> FilterGroup<T> createFilterGroup(Class c, String[] values) {
        if (c == null || values == null) {
            throw new IllegalArgumentException("Cannot create FilterGroup without both Class and String array");
        }
        FilterGroup filterGroup = new FilterGroup<T>();
        List<FieldFilter> fieldFilters = new ArrayList<FieldFilter>();

        for (int i = 0; i < values.length; i++) {
            FieldFilter f;
            try {
                f = valueOf(c, values[i]);
                fieldFilters.add(f);
            } catch (IllegalArgumentException e) {
                _log.warn("Could not get FieldFilter for " + values[i] + ", skipping.", e);
            }
        }
        
        filterGroup.setFieldFilters(fieldFilters.toArray(new FieldFilter[0]));
        return filterGroup;
    }

    public<T extends FieldFilter> FilterGroup<T> createFilterGroup(T obj, String[] values) {
        return createFilterGroup(obj.getClass(), values);
    }

    /**
     * Returns a FieldFilter enum with a type equal to provided filter's type, and name equal to provided name.
     * @param filter an enum of the desired type
     * @param name the enum's name
     * @param <T>
     * @return
     */
    public <T> FieldFilter valueOf(T filter, String name) {
        return valueOf(filter.getClass(), name);
    }

    public FieldFilter valueOf(Class enumClassName, String name) {
        name = StringUtils.upperCase(name);

        try {
            if (enumClassName.equals(FieldFilter.SchoolTypeFilter.class)) {
                return FieldFilter.SchoolTypeFilter.valueOf(name);
            } else if (enumClassName.equals(FieldFilter.GradeLevelFilter.class)) {
                return FieldFilter.GradeLevelFilter.valueOf(name);
            } else if (enumClassName.equals(FieldFilter.AffiliationFilter.class)) {
                return FieldFilter.AffiliationFilter.valueOf(name);
            } else if (enumClassName.equals(FieldFilter.StudentTeacherRatio.class)) {
                return FieldFilter.StudentTeacherRatio.valueOf(name);
            } else if (enumClassName.equals(FieldFilter.SchoolSize.class)) {
                return FieldFilter.SchoolSize.valueOf(name);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not find FieldFilter for " + name, e);
        }

        return null;
    }
}
