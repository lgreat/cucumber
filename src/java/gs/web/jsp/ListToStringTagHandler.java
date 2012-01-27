package gs.web.jsp;

import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.List;

/**
 * String-izes a list of objects.
 *
 * @author jkirton
 */
public class ListToStringTagHandler extends SimpleTagSupport {

    private String _dlm;
    
    private String _nullListToken;
    
    private String _emptyListToken;
    
    private List<Object> _list;

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter jspWriter = getJspContext().getOut();

        if(_list == null) {
            if(_nullListToken != null) jspWriter.write(_nullListToken);
            return;
        }

        if(_list.size() == 0) {
            if(_emptyListToken != null) jspWriter.write(_emptyListToken);
            return;
        }
        
        if(_dlm == null || _dlm.length() == 0) _dlm = ", ";

        String s = StringUtils.join(_list, _dlm);
        
        if(s != null) s = s.trim();

        jspWriter.write(s);
    }

    public String getDlm() {
        return _dlm;
    }

    public String getNullListToken() {
        return _nullListToken;
    }

    public String getEmptyListToken() {
        return _emptyListToken;
    }

    public List<Object> getList() {
        return _list;
    }

    public void setDlm(String dlm) {
        this._dlm = dlm;
    }

    public void setList(List<Object> list) {
        this._list = list;
    }

    public void setNullListToken(String nullListToken) {
        this._nullListToken = nullListToken;
    }

    public void setEmptyListToken(String emptyListToken) {
        this._emptyListToken = emptyListToken;
    }
}
