package gs.web.school.usp;

import gs.data.school.EspResponse;
import gs.data.school.EspResponseFluentInterface;
import gs.data.school.EspResponseSource;
import gs.data.school.School;
import gs.data.util.ListUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertNull;

@SuppressWarnings("unchecked")
public class EspResponseDataTest {
    EspResponseData _espResponseData;
    School _school;

    private EspResponseFluentInterface basicResponse() {
        return EspResponse.with()
            .school(_school)
            .active(true)
            .memberId(1)
            .value("value")
            .created(new Date());
    }

    @Before
    public void setUp() throws Exception {
        _school = new School();
        _school.setId(1);
        _school.setName("Test school");

        _espResponseData = new EspResponseData();
    }

    @Test
    public void testBasics() {
        assertNull(
            "Expect espResponseData to have null map", ReflectionTestUtils.getField(
                _espResponseData, "_responsesBySource"
            )
        );
    }

    @Test
    public void testGetOspResponses() throws Exception {
        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.osp).create(),
                basicResponse().source(EspResponseSource.usp).create()
            )
        );

        List<EspResponse> responses = _espResponseData.getOspResponses();

        assertEquals(
            "Expect responses to contain only one result, since only one result had source of OSP", 1, responses.size()
        );
        assertEquals(
            "Expect to receive only responses with source of osp", EspResponseSource.osp, responses.get(0).getSource()
        );

        // test making the call again returns same results:

        responses = _espResponseData.getOspResponses();

        assertEquals(
            "Expect responses to contain only one result, since only one result had source of OSP", 1, responses.size()
        );
        assertEquals(
            "Expect to receive only responses with source of osp", EspResponseSource.osp, responses.get(0).getSource()
        );
    }

    @Test
    public void testGetOspResponses_noResults() throws Exception {
        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.usp).create(),
                basicResponse().source(EspResponseSource.usp).create()
            )
        );

        List<EspResponse> responses = _espResponseData.getOspResponses();

        assertEquals("Expect responses to no results", 0, responses.size());
    }

    @Test
    public void testGetOspResponses_noResults_noInput() throws Exception {
        assertEquals("Expect responses to no results", 0, _espResponseData.getOspResponses().size());
    }

    @Test
    public void testGetUspResponses() throws Exception {
        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.osp).create(),
                basicResponse().source(EspResponseSource.usp).create()
            )
        );

        List<EspResponse> responses = _espResponseData.getOspResponses();

        assertEquals(
            "Expect responses to contain only one result, since only one result had source of OSP", 1, responses.size()
        );
        assertEquals(
            "Expect to receive only responses with source of osp", EspResponseSource.osp, responses.get(0).getSource()
        );

        // test making the call again gives same results:

        responses = _espResponseData.getOspResponses();

        assertEquals(
            "Expect responses to contain only one result, since only one result had source of OSP", 1, responses.size()
        );
        assertEquals(
            "Expect to receive only responses with source of osp", EspResponseSource.osp, responses.get(0).getSource()
        );
    }

    @Test
    public void testGetUspResponses_noResults() throws Exception {
        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.osp).create(),
                basicResponse().source(EspResponseSource.osp).create()
            )
        );

        List<EspResponse> responses = _espResponseData.getUspResponses();

        assertEquals("Expect responses to no results", 0, responses.size());
    }

    @Test
    public void testGetUspResponses_noResults_noInput() throws Exception {
        assertEquals("Expect responses to no results", 0, _espResponseData.getUspResponses().size());
    }

    @Test
    public void testHasOspResponseData() throws Exception {
        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.osp).create()
            )
        );

        assertTrue("Expect espResponseData to have OSP data", _espResponseData.hasOspResponseData());

        _espResponseData.clear();

        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.usp).create()
            )
        );

        assertFalse("Expect espResponseData to have no OSP data", _espResponseData.hasOspResponseData());
    }

    @Test
    public void testHasOspResponseData_noInput() throws Exception {
        assertFalse("Expect espResponseData to have no OSP data", _espResponseData.hasOspResponseData());
    }

    @Test
    public void testHasUspResponseData() throws Exception {
        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.usp).create()
            )
        );

        assertTrue("Expect espResponseData to have USP data", _espResponseData.hasUspResponseData());
    }

    @Test
    public void testHasUspResponseData_noInput() throws Exception {
        assertFalse("Expect espResponseData to have no USP data", _espResponseData.hasUspResponseData());
    }

    @Test
    public void testGetResponsesBySource() throws Exception {
        assertEquals(
            "Expect getResponses to empty map, rather than null", 0, _espResponseData.getResponsesBySource(
            EspResponseSource.osp
        ).size()
        );

        _espResponseData.addAll(
            ListUtils.newArrayList(
                basicResponse().source(EspResponseSource.osp).create(),
                basicResponse().source(EspResponseSource.usp).create(),
                basicResponse().source(EspResponseSource.osp).create(),
                basicResponse().source(EspResponseSource.usp).create()
            )
        );

        assertEquals(
            "Expect getResponses to return same thing as helper method",
            _espResponseData.getOspResponses(),
            _espResponseData.getResponsesBySource(EspResponseSource.osp)
        );
        assertEquals(
            "Expect getResponses to return same thing as helper method",
            _espResponseData.getUspResponses(),
            _espResponseData.getResponsesBySource(EspResponseSource.usp)
        );
    }

    @Test
    public void testGroupBySource() throws Exception {
        Map<EspResponseSource, List<EspResponse>> responseSourceMap = new HashMap<EspResponseSource, List<EspResponse>>();

        assertNull(
            "Expect espResponseData to have null map", ReflectionTestUtils.getField(
            _espResponseData, "_responsesBySource"
        )
        );

        EspResponse response1 = basicResponse().source(EspResponseSource.osp).create();
        EspResponse response2 = basicResponse().source(EspResponseSource.usp).create();
        EspResponse response3 = basicResponse().source(EspResponseSource.osp).create();
        EspResponse response4 = basicResponse().source(EspResponseSource.usp).create();

        _espResponseData.addAll(ListUtils.newArrayList(response1, response2, response3, response4));

        _espResponseData.groupBySource();

        responseSourceMap.put(EspResponseSource.osp, ListUtils.newArrayList(response1, response3));
        responseSourceMap.put(EspResponseSource.usp, ListUtils.newArrayList(response2, response4));

        assertFalse(
            "Expect espResponseData to have non-empty map",
            ((Map<EspResponseSource, List<EspResponse>>) ReflectionTestUtils.getField(
                _espResponseData, "_responsesBySource"
            )).isEmpty()
        );


        Map<EspResponseSource, List<EspResponse>> resultMap = ((Map<EspResponseSource, List<EspResponse>>) ReflectionTestUtils
            .getField(
                _espResponseData, "_responsesBySource"
            ));

        for (EspResponseSource source : resultMap.keySet()) {
            assertEquals(
                "Expect espResponseData's internal map to match our expected map",
                responseSourceMap.get(source),
                resultMap.get(source)
            );
        }
    }
}
