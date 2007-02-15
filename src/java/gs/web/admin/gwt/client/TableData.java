package gs.web.admin.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.*;

public class TableData extends Object implements IsSerializable {
    public static final DatabaseDirection DEV_TO_STAGING = new DatabaseDirection("dev -> staging", "dev", "staging");
    public static final DatabaseDirection PRODUCTION_TO_DEV = new DatabaseDirection("production -> dev", "ditto", "dev");
    private DatabaseDirection direction;

    /**
     * @gwt.typeArgs <gs.web.admin.gwt.client.TableData.DatabaseTables>
     */
    private List _databasesAndTables = new ArrayList();

    private Map _databases = new HashMap();

    public TableData() {}

    public TableData(DatabaseDirection direction) {
        this.direction = direction;
    }

    public void addDatabase(String databaseName, List tableList) {
        DatabaseTables databaseAndTables = new DatabaseTables(databaseName, tableList);
        _databasesAndTables.add(databaseAndTables);
        _databases.put(databaseName, databaseAndTables);
    }

    public DatabaseDirection getDirection() {
        return direction;
    }

    public void setDirection(DatabaseDirection direction) {
        this.direction = direction;
    }

    public List getDatabasesAndTables() {
        return _databasesAndTables;
    }

    public void setDatabasesAndTables(List databasesAndTables) {
        this._databasesAndTables = databasesAndTables;
    }

    public void addDatabaseAndTable(String database, String table) {
        DatabaseTables databaseAndTables = (DatabaseTables) _databases.get(database);
        if (databaseAndTables == null) {
            addDatabase(database, new ArrayList());
            databaseAndTables = (DatabaseTables) _databases.get(database);
        }
        databaseAndTables.getTables().add(table);
    }

    public DatabaseTables getDatabase(String databaseName) {
        return (DatabaseTables) _databases.get(databaseName);
    }

    public void filterDatabases(List databasesToRemove) {
        for (Iterator iterator = databasesToRemove.iterator(); iterator.hasNext();) {
            String database = (String) iterator.next();
            _databases.remove(database);
            for (Iterator databaseTablesList = _databasesAndTables.iterator(); databaseTablesList.hasNext();) {
                DatabaseTables databaseAndTables = (DatabaseTables) databaseTablesList.next();
                if (database.equals(databaseAndTables.getDatabaseName())) {
                    databaseTablesList.remove();
                }
            }
        }
    }

    public void filterTables(Map tablesToKeep) {
        for (Iterator iterator = tablesToKeep.keySet().iterator(); iterator.hasNext();) {
            String databaseName = (String) iterator.next();
            DatabaseTables databaseTables = (DatabaseTables) _databases.get(databaseName);
            if (databaseTables != null) {
                databaseTables.getTables().retainAll((Collection) tablesToKeep.get(databaseName));
            }
        }
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


        public boolean equals(Object object) {
            DatabaseDirection other = (DatabaseDirection) object;

            if (source == null || target == null || label == null) {
                return false;
            }

            return source.equals(other.source) && target.equals(other.target) && label.equals(other.label);
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
