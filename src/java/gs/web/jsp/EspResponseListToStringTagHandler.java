package gs.web.jsp;

import gs.data.school.EspResponse;
import gs.data.util.string.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * String-izes a list of objects.
 *
 * @author jkirton
 */
public class EspResponseListToStringTagHandler extends SimpleTagSupport {

    private String _dlm;
    
    private String _nullListToken;
    
    private String _emptyListToken;
    
    private List<EspResponse> _list;
    
    private Boolean _pretty = Boolean.TRUE;

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
        
        if(_pretty == null) _pretty = Boolean.TRUE;
        
        ArrayList<String> slist = new ArrayList<String>(_list.size());
        for(EspResponse r : _list) {
            if(r != null) {
                String s = _pretty ? r.getPrettyValue() : r.getValue(); 
                if(s != null) slist.add(s);
            }
        }

        String s = StringUtils.joinPretty(slist.iterator(), _dlm);
        
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

    public List<?> getList() {
        return _list;
    }

    public void setDlm(String dlm) {
        this._dlm = dlm;
    }

    public void setList(List<EspResponse> list) {
        this._list = list;
    }

    public void setNullListToken(String nullListToken) {
        this._nullListToken = nullListToken;
    }

    public void setEmptyListToken(String emptyListToken) {
        this._emptyListToken = emptyListToken;
    }

    public Boolean isPretty() {
        return _pretty;
    }

    public void setPretty(Boolean pretty) {
        this._pretty = pretty;
    }
}
