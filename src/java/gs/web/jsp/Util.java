package gs.web.jsp;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class Util {

    public static String toLowercase(String word) {
        String lowered = null;
        if (word != null) {
            lowered = word.toLowerCase();
        }
        return lowered;
    }
}
