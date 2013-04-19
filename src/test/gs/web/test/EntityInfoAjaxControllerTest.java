package gs.web.test;

import gs.data.json.JSONObject;
import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created with IntelliJ IDEA.
 * User: eddie
 * Date: 4/15/13
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class EntityInfoAjaxControllerTest extends BaseControllerTestCase {
    private EntityInfoAjaxController _controller;
    private DistrictsInCityAjaxController _distcontroller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (EntityInfoAjaxController)getApplicationContext().
                getBean(EntityInfoAjaxController.BEAN_ID);
        _distcontroller = (DistrictsInCityAjaxController)getApplicationContext().
                getBean(DistrictsInCityAjaxController.BEAN_ID);
    }

    public void testHandleRequestMultiLevel() throws Exception {
         /*
        getRequest().setParameter("state", "CA");
        getRequest().setParameter("county", "Alamedaa");
        getRequest().setParameter("printOptionsOnly", "true");
        ModelAndView mAndVdist = _distcontroller.handleRequest(getRequest(), getResponse());
        System.out.println("distresult:" + getResponse().getContentAsString());
         */
        getRequest().setParameter("state", "CA");
        getRequest().setParameter("id", "1");
        getRequest().setParameter("entityType", "district");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertNull(mAndV);
        String jsonOutput = getResponse().getContentAsString();
        JSONObject json = new JSONObject(jsonOutput,"UTF-8");
        System.out.println(json.get("grades"));
        //System.out.println(json.toString());
        /*
        System.out.println(json.get("districtId"));
        System.out.println(json.get("schoolType"));
        System.out.println(json.get("grades"));
        System.out.println(json.get("name"));
        System.out.println(json.get("street"));
        System.out.println(json.get("streetLine2"));
        System.out.println(json.get("city"));
        System.out.println(json.get("Zipcode"));
        System.out.println(json.get("county"));
        //System.out.println(json.get("capacity"));
        System.out.println(json.get("enrollment"));
        System.out.println(json.get("phone"));
        System.out.println(json.get("fax"));
        System.out.println(json.get("webSite"));
        System.out.println(json.get("headOfficialName"));
        System.out.println(json.get("headOfficialEmail"));



        System.out.println(json.get("startTime"));
        System.out.println(json.get("endTime"));   //
        System.out.println(json.get("affiliation"));
        System.out.println(json.get("association"));
        System.out.println(json.get("gender"));   //


        System.out.println(json.get("lowAge"));   //
        System.out.println(json.get("highAge"));
        System.out.println(json.get("bilingual"));
        System.out.println(json.get("specialEd"));
        System.out.println(json.get("computers"));
        System.out.println(json.get("extendedCare"));
        System.out.println(json.get("preschoolSubtype"));
        //System.out.println(jsonOutput);
        //System.out.println("ok");
        */
    }


}
