package net.coderazzi.filters.gui;

import java.awt.Component;

/** Interface to customize the content of the editor */
public interface ChoiceRenderer {
	public Component getRendererComponent(IFilterEditor editor,
			Object value, boolean isSelected);
}