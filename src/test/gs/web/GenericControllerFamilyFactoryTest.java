package gs.web;

import gs.web.search.SchoolSearchController;
import gs.web.search.SchoolSearchMobileController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GenericControllerFamilyFactoryTest {

    Map<ControllerFamily, IControllerFamilySpecifier> desktopAndMobileControllerFamilyMap;
    Map<ControllerFamily, IControllerFamilySpecifier> desktopOnlyControllerFamilyMap;
    Map<ControllerFamily, IControllerFamilySpecifier> mobileOnlyControllerFamilyMap;

    class ControllerFamilyFactoryThatAlwaysReturnsMobile extends GenericControllerFamilyFactory {
        @Override
        public ControllerFamily resolveControllerFamily(Set<ControllerFamily> availableControllerFamilies) {
            return ControllerFamily.MOBILE;
        }
    }

    class AlwaysReturnsMobileResolver implements IControllerFamilyResolver {
        public ControllerFamily resolveControllerFamily() {
            return ControllerFamily.MOBILE;
        }
    }

    class AlwaysReturnsDesktopResolver implements IControllerFamilyResolver {
        public ControllerFamily resolveControllerFamily() {
            return ControllerFamily.DESKTOP;
        }
    }

    class AlwaysReturnsFruitcakeResolver implements IControllerFamilyResolver {
        public ControllerFamily resolveControllerFamily() {
            return ControllerFamily.FRUITCAKE;
        }
    }

    class ControllerThatHandlesMobile implements IControllerFamilySpecifier {
        public ControllerFamily getControllerFamily() {
            return ControllerFamily.MOBILE;
        }
    }

    class ControllerThatHandlesFruitcake implements IControllerFamilySpecifier {
        public ControllerFamily getControllerFamily() {
            return ControllerFamily.FRUITCAKE;
        }
    }

    class ControllerThatHandlesDesktop implements IControllerFamilySpecifier {
        public ControllerFamily getControllerFamily() {
            return ControllerFamily.DESKTOP;
        }
    }

    ControllerFamilyFactoryThatAlwaysReturnsMobile _controllerFamilyFactoryThatAlwaysResolvesMobileFamily;
    GenericControllerFamilyFactory _genericControllerFamilyFactory;

    public ArrayList<IControllerFamilyResolver> getMobileAndFruitcakeResolvers() {
        ArrayList<IControllerFamilyResolver> resolvers = new ArrayList<IControllerFamilyResolver>();
        resolvers.add(new AlwaysReturnsMobileResolver());
        resolvers.add(new AlwaysReturnsFruitcakeResolver());
        return resolvers;
    }

    public ArrayList<IControllerFamilyResolver> getMobileResolver() {
        ArrayList<IControllerFamilyResolver> resolvers = new ArrayList<IControllerFamilyResolver>();
        resolvers.add(new AlwaysReturnsMobileResolver());
        return resolvers;
    }

    public ArrayList<IControllerFamilyResolver> getFruitcakeResolver() {
        ArrayList<IControllerFamilyResolver> resolvers = new ArrayList<IControllerFamilyResolver>();
        resolvers.add(new AlwaysReturnsFruitcakeResolver());
        return resolvers;
    }

    @Before
    public void setUp() {
        _controllerFamilyFactoryThatAlwaysResolvesMobileFamily = new ControllerFamilyFactoryThatAlwaysReturnsMobile();
        _genericControllerFamilyFactory = new GenericControllerFamilyFactory();

        desktopAndMobileControllerFamilyMap = new HashMap<ControllerFamily, IControllerFamilySpecifier>();
        desktopAndMobileControllerFamilyMap.put(ControllerFamily.DESKTOP, new SchoolSearchController());
        desktopAndMobileControllerFamilyMap.put(ControllerFamily.MOBILE, new SchoolSearchMobileController());

        desktopOnlyControllerFamilyMap = new HashMap<ControllerFamily, IControllerFamilySpecifier>();
        desktopOnlyControllerFamilyMap.put(ControllerFamily.DESKTOP, new SchoolSearchController());

        mobileOnlyControllerFamilyMap = new HashMap<ControllerFamily, IControllerFamilySpecifier>();
        mobileOnlyControllerFamilyMap.put(ControllerFamily.MOBILE, new SchoolSearchController());
    }

    @Test
    public void testGetController() throws Exception {
        RequestContextHolder.resetRequestAttributes();
        RequestAttributes attributes = new ServletRequestAttributes(new GsMockHttpServletRequest());
        RequestContextHolder.setRequestAttributes(attributes);

        List<IControllerFamilySpecifier> desktopAndFruitcakeControllers = new ArrayList<IControllerFamilySpecifier>();
        desktopAndFruitcakeControllers.add(new ControllerThatHandlesDesktop());
        desktopAndFruitcakeControllers.add(new ControllerThatHandlesFruitcake());

        _genericControllerFamilyFactory.setResolvers(getMobileAndFruitcakeResolvers());
        assertEquals("Expect to get the ControllerThatHandlesFruitcake, since we're configuring the factory with " + "mobile and fruitcake resolvers, and passing in desktop plus fruitcake controllers", desktopAndFruitcakeControllers.get(1), _genericControllerFamilyFactory.getController(desktopAndFruitcakeControllers));


        _genericControllerFamilyFactory.setResolvers(getMobileResolver());
        assertEquals("Expect to get the ControllerThatHandlesDesktop, since we're configuring the factory with " +
                "only a mobile resolver, but passing in only a desktop controller. resolveControllerFamily() will" +
                "know that there are no controllers to support mobile and default to desktop",
                desktopAndFruitcakeControllers.get(0),
                _genericControllerFamilyFactory.getController(desktopAndFruitcakeControllers));

        try {
            _controllerFamilyFactoryThatAlwaysResolvesMobileFamily.getController(desktopAndFruitcakeControllers);
            fail("Expected exception to be thrown since this test factory overrides resolveControllerFamkly to always " +
                    "return Mobile family, but passed in controllers doesn't contain Mobile controller");
        } catch (IllegalStateException e) {
            // good
        }
    }

    @Test
    public void testResolveControllerFamily() throws Exception {
        _genericControllerFamilyFactory.setResolvers(getMobileAndFruitcakeResolvers());
        assertEquals("Expect to receive Mobile family since map contains an entry for Mobile controller family, " +
                "and Mobile resolver is first in resolver list",
                ControllerFamily.MOBILE, _genericControllerFamilyFactory.resolveControllerFamily(desktopAndMobileControllerFamilyMap.keySet()));

        _genericControllerFamilyFactory.setResolvers(getFruitcakeResolver());
        assertEquals("Expect to receive Desktop family since map contains entries for mobile and desktop controller families, " +
                "but Fruitcake resolver is only configured resolver",
                ControllerFamily.DESKTOP, _genericControllerFamilyFactory.resolveControllerFamily(desktopAndMobileControllerFamilyMap.keySet()));

        _genericControllerFamilyFactory.setResolvers(getFruitcakeResolver());
        assertEquals("Expect to receive Desktop family since map contains only an entry for mobile controller family, " +
                "but Fruitcake resolver is only configured resolver",
                ControllerFamily.DESKTOP, _genericControllerFamilyFactory.resolveControllerFamily(mobileOnlyControllerFamilyMap.keySet()));
    }

    @Test
    public void testGetControllerFamilies() throws Exception {
        List<IControllerFamilySpecifier> desktopAndMobileControllers = new ArrayList<IControllerFamilySpecifier>();
        desktopAndMobileControllers.add(new ControllerThatHandlesDesktop());
        desktopAndMobileControllers.add(new ControllerThatHandlesMobile());

        List<IControllerFamilySpecifier> desktopAndMobileControllersWithDupes = new ArrayList<IControllerFamilySpecifier>();
        desktopAndMobileControllersWithDupes.add(new ControllerThatHandlesDesktop());
        desktopAndMobileControllersWithDupes.add(new ControllerThatHandlesMobile());
        desktopAndMobileControllersWithDupes.add(new ControllerThatHandlesMobile());

        Map<ControllerFamily,IControllerFamilySpecifier> controllerFamilyMap = new LinkedHashMap<ControllerFamily, gs.web.IControllerFamilySpecifier>();
        controllerFamilyMap.put(ControllerFamily.DESKTOP, desktopAndMobileControllers.get(0));
        controllerFamilyMap.put(ControllerFamily.MOBILE, desktopAndMobileControllers.get(1));

        assertEquals("Expect getControllerFamilies to work under normal circumstances", controllerFamilyMap, _genericControllerFamilyFactory.getControllerFamilies(desktopAndMobileControllers));

        try {
            _genericControllerFamilyFactory.getControllerFamilies(desktopAndMobileControllersWithDupes);
            fail("Expect getControllerFamilies to throw exception when given multiple controllers that handle the same ControllerFamily");
        } catch (IllegalStateException e) {
            // good
        }

        try {
            _genericControllerFamilyFactory.getControllerFamilies(new ArrayList<IControllerFamilySpecifier>());
            fail("Expect getControllerFamilies to throw exception when given an empty list");
        } catch (IllegalStateException e) {
            // good
        }

    }

}
