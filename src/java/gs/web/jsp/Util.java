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

    /**
     * Returns the string odd or even depending on the Integer
     * Will throw NPE if null is passed
     *
     * @param number
     * @return "odd", "even", or "null"
     * @throws NullPointerException if null is passed
     */
    public static String oddOrEven(Integer number) {
        String oddOrEven = "odd";
        if (number.intValue() % 2 == 0) {
            oddOrEven = "even";
        }
        return oddOrEven;
    }
}
