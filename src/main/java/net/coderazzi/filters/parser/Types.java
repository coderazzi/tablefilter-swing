/**
 * Author:  Luis M Pena  ( lu@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  lu@coderazzi.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.coderazzi.filters.parser;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.coderazzi.filters.IFilterTextParser;
import net.coderazzi.filters.gui.FilterSettings;
import net.coderazzi.filters.gui.TableFilterHeader;


/**
 * Default {@link Format} instances, supporting all the basic java types<br>
 * It also includes support for {@link Comparator} of {@link Date} instances.<br>
 * The default {@link IFilterTextParser} is automatically configured to use these 
 * {@link Format} instances, when created by the {@link TableFilterHeader}.<br>
 * Users can add any {@link Format} or {@link Comparator} definitions, as the 
 * class is used as a singleton.
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public class Types{

    Map<Class<?>, Format> formatters = new HashMap<Class<?>, Format>();
    Map<Class<?>, Comparator<?>> comparators = 
    	new HashMap<Class<?>, Comparator<?>>();

    public Types() {
    	setFormat(String.class, new StringTypeFormat());
    	setFormat(Boolean.class, new BooleanTypeFormat());
    	setFormat(Integer.class, new IntegerTypeFormat());
    	setFormat(Long.class, new LongTypeFormat());
    	setFormat(Short.class, new ShortTypeFormat());
    	setFormat(Float.class, new FloatTypeFormat());
    	setFormat(Double.class, new DoubleTypeFormat());
    	setFormat(Byte.class, new ByteTypeFormat());
    	setFormat(Character.class, new CharacterTypeFormat());
    	setFormat(Date.class, getDefaultDateFormat());
    }
    
    /** Returns the {@link Format} for the given class */
    public final Format getFormat(Class<?> c){
    	return formatters.get(c);
    }

    /** Defines the {@link Format} for the given class */
    public final void setFormat(Class<?> c, Format format){
    	//the formatter for the String class can never be null
    	if (format==null && String.class==c){
    		throw new IllegalArgumentException();
    	}
   		formatters.put(c, format);
    }
    
    /** Returns the {@link Comparator} for the given class */
    public Comparator<?> getComparator(Class<?> c){
    	return comparators.get(c);
    }

    /** Defines the {@link Comparator} for the given class */
    public void setComparator(Class<?> c, Comparator<?> format){
    	comparators.put(c, format);
    }
    
    /** 
     * Configures the passed {@link IFilterTextParser} to use the given 
     * {@link Comparator} and {@link Format} definitions
     */
    public void configure(IFilterTextParser textParser){
    	for (Entry<Class<?>, Format> entry : formatters.entrySet()){
    		textParser.setFormat(entry.getKey(), entry.getValue());
    	}
    	for (Entry<Class<?>, Comparator<?>> entry : comparators.entrySet()){
    		textParser.setComparator(entry.getKey(), entry.getValue());
    	}
    }

	private DateFormat getDefaultDateFormat(){
		String definition = FilterSettings.dateFormat;
		if (definition!=null){
			try{
				return new SimpleDateFormat(definition);
			} catch(Exception ex){//return the basic format
			}
		}
		return DateFormat.getDateInstance(DateFormat.SHORT);
	}

	static abstract class TypeFormat extends Format {
		private static final long serialVersionUID = -6161901343218446716L;

		@Override
    	public StringBuffer format(Object obj, StringBuffer toAppendTo, 
    			FieldPosition pos) {
			if (obj!=null){
				toAppendTo.append(obj);
			}
        	return toAppendTo;
    	}
    	@Override
    	public abstract Object parseObject(String source) throws ParseException;
    	
    	@Override
    	public Object parseObject(String source, ParsePosition pos) {
    		return null;
    	}
    }
    
    /** Factory to build string objects */
    public static class StringTypeFormat extends TypeFormat {
		private static final long serialVersionUID = 1641138429288273113L;

		@Override
        public Object parseObject(String source) {
        	return source;
        }        
    }

    /** Factory to build boolean objects */
    public static class BooleanTypeFormat extends TypeFormat {
		private static final long serialVersionUID = -6014041038273288651L;

		@Override
        public Object parseObject(String text){
        	return Boolean.valueOf(text);
        }
    }

    /** Factory to build integer objects */
    public static class IntegerTypeFormat extends TypeFormat {
		private static final long serialVersionUID = 314115124294512565L;

		@Override
        public Object parseObject(String text) throws ParseException{
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException nfe) {
                throw new ParseException(text, 0);
            }
        }
    }

    /** Factory to build long objects */
    public static class LongTypeFormat extends TypeFormat {
		private static final long serialVersionUID = 1165105738539025608L;

		@Override
        public Object parseObject(String text) throws ParseException{
            try {
                return Long.valueOf(text);
            } catch (NumberFormatException nfe) {
                throw new ParseException(text, 0);
            }
        }
    }

    /** Factory to build short objects */
    public static class ShortTypeFormat extends TypeFormat {
		private static final long serialVersionUID = -2237230150685513628L;

		@Override
        public Object parseObject(String text) throws ParseException{
            try {
                return Short.valueOf(text);
            } catch (NumberFormatException nfe) {
                throw new ParseException(text, 0);
            }
        }
    }

    /** Factory to build float objects */
    public static class FloatTypeFormat extends TypeFormat {
		private static final long serialVersionUID = 945229095107692481L;

		@Override
        public Object parseObject(String text) throws ParseException{
            try {
                return Float.valueOf(text);
            } catch (NumberFormatException nfe) {
                throw new ParseException(text, 0);
            }
        }
    }

    /** Factory to build double objects */
    public static class DoubleTypeFormat extends TypeFormat {
		private static final long serialVersionUID = -6081024614795175063L;

		@Override
        public Object parseObject(String text) throws ParseException{
            try {
                return Double.valueOf(text);
            } catch (NumberFormatException nfe) {
                throw new ParseException(text, 0);
            }
        }
    }

    /** Factory to build byte objects */
    public static class ByteTypeFormat extends TypeFormat {
		private static final long serialVersionUID = -8872549512274058519L;

		@Override
        public Object parseObject(String text) throws ParseException{
            try {
                return Byte.valueOf(text);
            } catch (NumberFormatException nfe) {
                throw new ParseException(text, 0);
            }
        }
    }

    /** Factory to build character objects */
    public static class CharacterTypeFormat extends TypeFormat {
		private static final long serialVersionUID = -7238741018044298862L;

		@Override
        public Object parseObject(String text) throws ParseException{
            if (text.length() != 1){
            	throw new ParseException(text, 0);
            }
            return new Character(text.charAt(0));
        }
    }
    
}
