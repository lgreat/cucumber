package gs.web.search;

import org.apache.commons.lang.StringUtils;

public class FilterFactory {

    public<T extends FieldFilter> FilterGroup<T> createFilterGroup(Class c, String[] values) {
        if (c == null || values == null) {
            throw new IllegalArgumentException("Cannot create FilterGroup without both Class and String array");
        }
        FilterGroup filterGroup = new FilterGroup<T>();
        FieldFilter[] fieldFilters = new FieldFilter[values.length];

        for (int i = 0; i < values.length; i++) {
            fieldFilters[i] = valueOf(c, values[i]);
        }
        filterGroup.setFieldFilters(fieldFilters);
        return filterGroup;
    }

    public<T extends FieldFilter> FilterGroup<T> createFilterGroup(T obj, String[] values) {
        if (obj == null || values == null) {
            throw new IllegalArgumentException("Cannot create FilterGroup without both enum and String array");
        }

        FilterGroup filterGroup = new FilterGroup<T>();
        FieldFilter[] fieldFilters = new FieldFilter[values.length];

        for (int i = 0; i < values.length; i++) {
            fieldFilters[i] = valueOf(obj, values[i]);
        }
        filterGroup.setFieldFilters(fieldFilters);
        return filterGroup;
    }

    public<T extends FieldFilter> FilterGroup<T> createFilterGroup(String enumClassName, String[] values) {
        if (enumClassName == null || values == null) {
            throw new IllegalArgumentException("Cannot create FilterGroup without both enum class name and String array");
        }

        FilterGroup filterGroup = new FilterGroup<T>();
        FieldFilter[] fieldFilters = new FieldFilter[values.length];

        for (int i = 0; i < values.length; i++) {
            fieldFilters[i] = valueOf(enumClassName, values[i]);
        }
        filterGroup.setFieldFilters(fieldFilters);
        return filterGroup;
    }


    /**
     * Returns a FieldFilter enum with a type equal to provided filter's type, and name equal to provided name.
     * @param filter an enum of the desired type
     * @param name the enum's name
     * @param <T>
     * @return
     */
    public <T> FieldFilter valueOf(T filter, String name) {
        name = StringUtils.upperCase(name);
        if (filter instanceof FieldFilter.SchoolTypeFilter) {
            return FieldFilter.SchoolTypeFilter.valueOf(name);
        }
        return null;
    }

    /**
     * Returns an enum of a type matching the provided type and name matching the provided name
     * @param enumClassName the enum's class name
     * @param name the enum's name
     * @return
     */
    public FieldFilter valueOf(String enumClassName, String name) {
        name = StringUtils.upperCase(name);
        
        if (enumClassName.equals(FieldFilter.SchoolTypeFilter.class.getSimpleName())) {
            return FieldFilter.SchoolTypeFilter.valueOf(name);
        }

        return null;
    }

    public FieldFilter valueOf(Class enumClassName, String name) {
        name = StringUtils.upperCase(name);

        if (enumClassName.equals(FieldFilter.SchoolTypeFilter.class)) {
            return FieldFilter.SchoolTypeFilter.valueOf(name);
        }

        return null;
    }
}
