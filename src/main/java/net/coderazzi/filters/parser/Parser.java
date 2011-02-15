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
 * <p>In addition, providing no operator will behave as the operator ~, unless
 * the text can be parsed as a (non string) object; in this case, it will behave
 * as operator =.</p>
 */
public class Parser implements IParser {

    FormatWrapper format;
    Comparator comparator;
    boolean ignoreCase;
    Comparator<String> stringComparator;
    int modelIndex;
    private static Map<String, IOperand> operands;
    private static IOperand nullOperand;
    private static Pattern expressionMatcher;

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

    @Override public RowFilter parseText(String expression)
                                  throws ParseException {
        Matcher matcher = expressionMatcher.matcher(expression);
        if (matcher.matches()) {
            // all expressions match!
            IOperand op = operands.get(matcher.group(1));
            if (op == null) {
                op = nullOperand;
            }

            return op.create(this, matcher.group(2).trim());
        }

        return null;
    }


    @Override public String escape(String expression) {
        boolean needs = true;
        expression = expression.trim();

        Matcher matcher = expressionMatcher.matcher(expression);
        if (matcher.matches()) {
            // all expressions match!
            String operand = matcher.group(1);
            if (operand == null) {
                // if there is no operand, and class is String, or there
                // is no associated formatter, or the formatter does not
                // validate the given text, the string will be handled
                // with the match operator (~).
                try {
                    if ((format.format != null)
                            && (format.parseObject(expression) != null)) {
                        needs = false;
                    }
                } catch (ParseException pex) {
                    // invalid expression, needs remains true
                }

                needs = needs
                        && !expression.equals(
                            convertWilcardExpressionToRegularExpression(
                                expression));
            } else {
                needs = true;
            }
        }

        return needs ? ("= " + expression) : expression;
    }

    static String convertWilcardExpressionToRegularExpression(String s) {
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;

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

        return sb.toString();
    }

    /** Internal interface, to be implemented by all operands. */
    interface IOperand {
        RowFilter create(Parser self, String right) throws ParseException;
    }

    abstract static class ComparisonOperand implements IOperand {
        abstract boolean matches(int comparison);

        @Override public RowFilter create(Parser self, String right)
                                   throws ParseException {
            if (right == null) {
                throw new ParseException("", 0);
            }

            if (self.comparator == null) {
                return createStringOperator(right, self.modelIndex, self.format,
                        self.stringComparator);
            }

            Object o = self.format.parseObject(right);
            if (o == null) {
                throw new ParseException("", 0);
            }

            return createOperator(o, self.modelIndex, self.comparator);
        }

        private RowFilter createOperator(final Object     right,
                                         final int        modelIndex,
                                         final Comparator comparator) {
            return new RowFilter() {
                @SuppressWarnings("unchecked")
                @Override public boolean include(Entry entry) {
                    Object left = entry.getValue(modelIndex);

                    return (left != null)
                            && matches(comparator.compare(left, right));
                }
            };
        }

        private RowFilter createStringOperator(
                final String        right,
                final int           modelIndex,
                final FormatWrapper format,
                final Comparator    stringComparator) {
            return new RowFilter() {
                @SuppressWarnings("unchecked")
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

    static class EqualOperand implements IOperand {

        boolean expected;

        public EqualOperand(boolean expected) {
            this.expected = expected;
        }

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

        private RowFilter createOperator(final Object     right,
                                         final int        modelIndex,
                                         final Comparator comparator) {
            return new RowFilter() {
                @SuppressWarnings("unchecked")
                @Override public boolean include(Entry entry) {
                    Object left = entry.getValue(modelIndex);
                    boolean value = (left != null)
                            && (0 == comparator.compare(left, right));

                    return value == expected;
                }
            };
        }

        private RowFilter createNullOperator(final int modelIndex) {
            return new RowFilter() {
                @SuppressWarnings("unchecked")
                @Override public boolean include(Entry entry) {
                    Object left = entry.getValue(modelIndex);

                    return expected == (left == null);
                }
            };
        }

        private RowFilter createStringOperator(
                final String        right,
                final int           modelIndex,
                final FormatWrapper format,
                final Comparator    stringComparator) {
            return new RowFilter() {
                @SuppressWarnings("unchecked")
                @Override public boolean include(Entry entry) {
                    Object left = entry.getValue(modelIndex);
                    String value = format.format(left);

                    return expected = (stringComparator.compare(value, right)
                                    == 0);
                }
            };
        }
    }

    static class REOperand implements IOperand {
        boolean equals;

        public REOperand(boolean equals) {
            this.equals = equals;
        }

        @Override public RowFilter create(Parser self, String right)
                                   throws ParseException {
            return createWithPattern(self, getPattern(right, self.ignoreCase));
        }

        protected RowFilter createWithPattern(Parser        self,
                                              final Pattern pattern) {
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

        protected Pattern getPattern(String right, boolean ignoreCase)
                              throws ParseException {
            try {
                return Pattern.compile(right,
                        ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
            } catch (PatternSyntaxException pse) {
                throw new ParseException("", pse.getIndex());
            }
        }
    }

    static class SimpleREOperand extends REOperand {

        private boolean wildcardExpression;

        public SimpleREOperand(boolean equals) {
            super(equals);
        }

        public boolean wildcardExpression() {
            return wildcardExpression;
        }

        @Override protected Pattern getPattern(String  right,
                                               boolean ignoreCase)
                                        throws ParseException {
            String converted = convertWilcardExpressionToRegularExpression(
                    right);
            wildcardExpression = !converted.equals(right);

            return super.getPattern(converted, ignoreCase);
        }
    }

    static class NullOperand extends SimpleREOperand {

        IOperand defaultOperand;

        public NullOperand(IOperand defaultOperand) {
            super(true);
            this.defaultOperand = defaultOperand;
        }

        @Override public RowFilter create(Parser self, String right)
                                   throws ParseException {
            boolean problem = false;
            if (self.comparator != null) {
                try {
                    return defaultOperand.create(self, right);
                } catch (ParseException pex) {
                    problem = true;
                }
            }

            // default to string wildcard expression
            RowFilter ret = super.create(self, right);
            if (problem && !wildcardExpression()) {
                throw new ParseException(right, 0);
            }

            return ret;
        }
    }

    static {
        expressionMatcher = Pattern.compile(
                "^(>=|<=|<>|!=|!~|~~|==|>|<|=|~|!)?\\s*(.*)$");

        IOperand equalOperand = new EqualOperand(true);
        // nullOperand is used when the user enters no operator. It treats
        // the input as a match operand (~) if handled as a String or when
        // it is not possible to parse the given expression (for example,
        // entering '5*' on a Integer column
        nullOperand = new NullOperand(equalOperand);
        operands = new HashMap<String, IOperand>();
        operands.put("~~", new REOperand(true));
        operands.put("!~", new SimpleREOperand(false));
        operands.put("!", new EqualOperand(false));
        operands.put("=", equalOperand);
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
        operands.put("~", new SimpleREOperand(true));
    }

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
