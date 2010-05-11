package parser;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

import net.coderazzi.filters.parser.FilterTextParser;

import org.junit.Test;


public class CheckStringFormatTest extends AbstractTestCase {

    static class StrangeInteger implements Comparable<StrangeInteger> {
        Integer i;

        StrangeInteger(int i) {
            this.i = i;
        }

        @Override
        public String toString() {
        	//check that the string format is not important
            return "Integer: " + i; 
        }

        @Override
        public int compareTo(StrangeInteger o) {
            return i.compareTo(o.i);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof StrangeInteger) 
            	&& (((StrangeInteger) obj).i == i);
        }

		@Override
		public int hashCode() {
			return i;
		}
    }

    class StrangeIntegerFormat extends Format {

        private static final long serialVersionUID = -4046987506189773066L;

		@Override
        public StringBuffer format(Object obj,
                                   StringBuffer toAppendTo,
                                   FieldPosition pos) {
            return toAppendTo.append(((StrangeInteger) obj).i);
        }

        @Override
        public Object parseObject(String source,
                                  ParsePosition pos) {
            try {
                Object ret = new StrangeInteger(Integer.valueOf(source));
                pos.setIndex(source.length());
                return ret;
            } catch (Exception ex) {
                pos.setErrorIndex(0);
                return null;
            }
        }
    }

    class StrangeIntegerCustomizer implements ParserCustomizer {
        @Override
        public void customize(FilterTextParser parser) {
            parser.setFormat(StrangeInteger.class, new StrangeIntegerFormat());
        }
    }

    @Test
    public void testBasic1() throws ParseException {
        assertFalse(check(StrangeInteger.class, 
        		new StrangeInteger(2), "= 2", null));
    }

    @Test
    public void testBasic2() throws ParseException {
        assertTrue(check(StrangeInteger.class, new StrangeInteger(2), "= 2", 
        		new StrangeIntegerCustomizer()));
    }

}