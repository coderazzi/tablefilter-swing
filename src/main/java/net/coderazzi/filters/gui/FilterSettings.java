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

package net.coderazzi.filters.gui;

import java.awt.Color;
import java.awt.Font;
import java.text.Collator;
import java.util.Comparator;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.coderazzi.filters.IFilterTextParser;
import net.coderazzi.filters.gui.TableFilterHeader.Position;
import net.coderazzi.filters.parser.FilterTextParser;
import net.coderazzi.filters.parser.Types;


/**
 * Class to define some common settings to the TableFilter library.<br>
 * It is just a sugar replacement to using directly system properties 
 * (which could be not available, anyway)
 */
public class FilterSettings {
	
	/** Properties must be defined with this prefix */
	public final static String PROPERTIES_PREFIX="net.coderazzi.filters.";
	
    /**
     * Set to true to perform automatically the selection of a row that is 
     * uniquely identified by the existing filter. It is true by default.
     */
    public static boolean autoSelection = 
    	Boolean.parseBoolean(getString("AutoSelection", "true"));

    /** Whether to automatically fill with content the editor field's options */
    public static boolean autoOptions = 
    	Boolean.parseBoolean(getString("AutoOptions", "false"));

    /** Whether to ignore case or not, false by default (case sensitive) */
    public static boolean ignoreCase = 
    	Boolean.parseBoolean(getString("IgnoreCase", "true"));

    /** The header position, {@link Position#INLINE} by default. */
    public static Position headerPosition = 
    	Position.valueOf(getString("Header.Position", "INLINE"));

    /** The default date format, used on the default text parser */
    public static String dateFormat = getString("DateFormat", null);

    /** The maximum number of visible tows on the popup menus */
    public static int maxVisiblePopupRows = getInteger("Popup.maxVisibleRows", 8);
    
    /** The maximum size of the history when no options are present */
    public static int maxPopupHistory = getInteger("Popup.maxHistory", 2);
    
    /** The types used by default on the parser */
    public static Types types = new Types();

    /** The default icon used to represent null/empty values */
    public static Icon matchEmptyFilterIcon = new ImageIcon( 
    	IFilterTextParser.class.getResource("resources/matchEmptyIcon.png"));

    /** 
     * The default string associated to a nop operation.<p>
     * It is chosen as = because that is the expression that the default
     * text parser can use to find null/empty values. If any other parse
     * is chosen, it could be meaningful to update this string. 
     */
    public static String matchEmptyFilterString = "=";

    /** Header's background color*/
    public  static Color backgroundColor = getColor("backgroundColor", null);
    
    /** Header's foreground color*/
    public  static Color foregroundColor = getColor("foregroundColor", null);
    
    /** Header's error color*/
    public  static Color errorColor = getColor("errorColor", Color.red);
    
    /** Header's grid color*/
    public  static Color gridColor = getColor("gridColor", null);
    
    /** Header's disabled color*/
    public  static Color disabledColor = getColor("disabledColor", null);
    
    /** Header's selection background color*/
    public  static Color selectionBackgroundColor  =    	
    	getColor("selectionBackgroundColor", null);
    
    /** Header's selection foreground color*/
    public  static Color selectionForegroundColor = 
    	getColor("selectionForegroundColor", null);
    
    /** Header's font*/
    public static Font font;

    /**
     * The class to handle the text parsing by default.<br>
     * It must have a default constructor. <br>
     * It corresponds to the property TextParser.class
     */
    public static Class<? extends IFilterTextParser> filterTextParserClass;

    /** Creates a TextParser as defined by default */
    public static IFilterTextParser newTextParser() {
        try {
            IFilterTextParser ret = filterTextParserClass.newInstance();
            ret.setIgnoreCase(ignoreCase);
            types.configure(ret);
            return ret;
        } catch (Exception ex) {
            throw new RuntimeException("Error creating filter text parser of type "
                                       + filterTextParserClass, ex);
        }
    }
    
    /** Internal TableFilter method to get the appropriated String comparator*/
    public static Comparator<String> getStringComparator(boolean ignoreCase){
    	Comparator<String> ret;
    	if (ignoreCase){
    		if (icCollator==null){
    			icCollator=createStringComparator(true);
    		}
    		ret=icCollator;
    	} else {
    		if (collator==null){
    			collator=createStringComparator(false);
    		}
    		ret=collator;    		
    	}
    	return ret;
    }
    
    private static Comparator<String> createStringComparator(boolean ignoreCase){
    	Collator ret=Collator.getInstance();
    	ret.setStrength(ignoreCase? Collator.PRIMARY : Collator.TERTIARY);    			
    	return (Comparator) ret;
    }
    
    private static Comparator<String> collator, icCollator; 


    static {
    	try{
    		font=Font.decode(getString("font"));
    	} catch(Exception ex){
    		//font remains null
    	}
        filterTextParserClass = FilterTextParser.class;
        String cl = getString("TextParser.class", null);
        if (cl != null) {
            try {
                filterTextParserClass = (Class<? extends IFilterTextParser>) 
                                        Class.forName(cl);
            } catch (ClassNotFoundException cne) {
                throw new RuntimeException(
                		"Error finding filter text parser of class " + cl, cne);
            } catch (ClassCastException cce) {
                throw new RuntimeException(
                		"Filter text parser of class " + cl
                        + " is not a valid IFilterTextParser class");
            }
        }
    }
    
    private static String getString(String name,
                                    String defaultValue) {
    	String ret = getString(name);
    	return ret==null? defaultValue : ret;
    }
    
    private static String getString(String name) {
    	try {
    		return System.getProperty(PROPERTIES_PREFIX + name);
		} catch (Exception ex) {
			return null;
		}
    }

    private static int getInteger(String name, int defaultValue) {
    	String ret = getString(name);
    	if (ret!=null){
			try {
				return Integer.valueOf(ret);
			} catch (Exception ex) {
				//return defaultValue
			}
    	}
    	return defaultValue;
    }
      
    private static Color getColor(String name, Color defaultValue) {
    	String prop = getString(name);
    	if (prop!=null){
    		try{
    			return Color.decode(prop);
    		} catch(Exception ex){
    			//return defaultValue
    		}
    	}
    	return defaultValue;
    }
  
}