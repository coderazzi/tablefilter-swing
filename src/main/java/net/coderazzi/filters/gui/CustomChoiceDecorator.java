package net.coderazzi.filters.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

/** 
 * Interface that allows customizing the appearance of CustomChoices
 * in those {@link IFilterEditor}s without associated {@link ChoiceRenderer}. 
 */
public interface CustomChoiceDecorator {
	
	/** Returns the background color, or null to use the default one */
	public Color getBackground(CustomChoice choice, IFilterEditor editor, boolean isSelected);

	/** Returns the foreground color, or null to use the default one */
	public Color getForeground(CustomChoice choice, IFilterEditor editor, boolean isSelected);

	/** Returns the font, or null to use the default one */
	public Font getFont(CustomChoice choice, IFilterEditor editor, boolean isSelected);

	/** Decorates the choice on the given editor */
	public void decorateComponent(CustomChoice choice, IFilterEditor editor, boolean isSelected, Component c, Graphics g);
	
	/**
	 * Default decorator, delegating always to the associated methods
	 * on the {@link CustomChoice} instances.
	 * The font, by default, will be cursive
	 */
	public class DefaultDecorator implements CustomChoiceDecorator{
		
		private Font baseFont;
		private Font italicFont;
		
		@Override public void decorateComponent(CustomChoice choice,
				IFilterEditor editor, boolean isSelected, Component c,
				Graphics g) {
			choice.decorateComponent(editor, isSelected, c, g);
		}
		
		@Override public Font getFont(CustomChoice choice, IFilterEditor editor,
				boolean isSelected) {
			Font ret = choice.getFont(editor, isSelected);
			if (ret==null){
				ret=editor.getLook().getFont();
				if (ret!=baseFont){
					baseFont = ret;
					italicFont=baseFont.deriveFont(Font.ITALIC);
				}
				ret=italicFont;
			}
			return ret;
		}
		
		@Override public Color getBackground(CustomChoice choice, IFilterEditor editor,
				boolean isSelected) {
			return null;
		}
		
		@Override public Color getForeground(CustomChoice choice, IFilterEditor editor,
				boolean isSelected) {
			return Color.lightGray;
		}
	}

}