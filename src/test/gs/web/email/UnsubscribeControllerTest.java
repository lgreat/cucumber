package gs.web.email;

import gs.data.community.*;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.util.UrlBuilder;
import org.easymock.IArgumentMatcher;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.createStrictMock;

/**
 * Created by IntelliJ IDEA.
 * User: rramachandran
 * Date: 2/24/12
 * Time: 9:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class UnsubscribeControllerTest extends BaseControllerTestCase {

    private UnsubscribeController _unsubscribeController;

    private IUnsubscribeDao _unsubscribeDao;
    private IUnsubscribedProductsDao _unsubscribedProductsDao;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ExactTargetAPI _exactTargetAPI;

    private User _user;
    private UnsubscribeCommand _command;
    private BindingResult _bindingResult;
    private ModelMap _modelMap;
    private String _view;
    private List<Subscription> _allSubscriptions;

    private static final String Login_Page_View = "redirect:/community/loginOrRegister.page?redirect=/email/management.page";
    private static final String Unsubscribe_Form_View = "/email/unsubscribe";
    private static final String Unsubscribe_Survey_View = "/email/unsubscribeSurvey";

    protected void setUp() throws Exception {
        super.setUp();
        _unsubscribeController = new UnsubscribeController();

        _subscriptionDao = createStrictMock(ISubscriptionDao.class);
        _userDao = createStrictMock(IUserDao.class);
        _unsubscribeDao = createStrictMock(IUnsubscribeDao.class);
        _unsubscribedProductsDao = createStrictMock(IUnsubscribedProductsDao.class);
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);

        _unsubscribeController.setSubscriptionDao(_subscriptionDao);
        _unsubscribeController.setUserDao(_userDao);
        _unsubscribeController.setUnsubscribeDao(_unsubscribeDao);
        _unsubscribeController.setUnsubscribedProductsDao(_unsubscribedProductsDao);
        _unsubscribeController.setExactTargetAPI(_exactTargetAPI);

        _user = new User();
        _user.setId(6789);
        _user.setEmail("me@xyz.com");

        _command = new UnsubscribeCommand();
        /* http://stackoverflow.com/questions/877423/mocking-spring-mvc-bindingresult-when-using-annotations */
        _bindingResult = new BeanPropertyBindingResult(_command, "emailCmd");

        _modelMap = new ModelMap();

//        SessionContext sc = new SessionContext();
//        sc.setUser(_user);
//        SessionContextUtil util = new SessionContextUtil();
//        sc.setSessionContextUtil(util);
//        CookieGenerator cg = new CookieGenerator();
//        cg.setCookieName("MEMID");
//        util.setMemberIdCookieGenerator(cg);
//        getRequest().setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sc);
    }

    public void replayAll() {
        super.replayMocks(_subscriptionDao, _unsubscribeDao, _userDao, _unsubscribedProductsDao, _exactTargetAPI);
    }

    public void verifyAll() {
        super.verifyMocks(_subscriptionDao, _unsubscribeDao, _userDao, _unsubscribedProductsDao, _exactTargetAPI);
    }

    public void resetAll() {
        super.resetMocks(_subscriptionDao, _unsubscribeDao, _userDao, _unsubscribedProductsDao, _exactTargetAPI);
    }

    public void setSampleSubscriptions(){
        _allSubscriptions = new ArrayList<Subscription>();

        _allSubscriptions.add(createSubscription(1234, SubscriptionProduct.PARENT_ADVISOR, 0, State.AZ));
        _allSubscriptions.add(createSubscription(1235, SubscriptionProduct.DAILY_TIP, 0, State.CO));
        _allSubscriptions.add(createSubscription(1236, SubscriptionProduct.MYSTAT, 50, State.CO));
        _allSubscriptions.add(createSubscription(1237, SubscriptionProduct.MYSTAT, 51, State.CO));
        _allSubscriptions.add(createSubscription(1238, SubscriptionProduct.MYSTAT, 52, State.CO));
        _allSubscriptions.add(createSubscription(1239, SubscriptionProduct.SPONSOR_OPT_IN, 0, State.CT));

        _command.setUserId(_user.getId());
        _command.setEmail(_user.getEmail());

        getRequest().setParameter("email", _user.getEmail());
        getRequest().setParameter("ref", _user.getId().toString());
        getRequest().setRequestURI("/email/unsubscribe.page");
    }

    public void nonExistingUserTest(){
        getRequest().setRequestURI("/email/unsubscribe.page");
        getRequest().setParameter("email", "blah@blahblah.org");
        getRequest().setParameter("ref", _user.getId().toString());

        resetAll();
        expect(_userDao.findUserFromEmailIfExists(getRequest().getParameter("email"))).andReturn(null);
    }

    /* Invalid user - email address and ref id parameters do not belong to the same user */
    public void testUnsubscribeShowFormInvalidUser(){
        nonExistingUserTest();
        replayAll();
        _view = _unsubscribeController.showForm(_command, _modelMap, getRequest(), getResponse(),
                getRequest().getParameter("email"), getRequest().getParameter("ref"));
        verifyAll();

        assertEquals(Login_Page_View, _view);

        resetAll();
        expect(_userDao.findUserFromEmailIfExists(getRequest().getParameter("email"))).andReturn(_user);

        replayAll();
        _view = _unsubscribeController.showForm(_command, _modelMap, getRequest(), getResponse(),
                getRequest().getParameter("email"), "9876");
        verifyAll();

        assertEquals(Login_Page_View, _view);
    }

    public void testUnsubscribeShowFormValidUser() {
        resetAll();
        expect(_userDao.findUserFromEmailIfExists(getRequest().getParameter("email"))).andReturn(_user).anyTimes();

        replayAll();
        _view = _unsubscribeController.showForm(_command, _modelMap, getRequest(), getResponse(),
                getRequest().getParameter("email"), getRequest().getParameter("ref"));
        verifyAll();

        assertEquals(Unsubscribe_Form_View, _view);
        assertEquals(_modelMap.get("emailCmd"), _command);
    }

    public void testUnsubscribeFormOnSubmitInvalidUser() {
        nonExistingUserTest();
        replayAll();
        _view = _unsubscribeController.showForm(_command, _modelMap, getRequest(), getResponse(),
                getRequest().getParameter("email"), getRequest().getParameter("ref"));
        verifyAll();

        assertEquals(Login_Page_View, _view);
    }

    public void testUnsubscribeFormOnSubmitWithErrors() {
        setSampleSubscriptions();
        getRequest().setParameter("Unsubscribe", "Unsubscribe");

        resetAll();

        replayAll();
        try{
            _view = _unsubscribeController.onSubmit(_command, _bindingResult, _modelMap, getRequest());
        }
        catch (Exception ex){
            _view = "";
        }
        verifyAll();

        assertEquals(Unsubscribe_Form_View, _view);
        assertEquals(true, _bindingResult.hasErrors());
    }

    public void testUnsubscribeFormSubmitCheckboxMethod() {
        setSampleSubscriptions();
        getRequest().setParameter("Unsubscribe", "Unsubscribe");
        resetAll();
        _command.setMss(true);
        _command.setDailyNl(true);
        expect(_userDao.findUserFromId(_user.getId())).andReturn(_user);
        expect(_subscriptionDao.getUserSubscriptions(_user)).andReturn(_allSubscriptions);
        _exactTargetAPI.unsubscribeProduct((String) anyObject(), (String) anyObject());
        expectLastCall().times(4);

        /*http://stackoverflow.com/questions/859031/easymock-void-methods, http://burtbeckwith.com/blog/?p=43*/
        _subscriptionDao.removeSubscription(anyInt());
        expectLastCall().times(4);
        _unsubscribeDao.saveUnsubscribe(isUnsubscribe());
        expectLastCall();
        _unsubscribedProductsDao.saveUnsubscribedProducts((UnsubscribedProducts)anyObject());
        expectLastCall().times(2);
        expect(_subscriptionDao.findMynthSubscriptionsByUser(_user)).andReturn(new ArrayList<Student>());

        replayAll();
        try{
            _view = _unsubscribeController.onSubmit(_command, _bindingResult, _modelMap, getRequest());
        }
        catch (Exception ex){
            _view = "";
        }
        verifyAll();

        assertEquals(_modelMap.get("emailCmd"), _command);
        assertEquals(567, _command.getUnsubscribeId());
        assertEquals(true, _command.getUnsubscribedSuccess());
        assertEquals(Unsubscribe_Survey_View, _view);
    }

    public void testUnsubscribeFormSubmitAllMethod() {
        setSampleSubscriptions();
        getRequest().removeParameter("Unsubscribe");
        getRequest().setParameter("UnsubscribeAll", "UnsubscribeAll");

        resetAll();
        expect(_userDao.findUserFromId(_user.getId())).andReturn(_user);
        expect(_subscriptionDao.getUserSubscriptions(_user)).andReturn(_allSubscriptions);

        /*http://stackoverflow.com/questions/859031/easymock-void-methods, http://burtbeckwith.com/blog/?p=43*/
        _subscriptionDao.removeSubscription(anyInt());
        expectLastCall().times(6);
        _unsubscribeDao.saveUnsubscribe(isUnsubscribe());
        expectLastCall();
        _unsubscribedProductsDao.saveUnsubscribedProducts((UnsubscribedProducts)anyObject());
        expectLastCall().times(4);
        expect(_subscriptionDao.findMynthSubscriptionsByUser(_user)).andReturn(new ArrayList<Student>());
        _exactTargetAPI.unsubscribeAll(_user.getEmail());
        expectLastCall();

        replayAll();
        try{
            _view = _unsubscribeController.onSubmit(_command, _bindingResult, _modelMap, getRequest());
        }
        catch (Exception ex){
            _view = "";
        }
        verifyAll();

        assertEquals(_modelMap.get("emailCmd"), _command);
        assertEquals(567, _command.getUnsubscribeId());
        assertEquals(true, _command.getUnsubscribedSuccess());
        assertEquals(Unsubscribe_Survey_View, _view);
    }

    public void testUnsubscribeSurveyOnSubmit() {
        setSampleSubscriptions();
        _command.setUnsubscribedSuccess(true);
        _command.setUnsubscribeReason("I don't recall subscribing");
        _command.setOtherReasonsText("qwerty");
        _command.setUnsubscribeDateTime(new Date());
        Unsubscribe unsubscribe = new Unsubscribe(_user, "checkbox");
        unsubscribe.setId(567);
        Date unsubscribeDate = new Date();
        unsubscribe.setUnsubcribeDateTime(unsubscribeDate);
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.HOME);
        String homePageRedirectUri = "redirect:" + urlBuilder.asFullUrl(getRequest());
        resetAll();
        expect(_unsubscribeDao.findUnsubscribeById(_command.getUnsubscribeId())).andReturn(unsubscribe);

        _unsubscribeDao.updateUnsubscribedReason(unsubscribe, _command.getUnsubscribeReason(),
                _command.getOtherReasonsText(), unsubscribe.getUnsubcribeDateTime());
        expectLastCall();

        replayAll();
        _view = _unsubscribeController.onSubmitSurveyForm(_command, _modelMap, getRequest());
        verifyAll();

        assertEquals(homePageRedirectUri, _view);
    }

    private static class SavedUnsubscribeEquals implements IArgumentMatcher {
        public boolean matches(Object actual) {
            if(!(actual instanceof Unsubscribe)) {
                return false;
            }
            Unsubscribe unsubscribe = (Unsubscribe) actual;
            unsubscribe.setId(567);
            unsubscribe.setUnsubcribeDateTime(new Date());
            return true;
        }

        public void appendTo(StringBuffer stringBuffer) {

        }
    }

    private static Unsubscribe isUnsubscribe() {
        reportMatcher(new SavedUnsubscribeEquals());
        return null;
    }
    
    private Subscription createSubscription(int id, SubscriptionProduct subscriptionProduct, int schoolId, State state) {
        Subscription subscription = new Subscription();
        subscription.setId(id);
        subscription.setUser(_user);
        subscription.setProduct(subscriptionProduct);
        subscription.setSchoolId(schoolId);
        subscription.setState(state);
        return subscription;
    }
}