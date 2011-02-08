package net.coderazzi.filters.gui.editor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;

/**
 * Custom implementation of the arrow used to display the popup menu.<br>
 */
final class FilterArrowButton extends JButton {
	private static final long serialVersionUID = -777416843479142582L;
	private final static int MIN_X = 6;
	private final static int MIN_Y = 6;
	private final static int FILL_X[] = { 0, 3, 6 };
	private final static int FILL_Y[] = { 0, 5, 0 };
	
	private boolean focus;
	private Look look;
	
	public void setLook(Look look){
		this.look=look;
		repaint();
	}
	
	public Look getLook(){
		return look;
	}
	
	public void setFocused(boolean focus){
		this.focus=focus;
		repaint();
	}
	
	
	@Override public void paint(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		int height = getHeight();
		int width = getWidth();

		g.setColor(isEnabled() && focus? look.selectionBackground: look.background);
		g.fillRect(0, 0, width, height);

		width = (width - MIN_X) / 2;
		height = Math.min(height / 2, height - MIN_Y);
		g.translate(width, height);
		if (isEnabled()){
			g.setColor(focus? look.selectionForeground : look.foreground);
		} else {
			g.setColor(look.disabledForeground);
		}
		g.fillPolygon(FILL_X, FILL_Y, FILL_X.length);
	}

	@Override protected void paintBorder(Graphics g) {
		super.paintBorder(g);
	}

	@Override public boolean isFocusable() {
		return false;
	}

	@Override public Dimension getPreferredSize() {
		return new Dimension(12, 12);
	}
}