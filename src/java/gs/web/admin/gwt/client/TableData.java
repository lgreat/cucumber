package gs.web.admin.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.*;

public class TableData extends Object implements IsSerializable {
    public static final DatabaseDirection DEV_TO_STAGING = new DatabaseDirection("dev -> staging", "dev", "staging");
    public static final DatabaseDirection PRODUCTION_TO_DEV = new DatabaseDirection("production -> dev", "production", "dev");
    private DatabaseDirection direction;

    // GWT's JRE emulation library doesn't have LinkedHashMap or sorted maps/sets, so we fake it here
    private List databaseNames = new ArrayList();
    private Map databases = new HashMap();


    public TableData() {}

    public TableData(DatabaseDirection direction) {
        this.direction = direction;
    }

    public void addDatabase(String databaseName, List tableList) {
        databaseNames.add(databaseName);
        databases.put(databaseName, tableList);
    }

    public List getDatabases() {
        return databaseNames;
    }

    public List getTablesForDatabase(String databaseName) {
        return (List) databases.get(databaseName);
    }

    public DatabaseDirection getDirection() {
        return direction;
    }

    public static class DatabaseDirection implements IsSerializable {
        public String source;
        public String label;
        public String target;


        public DatabaseDirection() {}

        public DatabaseDirection(String display, String source, String target) {
            this.source = source;
            this.target = target;
            this.label = display;
        }
    }
}
