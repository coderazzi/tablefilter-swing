package parser;

import org.junit.Test;

import java.text.ParseException;


public class DefaultOperand extends DefaultTestCase {


    @Test
    public void testBasic1() throws ParseException {
        assertTrue(check(String.class, "hello", "hello", null));
    }

    @Test
    public void testBasic2() throws ParseException {
        assertFalse(check(String.class, "hello", "hello2", null));
    }

    @Test
    public void testBasic3() throws ParseException {
        assertFalse(check(String.class, "hello", "Hello", null));
    }

    @Test
    public void testBasic11() throws ParseException {
        assertTrue(check(String.class, "hello", "hello", ignoreCase));
    }

    @Test
    public void testBasic12() throws ParseException {
        assertFalse(check(String.class, "hello", "hello2", ignoreCase));
    }

    @Test
    public void testBasic13() throws ParseException {
        assertTrue(check(String.class, "hello", "Hello", ignoreCase));
    }

}