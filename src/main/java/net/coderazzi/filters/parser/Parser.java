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

import java.text.Format;
import java.text.ParseException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.RowFilter;

import net.coderazzi.filters.IParser;


/**
 * Basic implementation of a {@link IParser}, supporting only simple operators
 * referring to the content of a single column.<br>
 * The supporter operators include:
 *
 * <ul>
 *   <li>Comparison operators. The comparison is done on the parsed object, not
 *     on the string representation, unless no {@link Format} or {@link
 *     Comparator} is defined for the given type. For example, specifying the
 *     text &quot;&gt;= 4&quot; implies, for a column with integer types, that a
 *     direct comparison between integers will be performed. These operators
 *     are:
 *
 *     <ul>
 *       <li>&gt;=</li>
 *       <li>&gt;</li>
 *       <li>&lt;</li>
 *       <li>&lt;=</li>
 *       <li>&lt;&gt;, !: all behave the same</li>
 *     </ul>
 *   </li>
 *   <li>Basic wildcard operators. These operators work using the string
 *     representation of the types (using, when possible, the defined {@link
 *     Format} instance). Only two wildcard characters are defined: * and ?
 *
 *     <ul>
 *       <li>~: for example ~ *vadis* will filter in all expressions including
 *         the substring vadis</li>
 *       <li>!~: negates the previous operator</li>
 *     </ul>
 *   </li>
 *   <li>Regular expression operator. There is only one such operator: ~~,
 *     accepting a java regular expression.</li>
 * </ul>
 *
 * <p>In addition, providing no operator will behave as the operator ~</p>
 */
public class Parser implements IParser {

    FormatWrapper format;
    Comparator comparator;
    boolean ignoreCase;
    Comparator<String> stringComparator;
    int modelIndex;
    private static Map<String, IOperand> operands;
    private static IOperand wildcardOperand;
    private static WildcardOperand instantOperand;
    private static Pattern expressionMatcher;
    private static StringBuilder escapeBuffer = new StringBuilder();

    public Parser(Format             format,
                  Comparator         classComparator,
                  Comparator<String> stringComparator,
                  boolean            ignoreCase,
                  int                modelIndex) {
        this.format = new FormatWrapper(format);
        this.comparator = classComparator;
        this.stringComparator = stringComparator;
        this.ignoreCase = ignoreCase;
        this.modelIndex = modelIndex;
    }

    /** {@link IParser} interface. */
    @Override public RowFilter parseText(String expression)
                                  throws ParseException {
        Matcher matcher = expressionMatcher.matcher(expression);
        if (matcher.matches()) {
            // all expressions match!
            IOperand op = operands.get(matcher.group(1));
            if (op == null) {
                // note that instant does not apply if there is an operator!
                op = wildcardOperand;
            }

            return op.create(this, matcher.group(2).trim());
        }

        return null;
    }

    /** {@link IParser} interface. */
    @Override public InstantFilter parseInstantText(String expression)
                                             throws ParseException {
    	expression = expression.trim();
        Matcher matcher = expressionMatcher.matcher(expression);
        if (matcher.matches()) {
            // all expressions match!
            IOperand op = operands.get(matcher.group(1));
            if (op == null) {
                // note that instant does not apply if there is an operator!
                op = instantOperand;
            }

            InstantFilter ret = new InstantFilter();
            ret.filter = op.create(this, matcher.group(2));
            ret.expression = (op == instantOperand)
                ? instantOperand.getAppliedExpression(expression) : expression;

            return ret;
        }

        return null;
    }

    /** {@link IParser} interface. */
    @Override public String escape(String expression) {
    	expression = expression.trim();
        Matcher matcher = expressionMatcher.matcher(expression);
        if (matcher.matches()) {
            String operator = matcher.group(1);
            int lastAdded = 0;
            if (operator != null) {
                escapeBuffer.append('\\').append(operator);
                expression = matcher.group(2);
            }

            int total = expression.length();
            for (int i = 0; i < total; i++) {
                char ch = expression.charAt(i);
                if ((ch == '*') || (ch == '?') || (ch == '\\')) {
                    escapeBuffer.append(expression.substring(lastAdded, i));
                    escapeBuffer.append('\\').append(ch);
                    lastAdded = i + 1;
                }
            }

            if (escapeBuffer.length() > 0) {
                escapeBuffer.append(expression.substring(lastAdded, total));
                expression = escapeBuffer.toString();
                escapeBuffer.delete(0, escapeBuffer.length());
            }
        }

        return expression;
    }

    /** Internal interface, to be implemented by all operands. */
    interface IOperand {
        RowFilter create(Parser self, String right) throws ParseException;
    }

    /** IOperand for comparison operations. */
    abstract static class ComparisonOperand implements IOperand {
        abstract boolean matches(int comparison);

        /** {@link IOperand} interface. */
        @Override public RowFilter create(Parser self, String right)
                                   throws ParseException {

            if (right != null) {
                if (self.comparator == null) {
                    return createStringOperator(right, self.modelIndex,
                            self.format, self.stringComparator);
                }

                Object o = self.format.parseObject(right);
                if (o != null) {
                    return createOperator(o, self.modelIndex, self.comparator);
                }
            }

            throw new ParseException("", 0);
        }

        /** Operator fine for given type, apply it. */
        private RowFilter createOperator(final Object     right,
                                         final int        modelIndex,
                                         final Comparator comparator) {
            return new RowFilter() {
                @Override public boolean include(Entry entry) {
                    Object left = entry.getValue(modelIndex);

                    return (left != null)
                            && matches(comparator.compare(left, right));
                }
            };
        }

        /** Operator invalid for given type, filter by string representation. */
        private RowFilter createStringOperator(
                final String        right,
                final int           modelIndex,
                final FormatWrapper format,
                final Comparator    stringComparator) {
            return new RowFilter() {
                @Override public boolean include(Entry entry) {
                    Object left = entry.getValue(modelIndex);
                    if (left == null) {
                        return false;
                    }

                    String s = format.format(left);

                    return (s.length() > 0)
                            && matches(stringComparator.compare(s, right));
                }
            };
        }
    }

    /** IOperand for equal/unequal operations. */
    static class EqualOperand implements IOperand {

        boolean expected;

        /**
         * Single constructor.
         *
         * @param  expected  true if the operand expects the equal operation to
         *                   succeed
         */
        public EqualOperand(boolean expected) {
            this.expected = expected;
        }

        /** {@link IOperand} interface. */
        @Override public RowFilter create(Parser self, String right)
                                   throws ParseException {
            if (self.comparator == null) {
                return createStringOperator(right, self.modelIndex, self.format,
                        self.stringComparator);
            }

            if (right.length() == 0) {
                return createNullOperator(self.modelIndex);
            }

            Object o = self.format.parseObject(right);
            if (o == null) {
                throw new ParseException("", 0);
            }

            return createOperator(o, self.modelIndex, self.comparator);
        }

        /** Operator fine for given type, apply it. */
        private RowFilter createOperator(final Object     right,
                                         final int        modelIndex,
                                         final Comparator comparator) {
            return new RowFilter() {
                @Override public boolean include(Entry entry) {
                    Object left = entry.getValue(modelIndex);
                    boolean value = (left != null)
                            && (0 == comparator.compare(left, right));

                    return value == expected;
                }
            };
        }

        /** No right operand give, comparing against 'null'. */
        private RowFilter createNullOperator(final int modelIndex) {
            return new RowFilter() {
                @Override public boolean include(Entry entry) {
                    Object left = entry.getValue(modelIndex);

                    return expected == (left == null);
                }
            };
        }

        /** Operator invalid for given type, filter by string representation. */
        private RowFilter createStringOperator(
                final String        right,
                final int           modelIndex,
                final FormatWrapper format,
                final Comparator    stringComparator) {
            return new RowFilter() {
                @Override public boolean include(Entry entry) {
                    Object left = entry.getValue(modelIndex);
                    String value = format.format(left);

                    return expected = (stringComparator.compare(value, right)
                                    == 0);
                }
            };
        }
    }

    /** Operand for regular expressions. */
    static class REOperand implements IOperand {
        boolean equals;

        /**
         * Single constructor.
         *
         * @param  equals  true if the operand expects the regular expression
         *                 matching to succeed
         */
        public REOperand(boolean equals) {
            this.equals = equals;
        }

        /** {@link IOperand} interface. */
        @Override public RowFilter create(Parser self, String right)
                                   throws ParseException {
            final Pattern pattern = getPattern(right, self.ignoreCase);
            final int modelIndex = self.modelIndex;
            final FormatWrapper format = self.format;

            return new RowFilter() {

                @Override public boolean include(Entry entry) {
                    Object o = entry.getValue(modelIndex);
                    String left = format.format(o);

                    return equals == pattern.matcher(left).matches();
                }
            };
        }

        /**
         * Returns the {@link Pattern} instance associated to the provided
         * expression.
         */
        protected Pattern getPattern(String expression, boolean ignoreCase)
                              throws ParseException {
            try {
                return Pattern.compile(expression,
                        ignoreCase
                            ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                            : 0);
            } catch (PatternSyntaxException pse) {
                throw new ParseException("", pse.getIndex());
            }
        }
    }

    /** Operand for wildcard expressions. */
    static class WildcardOperand extends REOperand {

        private boolean instant;
        private boolean instantApplied;

        /** Constructor for instant operand. */
        public WildcardOperand() {
            super(true);
            this.instant = true;
        }

        /** Constructor for equal/unequal simple regular expression. */
        public WildcardOperand(boolean equals) {
            super(equals);
        }

        /**
         * After the operand is used, this method returns the expression that
         * has been really applied to obtain the filter.
         */
        public String getAppliedExpression(String baseExpression) {
            if (instantApplied) {
                return baseExpression + "*";
            }

            return baseExpression;
        }

        /** {@link REOperand} interface. */
        @Override protected Pattern getPattern(String  right,
                                               boolean ignoreCase)
                                        throws ParseException {
            return super.getPattern(convertToRE(right), ignoreCase);
        }

        /** Converts a wildcard expression into a regular expression. */
        protected String convertToRE(String s) {
            StringBuilder sb = new StringBuilder();
            boolean escaped = false;
            instantApplied = false;

            for (char c : s.toCharArray()) {

                switch (c) {

                case '\\':

                    if (escaped) {
                        sb.append("\\");
                    }

                    escaped = !escaped;

                    break;

                case '[':
                case ']':
                case '^':
                case '$':
                case '+':
                case '{':
                case '}':
                case '|':
                case '(':
                case ')':
                case '.':
                    sb.append('\\').append(c);
                    escaped = false;

                    break;

                case '*':

                    if (escaped) {
                        sb.append("\\*");
                        escaped = false;
                    } else {
                        sb.append(".*");
                    }

                    break;

                case '?':

                    if (escaped) {
                        sb.append("\\?");
                        escaped = false;
                    } else {
                        sb.append(".");
                    }

                    break;

                default:
                    sb.append(c);
                    escaped = false;

                    break;
                }
            }

            if (instant) {
                int l = sb.length();
                if ((l < 2) || !sb.substring(l - 2).equals(".*")) {
                    instantApplied = true;
                    sb.append(".*");
                }
            }

            return sb.toString();
        }

    }

    static {
        expressionMatcher = Pattern.compile(
        		"^\\s*(>=|<=|<>|!~|~~|>|<|=|~|!)?\\s*(.*)$");

        operands = new HashMap<String, IOperand>();
        operands.put("~~", new REOperand(true));
        operands.put("!~", new WildcardOperand(false));
        operands.put("!", new EqualOperand(false));
        operands.put(">=", new ComparisonOperand() {
                @Override boolean matches(int comparison) {
                    return comparison >= 0;
                }
            });
        operands.put(">", new ComparisonOperand() {
                @Override boolean matches(int comparison) {
                    return comparison > 0;
                }
            });
        operands.put("<=", new ComparisonOperand() {
                @Override boolean matches(int comparison) {
                    return comparison <= 0;
                }
            });
        operands.put("<", new ComparisonOperand() {
                @Override boolean matches(int comparison) {
                    return comparison < 0;
                }
            });
        operands.put("<>", new ComparisonOperand() {
                @Override boolean matches(int comparison) {
                    return comparison != 0;
                }
            });
        operands.put("~", wildcardOperand = new WildcardOperand(true));
        operands.put("=", new EqualOperand(true));
        instantOperand = new WildcardOperand();
    }

    /** Helper class to deal with null formats. */
    static class FormatWrapper {
        Format format;

        FormatWrapper(Format format) {
            this.format = format;
        }

        public String format(Object o) {
            return (format == null) ? ((o == null) ? "" : o.toString())
                                    : format.format(o);
        }

        public Object parseObject(String content) throws ParseException {
            return (format == null) ? null : format.parseObject(content);
        }
    }

}
