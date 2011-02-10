package net.coderazzi.filters.gui;

import java.awt.Component;

/** 
 * Interface to customize the rendering of choices in the {@link IFilterEditor} 
 **/
public interface ChoiceRenderer {
	/**
	 * Returns the component used to represent the choice (normally, an element
	 * from the associated table).<br>
	 * The value can be as well {@link CustomChoice} instances; to use the
	 * default rendering in this case, the method should return null.
	 */
	public Component getRendererComponent(IFilterEditor editor,
			Object value, boolean isSelected);
}