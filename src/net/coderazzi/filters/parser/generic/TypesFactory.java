/**
 * Author:  Luis M Pena  ( sen@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  sen@coderazzi.net
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

package net.coderazzi.filters.parser.generic;

import net.coderazzi.filters.parser.FilterTextParsingException;
import net.coderazzi.filters.parser.ITypeBuilder;
import net.coderazzi.filters.resources.Messages;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>Default {@link ITypesFactory}, supporting all the basic java types and enumerations</p>
 *
 * @author  Luis M Pena - sen@coderazzi.net
 */
public class TypesFactory implements ITypesFactory {

    public static ITypeBuilder stringTypeFactory = new StringTypeFactory();
    public static ITypeBuilder booleanTypeFactory = new BooleanTypeFactory();
    public static ITypeBuilder integerTypeFactory = new IntegerTypeFactory();
    public static ITypeBuilder longTypeFactory = new LongTypeFactory();
    public static ITypeBuilder shortTypeFactory = new ShortTypeFactory();
    public static ITypeBuilder floatTypeFactory = new FloatTypeFactory();
    public static ITypeBuilder doubleTypeFactory = new DoubleTypeFactory();
    public static ITypeBuilder byteTypeFactory = new ByteTypeFactory();
    public static ITypeBuilder charTypeFactory = new CharacterTypeFactory();

    final static String BOOLEAN_TRUE_TEXT = Messages.getString("Filters.BooleanTrue");
    final static String BOOLEAN_FALSE_TEXT = Messages.getString("Filters.BooleanFalse");

    protected Map<Class<?>, ITypeBuilder> factories = new HashMap<Class<?>, ITypeBuilder>();

    public TypesFactory() {
        factories.put(String.class, stringTypeFactory);
        factories.put(Boolean.class, booleanTypeFactory);
        factories.put(Integer.class, integerTypeFactory);
        factories.put(Long.class, longTypeFactory);
        factories.put(Short.class, shortTypeFactory);
        factories.put(Float.class, floatTypeFactory);
        factories.put(Double.class, doubleTypeFactory);
        factories.put(Byte.class, byteTypeFactory);
        factories.put(Character.class, charTypeFactory);
        factories.put(Date.class, DateHandler.getDefault());
    }

    /**
     * @see  ITypesFactory#buildObject(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
	public Object buildObject(Class c, String s) throws FilterTextParsingException {
        ITypeBuilder factory = factories.get(c);
        if (factory != null)
            return factory.parse(s);
        if (!c.isEnum())
            return s;
        try {
            return Enum.valueOf(c, s);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    /**
     * @see  ITypesFactory#getBuildType(java.lang.Class)
     */
    public Class<?> getBuildType(Class<?> c) {
        return (factories.containsKey(c) || c.isEnum()) ? c : String.class;
    }

    /**
     * @see  ITypesFactory#setFactory(java.lang.Class, net.coderazzi.filters.parser.ITypeBuilder)
     */
    public void setFactory(Class<?> c, ITypeBuilder factory) {
        factories.put(c, factory);
    }

    /**
     * Factory to build string objects
     */
    public static class StringTypeFactory implements ITypeBuilder {
        public Object parse(String text) {
            return text;
        }
    }

    /**
     * Factory to build boolean objects
     */
    public static class BooleanTypeFactory implements ITypeBuilder {
        public Object parse(String text) {

            if (text.equalsIgnoreCase(BOOLEAN_TRUE_TEXT))
                return Boolean.TRUE;

            if (text.equalsIgnoreCase(BOOLEAN_FALSE_TEXT))
                return Boolean.FALSE;

            return null;
        }
    }

    /**
     * Factory to build integer objects
     */
    public static class IntegerTypeFactory implements ITypeBuilder {
        public Object parse(String text) {

            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    /**
     * Factory to build long objects
     */
    public static class LongTypeFactory implements ITypeBuilder {
        public Object parse(String text) {

            try {
                return Long.valueOf(text);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    /**
     * Factory to build short objects
     */
    public static class ShortTypeFactory implements ITypeBuilder {
        public Object parse(String text) {

            try {
                return Short.valueOf(text);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    /**
     * Factory to build float objects
     */
    public static class FloatTypeFactory implements ITypeBuilder {
        public Object parse(String text) {

            try {
                return Float.valueOf(text);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    /**
     * Factory to build double objects
     */
    public static class DoubleTypeFactory implements ITypeBuilder {
        public Object parse(String text) {

            try {
                return Double.valueOf(text);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    /**
     * Factory to build byte objects
     */
    public static class ByteTypeFactory implements ITypeBuilder {
        public Object parse(String text) {

            try {
                return Byte.valueOf(text);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    /**
     * Factory to build character objects
     */
    public static class CharacterTypeFactory implements ITypeBuilder {
        public Object parse(String text) {

            if (text.length() > 1)
                return null;

            return new Character(text.charAt(0));
        }
    }

}
