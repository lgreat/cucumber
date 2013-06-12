package gs.web.school.usp;

import gs.data.school.EspResponse;
import gs.data.school.EspResponseSource;
import gs.data.school.IEspResponseDao;
import gs.data.school.School;
import gs.web.BaseControllerTestCase;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.easymock.EasyMock.expect;

public class EspStatusManagerTest extends BaseControllerTestCase {
    EspStatusManager _espStatusManager;
    private IEspResponseDao _espResponseDao;
    private School _school;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        _espResponseDao = EasyMock.createStrictMock(IEspResponseDao.class);
        _school = new School();
        _espStatusManager = new EspStatusManager(_school);
        _espStatusManager.setEspResponseDao(_espResponseDao);
    }

    private void resetAllMocks() {
        _school = new School();
        _espStatusManager = new EspStatusManager(_school);
        _espStatusManager.setEspResponseDao(_espResponseDao);
        resetMocks(_espResponseDao);
    }

    private void replayAllMocks() {
        replayMocks(_espResponseDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_espResponseDao);
    }

    @Test
    public void testGetStatusNull() {
        //Null responses
        expect(_espResponseDao.getAllActiveResponses(_school)).andReturn(null);
        replayAllMocks();
        EspStatus status = _espStatusManager.getEspStatus();
        verifyAllMocks();
        assertEquals("Null response from the database.Hence NO_DATA.", EspStatus.NO_DATA, status);
    }

    @Test
    public void testGetStatusEmpty() {
        //empty responses
        expect(_espResponseDao.getAllActiveResponses(_school)).andReturn(new ArrayList<EspResponse>());
        replayAllMocks();
        EspStatus status = _espStatusManager.getEspStatus();
        verifyAllMocks();
        assertEquals("Empty responses from the database.Hence NO_DATA.", EspStatus.NO_DATA, status);
    }

    @Test
    public void testGetStatus_OspPreferred() throws Exception {
        expect(_espResponseDao.getAllActiveResponses(_school)).andReturn(getAllOspResponses_RecentResponses());
        replayAllMocks();
        EspStatus status = _espStatusManager.getEspStatus();
        verifyAllMocks();
        assertEquals("All the osp questions were answered and the responses were recent.", EspStatus.OSP_PREFERRED, status);

        resetAllMocks();

        List<EspResponse> responses = new ArrayList<EspResponse>();
        responses.addAll(getAllOspResponses_RecentResponses());
        responses.addAll(getUspResponses());
        expect(_espResponseDao.getAllActiveResponses(_school)).andReturn(responses);
        replayAllMocks();
        status = _espStatusManager.getEspStatus();
        verifyAllMocks();
        assertEquals("There are usp responses. However all the osp questions were answered and the responses were " +
                "recent.", EspStatus.OSP_PREFERRED, status);
    }

    @Test
    public void testGetStatus_UspOnly() throws Exception {
        expect(_espResponseDao.getAllActiveResponses(_school)).andReturn(getUspResponses());
        replayAllMocks();
        EspStatus status = _espStatusManager.getEspStatus();
        verifyAllMocks();
        assertEquals("No osp responses, only usp responses.", EspStatus.USP_ONLY, status);
    }

    @Test
    public void testGetStatus_OspOutdated() throws Exception {
        expect(_espResponseDao.getAllActiveResponses(_school)).andReturn(getAllOspResponses_OutdatedResponses());
        replayAllMocks();
        EspStatus status = _espStatusManager.getEspStatus();
        verifyAllMocks();
        assertEquals("All the osp questions are answered.However they are not recent responses.", EspStatus.OSP_OUTDATED, status);

        resetAllMocks();

        expect(_espResponseDao.getAllActiveResponses(_school)).andReturn(getFewOspResponses_RecentResponses());
        replayAllMocks();
        status = _espStatusManager.getEspStatus();
        verifyAllMocks();
        assertEquals("All the osp questions were not answered.", EspStatus.OSP_OUTDATED, status);
    }

    @Test
    public void testGetStatus_Mix() throws Exception {
        List<EspResponse> responses = new ArrayList<EspResponse>();
        responses.addAll(getAllOspResponses_OutdatedResponses());
        responses.addAll(getUspResponses());

        expect(_espResponseDao.getAllActiveResponses(_school)).andReturn((responses));
        replayAllMocks();
        EspStatus status = _espStatusManager.getEspStatus();
        verifyAllMocks();
        assertEquals("All the osp questions are answered but are not recent.There are usp responses.", EspStatus.MIX, status);

        resetAllMocks();

        responses = new ArrayList<EspResponse>();
        responses.addAll(getFewOspResponses_RecentResponses());
        responses.addAll(getUspResponses());
        expect(_espResponseDao.getAllActiveResponses(_school)).andReturn(responses);
        replayAllMocks();
        status = _espStatusManager.getEspStatus();
        verifyAllMocks();
        assertEquals("All the osp questions are answered not answered.There are usp responses.", EspStatus.MIX, status);

        resetAllMocks();

        responses = new ArrayList<EspResponse>();
        responses.addAll(getFewOspResponses_OutdatedResponses());
        responses.addAll(getUspResponses());
        expect(_espResponseDao.getAllActiveResponses(_school)).andReturn(responses);
        replayAllMocks();
        status = _espStatusManager.getEspStatus();
        verifyAllMocks();
        assertEquals("All the osp questions are answered not answered.There are usp responses.", EspStatus.MIX, status);
    }

    //Helper method to get all recent osp responses for all the questions.
    public List<EspResponse> getAllOspResponses_RecentResponses() throws Exception {
        List<EspResponse> responses = new ArrayList<EspResponse>();
        Date date = new Date();

        EspResponse espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.ARTS_MEDIA_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.ARTS_MUSIC_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.ARTS_PERFORMING_WRITTEN_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.ARTS_VISUAL_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.EXTENDED_CARE_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.STAFF_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.FACILITIES_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        //Other field is filled in.
        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.GIRLS_SPORTS_OTHER_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        //Other field is filled in.
        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.FOREIGN_LANGUAGES_OTHER_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        //Other field is filled in.
        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.TRANSPORTATION_OTHER_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.BOYS_SPORTS_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.PARENT_INVOLVEMENT_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        return responses;
    }

    //Helper method to get all osp responses for all the questions.However parent involvement response is not recent.
    public List<EspResponse> getAllOspResponses_OutdatedResponses() throws Exception {
        List<EspResponse> responses = new ArrayList<EspResponse>();
        Date date = new Date();

        EspResponse espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.ARTS_MEDIA_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.ARTS_MUSIC_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.ARTS_PERFORMING_WRITTEN_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.ARTS_VISUAL_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.EXTENDED_CARE_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.STAFF_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.FACILITIES_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.GIRLS_SPORTS_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.FOREIGN_LANGUAGES_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.TRANSPORTATION_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        //Other field is filled in.
        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.BOYS_SPORTS_OTHER_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        //Other field is filled in.
        //parent involvement response is not recent.
        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.PARENT_INVOLVEMENT_OTHER_RESPONSE_KEY);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy, MM, dd");
        date = dateFormat.parse("2009, 12, 9");
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        return responses;
    }

    //Helper method to get a few osp responses. Parent involvement response is not recent.
    public List<EspResponse> getFewOspResponses_OutdatedResponses() throws Exception {
        List<EspResponse> responses = new ArrayList<EspResponse>();
        Date date = new Date();

        EspResponse espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.ARTS_MEDIA_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.BOYS_SPORTS_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        //parent involvement response is not recent.
        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.PARENT_INVOLVEMENT_RESPONSE_KEY);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy, MM, dd");
        date = dateFormat.parse("2009, 12, 9");
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        return responses;
    }

    //Helper method to get a few osp responses.All the responses are recent.
    public List<EspResponse> getFewOspResponses_RecentResponses() throws Exception {
        List<EspResponse> responses = new ArrayList<EspResponse>();
        Date date = new Date();

        EspResponse espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.ARTS_MEDIA_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.BOYS_SPORTS_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.osp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.PARENT_INVOLVEMENT_RESPONSE_KEY);
        espResponse.setCreated(date);
        espResponse.setSource(EspResponseSource.osp);
        responses.add(espResponse);

        return responses;
    }

    //Helper method to get a few usp responses.All the responses are recent.
    public List<EspResponse> getUspResponses() throws Exception {
        List<EspResponse> responses = new ArrayList<EspResponse>();
        Date date = new Date();

        EspResponse espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.ARTS_MEDIA_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.usp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        espResponse = new EspResponse();
        espResponse.setKey(UspFormHelper.BOYS_SPORTS_RESPONSE_KEY);
        espResponse.setSource(EspResponseSource.usp);
        espResponse.setCreated(date);
        responses.add(espResponse);

        return responses;
    }

}