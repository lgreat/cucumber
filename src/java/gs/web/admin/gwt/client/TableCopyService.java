package gs.web.admin.gwt.client;

import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.core.client.GWT;


public interface TableCopyService extends RemoteService {
    public TableData getTables(TableData.DatabaseDirection direction);


    /**
     * Utility/Convinience class.
     * Use TableCopyService.App.getInstance() to access static instance of TableCopyServletAsync
     */
    public static class App {
        private static TableCopyServiceAsync ourInstance = null;
        static void setInstance(TableCopyServiceAsync servlet) {
            ourInstance = servlet;
        }

        public static synchronized TableCopyServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (TableCopyServiceAsync) GWT.create(TableCopyService.class);
//                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(GWT.getModuleBaseURL() + "gs.web.admin.gwt.TableCopy/TableCopyService");
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(GWT.getModuleBaseURL() + "/services/TableCopyService");
            }
            return ourInstance;
        }
    }
}
