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

import net.coderazzi.filters.parser.FilterTextParsingException;
import net.coderazzi.filters.parser.IFilterTextParser;
import net.coderazzi.filters.parser.ITypeBuilder;
import net.coderazzi.filters.parser.IdentifierInfo;
import net.coderazzi.filters.resources.Messages;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.RowFilter;


/**
 * <p>Specific implementation of the {@link net.coderazzi.filters.parser.IFilterTextParser}
 * interface.</p>
 *
 * <p>Own features of this implementation includes:
 *
 * <ul>
 * <li>Identifiers are case insensitive, unless there are multiple identifiers with the same
 * case-variant text.</li>
 * <li>It defines a predefined grammar, where the important characters are: <b>&amp;</b> <b>|</b>
 * <b>(</b> <b>)</b>, and <b>\</b> to escape them.</li>
 * <li>It supports an extensable grammar to define the relational operators, based on the {@link
 * IRelationalOperandFactory} interface.</li>
 * <li>It supports an interface to define how to build not known types, the {@link
 * IRelationalOperandFactory} interface.</li>
 * <li>It supports the notion of null. The specific text associated to null is configurable.</li>
 * <li>It support comparison between identifiers, but only if the operand is not string based and
 * both identifiers have the same type (and is not String)</li>
 * <li>A filter is composed by one or more subfilters, separated by <b>&amp;</b> or <b>|</b>
 * symbols, and possibly grouped using parenthesis.</li>
 * <li>The smallest filter unit is composed by any string not including the previous symbols. It is
 * interpreted as identifier-operand-expression, where the identifier and operand are optional. The
 * grammar itself is unaware of the operators, working through a {@link IRelationalOperandFactory}
 * that can identify any portion of string as an operand.<br>
 * Once an operand is identified, the previous part of the string is considered the identifier, and
 * the rest, the expression<br>
 * Examples
 *
 * <ul>
 * <li>(identifier &gt; 34)</li>
 * <li>(&gt; chain of characters)</li>
 * <li>(bravo)</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public class FilterTextParser implements IFilterTextParser {

    final public static char SPACE_CHAR = ' ';
    final public static char ESCAPE_CHAR = '\\';
    final public static char AND_CHAR = '&';
    final public static char OR_CHAR = '|';
    final public static char OPEN_PARENTHESIS_CHAR = '(';
    final public static char CLOSE_PARENTHESIS_CHAR = ')';
    final public static String DEFAULT_NULL_STRING = Messages.getString(
            "TextParser.NullFilterText");

    final static String INVALID_OPERAND_EXCEPTION = Messages.getString(
            "TextParser.InvalidOperand");
    final static String INVALID_OPERAND_IDENTIFIER_EXCEPTION = Messages.getString(
            "TextParser.InvalidOperandForIdentifier");
    final static String INVALID_TYPE_EXCEPTION = Messages.getString("TextParser.InvalidType");
    final static String UNCOMPLETE_FILTER_EXCEPTION = Messages.getString(
            "TextParser.UncompleteFilter");
    final static String UNEXPECTED_TEXT_EXCEPTION = Messages.getString(
            "TextParser.UnexpectedText");
    final static String INVALID_IDENTIFIER_EXCEPTION = Messages.getString(
            "TextParser.InvalidIdentifier");

    /** Set of defined identifiers, indexed by name */
    private Map<String, IdentifierInfo> identifiers;

    /** The factory to hadle relational operands */
    IRelationalOperandFactory operandFactory;

    /** The factory to build any type (even basic ones) */
    ITypesFactory typesFactory;

    /** Internal class that performs the parsing. */
    private Parser parser;

    /** The user-defined null string */
    String nullString = DEFAULT_NULL_STRING;

    /**
     * Simple contructor
     */
    public FilterTextParser() {
        this(null, null);
    }

    /**
     * <p>Constructor defining a different set of relational operands</p>
     *
     * <p>Note that the main operands: ( ) &amp; | are hardcoded in this class, cannot be modified
     * in the passed factory</p>
     */
    public FilterTextParser(IRelationalOperandFactory operandFactory) {
        this(operandFactory, null);
    }

    /**
     * Constructor defining a specific type factory.
     */
    public FilterTextParser(ITypesFactory typesFactory) {
        this(null, typesFactory);
    }

    /**
     * <p>Constructor defining a different set of relational operands and a type factory.</p>
     *
     * <p>Note that the main operands: ( ) &amp; | are hardcoded in this class, cannot be modified
     * in the passed factory</p>
     */
    public FilterTextParser(IRelationalOperandFactory operandFactory, ITypesFactory typesFactory) {
        this.operandFactory = (operandFactory == null) ? new OperandFactory() : operandFactory;
        this.typesFactory = (typesFactory == null) ? new TypesFactory() : typesFactory;
        this.identifiers = new HashMap<String, IdentifierInfo>();
        this.parser = new Parser();
    }

    /**
     * @see  IFilterTextParser#setIgnoreCase(boolean)
     */
    public void setIgnoreCase(boolean ignore) {
        operandFactory.setIgnoreCase(ignore);
    }

    /**
     * @see  IFilterTextParser#setIdentifiers(List)
     */
    public void setIdentifiers(List<IdentifierInfo> validIdentifiers) {
        identifiers.clear();
        for (IdentifierInfo info : validIdentifiers)
            identifiers.put(info.name, info);
    }

    /**
     * <p>Sets a specific comparator for a given class, that should override the default Compare
     * algorithm for the given class.</p>
     *
     * <p>If the class is not {@link java.lang.Comparable}, this method should be invoked to support
     * any comparison operator.</p>
     */
    public void setComparator(Class<?> c, Comparator<?> cmp) {
        operandFactory.setComparator(c, cmp);
    }

    /**
     * Provides the {@link ITypeBuilder} to build non basic types
     */
    public void setTypeBuilder(Class<?> c, ITypeBuilder parser) {
        typesFactory.setFactory(c, parser);
    }

    /**
     * @see  IFilterTextParser#parseText(String, int)
     */
    public RowFilter parseText(String exp, int defaultFilterPosition)
        throws FilterTextParsingException {
        return parser.parseText(exp, getIdentifier(defaultFilterPosition));
    }


    /**
     * Sets the representation of the null case, <b>null</b> by default.
     */
    public void setNullString(String nullString) {
        this.nullString = nullString;
    }

    /**
     * Returns the identifier associated to the given filter position, or null if the position is
     * not valid
     */
    private IdentifierInfo getIdentifier(int filterPosition) {
        if (filterPosition == NO_FILTER_POSITION)
            return null;
        for (IdentifierInfo info : identifiers.values())
            if (info.filterPosition == filterPosition)
                return info;

        return null;
    }

    /**
     * Returns an indentifier with the correct case, or null if the identifier is invalid.
     */
    IdentifierInfo getIdentifierIgnoringCase(String identifier) {

        IdentifierInfo ret = identifiers.get(identifier);
        if (ret == null)
            for (String i : identifiers.keySet())
                if (i.equalsIgnoreCase(identifier))
                    return identifiers.get(i);

        return ret;
    }

    /**
     * <p>Parser is the class that performs the text parsing.</p>
     *
     * <p>It creates {@link javax.swing.RowFilter} instances based on FilterNode and FilterLeaf
     * instances.</p>
     *
     * <p>A filter is considered a filter node, containing a filter leaf or connecting two filter
     * nodes.</p>
     *
     * <p>A filter leaf represents the smallest applicable filter, not composed of further
     * subfilters</p>
     */
    class Parser {
        private int parsingPosition;
        private IdentifierInfo defaultIdentifier;
        private String expression; //expression to parse

        /**
         * The default identifier is used in case that no identifier is provided in the expression.
         * It should belong to the set of existing identifiers, given to the FilterTextParser with
         * setIdentifiers. It can be null if the parser applies to ALL the identifiers In this case,
         * the returned filter means that if any of the identifiers passes the filter, the filter
         * return true
         */
        public FilterNode parseText(String exp, IdentifierInfo defaultIdentifier)
            throws FilterTextParsingException {
            parsingPosition = 0;
            this.defaultIdentifier = defaultIdentifier;
            this.expression = exp;

            return parse(exp.toCharArray(), false);
        }

        /**
         * main parsing algorithm, called recursively
         */
        private FilterNode parse(char[] characters, boolean closeParenthesisExpected)
            throws FilterTextParsingException {
            FilterNode currentNode = new FilterNode();
            FilterNode returningNode = currentNode;
            boolean escape = false;
            int firstCharacter = parsingPosition;
            int lengthString = 0;

            //int lastCharacter = -1;
            StringBuilder sb = new StringBuilder();
            int len = characters.length;
            IdentifierInfo identifier = null;
            IRelationalOperand operand = null;

            while (parsingPosition < len) {
                char c = characters[parsingPosition++];
                if (escape) {
                    switch (c) {

                    case OPEN_PARENTHESIS_CHAR:
                    case CLOSE_PARENTHESIS_CHAR:
                    case AND_CHAR:
                    case OR_CHAR:
                    case ESCAPE_CHAR:
                    case SPACE_CHAR:
                        break;

                    default:
                        sb.append(ESCAPE_CHAR);

                        break;
                    }
                    sb.append(c);
                    escape = false;
                    lengthString = sb.length();
                } else {
                    switch (c) {

                    case SPACE_CHAR: // dismiss initial spaces, do nothing with the rest
                        if (sb.length() > 0) {
                            sb.append(c);
                        } else {
                            firstCharacter = parsingPosition;
                        }

                        break;

                    case ESCAPE_CHAR: // dismiss next character. The character will be included.
                        escape = true;

                        break;

                    case OPEN_PARENTHESIS_CHAR:

                        // it is an error if there is some read text or current group
                        // does not expect a 'right' term
                        if ((sb.length() > 0) || !currentNode.acceptRight()) {
                            throwBasicParsingException(parsingPosition - 1);
                        }
                        currentNode = currentNode.right = parse(characters, true);

                        break;

                    case CLOSE_PARENTHESIS_CHAR:
                        if (lengthString > 0) // if this happen, the text was already expected
                            currentNode.right = createFilterNode(identifier, operand,
                                    firstCharacter, sb.substring(0, lengthString));
                        if (!closeParenthesisExpected || !currentNode.isComplete())
                            throwBasicParsingException(parsingPosition - 1);

                        return returningNode.right;

                    case AND_CHAR:
                    case OR_CHAR:
                        if (lengthString > 0) { // if this happen, the text was already expected
                            currentNode.right = createFilterNode(identifier, operand,
                                    firstCharacter, sb.substring(0, lengthString));
                            if (currentNode.isRoot()) {
                                currentNode = currentNode.right;
                            }
                        } else if (!currentNode.isComplete()) {
                            throwBasicParsingException(parsingPosition - 1);
                        }
                        currentNode.createLogicalCondition(c == AND_CHAR);
                        sb.delete(0, sb.length());
                        firstCharacter = parsingPosition;
                        operand = null;
                        lengthString = 0;

                        break;

                    default: // valid character
                        if (currentNode.acceptRight()) {
                            if (operand == null) {
                                operand = operandFactory.getOperand(c, expression, parsingPosition);
                                if (operand != null) {
                                    identifier = getIdentifier(sb.substring(0, lengthString),
                                            firstCharacter);
                                    if (!operand.appliesOnType(getIdentifierClass(identifier))) {
                                        String error;
                                        if (identifier == null) {
                                            error = String.format(INVALID_OPERAND_EXCEPTION,
                                                    operand.toString());
                                        } else {
                                            error = String.format(
                                                    INVALID_OPERAND_IDENTIFIER_EXCEPTION,
                                                    operand.toString(), identifier);
                                        }
                                        throw new FilterTextParsingException(error,
                                            parsingPosition - 1);
                                    }
                                    firstCharacter = parsingPosition += operand.symbolLength() - 1;
                                    sb.delete(0, sb.length());

                                    break;
                                }
                            }
                            sb.append(c);
                            lengthString = sb.length();

                            break;
                        }
                        throwBasicParsingException(parsingPosition - 1);
                    }
                }
            }
            if (escape) { // ended with the scape char
                throwBasicParsingException(len - 1);
            }
            if (closeParenthesisExpected) {
                throwBasicParsingException(len);
            }
            if (lengthString > 0) {
                currentNode.right = createFilterNode(identifier, operand, firstCharacter,
                        sb.substring(0, lengthString));
            } else if (operand != null) {
                throwBasicParsingException(len);
            } else if (!currentNode.isComplete() && !returningNode.isEmpty()) {
                throwBasicParsingException(len);
            }

            return returningNode.right;
        }


        /**
         * Creates a filter node using the given identifier and operand, and the substring of the
         * parsed expression contained in (firstCharacter, lastCharacter) For example, in a < 3, the
         * identifier is associated to 'a', the operand is '<', and '3' will be in
         * expression[firstCharacter:lastCharacter] Both the identifier and the operand can be null,
         * in which case it is used the default identifier and the default operator associated to
         * the class of the identier.
         */
        private FilterNode createFilterNode(IdentifierInfo identifier, IRelationalOperand operand,
            int firstCharacter, String content) throws FilterTextParsingException {

            Object info = null;

            if (identifier == null) {
                identifier = defaultIdentifier;
            }

            Class<?> base = getIdentifierClass(identifier);

            /**
             * We try now to handle the right part as an identifier, but only if the operand is not
             * string based and the type of the left identifier, if any, is not String. In addition,
             * the right identifier and left identifier should have the same type
             */
            IdentifierInfo rightIdentifier = null;

            if ((identifier != null) && (base != String.class) &&
                    ((operand == null) || !operand.stringBased())) {
                rightIdentifier = getIdentifierIgnoringCase(content);
                if ((rightIdentifier != null) && (base != getIdentifierClass(rightIdentifier))) {
                    rightIdentifier = null;
                }
            }

            boolean nullOp = (rightIdentifier == null) ? content.equals(nullString) : false;

            if (operand == null) {
                operand = operandFactory.getDefaultOperand(base, nullOp, rightIdentifier != null);
            }

            AbstractFilterLeaf leaf = null;

            if (rightIdentifier != null) {
                leaf = new FilterLeaf(identifier, operand, rightIdentifier);
            } else {

                if (!nullOp) {

                    if (operand.stringBased()) {
                        info = content;
                    } else {
                        int errorPosition = firstCharacter;

                        try {

                            //note: if the factory is not created for the class base,
                            //it is just returned the same string
                            info = typesFactory.buildObject(base, content);
                        } catch (FilterTextParsingException ex) {
                            errorPosition += ex.getPosition();
                        }

                        if (info == null) {
                            throw new FilterTextParsingException(String.format(
                                    INVALID_TYPE_EXCEPTION, base.getSimpleName(),
                                    expression.substring(errorPosition)), errorPosition);
                        }
                    }
                }

                IOperator op = operand.createOperator(info);

                if (op == null)
                    throwBasicParsingException(firstCharacter - 1);

                leaf = new ConstFilterLeaf(identifier, op);
            }

            return new FilterNode(leaf);
        }

        /**
         * Convenient method to throw an exception corresponding to the most general cases: filter
         * is uncomplete, or some text was unexpected.
         */
        private void throwBasicParsingException(int position) throws FilterTextParsingException {

            if (position >= expression.length())
                throw new FilterTextParsingException(String.format(UNCOMPLETE_FILTER_EXCEPTION,
                        expression), expression.length());

            throw new FilterTextParsingException(String.format(UNEXPECTED_TEXT_EXCEPTION,
                    expression.substring(0, position)), position);
        }

        /**
         * Returns the class that will be associated to the given identifier
         */
        private Class<?> getIdentifierClass(IdentifierInfo identifier) {
            return typesFactory.getBuildType((identifier == null) ? String.class
                                                                  : identifier.associatedClass);
        }

        /**
         * Returns the identifier associated to the substring of the parsed expression
         */
        private IdentifierInfo getIdentifier(String given, int firstCharacter)
            throws FilterTextParsingException {

            if (given.length() == 0)
                return defaultIdentifier;

            IdentifierInfo ret = getIdentifierIgnoringCase(given);

            if (ret == null)
                throw new FilterTextParsingException(String.format(INVALID_IDENTIFIER_EXCEPTION,
                        given), firstCharacter);

            return ret;
        }

    }

    /**
     * A AbstractFilterLeaf represents the smallest RowFilter possible, not composed of further
     * subfilters. It applies on a given identifier
     */
    static abstract class AbstractFilterLeaf extends RowFilter {

        IdentifierInfo identifier;

        AbstractFilterLeaf(IdentifierInfo identifier) {
            this.identifier = identifier;
        }

    }

    /**
     * A ConstFilterLeaf is a AbstractFilterLeaf applied on a constant value
     */
    class ConstFilterLeaf extends AbstractFilterLeaf {

        IOperator op;

        ConstFilterLeaf(IdentifierInfo identifier, IOperator op) {
            super(identifier);
            this.op = op;
        }

        @Override public boolean include(RowFilter.Entry rowEntry) {

            if (identifier == null) {
                for (IdentifierInfo info : identifiers.values()) {
                    if (op.apply(rowEntry.getStringValue(info.filterPosition).trim())) //$NON-NLS-1$
                        return true;
                }

                return false;
            }

            return op.apply(rowEntry.getValue(identifier.filterPosition));
        }

        @Override public String toString() {
            return ((identifier == null) ? "" : identifier.name) + ' ' + op.toString();
        }
    }

    /**
     * A FilterLeaf is a AbstractFilterLeaf applied against an identifier
     */
    static class FilterLeaf extends AbstractFilterLeaf {

        IRelationalOperand op;
        IdentifierInfo rightIdentifier;

        FilterLeaf(IdentifierInfo identifier, IRelationalOperand op,
            IdentifierInfo rightIdentifier) {
            super(identifier);
            this.op = op;
            this.rightIdentifier = rightIdentifier;
        }

        @Override public boolean include(RowFilter.Entry rowEntry) {
            IOperator operator = op.createOperator(rowEntry.getValue(
                        rightIdentifier.filterPosition));

            return (operator != null) &&
                operator.apply(rowEntry.getValue(identifier.filterPosition));
        }

        @Override public String toString() {
            return identifier.name + ' ' + op.toString() + ' ' + rightIdentifier.name;
        }
    }


    /**
     * A filter node is a container of a FilterLeaf, or a composition of two additional nodes,
     * logically binded with an AND or OR operation.
     */
    static class FilterNode extends RowFilter {

        boolean and;
        AbstractFilterLeaf info;
        FilterNode left, right;

        /**
         * Creates the root node
         */
        FilterNode() {
            left = this;
        }

        /**
         * Creates a node with a leaf filter.
         */
        FilterNode(AbstractFilterLeaf info) //leaf group
        {
            this.info = info;
        }

        @SuppressWarnings("unchecked")
		@Override public boolean include(RowFilter.Entry rowEntry) {

            if (info != null)
                return info.include(rowEntry);

            boolean start = left.include(rowEntry);

            if (and)
                return start && right.include(rowEntry);

            return start || right.include(rowEntry);
        }

        /**
         * Expands the tree associated to this node, associating a logical condition (AND/OR) to it.
         */
        void createLogicalCondition(boolean andCondition) //condition group
        {
            FilterNode newLeft = new FilterNode();
            newLeft.and = and;
            newLeft.info = info;
            newLeft.left = left;
            newLeft.right = right;
            info = null;
            and = andCondition;
            left = newLeft;
            right = null;
        }

        boolean isRoot() {
            return left == this;
        }

        boolean acceptRight() {
            return (info == null) && (right == null);
        }

        boolean isLeaf() {
            return info != null;
        }

        boolean isComplete() {
            return (info != null) || ((left != null) && (right != null));
        }

        boolean isEmpty() {
            return (info == null) && (left == this) && (right == null);
        }

        AbstractFilterLeaf getContent() {
            return info;
        }

    }
}
