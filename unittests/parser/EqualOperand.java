package parser;

import org.junit.Test;

import java.text.ParseException;


public class EqualOperand extends DefaultTestCase {


    @Test
    public void testBasic1() throws ParseException {
        assertTrue(check(String.class, "hello", "= hello", null));
    }

    @Test
    public void testBasic2() throws ParseException {
        assertTrue(check(String.class, "hello", "==hello", null));
    }

    @Test
    public void testBasic3() throws ParseException {
        assertTrue(check(Integer.class, 1, "== 01", integerCustomizer));
    }

    @Test
    public void testBasic4() throws ParseException {
        assertFalse(check(Integer.class, 1, "== 012", integerCustomizer));
    }

    @Test
    public void testBasic11() throws ParseException {
        assertTrue(check(String.class, "Hello", "<> h", null));
    }

    @Test
    public void testBasic12() throws ParseException {
        assertTrue(check(String.class, "Hello", "!= h", null));
    }

    @Test
    public void testBasic21() throws ParseException {
        assertFalse(check(Integer.class, 1, "<> 01", integerCustomizer));
    }

    @Test
    public void testBasic22() throws ParseException {
        assertTrue(check(Integer.class, 1, "<> 02", integerCustomizer));
    }

    @Test
    public void testBasic23() throws ParseException {
        assertTrue(check(Integer.class, 1, "!= 02", integerCustomizer));
    }


}