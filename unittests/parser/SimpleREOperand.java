package parser;

import org.junit.Test;

import java.text.ParseException;


public class SimpleREOperand extends DefaultTestCase {

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

    @Test
    public void testBasic31() throws ParseException {
        assertTrue(check(String.class, "hello", "~ hello", null));
    }

    @Test
    public void testBasic32() throws ParseException {
        assertFalse(check(String.class, "hello", "!~ hello", null));
    }

    @Test
    public void testBasic41() throws ParseException {
        assertTrue(check(String.class, "hello", "~ heLLo", ignoreCase));
    }

    @Test
    public void testBasic42() throws ParseException {
        assertFalse(check(String.class, "hello", "!~ heLLo", ignoreCase));
    }

    @Test
    public void testBasic51() throws ParseException {
        assertTrue(check(String.class, "hello", "~ h*o", null));
    }

    @Test
    public void testBasic52() throws ParseException {
        assertTrue(check(String.class, "hello", "~ h???o", null));
    }

    @Test
    public void testBasic53() throws ParseException {
        assertFalse(check(String.class, "hello", "~ H???o", null));
    }

    @Test
    public void testBasic54() throws ParseException {
        assertTrue(check(String.class, "hello", "~ H???o", ignoreCase));
    }

    @Test
    public void testBasic55() throws ParseException {
        assertTrue(check(Integer.class, 1289, "~ 1*", null));
    }
}