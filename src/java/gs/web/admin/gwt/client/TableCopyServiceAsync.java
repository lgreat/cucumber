package gs.web.admin.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface TableCopyServiceAsync {
    void getTables(TableData.DatabaseDirection direction, AsyncCallback async);
}
