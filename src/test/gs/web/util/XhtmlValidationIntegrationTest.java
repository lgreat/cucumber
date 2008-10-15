package gs.web.util;

import gs.web.BaseHtmlUnitIntegrationTestCase;

/**
 * Add pages that should undergoe XHTML validation to this test
 */
public class XhtmlValidationIntegrationTest extends BaseHtmlUnitIntegrationTestCase {

    public void testHomePage() {
        assertValidXhtml(INTEGRATION_HOST + "/");
    }

    public void testPrivacyPolicy() {
        assertValidXhtml(INTEGRATION_HOST + "/about/privacyStatement.page");
    }

    public void testSearchResults() {
        assertValidXhtml(INTEGRATION_HOST + "/search/search.page?state=AK&q=Anchorage&type=school");
    }

    public void testSponsorsAndPartners() {
        assertValidXhtml(INTEGRATION_HOST + "/about/sponsors.page?state=AK");
    }

    public void testPartnerOpportunities() {
        assertValidXhtml(INTEGRATION_HOST + "/about/partnerOpportunities.page?state=ca#advertise");
    }

    public void testResearchAndCompare() {
        assertValidXhtml(INTEGRATION_HOST + "/school/research.page?state=AK");
    }

    public void testResearchAndCompareNationalPage() {
        assertValidXhtml(INTEGRATION_HOST + "/school/research.page");
    }

    public void testCityPage() {
        assertValidXhtml(INTEGRATION_HOST + "/city/Anchorage/AK");
    }

    public void testNearbyCitiesPage() {
        assertValidXhtml(INTEGRATION_HOST + "/cities.page?includeState=1&order=alpha&all=1&city=Anchorage&state=AK");
    }

    public void testRatingsPage() {
        assertValidXhtml(INTEGRATION_HOST + "/school/rating.page?id=184&state=AK");
    }

    public void testSPPOverview() {
        assertValidXhtml(INTEGRATION_HOST + "/school/overview.page?state=ak&id=184");
    }

    public void testSPPParentReviews() {
        assertValidXhtml(INTEGRATION_HOST + "/school/parentReviews.page?state=ak&id=184");
    }

    public void testBrowseCity() {
        assertValidXhtml(INTEGRATION_HOST + "/alaska/hope/schools/");
    }

    public void testBrowseDistrict() {
        assertValidXhtml(INTEGRATION_HOST + "/schools.page?district=4&state=ak");
    }

    public void testMapSchool() {
        assertValidXhtml(INTEGRATION_HOST + "/school/mapSchool.page?state=ak&id=4");
    }

    public void testAllSchools() {
        assertValidXhtml(INTEGRATION_HOST + "/schools/Hope/AK");
    }

    public void testAllCities() {
        assertValidXhtml(INTEGRATION_HOST + "/schools/cities/Hope/AK");
    }

    public void testAllDistricts() {
        assertValidXhtml(INTEGRATION_HOST + "/schools/districts/Hope/AK");
    }

    public void testStyleGuide() {
        assertValidXhtml(INTEGRATION_HOST + "/sandbox/articleStyleGuide.page");
    }

    public void testBackToSchool() {
        assertValidXhtml(INTEGRATION_HOST + "/content/backToSchool.page");
    }

    public void testHolidayLearning() {
        assertValidXhtml(INTEGRATION_HOST + "/content/holidayLearning.page");
    }

    public void testHealthyKidsPage() {
        assertValidXhtml(INTEGRATION_HOST + "/content/healthyKids.page");
    }

    public void testSchoolChoiceCenter() {
        assertValidXhtml(INTEGRATION_HOST + "/content/schoolChoiceCenter.page");
    }

    public void testCountdownToCollege() {
        assertValidXhtml(INTEGRATION_HOST + "/content/countdownToCollege.page");
    }

    public void testSummerReading() {
        assertValidXhtml(INTEGRATION_HOST + "/content/summerReading.page");
    }

    public void testSummerPlanning() {
        assertValidXhtml(INTEGRATION_HOST + "/content/summerPlanning.page");
    }

    public void testSpecialNeeds() {
        assertValidXhtml(INTEGRATION_HOST + "/content/specialNeeds.page");
    }

    public void testMediaChoices() {
        assertValidXhtml(INTEGRATION_HOST + "/content/mediaChoices.page");
    }

    public void testLoginOrRegister() {
        assertValidXhtml(INTEGRATION_HOST + "/community/loginOrRegister.page");
    }

    public void testForgotPassword() {
        assertValidXhtml(INTEGRATION_HOST + "/community/forgotPassword.page");
    }

    public void testRegistrationPageOne() {
        assertValidXhtml(INTEGRATION_HOST + "/community/registration.page");
    }

    public void testSchoolLevelPage() {
        assertValidXhtml(INTEGRATION_HOST + "/survey/start.page?cpn=HouseAd_parentsurvey&id=4&state=AK");
    }

    public void testSurveyPage1() {
        assertValidXhtml(INTEGRATION_HOST + "/survey/form.page?level=e&id=4&state=AK");
    }

    public void testSurveyPage2() {
        assertValidXhtml(INTEGRATION_HOST + "/survey/form.page?id=4&state=AK&level=e&p=2&year=2004");
    }

    public void testSurveyPage3() {
        assertValidXhtml(INTEGRATION_HOST + "/survey/form.page?id=4&state=AK&level=e&p=3&year=2004");
    }

    public void testLibraryPage() {
        assertValidXhtml(INTEGRATION_HOST + "/education-topics/");
    }
}
