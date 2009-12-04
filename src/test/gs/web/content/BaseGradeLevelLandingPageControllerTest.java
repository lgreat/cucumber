package gs.web.content;

import gs.web.BaseControllerTestCase;
import gs.web.util.MockSessionContext;
import gs.data.util.table.ITableDao;
import gs.data.util.table.HashMapTableRow;
import gs.data.util.table.ITableRow;
import gs.data.util.NameValuePair;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.geo.ICity;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import static org.easymock.EasyMock.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: npatury
 * Date: Mar 6, 2009
 * Time: 6:02:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class BaseGradeLevelLandingPageControllerTest extends BaseControllerTestCase{
    BaseGradeLevelLandingPageController _controller;
    private ITableDao _tableDao;
//    private ICity _userCity;
    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;


    @Override
    public void setUp() throws Exception {
        _controller = new BaseGradeLevelLandingPageController();
        _tableDao = createStrictMock(ITableDao.class);
        _controller.setTableDao(_tableDao);
//        _userCity = createMock(ICity.class);
        _geoDao = createMock(IGeoDao.class);
        _schoolDao = createMock(ISchoolDao.class);
        _controller.setGeoDao(_geoDao);
        _controller.setSchoolDao(_schoolDao);

    }

    public void replayMocks(){
        replay(_tableDao);
        replay(_geoDao);
        replay(_schoolDao);        
    }

     public void verifyMocks(){
        verify(_tableDao);
        verify(_geoDao);
        verify(_schoolDao);
    }
   
    public void testLoadTableRowsIntoModel1(){

        List<ITableRow> rows = new ArrayList<ITableRow>();

        HashMapTableRow hashMapTableRow1 = new HashMapTableRow();
        hashMapTableRow1.addCell("key", "teaserText_k");
        hashMapTableRow1.addCell("text", "March is national reading month.Provide your child with many different language and reading experiences that are playful and fun.");
        hashMapTableRow1.addCell("url","");
        rows.add(hashMapTableRow1);
    
        HashMapTableRow hashMapTableRow2 = new HashMapTableRow();
        hashMapTableRow2 = new HashMapTableRow();
        hashMapTableRow2.addCell("key", "callToAction_k");
        hashMapTableRow2.addCell("text", "Find out how childrin develop reading skills >");
        hashMapTableRow2.addCell("url","http://www.greatschools.org/cgi-bin/showarticle/2037");
        rows.add(hashMapTableRow2);

        HashMapTableRow hashMapTableRow3 = new HashMapTableRow();
        hashMapTableRow3 = new HashMapTableRow();
        hashMapTableRow3.addCell("key", "articleLink_k");
        hashMapTableRow3.addCell("text", "Reading");
        hashMapTableRow3.addCell("url","http://www.greatschools.org/cgi-bin/showarticle/553");
        rows.add(hashMapTableRow3);

        HashMapTableRow hashMapTableRow4 = new HashMapTableRow();
        hashMapTableRow4 = new HashMapTableRow();
        hashMapTableRow4.addCell("key", "articleLink_k");
        hashMapTableRow4.addCell("text", "Math");
        hashMapTableRow4.addCell("url","http://www.greatschools.org/cgi-bin/showarticle/395");
        rows.add(hashMapTableRow4);

        _controller.setTableDao(_tableDao);
        expect(_tableDao.getAllRows()).andReturn(rows);
        Map<String,Object> model = new HashMap<String,Object>();
        replayMocks();
        _controller.loadTableRowsIntoModel(model);
        verifyMocks();
        assertNotNull(model.get("keyRowMap"));
        Map<String, Object> keyRowMap = (HashMap<String,Object>)model.get("keyRowMap");
        List<ITableRow> row1 = (ArrayList<ITableRow>)keyRowMap.get("teaserText_k");
        List<ITableRow> row2 = (ArrayList<ITableRow>)keyRowMap.get("callToAction_k");

        assertSame(hashMapTableRow1,row1.get(0));
        assertSame(hashMapTableRow2,row2.get(0));

        String keySuffix = "k";
        _controller.loadTableRowsIntoModel(model,keySuffix);
        assertEquals(model.get("teaserText_k"),hashMapTableRow1.get("text"));
        assertEquals(model.get("callToAction_k"),hashMapTableRow2.get("text"));

        List <NameValuePair<String, String>> textUrls = new ArrayList<NameValuePair<String, String>>();
        NameValuePair<String, String> textUrl = new NameValuePair<String, String>(hashMapTableRow3.get("text").toString(), hashMapTableRow3.get("url").toString());
        textUrls.add(textUrl);
        textUrl = new NameValuePair<String, String>(hashMapTableRow4.get("text").toString(), hashMapTableRow4.get("url").toString());
        textUrls.add(textUrl);

        List <NameValuePair<String, String>> expectedTextUrls = (ArrayList<NameValuePair<String, String>>)model.get("articleLink_k");
        assertEquals(expectedTextUrls.get(0).getValue(),textUrls.get(0).getValue());
        assertEquals(expectedTextUrls.get(0).getKey(),textUrls.get(0).getKey());

    }

    public void testLoadTopRatedSchools(){
        Map<String,Object> model = new HashMap<String,Object>();

        City c = new City();
        c.setName("Alameda");
        c.setState(State.CA);
        MockSessionContext sessionContext = new MockSessionContext();
        sessionContext.setCity(c);
        List<City> cities = new ArrayList<City>();
        cities.add(c);

        School s = new School();

        _controller.setLevelCode(LevelCode.ELEMENTARY);
        List<ISchoolDao.ITopRatedSchool> topRatedSchools = new ArrayList<ISchoolDao.ITopRatedSchool>();
        ISchoolDao.ITopRatedSchool topRatedSchool = createMock(ISchoolDao.ITopRatedSchool.class);
        topRatedSchools.add(topRatedSchool);
        expect(_geoDao.findAllCitiesByState(State.CA)).andReturn(cities);
        expect(_schoolDao.findTopRatedSchoolsInCity(c,1,LevelCode.ELEMENTARY.getLowestLevel(),5)).andReturn(topRatedSchools);
        expect(topRatedSchool.getSchool()).andReturn(s);
        replayMocks();
        replay(topRatedSchool);

        _controller.loadTopRatedSchools(model,sessionContext);
        verifyMocks();
        verify(topRatedSchool);
        
        assertNotNull(model);
        assertNotNull(model.get("cityObject"));
        assertSame(c,model.get("cityObject"));
        assertNotNull(model.get("cityList"));
        List<City> cityList = (List<City>)model.get("cityList");
        assertEquals(1,cityList.size());
        assertEquals("Alameda",cityList.get(0).getName());
        assertEquals(State.CA,cityList.get(0).getState());
        assertNotNull(model.get("topRatedSchools"));
        List<School> topRated = (ArrayList<School>)model.get("topRatedSchools");
        List<School> topSchool = (ArrayList<School>)model.get("topRatedSchools");
        assertEquals(1,topRated.size());
        assertNotNull(model.get("topSchools"));
        assertEquals(1,topSchool.size());

    }


}
