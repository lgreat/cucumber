package gs.web.school;

import gs.data.school.*;
import gs.data.school.census.CensusDataSet;
import gs.data.state.State;
import gs.web.request.RequestAttributeHelper;
import gs.web.school.AbstractDataHelper;
import gs.web.school.CensusDataHolder;
import gs.web.school.SchoolProfileCensusHelper;
import gs.web.school.SchoolProfileDataHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RequestMapping("/school/printYOChooser")
@Component("printYourOwnChooserController")
public class PrintYourOwnChooserController implements BeanFactoryAware {

    private BeanFactory _beanFactory;

    @Autowired
    private IEspResponseDao _espResponseDao;

    @Autowired
    private SchoolProfileCensusHelper _schoolProfileCensusHelper;

    @Autowired
    private ISchoolDao _schoolDaoHibernate;


    private static final String MODEL_KEY_BEST_KNOWN_FOR = "bestKnownFor";
    private static final String MODEL_KEY_ETHNICITY_MAP = "ethnicityMap";

    /**
     *
     */
    @RequestMapping(method= RequestMethod.GET)
    public String get(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
                      @RequestParam(value="states", required=true, defaultValue="CA") State[] states,
                      @RequestParam(value="schoolIds", required=true, defaultValue="1") Integer[] schoolIds) {

        AbstractDataHelper.initialize(request);

        List<School> schools = getSchoolsFromParams(states, schoolIds);

        modelMap.put("schools", schools);

        Map<String,Object> schoolData = new HashMap<String,Object>();


        for (School school : schools) {
            Map<String, Object> data = new HashMap<String, Object>();

            addBestKnownForQuoteToModel(school, data);

            addEthnicityDataToModel(school, data);

            schoolData.put(school.getStateAbbreviation().getAbbreviationLowerCase() + String.valueOf(school.getId()), data);
        }

        modelMap.put("schoolData", schoolData);

        return "printYourOwnChooser";

    }

    private void addBestKnownForQuoteToModel(School school, Map<String, Object> data) {
        Map<String, List<EspResponse>> espData = getEspData(school);

        // esp data - "best known for" quote
        String bestKnownFor = null;
        List<EspResponse> espResponses = espData.get( "best_known_for" );
        if (espResponses != null && espResponses.size() > 0) {
            bestKnownFor = espResponses.get(0).getSafeValue();
            data.put(MODEL_KEY_BEST_KNOWN_FOR, bestKnownFor);
        }
    }

    private void addEthnicityDataToModel(School school, Map<String, Object> data) {
        // ethnicity data
        Set<Integer> censusDataTypeIds = new HashSet<Integer>();
        censusDataTypeIds.add(9); // ethnicity
        Map<Integer, CensusDataSet> censusDataSets = _schoolProfileCensusHelper.getCensusDataSets(school.getDatabaseState(), censusDataTypeIds, school);

        // we just need school data for the census data sets we have
        CensusDataHolder censusDataHolder = (CensusDataHolder) _beanFactory.getBean("censusDataHandler", new Object[] {
                school, censusDataSets, censusDataSets, null, null
        });

        censusDataHolder.retrieveDataSetsAndSchoolData(); // after this operation, the censusDataSets will contain school values

        Map<String, String> ethnicityLabelValueMap = _schoolProfileCensusHelper.getEthnicityLabelValueMap(censusDataSets);
        data.put(MODEL_KEY_ETHNICITY_MAP, ethnicityLabelValueMap);
    }

    public Map<String, List<EspResponse>> getEspData(School school) {
        List<EspResponse> results = _espResponseDao.getResponses( school );

        // For performance reasons convert the results to a HashMap.  The key will be the espResponseKey
        // and the value will be the corresponding list of EspResponse objects
        Map<String, List<EspResponse>> espData = EspResponse.rollup(results);

        return espData;
    }

    public List<School> getSchoolsFromParams(State[] states, Integer[] schoolIds) {
        List<School> schools = new ArrayList<School>();

        if (states == null || schoolIds == null || states.length == 0 || schoolIds.length == 0) {
            throw new IllegalArgumentException("Neither states nor schoolIds arrays may be null or empty");
        }
        if (states.length > 1 && (states.length != schoolIds.length)) {
            throw new IllegalArgumentException("If more than one state is provided, the number of states and IDs must be equal");
        }

        for (int i = 0; i < schoolIds.length; i++) {
            State state;
            if (states.length > 1) {
                state = states[i];
            } else {
                state = states[0];
            }

            School s = _schoolDaoHibernate.getSchoolById(state, schoolIds[i]);
            schools.add(s);
        }

        return schools;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }
}
