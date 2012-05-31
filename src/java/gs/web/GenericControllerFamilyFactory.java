package gs.web;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class GenericControllerFamilyFactory implements IControllerFamilyFactory {

    // configure which resolvers to use here
    // order matters. First resolver to determine a Controller Family wins
    public ArrayList<IControllerFamilyResolver> _resolvers;

    public IControllerFamilySpecifier getController(List<IControllerFamilySpecifier> controllers) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (request == null) {
            throw new IllegalStateException("Request cannot be null.");
        }

        IControllerFamilySpecifier chosenController = null;

        // create a map of family --> controller
        Map<ControllerFamily,IControllerFamilySpecifier> controllerFamilyMap = getControllerFamilies(controllers);


        // Get a collection of the controller families that were configured to handle this request, in pages-servlet
        Set<ControllerFamily> availableControllerFamilies = controllerFamilyMap.keySet();
        request.setAttribute("controllerFamilies", availableControllerFamilies);


        // Determine the ControllerFamily that the Controller that handles this request should belong to
        ControllerFamily chosenControllerFamily = resolveControllerFamily(controllerFamilyMap.keySet());


        // We have a target controller family, so get the controller in the map that handles the family
        chosenController = controllerFamilyMap.get(chosenControllerFamily);


        if (chosenController == null) {
            throw new IllegalStateException("Configuration error while getting processing ControllerFamily controllers: No controller configured to handle family: " + chosenControllerFamily.name());
        }

        return chosenController;
    }

    /**
     * Iterate over all configured resolvers, and return the first non-null family that is received from
     * a Resolver, as long as there's a controller in the map that belows to that family
     *
     * @param availableControllerFamilies
     * @return Desktop controller family if no resolvers choose a family, otherwise a specific ControllerFamily
     */
    public ControllerFamily resolveControllerFamily(Set<ControllerFamily> availableControllerFamilies) {
        // possible performance enhancement: let resolvers specify which families they support
        for (IControllerFamilyResolver resolver : _resolvers) {
            ControllerFamily family = resolver.resolveControllerFamily();
            if (family != null && availableControllerFamilies.contains(family)) {
                return family;
            }
        }

        // no resolver chose a family, so default to desktop
        return ControllerFamily.DESKTOP;
    }

    public Map<ControllerFamily,IControllerFamilySpecifier> getControllerFamilies(List<IControllerFamilySpecifier> controllers) {
        Map<ControllerFamily,IControllerFamilySpecifier> controllerFamilyMap = new LinkedHashMap<ControllerFamily,IControllerFamilySpecifier>();

        for (IControllerFamilySpecifier controller : controllers) {
            ControllerFamily thisControllerFamily = controller.getControllerFamily();
            controllerFamilyMap.put(thisControllerFamily, controller);
        }

        if (controllers.size() == 0 || controllerFamilyMap.size() != controllers.size()) {
            throw new IllegalStateException("Configuration error while getting processing ControllerFamily controllers: Either no controllers were configured, or duplicate Family-->Controller entries exist");
        }

        return controllerFamilyMap;
    }

    public ArrayList<IControllerFamilyResolver> getResolvers() {
        return _resolvers;
    }

    public void setResolvers(ArrayList<IControllerFamilyResolver> resolvers) {
        _resolvers = resolvers;
    }
}
