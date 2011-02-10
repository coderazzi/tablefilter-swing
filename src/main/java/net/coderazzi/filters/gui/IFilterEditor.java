package net.coderazzi.filters.gui;

import java.awt.Component;
import java.text.Format;
import java.util.Comparator;
import java.util.Set;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.coderazzi.filters.IFilter;

/**
 * Public interface of the editors associated to each table's column.
 */
public interface IFilterEditor {
	
	/** Returns the model position associated to this editor*/
	public int getModelIndex();

	/** Returns the class associated to the editor on the model*/
	public Class<?> getModelClass();

	/**
	 * Returns the {@link IFilter} associated to the editor's content<br>
	 * The returned instance can then be used to enable or disable the
	 * filter and its GUI component. 
	 **/
	public IFilter getFilter();

	/**
	 * Resets the filter, which implies set its content to empty and reset
	 * its history choices
	 */
	public void resetFilter();

	/** Sets the content, adapted to the editors' type */
	public void setContent(Object content);

	/** Returns the current editor's content */
	public Object getContent();

	/**
	 * Using autoChoices, the choices displayed on the popup menu are 
	 * automatically extracted from the associated {@link TableModel}.<br>
	 * For editors associated to boolean or short enumerations, setting the
	 * AutoChoices automatically changes the editable flag: it is set to true
	 * for DISABLED values, false otherwise.
	 */
	public void setAutoChoices(AutoChoices mode);

	/** Returns the autoChoices mode*/
	public AutoChoices getAutoChoices();

	/** Sets the available choices, shown on the popup menu */
	public void setCustomChoices(Set<CustomChoice> choices);

	/** Returns the current choices */
	public Set<CustomChoice> getCustomChoices();

	/**
	 * Defines the editor, if text based -i.e., without associated 
	 * {@link Renderer}, as editable: this flag means that the user 
	 * can enter any text, not being limited to the existing choices
	 */
	public void setEditable(boolean enable);

	/** 
	 * Returns the editable flag
	 * @see #setEditable(boolean)
	 */
	public boolean isEditable();

	/**  Sets the ignore case flag */
	public void setIgnoreCase(boolean set);

	/**  Returns the ignore case flag */
	public boolean isIgnoreCase();

	/** 
	 * Sets the {@link Format} required by the editor to handle the user's
	 * input when the associated class is not a String<br>
	 * It is initially retrieved from the {@link IParserModel}.
	 */
	public void setFormat(Format format);

	/**  Returns the associated {@link Format}. */
	public Format getFormat();

	/** 
	 * Sets the {@link Comparator} required to compare (and sort) instances
	 * of the associated class in the table model.<br>
	 * It is initially retrieved from the {@link IParserModel}, and set also
	 * in the underlying {@link TableRowSorter} associated to the table.<br>
	 * Setting a comparator to null resets the used comparator (i.e: the
	 * comparator is never null)
	 */
	public void setComparator(Comparator comparator);

	/**  Returns the associated {@link Comparator}, which can nver be null. */
	public Comparator getComparator();

	/**
	 * Limits the history size. <br>
	 * This limit is only used when the popup contains also choices. Otherwise, 
	 * the maximum history size is to the maximum number of visible rows<br>
	 * The max history cannot be greater than the max visible rows
	 */
	public void setMaxHistory(int size);

	/** 
	 * Returns the maximum history size, as defined by the user.<br>
	 * This is not the real maximum history size, as it depends on the max 
	 * number of visible rows and whether the popup contains only history
	 * or also choices 
	 */
	public int getMaxHistory();

	/**
	 * Sets the {@link Renderer} for the choices / history.<p>
	 * It also affectes to how the content is rendered<br>
	 * If not null, the content cannot be text-edited anymore
	 * @param renderer
	 */
	public void setRenderer(Renderer renderer);

    /** Returns the associated {@link Renderer} */
	public Renderer getRenderer();

	/** Returns the current editor's look */
	public Look getLook();

	/** Interface to customize the content of the editor */
	public interface Renderer {
		public Component getRendererComponent(IFilterEditor editor,
				Object value, boolean isSelected);
	}

}