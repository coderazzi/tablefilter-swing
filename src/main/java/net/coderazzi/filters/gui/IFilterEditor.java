package net.coderazzi.filters.gui;

import java.awt.Color;
import java.awt.Font;
import java.text.Format;
import java.util.Comparator;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.coderazzi.filters.IFilter;

/**
 * Public interface of the editors associated to each table's column.
 */
public interface IFilterEditor {

	/**
	 * Returns the {@link IFilter} associated to the editor's content<br>
	 * The returned instance can then be used to enable or disable the
	 * filter and its GUI component. 
	 **/
	public abstract IFilter getFilter();

	/**
	 * Resets the filter, which implies set its content to empty and reset
	 * its history options
	 */
	public abstract void resetFilter();

	/** Returns the model position associated to this editor*/
	public abstract int getModelIndex();

	/** Returns the class associated to the editor on the model*/
	public abstract Class<?> getModelClass();

	/**
	 * Using autoOptions, the options displayed on the popup menu are 
	 * automatically extracted from the associated {@link TableModel}.<br>
	 * For editors associated to boolean or short enumerations, setting the
	 * AutoOptions automatically changes the editable flag: it is set to true
	 * for DISABLED values, false otherwise.
	 */
	public abstract void setAutoOptions(AutoOptions mode);

	/** Returns the autoOptions mode*/
	public abstract AutoOptions getAutoOptions();

	/**
	 * Defines the editor, if text based -i.e., without associated 
	 * {@link ListCellRenderer}, as editable: this flag means that the user 
	 * can enter any text, not being limited to the existing options
	 */
	public abstract void setEditable(boolean enable);

	/** 
	 * Returns the editable flag
	 * @see #setEditable(boolean)
	 */
	public abstract boolean isEditable();

	/** Sets the available options, shown on the popup menu */
	public abstract void setCustomChoices(Set<CustomChoice> options);

	/** Returns the current options */
	public abstract Set<CustomChoice> getCustomChoices();

	/** 
	 * Sets the {@link Comparator} required to compare (and sort) instances
	 * of the associated class in the table model.<br>
	 * It is initially retrieved from the {@link IParserModel}, and set also
	 * in the underlying {@link TableRowSorter} associated to the table.<br>
	 * Setting a comparator to null resets the used comparator (i.e: the
	 * comparator is never null)
	 */
	public abstract void setComparator(Comparator comparator);

	/**  Returns the associated {@link Comparator}, which can nver be null. */
	public abstract Comparator getComparator();

	/** 
	 * Sets the {@link Format} required by the editor to handle the user's
	 * input when the associated class is not a String<br>
	 * It is initially retrieved from the {@link IParserModel}.
	 */
	public abstract void setFormat(Format format);

	/**  Returns the associated {@link Format}. */
	public abstract Format getFormat();

	/**  Sets the ignore case flag */
	public abstract void setIgnoreCase(boolean set);

	/**  Returns the ignore case flag */
	public abstract boolean isIgnoreCase();

	/** Returns the current editor's content */
	public abstract Object getContent();

	/** Sets the content, adapted to the editors' type */
	public abstract void setContent(Object content);

	/**
	 * Sets the {@link ListCellRenderer} for the options / history.<p>
	 * It also affectes to how the content is rendered<br>
	 * If not null, the content cannot be text-edited anymore
	 * @param renderer
	 */
	public abstract void setListCellRenderer(ListCellRenderer renderer);

	/**
	 * <p>Sets as renderer for the editor a generic {@link TableCellRenderer}, 
	 * as used by the {@link JTable}</p>
	 *
	 * <p>This method allows reusing a renderer already written for a table 
	 * as the editor's renderer, but it has an important restriction: it only 
	 * works if the renderer does not depend on the cell coordinates</p>
	 */
	public abstract void setListCellRenderer(final TableCellRenderer renderer);

	/** Returns the associated {@link ListCellRenderer} */
	public abstract ListCellRenderer getListCellRenderer();

	/** 
	 * Sets the maximum number of visible rows in the popup menu<br>
	 * A minimum is always enforced
	 **/
	public abstract void setMaxVisibleRows(int maxVisibleRows);

	/** Returns the maximum number of visible rows in the popup menu*/
	public abstract int getMaxVisibleRows();

	/**
	 * Limits the history size. <br>
	 * This limit is only used when the popup contains also options. Otherwise, 
	 * the maximum history size is to the maximum number of visible rows<br>
	 */
	public abstract void setMaxHistory(int size);

	/** 
	 * Returns the maximum history size, as defined by the user.<br>
	 * This is not the real maximum history size, as it depends on the max 
	 * number of visible rows and whether the popup contains only history
	 * or also options 
	 */
	public abstract int getMaxHistory();

	/** Sets the background color */
	public abstract void setBackground(Color bg);

	/** Gets the background color */
	public abstract Color getBackground();

	/** Sets the foreground color */
	public abstract void setForeground(Color fg);

	/** Gets the foreground color */
	public abstract Color getForeground();

	/** Sets the color used to show filter's errors (invalid syntax) */
	public abstract void setErrorForeground(Color fg);

	/** Returns the color used to show filter's errors */
	public abstract Color getErrorForeground();

	/** Sets the color used to represent disabled state */
	public abstract void setDisabledForeground(Color fg);

	/** Returns the color used to represent disabled state */
	public abstract Color getDisabledForeground();

	/** Sets the foreground color used to represent selected state */
	public abstract void setSelectionForeground(Color fg);

	/** Returns the foreground color used to represent selected state */
	public abstract Color getSelectionForeground();

	/** Sets the background color used to represent selected state */
	public abstract void setSelectionBackground(Color bg);

	/** Returns the color used to represent disabled state */
	public abstract Color getSelectionBackground();
	
	/** Sets the grid color on the header */
	public abstract void setGridColor(Color c);

	/** Returns the grid color on the header */
	public abstract Color getGridColor();

	/** Sets the editor's font */
	public abstract void setFont(Font font);

	/** Returns the editor's font */
	public abstract Font getFont();

}