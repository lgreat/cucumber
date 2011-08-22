package gs.web.admin.database;

import java.util.*;

public class TableMoverServiceData {
    public static final DatabaseDirection DEV_TO_STAGING = new DatabaseDirection("dev -> staging", "dev", "staging");
    public static final DatabaseDirection PRODUCTION_TO_DEV = new DatabaseDirection("production -> dev", "ditto", "dev");
    private DatabaseDirection direction;

    private List<DatabaseTables> _databasesAndTables = new ArrayList<DatabaseTables>();

    private Map<String, DatabaseTables> _databases = new HashMap<String, DatabaseTables>();

    public TableMoverServiceData() {
    }

    public TableMoverServiceData(DatabaseDirection direction) {
        this.direction = direction;
    }

    public void addDatabase(String databaseName, List<String> tableList) {
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

    public List<DatabaseTables> getDatabasesAndTables() {
        return _databasesAndTables;
    }

    public void setDatabasesAndTables(List<DatabaseTables> databasesAndTables) {
        this._databasesAndTables = databasesAndTables;
    }

    public void addDatabaseAndTable(String database, String table) {
        DatabaseTables databaseAndTables = _databases.get(database);
        if (databaseAndTables == null) {
            addDatabase(database, new ArrayList<String>());
            databaseAndTables = _databases.get(database);
        }
        databaseAndTables.getTables().add(table);
    }

    public DatabaseTables getDatabase(String databaseName) {
        return _databases.get(databaseName);
    }

    public void filterDatabases(List<String> databasesToRemove) {
        for (String database : databasesToRemove) {
            _databases.remove(database);
            for (Iterator<DatabaseTables> databaseTablesList = _databasesAndTables.iterator(); databaseTablesList.hasNext();) {
                DatabaseTables databaseAndTables = databaseTablesList.next();
                if (database.equals(databaseAndTables.getDatabaseName())) {
                    databaseTablesList.remove();
                }
            }
        }
    }

    public void filterTables(Map tablesToKeep) {
        for (Object o : tablesToKeep.keySet()) {
            String databaseName = (String) o;
            DatabaseTables databaseTables = _databases.get(databaseName);
            if (databaseTables != null) {
                databaseTables.getTables().retainAll((Collection) tablesToKeep.get(databaseName));
            }
        }
    }

    public static class DatabaseDirection {
        public String source;
        public String label;
        public String target;

        public DatabaseDirection() {
        }

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
            if (!(object instanceof DatabaseDirection)) return false;                
            DatabaseDirection other = (DatabaseDirection) object;

            return !(source == null || target == null || label == null) &&
                    source.equals(other.source) && target.equals(other.target) &&
                    label.equals(other.label);
        }
    }

    public static class DatabaseTables {
        public String databaseName;

        public List<String> tables;


        public DatabaseTables() {
        }

        public DatabaseTables(String databaseName, List<String> tableList) {
            this.databaseName = databaseName;
            this.tables = tableList;
        }


        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public List<String> getTables() {
            return tables;
        }

        public void setTables(List<String> tables) {
            this.tables = tables;
        }
    }
}
