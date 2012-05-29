package gs.web.search;

import gs.data.school.SchoolType;
import org.apache.commons.lang.StringUtils;
import gs.web.pagination.Pagination;
import gs.web.pagination.RequestedPage;

import java.util.HashSet;
import java.util.Set;

public class SchoolSearchCommand2012 extends SchoolSearchCommand {

    public static final float DEFAULT_DISTANCE = 5f;

    public SchoolSearchCommand2012() {
        super();
        setDistance(String.valueOf(new Float(DEFAULT_DISTANCE).intValue()));
    }

}

