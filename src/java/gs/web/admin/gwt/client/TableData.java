package gs.web.admin.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.*;

public class TableData extends Object implements IsSerializable {
    public static final DatabaseDirection DEV_TO_STAGING = new DatabaseDirection("dev -> staging", "dev", "staging");
    public static final DatabaseDirection PRODUCTION_TO_DEV = new DatabaseDirection("production -> dev", "production", "dev");
    private DatabaseDirection direction;

    // GWT's JRE emulation library doesn't have LinkedHashMap or sorted maps/sets, so we fake it here
    /**
     * @gwt.typeArgs <java.lang.String>
     */
    private List databaseNames = new ArrayList();
    /**
     * @gwt.typeArgs <gs.web.admin.gwt.client.TableData.DatabaseTables>
     */
    private List databaseTables = new ArrayList();

    public TableData() {}

    public TableData(DatabaseDirection direction) {
        this.direction = direction;
    }

    public void addDatabase(String databaseName, List tableList) {
        databaseNames.add(databaseName);
        databaseTables.add(new DatabaseTables(databaseName, tableList));
    }

    public List getTablesForDatabase(String databaseName) {
        for (Iterator iterator = databaseTables.iterator(); iterator.hasNext();) {
            DatabaseTables tables = (DatabaseTables) iterator.next();
            if (databaseName.equals(tables.databaseName)) {
                return tables.tables;
            }
        }
        return null;
    }

    public DatabaseDirection getDirection() {
        return direction;
    }

    public void setDirection(DatabaseDirection direction) {
        this.direction = direction;
    }

    public List getDatabaseNames() {
        return databaseNames;
    }

    public void setDatabaseNames(List databaseNames) {
        this.databaseNames = databaseNames;
    }

    public List getDatabaseTables() {
        return databaseTables;
    }

    public void setDatabaseTables(List databaseTables) {
        this.databaseTables = databaseTables;
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


        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }

    public static class DatabaseTables implements IsSerializable {
        public String databaseName;

        /**
         * @gwt.typeArgs <java.lang.String>
         */
        public List tables;


        public DatabaseTables() {}

        public DatabaseTables(String databaseName, List tableList) {
            this.databaseName = databaseName;
            this.tables = tableList;
        }


        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public List getTables() {
            return tables;
        }

        public void setTables(List tables) {
            this.tables = tables;
        }
    }
}
