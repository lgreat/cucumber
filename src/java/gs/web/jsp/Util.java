package gs.web.jsp;

import java.util.Random;

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

    /**
     * Generate a random integer between 0 (inclusive) and upperLimit (exclusive)
     * @param upperLimit Th
     * @return random number
     */
    public static int randomNumber(int upperLimit) {
        Random r = new Random();
        return r.nextInt(upperLimit);
    }
}
