package net.coderazzi.filters.gui;

import java.awt.Color;
import java.awt.Font;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.IFilterTextParser;
import net.coderazzi.filters.gui.editor.CustomChoice;

public interface IFilterEditor {

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

	/** Returns the {@link IFilter} associated to the editor's content */
	public abstract IFilter getFilter();

	/** Returns the current editor's content */
	public abstract Object getContent();

	/** Sets the content, adapted to the editors' type */
	public abstract void setContent(Object content);

	/** Enabled/Disables the editor, and the associate filter */
	public abstract void setEnabled(boolean enabled);

	public abstract void setBackground(Color bg);

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

	public abstract void setForeground(Color fg);

	public abstract void setFont(Font font);

	/**
	 * Resets the filter, which implies:<ul>
	 * <li>Content set to empty</li>
	 * <li>History is lost<li>
	 * <li>Options are reset</li>
	 * <li>It becomes editable -unless there is a cell renderer-</li></ul></li>
	 * </ul>
	 */
	public abstract void resetFilter();

	/** 
	 * Sets the {@link IFilterTextParser}; if the editor does not have a
	 * {@link ListCellRenderer}, a parser is mandatory 
	 */
	public abstract void setTextParser(IFilterTextParser parser);

	/**Returns the associated {@link IFilterTextParser} */
	public abstract IFilterTextParser getTextParser();

	/** Returns the filter position associated to this editor*/
	public abstract int getModelPosition();

	/** Sets the available options, shown on the popup menu */
	public abstract void setCustomChoices(Set<CustomChoice> options);

	/** Returns the current options */
	public abstract Set<CustomChoice> getCustomChoices();

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

	/**
	 * Using autoOptions, the options displayed on the popup menu are 
	 * automatically extracted from the associated {@link TableModel}.
	 */
	public abstract void setAutoOptions(AutoOptions mode);

	/** Returns the autoOptions mode*/
	public abstract AutoOptions getAutoOptions();

}