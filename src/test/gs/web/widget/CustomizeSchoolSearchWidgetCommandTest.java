package gs.web.widget;

import junit.framework.TestCase;

import java.util.Map;
import java.util.Iterator;

/**
 * Date: Feb 27, 2009
 * Time: 10:54:09 AM
 */
public class CustomizeSchoolSearchWidgetCommandTest extends TestCase{
    private CustomizeSchoolSearchWidgetCommand _command;

    @Override
    protected void setUp() throws Exception{
        _command = new CustomizeSchoolSearchWidgetCommand();
    }

    public void testGetDefaultColorMap(){
        Map<String,String> map =_command.getDefaultColorMap();
        assertEquals(18,map.size());
        Iterator<String> iter = map.keySet().iterator();
        assertEquals("FFFFFF",iter.next());
        String lastColor ="";
        while(iter.hasNext()){
            lastColor = iter.next();
        }
        assertEquals("000000",lastColor);
    }

    public void testGetDefaultColorMapWithAddtionalColor(){
        Map<String,String> map =_command.getDefaultColorMap("000000","9999FF");
        assertEquals(19,map.size());
        Iterator<String> iter = map.keySet().iterator();
        assertEquals("FFFFFF",iter.next());
        assertEquals("",map.get("9999FF"));
    }


}
