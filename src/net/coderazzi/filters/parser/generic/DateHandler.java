package net.coderazzi.filters.parser.generic;

import java.awt.Component;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import net.coderazzi.filters.parser.FilterTextParsingException;
import net.coderazzi.filters.parser.ITypeBuilder;
import net.coderazzi.filters.resources.Messages;

/**
 * <p>Class to handle Dates</p>
 * <p>It is also a {@link Comparator} of {@link Date} instances, to ensure that differences in
 * milliseconds, not shown on the table, do not affect to the filter.</p>
 * <p>It also provides a {@link TableCellRenderer}, using the provided date format</p>
 *
 * @author  Luis M Pena - sen@coderazzi.net
 * @since version 2.0
 */
public class DateHandler implements ITypeBuilder, Comparator<Date>{
	
	private static DateHandler defaultInstance;
	private DateFormat parser;
	
	/**
	 * <p>Provides a default instance.</p>
	 * <p>The user can specify the following system property to alter the behavior of this method:</p>
	 * <ul><li>net.coderazzi.filters.DateFormat Identifies the expected date format
	 *   as defined in {@link SimpleDateFormat}.</li></ul>
	 * </ul>
	 */
	public static DateHandler getDefault(){
		if (defaultInstance==null){
			String definition = Messages.getString("net.coderazzi.filters.DateFormat", null);
			if (definition==null){
				defaultInstance = new DateHandler();
			} else {
				defaultInstance = new DateHandler(definition);
			}
		}
		return defaultInstance;
	}

	/**
	 * Build a simple instance, able to parse dates -without included time- in short format, using
	 * the current locale
	 */
	public DateHandler(){
		this(DateFormat.getDateInstance(DateFormat.SHORT));
	}
	
	/**
	 * Build an instance supporting provided valid formats
	 */
	public DateHandler(String validFormat){
		parser = new SimpleDateFormat(validFormat);
	}
	
	/**
	 * Build an instance supporting provided valid formats
	 */
	public DateHandler(DateFormat validFormat){
		parser=validFormat;
	}
	
	/**
	 * Returns the defined {@link DateFormat}
	 */
	public DateFormat getDateFormat(){
		return parser;
	}
	
	@Override
	public Object parse(String text) throws FilterTextParsingException {
		try{
			return parser.parse(text);
		} 
		catch(ParseException pex){
			throw new FilterTextParsingException("Invalid date", pex.getErrorOffset());
		}
	}
	
	@Override
	public int compare(Date d1, Date d2) {
		try{
			d1=parser.parse(parser.format(d1));
		} catch (Exception ex){
			d1=null;
		}
		try{
			d2=parser.parse(parser.format(d2));
		} catch (Exception ex){
			d2=null;
		}
		if (d1==null) {
			return d2==null? 0 : 1;
		}
		return d1.compareTo(d2);
	}
	
	/**
	 * Provides a table cell renderer suitable for the provided date format
	 */
	public TableCellRenderer getTableCellRenderer(){
		return new DefaultTableCellRenderer(){

			private static final long serialVersionUID = 8042527267257156699L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				if (value instanceof Date){
					value = parser.format(value);
				}
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}			
		};		
	}

}
