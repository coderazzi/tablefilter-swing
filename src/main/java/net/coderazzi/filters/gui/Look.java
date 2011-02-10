package net.coderazzi.filters.gui;

import java.awt.Color;
import java.awt.Font;


public class Look {
	Color foreground;
	Color background;
	Color selectionForeground;
	Color selectionBackground;
	Color disabledForeground;
	Color errorForeground;
	Color textSelection;
	Color gridColor;
	
	Font font;
	
	int maxVisiblePopupRows=FilterSettings.maxVisiblePopupRows;

	public Color getForeground() {
		return foreground;
	}

	public Color getBackground() {
		return background;
	}

	public Color getSelectionForeground() {
		return selectionForeground;
	}

	public Color getSelectionBackground() {
		return selectionBackground;
	}

	public Color getDisabledForeground() {
		return disabledForeground;
	}

	public Color getErrorForeground() {
		return errorForeground;
	}

	public Color getTextSelection() {
		return textSelection;
	}

	public Color getGridColor() {
		return gridColor;
	}

	public Font getFont() {
		return font;
	}

	public int getMaxVisiblePopupRows() {
		return maxVisiblePopupRows;
	}
}
