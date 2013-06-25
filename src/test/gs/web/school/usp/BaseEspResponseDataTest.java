package gs.web.school.usp;

import gs.data.school.EspResponse;
import gs.data.school.EspResponseFluentInterface;
import gs.data.school.EspResponseSource;
import gs.data.school.School;
import gs.data.util.ListUtils;
import org.junit.Test;
import org.junit.Before;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;

@SuppressWarnings("unchecked")
public class BaseEspResponseDataTest {
    private BaseEspResponseData _espResponseData;
    private School _school;

    private DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Before
    public void setUp() throws Exception {
        _school = new School();
        _school.setId(1);
        _school.setName("Test school");

        _espResponseData = new BaseEspResponseData();
    }

    private EspResponseFluentInterface basicResponse() {
        return EspResponse.with()
            .school(_school)
            .active(true)
            .memberId(1)
            .value("value")
            .created(new Date());
    }

    @Test
    public void testClearCache() throws Exception {
        assertNull(
            ReflectionTestUtils.getField(
                _espResponseData, "_responsesByKey"
            )
        );

        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.osp).key("abc").create()
            )
        );

        assertNull(
            ReflectionTestUtils.getField(
                _espResponseData, "_responsesByKey"
            )
        );

        Map<String, List<EspResponse>> responses = _espResponseData.getResponsesByKey();

        assertNotNull(
            ReflectionTestUtils.getField(
                _espResponseData, "_responsesByKey"
            )
        );

        assertEquals(
            "Expect responses to have been updated since we add an item to the underlying list",
            1,
            ((Map<String, List<EspResponse>>) ReflectionTestUtils.getField(
                _espResponseData, "_responsesByKey"
            )).size()
        );

        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.osp).created(new Date()).key("def").create()
            )
        );
        responses = _espResponseData.getResponsesByKey();

        assertNotNull(
            ReflectionTestUtils.getField(
                _espResponseData, "_responsesByKey"
            )
        );
        assertEquals(
            "Expect responses to have been updated since we add an item to the underlying list",
            2,
            ((Map<String, List<EspResponse>>) ReflectionTestUtils.getField(
                _espResponseData, "_responsesByKey"
            )).size()
        );
    }

    @Test
    public void testGetOldestResponseDate() throws Exception {
        assertEquals(
            "Expect to receive null if there is no response data", null, _espResponseData.getOldestResponseDate()
        );

        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.osp).created(DATE_FORMAT.parse("2013-02-01")).create(),
                basicResponse().source(EspResponseSource.osp).created(DATE_FORMAT.parse("2013-01-01")).create(),
                basicResponse().source(EspResponseSource.osp).created(DATE_FORMAT.parse("2013-03-01")).create()
            )
        );

        assertEquals(
            "Expect to receive the oldest date",
            DATE_FORMAT.parse("2013-01-01"),
            _espResponseData.getOldestResponseDate()
        );

        _espResponseData.clear();

        assertEquals(
            "Expect to receive null if there is no response data", null, _espResponseData.getOldestResponseDate()
        );
    }

    @Test
    public void testHasRecentYearOfData() throws Exception {
        assertFalse(
            "Expect there to be no recent year of data, if there is no data", _espResponseData.hasRecentYearOfData()
        );

        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.osp).create()
            )
        );

        assertTrue(
            "Expect there to be data in the recent year, since we added data for today",
            _espResponseData.hasRecentYearOfData()
        );

        _espResponseData.clear();

        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.osp).created(DATE_FORMAT.parse("2000-01-01")).create()
            )
        );

        assertFalse(
            "Expect there to be no recent year of data, since year 2000 was over a year ago",
            _espResponseData.hasRecentYearOfData()
        );
    }

    @Test
    public void testGetResponsesByKey() throws Exception {
        Map<String, List<EspResponse>> responsesByKey = new HashMap<String, List<EspResponse>>();

        assertEquals(
            "Expect empty map to be returned if there's no response data",
            responsesByKey,
            _espResponseData.getResponsesByKey()
        );

        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.osp).key("abc").create(),
                basicResponse().source(EspResponseSource.osp).key("def").create(),
                basicResponse().source(EspResponseSource.osp).key("abc").create(),
                basicResponse().source(EspResponseSource.osp).key("xyz").create()
            )
        );

        responsesByKey = _espResponseData.getResponsesByKey();

        assertEquals(
            "Expect two items for abc", 2, responsesByKey.get("abc").size()
        );
        assertEquals(
            "Expect one item for def", 1, responsesByKey.get("def").size()
        );
        assertEquals(
            "Expect one item for xyz", 1, responsesByKey.get("xyz").size()
        );
    }
}
