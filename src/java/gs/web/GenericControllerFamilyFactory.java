package gs.web;

import gs.web.util.context.SessionContextUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class GenericControllerFamilyFactory implements IControllerFactory {

    // configure which resolvers to use here
    // order matters. First resolver to determine a Controller Family wins
    public ArrayList<IControllerFamilyResolver> _resolvers;

    public Controller getController(List<? extends Controller> controllers) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (request == null) {
            throw new IllegalStateException("Request cannot be null.");
        }

        Controller chosenController = null;

        Map<Controller,ControllerFamily> controllerFamilyMap = getControllerFamilies(controllers);

        ControllerFamily chosenControllerFamily = resolveControllerFamily(controllerFamilyMap.values());
        Collection<ControllerFamily> availableControllerFamilies = controllerFamilyMap.values();

        request.setAttribute("controllerFamilies", availableControllerFamilies);

        for (IControllerFamilyResolver resolver : _resolvers) {
            ControllerFamily family = resolver.resolveControllerFamily();
            if (family != null && availableControllerFamilies.contains(family)) {
                chosenControllerFamily = family;
                break;
            }
        }

        if (chosenControllerFamily == null) {
            chosenControllerFamily = ControllerFamily.DESKTOP;
        };

        for (Map.Entry<Controller, ControllerFamily> entry : controllerFamilyMap.entrySet()) {
            if (entry.getValue().equals(chosenControllerFamily)) {
                chosenController = entry.getKey();
                break;
            }
        }

        if (chosenController == null) {
            chosenController = controllers.get(0);
        }

        return chosenController;
    }

    public ControllerFamily resolveControllerFamily(Collection<ControllerFamily> availableControllerFamilies) {
        // possible performance enhancement: let resolvers specify which families they support
        for (IControllerFamilyResolver resolver : _resolvers) {
            ControllerFamily family = resolver.resolveControllerFamily();
            if (family != null && availableControllerFamilies.contains(family)) {
                return family;
            }
        }
        return null;
    }

    public Map<Controller,ControllerFamily> getControllerFamilies(List<? extends Controller> controllers) {
        Map<Controller,ControllerFamily> controllerFamilyMap = new HashMap<Controller,ControllerFamily>();

        for (Controller controller : controllers) {
            if (controller instanceof IControllerFamilySpecifier) {
                ControllerFamily thisControllerFamily = ((IControllerFamilySpecifier) controller).getControllerFamily();
                controllerFamilyMap.put(controller, thisControllerFamily);
            }
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
