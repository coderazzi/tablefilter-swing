/**
 * Author:  Luis M Pena  ( dr.lu@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  dr.lu@coderazzi.net
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


import java.util.regex.Pattern;


/**
 * <p>Operand associated to expressions with wilcards.</p>
 *
 * <p>It accepts two wildcard characters, used as widely known in filesystems:
 *
 * <ul>
 * <li><b>*</b>: zero or more characters.</li>
 * <li><b>?</b>: exactly one character.</li>
 * </ul>
 *
 * For example: A?m*s will match: Armies, Almanacs, ...</p>
 *
 * <p>The implementation handles at one equal and distinct operators, case sensitive or
 * insensitive</p>
 *
 * @author  Luis M Pena - dr.lu@coderazzi.net
 */
public class StringWildcardOperand implements IRelationalOperand {
    private String repr; //only used for toString
    private boolean ignoreCase;
    boolean equal;

    /**
     * Constructor
     *
     * @param  repr        The string representation of this operand
     * @param  equal       Whether to apply equal or different semantics
     * @param  ignoreCase  Set to true to make the operand case insensitive
     */
    public StringWildcardOperand(String repr, boolean equal, boolean ignoreCase) {
        this.repr = repr;
        this.equal = equal;
        this.ignoreCase = ignoreCase;
    }

    /**
     * Makes the operand case sensitive/insensitive
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override public String toString() {
        return repr;
    }

    /**
     * @see  IRelationalOperand#stringBased()
     */
    public boolean stringBased() {
        return true;
    }

    /**
     * @see  IRelationalOperand#appliesOnType(Class)
     */
    public boolean appliesOnType(Class<?> c) {
        return true;
    }

    /**
     * @see  IRelationalOperand#symbolLength()
     */
    public int symbolLength() {
        return repr.length();
    }

    /**
     * @see  IRelationalOperand#createOperator(Object)
     */
    public IOperator createOperator(final Object right) {

        if (right == null)
            return null;

        String c = convertWilcardExpressionToRegularExpression(right.toString());
        final Pattern pattern = ignoreCase ? Pattern.compile(c, Pattern.CASE_INSENSITIVE)
                                           : Pattern.compile(c);

        return new IOperator() {
                public boolean apply(Object left) {

                    if (left == null) {
                        return !equal;
                    }

                    return pattern.matcher(left.toString()).matches() == equal;
                }

                @Override public String toString() {
                    return StringWildcardOperand.this.toString() + " " + right.toString();
                }
            };
    }

    /**
     * Converts an expression with wilcards into a regular expression
     */
    private String convertWilcardExpressionToRegularExpression(String s) {
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;

        for (char c : s.toCharArray()) {

            switch (c) {

            case '\\':

                if (escaped)
                    sb.append("\\");

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
}
