package gs.web.admin.gwt.client;

import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.core.client.GWT;


public interface TableCopyService extends RemoteService {
    /**
     *
     * @param direction The direction in which the tables should be copied
     * @return  A TableData object listing the available databases and tables
     */
    public TableData getTables(TableData.DatabaseDirection direction);

    /**
     *
     * @param direction The direction in which the tables should be copied
     * @param tableList An array of strings in the format database.table indicating the tables to be copied
     * @return  The formatted wiki text for the TableToMove page indicating which tables were copied
     */
    public String copyTables(TableData.DatabaseDirection direction, String[] tableList) throws Exception;


    /**
     * Utility/Convinience class.
     * Use TableCopyService.App.getInstance() to access static instance of TableCopyServletAsync
     */
    public static class App {
        private static TableCopyServiceAsync ourInstance = null;

        public static synchronized TableCopyServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (TableCopyServiceAsync) GWT.create(TableCopyService.class);
//                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(GWT.getModuleBaseURL() + "gs.web.admin.gwt.TableCopy/TableCopyService");
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("services/TableCopyService");
//                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/gs.web.admin.gwt.TableCopyGWTPanel/services/TableCopyService");
            }
            return ourInstance;
        }
    }
}
