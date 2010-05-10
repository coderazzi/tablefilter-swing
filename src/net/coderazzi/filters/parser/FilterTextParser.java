package net.coderazzi.filters.parser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.Format;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import net.coderazzi.filters.IFilterTextParser;


/**
 * Basic implementation of a {@link IFilterTextParser}, supporting only simple operators referring
 * to the content of a single column.<br>
 * The supporter operators include:
 *
 * <ul>
 *   <li>Comparison operators. The comparison is done on the parsed object, not on the string
 *     representation, unless no {@link Format} or {@link Comparator} is defined for the given type.
 *     For example, specifying the text &quot;&gt;= 4&quot; implies, for a column with integer
 *     types, that a direct comparison between integers will be performed. These operators are:
 *
 *     <ul>
 *       <li>&gt;=</li>
 *       <li>&gt;</li>
 *       <li>&lt;</li>
 *       <li>&lt;=</li>
 *       <li>&lt;&gt;</li>
 *     </ul>
 *   </li>
 *   <li>Equal operators. The comparison is done on the parsed object, not on the string
 *     representation, unless no {@link Format} is defined for the given type. The comparison is
 *     performed using the equals method. These operators are:
 *
 *     <ul>
 *       <li>!=: note that, in most cases, it will behave as the operator &lt;&gt;</li>
 *       <li>! : equivalent to !=</li>
 *       <li>&lt;</li>
 *       <li>=</li>
 *       <li>==: equivalent to =</li>
 *     </ul>
 *   </li>
 *   <li>Basic wildcard operators. These operators work using the string representation of the types
 *     (using, when possible, the defined {@link Format} instance). Only two wildcard characters are
 *     defined: * and ?
 *
 *     <ul>
 *       <li>~: for example ~ *vadis* will filter in all expressions including the substring
 *         vadis</li>
 *       <li>!~: negates the previous operator</li>
 *     </ul>
 *   </li>
 *   <li>Regular expression operator. There is only one such operator: ~~, accepting a java regular
 *     expression.</li>
 * </ul>
 */
public class FilterTextParser implements IFilterTextParser {

    Map<Class<?>, Format> formatters = new HashMap<Class<?>, Format>();
    Map<Class<?>, Comparator<?>> comparators = new HashMap<Class<?>, Comparator<?>>();
    boolean ignoreCase;
    private IOperand defaultOperand;
    private String defaultOperandString = "~";
    private TableModel model;
    private Pattern expressionMatcher;
    private Map<String, IOperand> operands;
    private PropertyChangeSupport propertiesHandler = new PropertyChangeSupport(this);

    public FilterTextParser() {
        expressionMatcher = Pattern.compile("^(>=|<=|<>|!=|!~|~~|==|>|<|=|~|!)?\\s*(.*)$");
        operands = new HashMap<String, IOperand>();
        operands.put("~~", new REOperand(true));
        operands.put("~", new SimpleREOperand(true));
        operands.put("!~", new SimpleREOperand(false));
        operands.put("!", new EqualOperand(false));
        operands.put("!=", new EqualOperand(false));
        operands.put("=", new EqualOperand(true));
        operands.put("==", new EqualOperand(true));
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
        defaultOperand = operands.get(defaultOperandString);
    }

    @Override public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertiesHandler.addPropertyChangeListener(listener);
    }

    @Override public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertiesHandler.removePropertyChangeListener(listener);
    }

    @Override public void setTableModel(TableModel model) {
        TableModel oldModel = model;
        this.model = model;
        propertiesHandler.firePropertyChange("tableModel", oldModel, model);
    }

    @Override public String getDefaultOperator() {
        return defaultOperandString;
    }

    @Override public void setDefaultOperator(String s) {
        String old = defaultOperandString;
        IOperand op = operands.get(s);
        if (op != null) {
            defaultOperand = op;
            propertiesHandler.firePropertyChange("defaultOperand", old, s);
        }
    }

    @Override public Format getFormat(Class<?> c) {
        return formatters.get(c);
    }

    /**
     * Sets the {@link Format} for the given Class. If the class belongs to
     * the {@link Date} hierarchy, it creates automatically a {@link Comparator}
     * for it, based on the {@link DateComparator} class, unless one
     * comparator has been already set.
     */
    @Override public void setFormat(Class<?> c,
                          Format format) {
        Format old = formatters.put(c, format);
        propertiesHandler.firePropertyChange("format", old, format);
        if (Date.class.isAssignableFrom(c) && (format != null)) {
            Comparator<?> comparator = getComparator(c);
            if ((comparator == null) || (comparator instanceof DateComparator)) {
                setComparator(c, DateComparator.getDateComparator(format));
            }
        }
    }

    @Override public void setComparator(Class<?> c,
                              Comparator<?> cmp) {
        Comparator<?> old = comparators.put(c, cmp);
        propertiesHandler.firePropertyChange("comparator", old, cmp);
    }

    @Override public Comparator<?> getComparator(Class<?> c) {
        return comparators.get(c);
    }

    @Override public void setIgnoreCase(boolean ignore) {
        if (ignore != this.ignoreCase) {
            boolean old = this.ignoreCase;
            this.ignoreCase = ignore;
            propertiesHandler.firePropertyChange("ignoreCase", old, ignore);
        }
    }

    @Override public boolean isIgnoreCase() {
        return ignoreCase;
    }

    @Override public RowFilter parseText(String expression,
                               int modelPosition) throws ParseException {
        Class<?> c = model.getColumnClass(modelPosition);
        Matcher matcher = expressionMatcher.matcher(expression);
        if (matcher.matches()) {
            // all expressions match!
            IOperand op = operands.get(matcher.group(1));
            if (op == null) {
                op = defaultOperand;
            }
            try {
                return op.create(matcher.group(2).trim(), c, modelPosition);
            } catch (ParseException pex) {
                throw new ParseException("", pex.getErrorOffset() + matcher.start(2));
            }
        }
        return null;
    }

    /** Basic {@link Comparator} using {@link Comparable} instances */
    static Comparator<Comparable> comparatorOfComparables = new Comparator<Comparable>() {
            @SuppressWarnings("unchecked")
            @Override public int compare(Comparable o1,
                               Comparable o2) {
                return o1.compareTo(o2);
            }
        };

    /**
     * Internal interface, to be implemented by all operands
     */
    interface IOperand {
        RowFilter create(String right,
                         Class<?> c,
                         int modelPosition) throws ParseException;
    }

    abstract class ComparisonOperand implements IOperand {
        protected Comparator defaultComparator;

        abstract boolean matches(int comparison);

        @Override public RowFilter create(String right,
                                Class<?> c,
                                int modelPosition) throws ParseException {
            if (String.class.equals(c)) {
                return createStringOperator(right, null, modelPosition);
            }
            Format format = formatters.get(c);
            if (format == null) {
                return createStringOperator(right, null, modelPosition);
            }
            Comparator<?> comparator = comparators.get(c);
            if (comparator == null) {
                if (Comparable.class.isAssignableFrom(c)) {
                    comparator = comparatorOfComparables;
                } else if (defaultComparator == null) {
                    return createStringOperator(right, format, modelPosition);
                } else {
                    comparator = defaultComparator;
                }
            }
            Object o = format.parseObject(right);
            if (o == null) {
                throw new ParseException("", 0);
            }
            return createOperator(o, comparator, modelPosition);
        }

        protected RowFilter createOperator(final Object right,
                                           final Comparator comparator,
                                           final int modelPosition) {
            return new RowFilter() {
                    @SuppressWarnings("unchecked")
                    @Override public boolean include(Entry entry) {
                        Object left = entry.getValue(modelPosition);
                        return (left != null) && matches(comparator.compare(left, right));
                    }
                };
        }

        protected RowFilter createStringOperator(final String right,
                                                 Format formatter,
                                                 int modelPosition) {
            return new StringRowFilter(modelPosition, formatter) {
                    @Override public boolean include(String left) {
                        return matches(ignoreCase ? left.compareToIgnoreCase(right) : left.compareTo(right));
                    }
                };
        }
    }

    class EqualOperand extends ComparisonOperand implements Comparator {
        boolean equals;

        public EqualOperand(boolean equals) {
            this.equals = equals;
            defaultComparator = this;
        }

        @Override public boolean matches(int comparison) {
            return equals == (comparison == 0);
        }

        @Override public int compare(Object o1,
                           Object o2) {
            // o1 is never null
            return o1.equals(o2) ? 0 : 1;
        }
    }


    class REOperand implements IOperand {
        boolean equals;

        public REOperand(boolean equals) {
            this.equals = equals;
        }

        @Override public RowFilter create(String right,
                                Class<?> c,
                                final int modelPosition) throws ParseException {
            final Pattern pattern = getPattern(right);
            Format format = String.class.equals(c) ? null : formatters.get(c);
            return new StringRowFilter(modelPosition, format) {

                    @Override boolean include(String left) {
                        return equals == pattern.matcher(left).matches();
                    }
                };
        }

        protected Pattern getPattern(String right) throws ParseException {
            try {
                return Pattern.compile(right, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
            } catch (PatternSyntaxException pse) {
                throw new ParseException("", pse.getIndex());
            }
        }
    }

    class SimpleREOperand extends REOperand {

        public SimpleREOperand(boolean equals) {
            super(equals);
        }

        @Override protected Pattern getPattern(String right) throws ParseException {
            return super.getPattern(convertWilcardExpressionToRegularExpression(right));
        }

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

    abstract class StringRowFilter extends RowFilter {
        int modelPosition;
        Format formatter;

        StringRowFilter(int modelPosition,
                        Format formatter) {
            this.modelPosition = modelPosition;
            this.formatter = formatter;

        }

        @Override public boolean include(Entry entry) {
            Object o = entry.getValue(modelPosition);
            String left = (o == null) ? "" : ((formatter == null) ? o.toString() : formatter.format(o));
            return include(left);
        }

        abstract boolean include(String left);
    }

}