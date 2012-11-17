package net.coderazzi.filters.gui;

import java.awt.Component;

import javax.swing.JLabel;

public class HtmlChoiceRenderer extends JLabel implements ChoiceRenderer {

	private static final long serialVersionUID = -825539410560961416L;
	
	@Override
	public Component getRendererComponent(IFilterEditor editor, Object value,
			boolean isSelected) {
		Look look = editor.getLook();
        if (isSelected) {
            setBackground(look.getSelectionBackground());
            setForeground(look.getSelectionForeground());
        } else {
            setBackground(look.getBackground());
            setForeground(look.getForeground());
        }
		setText(value==null? "" : value.toString());
		return this;
	}
}
