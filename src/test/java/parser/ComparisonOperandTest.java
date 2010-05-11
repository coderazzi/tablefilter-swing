package parser;

import java.text.ParseException;
import java.util.Comparator;

import net.coderazzi.filters.parser.FilterTextParser;

import org.junit.Test;


public class ComparisonOperandTest extends AbstractTestCase {


    @Test
    public void testBasic1() throws ParseException {
        assertTrue(check(String.class, "hello", "> h", null));
    }

    @Test
    public void testBasic2() throws ParseException {
        assertFalse(check(String.class, "hello", "> i", null));
    }

    @Test
    public void testBasic3() throws ParseException {
        assertTrue(check(String.class, "hello", ">h", null));
    }

    @Test
    public void testBasic4() throws ParseException {
        assertTrue(check(String.class, "hello", ">      h", null));
    }

    @Test
    public void testBasic11() throws ParseException {
        assertFalse(check(String.class, "Hello", "> h", null));
    }

    @Test
    public void testBasic12() throws ParseException {
        assertTrue(check(String.class, "Hello", "> h", ignoreCase));
    }

    @Test
    public void testBasic21() throws ParseException {
        assertFalse(check(String.class, "h", "> h", null));
    }

    @Test
    public void testBasic22() throws ParseException {
        assertTrue(check(String.class, "h", ">= h", null));
    }

    @Test
    public void testBasic23() throws ParseException {
        assertFalse(check(String.class, "h", "< h", null));
    }

    @Test
    public void testBasic24() throws ParseException {
        assertTrue(check(String.class, "h", "<= h", null));
    }


    @Test
    public void testInteger1() throws ParseException {
        assertFalse(check(String.class, "11", "> 2", null));
    }

    @Test
    public void testInteger2() throws ParseException {
        assertTrue(check(Integer.class, 11, "> 2", integerCustomizer));
    }

    @Test
    public void testInteger11() {
        int error;
        try {
            check(Integer.class, 11, "> 2as", integerCustomizer);
            error = -1;
        } catch (ParseException pex) {
            error = pex.getErrorOffset();
        }
        assertTrue(error == 2);
    }

    @Test
    public void testInteger12() {
        int error;
        try {
            check(Integer.class, 11, ">2as", integerCustomizer);
            error = -1;
        } catch (ParseException pex) {
            error = pex.getErrorOffset();
        }
        assertTrue(error == 1);
    }

    @Test
    public void testInteger13() {
        int error;
        try {
            check(Integer.class, 11, ">   2as", integerCustomizer);
            error = -1;
        } catch (ParseException pex) {
            error = pex.getErrorOffset();
        }
        assertTrue(error == 4);
    }


    @Test
    public void testInteger21() throws ParseException {
        assertFalse(check(Integer.class, 1, "> 2", integerCustomizer));
    }

    @Test
    public void testInteger22() throws ParseException {
        assertTrue(check(Integer.class, 1, "> 2", new ParserCustomizer() {

                    @Override
                    public void customize(FilterTextParser parser) {
                        integerCustomizer.customize(parser);
                        parser.setComparator(Integer.class, new Comparator<Integer>() {
                                @Override
                                public int compare(Integer o1,
                                                   Integer o2) {
                                    return o2.compareTo(o1);
                                }
                            });
                    }
                }));
    }

}