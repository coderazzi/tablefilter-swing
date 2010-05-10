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

package net.coderazzi.filters.parser.generic;


import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.coderazzi.filters.resources.Messages;


/**
 * <p>Default {@link IRelationalOperandFactory} implementation</p>
 *
 * <p>List of supported operands:
 *
 * <ul>
 * <li>Basic ones:
 *
 * <ul>
 * <li>&gt;</li>
 * <li>&lt;</li>
 * <li>&gt;=</li>
 * <li>&lt;=</li>
 * <li>=</li>
 * <li>!= : the same as &lt;&gt;</li>
 * <li>&lt;&gt; : the same as !=</li>
 * </ul>
 * </li>
 * <li>Case insensitive operators: work on the stringfied representation of the objects
 *
 * <ul>
 * <li>&gt;@</li>
 * <li>&lt;@</li>
 * <li>&gt;=@</li>
 * <li>&lt;=@</li>
 * <li>=@</li>
 * <li>!=@</li>
 * <li>&lt;&gt;@</li>
 * </ul>
 * </li>
 * <li>'Regular expression ones': works on strings only. The regular expressions are simplified:
 * only characters * ? are supported
 *
 * <ul>
 * <li>~ : For example, 'a~A*B' returns true if the expression a starts with A upper case and ends
 * with B</li>
 * <li>!~ : negation of the previous operator</li>
 * <li>~@ : insensitive case of the basic operator; For example, 'a~A*B' returns true if the
 * expression a starts with A or a and ends with B or b</li>
 * <li>!~@ : negation of the previous operator</li>
 * </ul>
 * </li></p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public class OperandFactory implements IRelationalOperandFactory {

    /** The provided comparators. */
    protected Map<Class<?>, Comparator<?>> comparators = new HashMap<Class<?>, Comparator<?>>();

    /** Whether to ignore case or not */
    protected boolean ignoreCase;

    /** Operator GREATER THAN */
    public final IRelationalOperand greaterOperand = new ComparisonOperand(">", false) {
            @Override public boolean applyOnCompare(int n) {
                return n > 0;
            }
        };

    /** Operator GREATER OR EQUAL THAN */
    public final IRelationalOperand greaterOrEqualOperand = new ComparisonOperand(">=", false) {
            @Override public boolean applyOnCompare(int n) {
                return n >= 0;
            }
        };

    /** Operator LOWER THAN */
    public final IRelationalOperand lowerOperand = new ComparisonOperand("<", false) {
            @Override public boolean applyOnCompare(int n) {
                return n < 0;
            }
        };

    /** Operator LOWER OR EQUAL THAN */
    public final IRelationalOperand lowerOrEqualOperand = new ComparisonOperand("<=", false) {
            @Override public boolean applyOnCompare(int n) {
                return n <= 0;
            }
        };

    /** Operator EQUAL THAN */
    public final IRelationalOperand equalOperand = new ComparisonOperand("=", true) {
            @Override public boolean applyOnCompare(int n) {
                return n == 0;
            }
        };

    /** Operator DIFFERENT THAN */
    public final IRelationalOperand distinctOperand = new ComparisonOperand("!=", true) {
            @Override public boolean applyOnCompare(int n) {
                return n != 0;
            }
        };

    /** Operator GREATER THAN, applied to strings and ignoring case */
    public final IRelationalOperand greaterICaseOperand = new ComparisonICaseOperand(">@") {
            @Override public boolean applyOnCompare(int n) {
                return n > 0;
            }
        };

    /** Operator GREATER OR EQUAL THAN, applied to strings and ignoring case */
    public final IRelationalOperand greaterOrEqualICaseOperand = new ComparisonICaseOperand(">=@") {
            @Override public boolean applyOnCompare(int n) {
                return n >= 0;
            }
        };

    /** Operator LOWER THAN, applied to strings and ignoring case */
    public final IRelationalOperand lowerICaseOperand = new ComparisonICaseOperand("<@") {
            @Override public boolean applyOnCompare(int n) {
                return n < 0;
            }
        };

    /** Operator LOWER OR EQUAL THAN, applied to strings and ignoring case */
    public final IRelationalOperand lowerOrEqualICaseOperand = new ComparisonICaseOperand("<=@") {
            @Override public boolean applyOnCompare(int n) {
                return n <= 0;
            }
        };

    /** Operator EQUAL THAN, applied to strings and ignoring case */
    public final IRelationalOperand equalICaseOperand = new ComparisonICaseOperand("=@") {
            @Override public boolean applyOnCompare(int n) {
                return n == 0;
            }
        };

    /** Operator DIFFERENT THAN, applied to strings and ignoring case */
    public final IRelationalOperand distinctICaseOperand = new ComparisonICaseOperand("!=@") {
            @Override public boolean applyOnCompare(int n) {
                return n != 0;
            }
        };

    /**
     * Operator EQUAL THAN, applied to strings, using limited (basic wildcards) regular expressions
     */
    public final StringWildcardOperand equalREOperand = new StringWildcardOperand("~", true, false);

    /**
     * Operator DIFFERENT THAN, applied to strings, using limited (basic wildcards) regular
     * expressions
     */
    public final StringWildcardOperand distinctREOperand = new StringWildcardOperand("!~", false,
            false);

    /**
     * Operator EQUAL THAN, applied to strings, using limited (basic wildcards) regular expressions,
     * and ignoring case
     */
    public final StringWildcardOperand equalICaseREOperand = new StringWildcardOperand("~@", true,
            true);

    /**
     * Operator DIFFERENT THAN, applied to strings, using limited (basic wildcards) regular
     * expressions, and ignoring case
     */
    public final StringWildcardOperand distinctICaseREOperand = new StringWildcardOperand("!~@",
            true, true);

    /**
     * Default constructor
     * It sets the DateBuilder as default comparator, if
     * net.coderazzi.filters.TextParser.CompareDatesAsRendered is defined as true
     * In the same mode, it will be case insensitive if 
     * net.coderazzi.filters.TextParser.IgnoreCase is defined as true
     */
    public OperandFactory() {
    	if (Boolean.parseBoolean(Messages.getString("TextParser.CompareDatesAsRendered", ""))){
        	setComparator(Date.class, DateHandler.getDefault());    		
    	}
    	ignoreCase=Boolean.parseBoolean(Messages.getString("TextParser.IgnoreCase", ""));
	}

    /**
     * @see  IRelationalOperandFactory#getOperand(char, String, int)
     */
    public IRelationalOperand getOperand(char c, String all, int index) {

        switch (c) {

        case '~':
            return ((all.length() > index) && (all.charAt(index) == '@')) ? equalICaseREOperand
                                                                          : equalREOperand;

        case '=':
            return ((all.length() > index) && (all.charAt(index) == '@')) ? equalICaseOperand
                                                                          : equalOperand;

        case '<':

            switch ((all.length() > index) ? all.charAt(index++) : '-') {

            case '@':
                return lowerICaseOperand;

            case '=':
                return ((all.length() > index) && (all.charAt(index) == '@'))
                    ? lowerOrEqualICaseOperand : lowerOrEqualOperand;

            case '>':
                return ((all.length() > index) && (all.charAt(index) == '@')) ? distinctICaseOperand
                                                                              : distinctOperand;

            default:
                return lowerOperand;
            }

        case '>':

            switch ((all.length() > index) ? all.charAt(index++) : '-') {

            case '@':
                return greaterICaseOperand;

            case '=':
                return ((all.length() > index) && (all.charAt(index) == '@'))
                    ? greaterOrEqualICaseOperand : greaterOrEqualOperand;

            default:
                return greaterOperand;
            }

        case '!':

            switch ((all.length() > index) ? all.charAt(index++) : '-') {

            case '~':
                return ((all.length() > index) && (all.charAt(index) == '@'))
                    ? distinctICaseREOperand : distinctREOperand;

            case '=':
                return ((all.length() > index) && (all.charAt(index) == '@')) ? distinctICaseOperand
                                                                              : distinctOperand;
            }
        }

        return null;
    }

    /**
     * The default implementation returns the EQUAL THAN operand for all types except String, and
     * the regular expression based EQUAL THAN for strings.
     *
     * @see  IRelationalOperandFactory#getDefaultOperand(Class, boolean, boolean)
     */
    public IRelationalOperand getDefaultOperand(Class<?> c, boolean nullOp, boolean rightId) {

        if (nullOp || rightId || (c != String.class))
            return equalOperand;

        return ignoreCase ? equalICaseREOperand : equalREOperand;
    }


    /**
     * @see  IRelationalOperandFactory#setComparator(Class, Comparator)
     */
    public void setComparator(Class<?> c, Comparator<?> cmp) {
        comparators.put(c, cmp);
    }

    /**
     * @see  IRelationalOperandFactory#setIgnoreCase(boolean)
     */
    public void setIgnoreCase(boolean ignore) {
        ignoreCase = ignore;
        equalREOperand.setIgnoreCase(ignore);
        distinctREOperand.setIgnoreCase(ignore);
    }

    /**
     * Parent class to implement any basic comparison operand.
     */
    public abstract class ComparisonOperand implements IRelationalOperand {
        protected int n;
        private String repr; //only used for toString
        private boolean equalOperator;

        public ComparisonOperand(String repr, boolean equalOperator) {
            this.repr = repr;
            this.n = repr.length();
            this.equalOperator = equalOperator;
        }

        @Override public String toString() {
            return repr;
        }

        /**
         * @see  IRelationalOperand#stringBased()
         */
        public boolean stringBased() {
            return false;
        }

        /**
         * @see  IRelationalOperand#symbolLength()
         */
        public int symbolLength() {
            return n;
        }

        /**
         * @see  IRelationalOperand#appliesOnType(Class)
         */
        public boolean appliesOnType(Class<?> c) {
            return Comparable.class.isAssignableFrom(c) || (comparators.get(c) != null) ||
                equalOperator;
        }

        /**
         * @see  IRelationalOperand#createOperator(Object)
         */
        public IOperator createOperator(final Object right) {

            if (right == null) {

                if (equalOperator) {
                    return new IOperator() {
                            public boolean apply(Object left) {
                                return applyOnCompare(
                                        ((left == null) || (left.toString().trim().length() == 0))
                                        ? 0 : -1);
                            }

                            @Override public String toString() {
                                return String.format("%s %s", ComparisonOperand.this.toString(),
                                        FilterTextParser.DEFAULT_NULL_STRING);
                            }
                        };
                }

                return null;
            }

            if (right instanceof String) {
                return new IOperator() {
                        public boolean apply(Object left) {
                            return applyString(left, (String) right);
                        }

                        @Override public String toString() {
                            return ComparisonOperand.this.toString() + " " + right.toString();
                        }
                    };
            }

            return new IOperator() {
                    public boolean apply(Object left) {
                        return apply2(left, right);
                    }

                    @Override public String toString() {
                        return ComparisonOperand.this.toString() + " " + right.toString();
                    }
                };
        }

        /**
         * Implementation of the operand
         */
        @SuppressWarnings("unchecked")
        protected boolean apply2(Object left, Object right) {

            if (left == null) {
                return applyOnCompare(-1);
            }

            Comparator comp = comparators.get(left.getClass());

            if (comp != null)
                return applyOnCompare(comp.compare(left, right));

            if (left instanceof Comparable)
                return applyOnCompare(((Comparable) left).compareTo(right));

            //if we are here, is beacuse this is a equal operator
            return applyOnCompare(left.equals(right) ? 0 : -1);
        }

        /**
         * Implementation of the operand when the affected objects are strings
         */
        @SuppressWarnings("unchecked")
        protected boolean applyString(Object left, String right) {

            if (left == null) {
                return applyOnCompare(-1);
            }

            String l = (left instanceof String) ? (String) left : left.toString();
            Comparator comp = comparators.get(l.getClass());

            if (comp != null)
                return applyOnCompare(comp.compare(l, right));

            return applyOnCompare(ignoreCase ? l.compareToIgnoreCase(right) : l.compareTo(right));
        }

        /**
         * Abstract method, containing the result of comparing left against b, following the
         * semantics in {@link java.util.Comparator#compare(Object, Object)}
         */
        public abstract boolean applyOnCompare(int n);
    }


    /**
     * Parent class to implement any comparison operand based on strings ignoring case
     */
    public abstract class ComparisonICaseOperand extends ComparisonOperand {
        public ComparisonICaseOperand(String repr) {
            super(repr, false);
        }

        /**
         * @see  IRelationalOperand#stringBased()
         */
        @Override public boolean stringBased() {
            return true;
        }

        /**
         * @see  IRelationalOperand#appliesOnType(Class)
         */
        @Override public boolean appliesOnType(Class<?> c) {
            return true;
        }

        /**
         * @see  IRelationalOperand#createOperator(Object)
         */
        @Override public IOperator createOperator(Object right) {

            if (right == null)
                return null;

            return super.createOperator(right.toString().toLowerCase());
        }

        /**
         * Reimplementation of the operand, converting the objects to strings, lower case, and then
         * performing the comparison
         */
        @Override protected boolean apply2(Object left, Object right) {

            if (left == null) {
                return applyOnCompare(-1);
            }

            return applyOnCompare(left.toString().toLowerCase().compareTo((String) right));
        }


    }

}
