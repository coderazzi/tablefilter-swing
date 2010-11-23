package net.coderazzi.filters.parser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.Collator;
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
import net.coderazzi.filters.gui.FilterSettings;


/**
 * Basic implementation of a {@link IFilterTextParser}, supporting only 
 * simple operators referring to the content of a single column.<br>
 * The supporter operators include:
 *
 * <ul>
 *   <li>Comparison operators. The comparison is done on the parsed object, 
 *     not on the string representation, unless no {@link Format} or 
 *     {@link Comparator} is defined for the given type.
 *     For example, specifying the text &quot;&gt;= 4&quot; implies, for a 
 *     column with integer types, that a direct comparison between integers 
 *     will be performed. These operators are:
 *
 *     <ul>
 *       <li>&gt;=</li>
 *       <li>&gt;</li>
 *       <li>&lt;</li>
 *       <li>&lt;=</li>
 *       <li>&lt;&gt;</li>
 *     </ul>
 *   </li>
 *   <li>Equal operators. The comparison is done on the parsed object, 
 *     not on the string representation, unless no {@link Format} is 
 *     defined for the given type. The comparison is performed using the 
 *     equals method. These operators are:
 *
 *     <ul>
 *       <li>!=: note that, in most cases, it will behave 
 *           as the operator &lt;&gt;</li>
 *       <li>! : equivalent to !=</li>
 *       <li>&lt;</li>
 *       <li>=</li>
 *       <li>==: equivalent to =</li>
 *     </ul>
 *   </li>
 *   <li>Basic wildcard operators. These operators work using the string 
 *     representation of the types (using, when possible, the defined 
 *     {@link Format} instance). 
 *     Only two wildcard characters are defined: * and ?
 *
 *     <ul>
 *       <li>~: for example ~ *vadis* will filter in all expressions 
 *         including the substring vadis</li>
 *       <li>!~: negates the previous operator</li>
 *     </ul>
 *   </li>
 *   <li>Regular expression operator. There is only one such operator: ~~, 
 *     accepting a java regular expression.</li>
 * </ul>
 */
public class FilterTextParser implements IFilterTextParser {

    Map<Class<?>, Format> formatters = new HashMap<Class<?>, Format>();
    Map<Class<?>, Comparator<?>> comparators = 
    	new HashMap<Class<?>, Comparator<?>>();
	Format defaultFormatter; 
    boolean ignoreCase;
    Collator stringComparator;
    private TableModel model;
    private Map<String, IOperand> operands;
    private PropertyChangeSupport propertiesHandler = new PropertyChangeSupport(this);
    private IOperand nullOperand;
    private static Pattern expressionMatcher =
    	Pattern.compile("^(>=|<=|<>|!=|!~|~~|==|>|<|=|~|!)?\\s*(.*)$");
    

    public FilterTextParser() {
    	this(false);
    }
    
    public FilterTextParser(boolean ignoreCase) {
    	stringComparator = Collator.getInstance();
    	internalSetIgnoreCase(ignoreCase);
        operands = new HashMap<String, IOperand>();
        operands.put("~~", new REOperand(true));
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
        final SimpleREOperand matchOperand = new SimpleREOperand(true);
        operands.put("~", matchOperand);

        //nullOperand is used when the user enters no operator. It treats
        //the input as a match operand (~) if handled as a String or when
        //it is not possible to parse the given expression (for example,
        //entering '5*' on a Integer column
        nullOperand = new EqualOperand(true){
        	@Override
			protected RowFilter createStringOperator(String right, 
        			Format formatter, int modelPosition) throws ParseException
        	{
        		return matchOperand.create(right, modelPosition, formatter);
        	}
        	@Override
			protected RowFilter createOperator(String right, Format format, 
        			Comparator comparator, int modelPosition) 
        	throws ParseException 
        	{
        		try{
        			return super.createOperator(right, format, comparator, 
        					modelPosition);
        		} catch (ParseException pex){
            		return matchOperand.create(right, modelPosition, format);        			
        		}
        	}
        };
        setFormat(String.class, null);
    }
    
    @Override
    public FilterTextParser clone(){
    	FilterTextParser ret = new FilterTextParser(ignoreCase);
    	ret.model=model;
    	ret.formatters.putAll(formatters);
    	ret.comparators.putAll(comparators);
    	ret.defaultFormatter=defaultFormatter;
    	return ret;
    }

    @Override 
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertiesHandler.addPropertyChangeListener(listener);
    }

    @Override 
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertiesHandler.removePropertyChangeListener(listener);
    }

    @Override public void setTableModel(TableModel model) {
        TableModel oldModel = model;
        this.model = model;
        propertiesHandler.firePropertyChange(TABLE_MODEL_PROPERTY, 
        		oldModel, model);
    }
    
    @Override public TableModel getTableModel() {
    	return model;
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
        if (c==String.class){
        	if (format==null){
        		format = FilterSettings.types.getFormat(String.class);
        	}
        	defaultFormatter=format;
        }
        Format old = formatters.put(c, format);
        propertiesHandler.firePropertyChange(FORMAT_PROPERTY, 
        		old==null? null : c, c);
        if (Date.class.isAssignableFrom(c) && (format != null)) {
            Comparator<?> comparator = getComparator(c);
            if ((comparator == null) || (comparator instanceof DateComparator)){
                setComparator(c, DateComparator.getDateComparator(format));
            }
        }
    }

    @Override public void setComparator(Class<?> c,
                              Comparator<?> cmp) {
        Comparator<?> old = comparators.put(c, cmp);
        propertiesHandler.firePropertyChange(COMPARATOR_PROPERTY, 
        		old==null? null : c, c);
    }

    @Override public Comparator<?> getComparator(Class<?> c) {
        return comparators.get(c);
    }

    @Override public void setIgnoreCase(boolean ignore) {
    	boolean old =this.ignoreCase;
        if (ignore != this.ignoreCase) {
        	internalSetIgnoreCase(ignore);
            propertiesHandler.firePropertyChange(IGNORE_CASE_PROPERTY, 
            		old, ignore);
        }
    }

    private void internalSetIgnoreCase(boolean ignore) {
        this.ignoreCase = ignore;
        stringComparator.setStrength(ignore? Collator.PRIMARY : Collator.TERTIARY);
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
            if (op==null){
            	op = nullOperand;
            }
            String content = matcher.group(2).trim();
            return op.create(content, c, modelPosition);
        }
        return null;
    }
	

    @Override public String escape(String expression, int modelPosition) {
    	boolean needs=true;
    	expression = expression.trim();
        Matcher matcher = expressionMatcher.matcher(expression);
        if (matcher.matches()) {
            // all expressions match!
        	String operand = matcher.group(1);
        	if (operand==null){
        		//if there is no operand, and class is String, or there
        		//is no associated formatter, or the formatter does not
        		//validate the given text, the string will be handled
        		//with the match operator (~).
                Class<?> c = model.getColumnClass(modelPosition);
                if (! String.class.equals(c)) {
	                Format format = formatters.get(c);
	                try{
	                	if (format!=null && format.parseObject(expression)!=null){
	                		needs=false;
	                	}
	                } catch (ParseException pex){
	                	//invalid expression, needs remains true
	                }
                }
                needs=needs && !expression.equals(
                		convertWilcardExpressionToRegularExpression(expression));
        	} else {
        		needs=true;
        	}
        }
        return needs? "= "+expression : expression;
    }

    String convertWilcardExpressionToRegularExpression(String s) {
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

    /** Basic {@link Comparator} using {@link Comparable} instances */
    static Comparator<Comparable> comparatorOfComparables = 
    	new Comparator<Comparable>() {
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
            return createOperator(right, format, comparator, modelPosition);
        }

        protected RowFilter createOperator(String right,
                                           Format format,
                                           final Comparator comparator, 
                                           final int modelPosition) 
        	throws ParseException
        {
            final Object o = right.length()==0? null : format.parseObject(right);
            if (o ==null){
            	return createNullOperator(modelPosition);
            }
            return new RowFilter() {
                    @SuppressWarnings("unchecked")
                    @Override public boolean include(Entry entry) {
                        Object left = entry.getValue(modelPosition);
                        return (left != null) && 
                        	matches(comparator.compare(left, o));
                    }
                };
        }
        
        protected RowFilter createNullOperator(int modelPosition) 
        	throws ParseException
        {
            throw new ParseException("", 0);        	
        }

        @SuppressWarnings("unused")
		protected RowFilter createStringOperator(final String right,
                                                 Format formatter,
                                                 int modelPosition)
        	throws ParseException
        {
            return new StringRowFilter(modelPosition, formatter) {
                    @Override public boolean include(String left) {
                        return matches(stringComparator.compare(left, right));
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
        
        @Override
        protected RowFilter createNullOperator(final int modelPosition) 
        	throws ParseException 
        {
            return new RowFilter() {
                @SuppressWarnings("unchecked")
                @Override public boolean include(Entry entry) {
                    Object left = entry.getValue(modelPosition);
                    return equals == (left == null);
                }
            };        		
        }

        @Override public int compare(Object o1,
                           Object o2) {
            if (o1==null) return o2==null? 0 : 1;
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
                                int modelPosition) throws ParseException {
        	return create(right, modelPosition, 
        					String.class.equals(c) ? null : formatters.get(c));
        }

        public RowFilter create(String right,
        		final int modelPosition, 
        		Format format) throws ParseException {
        	final Pattern pattern = getPattern(right);
        	return new StringRowFilter(modelPosition, format) {

        		@Override boolean include(String left) {
        			return equals == pattern.matcher(left).matches();
        		}
        	};
        }

        protected Pattern getPattern(String right) throws ParseException {
            try {
                return Pattern.compile(right, 
                		ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
            } catch (PatternSyntaxException pse) {
                throw new ParseException("", pse.getIndex());
            }
        }
    }

    class SimpleREOperand extends REOperand {

        public SimpleREOperand(boolean equals) {
            super(equals);
        }

        @Override 
        protected Pattern getPattern(String right) throws ParseException {
            return super.getPattern(
            		convertWilcardExpressionToRegularExpression(right));
        }
    }

    abstract class StringRowFilter extends RowFilter {
        int modelPosition;
        Format formatter;

        StringRowFilter(int modelPosition,
                        Format formatter) {
            this.modelPosition = modelPosition;
            this.formatter = formatter==null? defaultFormatter : formatter;

        }

        @Override public boolean include(Entry entry) {
            Object o = entry.getValue(modelPosition);
            String left = formatter.format(o);
            return include(left);
        }

        abstract boolean include(String left);
    }
   
}