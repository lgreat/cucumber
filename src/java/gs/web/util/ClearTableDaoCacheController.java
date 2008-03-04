package gs.web.util;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import gs.data.util.table.AbstractCachedTableDao;
import gs.data.util.table.ITableRow;
import gs.data.util.CachedItem;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ClearTableDaoCacheController implements Controller {

    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        InformativeCachedDao dao = new InformativeCachedDao();

        if (request.getParameter("debug") != null) {
            try {
                out.print("Dumping contents of cache:\n\n");
                Map<String, CachedItem<List<ITableRow>>> cache =
                        new HashMap<String, CachedItem<List<ITableRow>>>(dao.getCache());
                Set<String> keys = cache.keySet();
                if (keys != null) {
                    for (String key: keys) {
                        out.print(key + ":\n");
                        CachedItem<List<ITableRow>> item = cache.get(key);
                        List<ITableRow> rows = item.getData();
                        for (ITableRow row: rows) {
                            out.print("Item {\n");
                            for (Object col: row.getColumnNames()) {
                                out.print("  " + col + ": " + row.get(col) + "\n");
                            }
                            out.print("}\n");
                        }
                        out.print("------------------------------\n");
                    }
                }
            } catch (Exception e) {
                out.print("Error dumping cache contents of type " +
                        e.getClass().getName() + ": " + e.getMessage());
            }
        }
        if (request.getParameter("debug") == null) {
            out.print("\n\n\nClearing cache ...");
            dao.clearCache();
            out.print(" done!");
        }

        out.flush();
        return null;
    }

    private static class InformativeCachedDao extends AbstractCachedTableDao {
        protected String getCacheKey() { return null; }
        protected List<ITableRow> getRowsByKeyExternal(String keyName, String keyValue) { return null; }

        public Map<String, CachedItem<List<ITableRow>>> getCache() {
            return _cache;
        }
    }
}
