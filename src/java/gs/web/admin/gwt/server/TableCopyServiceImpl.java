package gs.web.admin.gwt.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import gs.web.admin.gwt.client.TableCopyService;
import gs.web.admin.gwt.client.TableData;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;


public class TableCopyServiceImpl extends RemoteServiceServlet implements TableCopyService {
    List gsList = new ArrayList();
    List azList = new ArrayList();
    TableData tableData = new TableData();


    public TableCopyServiceImpl() {
        gsList.add("table1");
        gsList.add("table2");
        azList.add("az_table1");
        azList.add("az_table2");
        tableData.addDatabase("gs_schooldb", gsList);
        tableData.addDatabase("_az", azList);
    }

    public TableData getTables(TableData.DatabaseDirection direction) {
        return tableData;
    }
}