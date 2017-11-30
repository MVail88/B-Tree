import java.util.function.Supplier;

/**
 * Provides very basic testing methods for code
 */
public class BTest {

    public static void testException(String testName, Class<? extends Exception> exception, Supplier<Boolean> func) {
        boolean noThrow = false;
        try {
            noThrow = func.get();
        }
        catch (Exception e) {
            if (e.getClass().equals(exception)) {
                pass(testName);
            }
            else {
                fail(testName, exception.toString(), e.getClass().toString() );
            }
        }

        if (noThrow) {
            fail(testName, exception.toString(), "No exception thrown");
        }

    }

    /**
     * compares two Objects
     * @param testName name of test
     * @param value tested value
     * @param expected expected value
     */
    public static void testObject(String testName, Object value, Object expected) {
        if (value.equals(expected)) {
            pass(testName);
        }
        else {
            fail(testName, expected.toString(), value.toString());
        }
    }

    /**
     * compares two Strings
     * @param testName name of test
     * @param value tested value
     * @param expected expected value
     */
    public static void testString(String testName, String value, String expected) {
        if (value.equals(expected)) {
            pass(testName);
        }
        else {
            failString(testName, expected, value);
        }
    }

    /**
     * compares two ints
     * @param testName name of test
     * @param value tested value
     * @param expected expected value
     */
    public static void testInt(String testName, int value, int expected) {
        if (value == expected) {
            pass(testName);
        }
        else {
            fail(testName, "" + expected, "" + value);
        }
    }

    /**
     * compares two longs
     * @param testName name of test
     * @param value tested value
     * @param expected expected value
     */
    public static void testLong(String testName, long value, long expected) {
        if (value == expected) {
            pass(testName);
        }
        else {
            fail(testName, "" + expected, "" + value);
        }
    }

    /**
     * compares two booleans
     * @param testName name of test
     * @param value tested value
     * @param expected expected value
     */
    public static void testBoolean(String testName, boolean value, boolean expected) {
        if (value == expected) {
            pass(testName);
        }
        else {
            fail(testName, "" + expected, "" + value);
        }
    }

    /**
     * Prints that the test passed to std out
     * @param testName the name of the test
     */
    private static void pass(String testName) {
        System.out.println("Passed: " + testName);
    }

    /**
     * Prints that the test failed to std out
     * @param testName the name of test
     * @param expected the expected result
     * @param actual the actual result
     */
    private static void fail(String testName, String expected, String actual) {
        System.out.println("Failed: " + testName);
        System.out.println("        expected: " + expected);
        System.out.println("          actual: " + actual);
    }

    /**
     * Prints that the test failed to std out with printlns for better string formatting
     * @param testName the name of test
     * @param expected the expected result
     * @param actual the actual result
     */
    private static void failString(String testName, String expected, String actual) {
        System.out.println("Failed: " + testName);
        System.out.println("        expected: \n" + expected);
        System.out.println("\n          actual: \n" + actual + "\n");
    }
}
