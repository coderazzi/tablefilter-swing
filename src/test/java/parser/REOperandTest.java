package parser;

import org.junit.Test;

import java.text.ParseException;


public class REOperandTest extends AbstractTestCase {

    @Test
    public void testBasic1() throws ParseException {
        assertTrue(check(String.class, "hello", "~~ he(i|j|l){2}o.*", null));
    }

    @Test
    public void testBasic2() throws ParseException {
        assertTrue(check(String.class, "hello!!!", "~~ he(i|j|l){2}o.*", null));
    }

    @Test
    public void testBasic3() throws ParseException {
        assertFalse(check(String.class, "helllo!!!", "~~ he(i|j|l){2}o.*", null));
    }

    @Test
    public void testBasic4() throws ParseException {
        assertTrue(check(String.class, "or hello!!!", "~~ .*he(i|j|l){2}o.*", null));
    }


}