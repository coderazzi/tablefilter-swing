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

package net.coderazzi.filters.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.RowFilter;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import net.coderazzi.filters.BaseFilter;
import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.IFilterTextParser;

/**
 * Custom component to handle the filter' editors<br>
 * It includes:<ul>
 * <li>A editor component, usually a text field to enter the filter text.</li>
 * <li>A popup menu containing both history and predefined option elements.</li>
 * <li>An arrow button to display the popup menu.</li></ul>
 * The component keeps the same look and feel under all cases 
 * (being editable or not, having custom cell renderers or not). 
 * Mixing therefore different editors under the same filter header
 * should keep the look and feel consistency.
 */
public class FilterEditor extends JComponent{

	private static final long serialVersionUID = 6908400421021655278L;
	private AutoOptionsHandler autoOptionsHandler;
	private PropertyChangeListener textParserListener;
	private EditorBorder border = new EditorBorder();
	FilterArrowButton downButton = new FilterArrowButton();
    Filter filter = new Filter();
	EditorComponent editor;
	PopupComponent popup;

	public FilterEditor() {
		setLayout(new BorderLayout());
		setBorder(border);
		
		//update the filter automatically when the parser is updated
		textParserListener = new PropertyChangeListener() {
			
			@Override public void propertyChange(PropertyChangeEvent evt) {
				popup.setIgnoreCase(((IFilterTextParser)evt.getSource())
						.isIgnoreCase());
				filter.update();
			}
		};
		popup = new PopupComponent() {

			@Override
			protected void optionSelected(Object selection) {
				popupSelection(selection);
			}
		};
		downButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				triggerPopup(downButton);
			}
		});	
		downButton.setCanPopup(false);
		
		add(downButton, BorderLayout.EAST);
		setupEditorComponent(null);
		setFont(editor.getComponent().getFont());
		setBackground(editor.getComponent().getBackground());
		setForeground(editor.getForeground());
		setDisabledForeground(Color.lightGray);
	}
	
	/** 
	 * Sets the maximum number of visible rows in the popup menu<br>
	 * A minimum is always enforced
	 **/
	public void setMaxVisibleRows(int maxVisibleRows) {
		popup.setMaxVisibleRows(maxVisibleRows);
	}

	/** Returns the maximum number of visible rows in the popup menu*/
	public int getMaxVisibleRows() {
		return popup.getMaxVisibleRows();
	}

	/**
	 * Limits the history size. <br>
	 * This limit is only used when the popup contains also options. Otherwise, 
	 * the maximum history size is to the maximum number of visible rows<br>
	 */
	public void setMaxHistory(int size) {
		popup.setMaxHistory(size);
	}

	/** 
	 * Returns the maximum history size, as defined by the user.<br>
	 * This is not the real maximum history size, as it depends on the max 
	 * number of visible rows and whether the popup contains only history
	 * or also options 
	 */
	public int getMaxHistory() {
		return popup.getMaxHistory();
	}
	
	/** Returns the {@link IFilter} associated to the editor's content */
	public IFilter getFilter() {
		return filter;
	}	
	
	/** Returns the current editor's content */
	public Object getContent() {
		return editor.getContent();
	}
	
	/** Sets the content, adapted to the editors' type */
	public void setContent(Object content){
		if (content==null){
			setEditorContent(EditorComponent.EMPTY_FILTER);
		} else if (isEditable()){
			//we need to use, eventually, the provided formatter
			setEditorContent(popup.format(content));
		} else if (popup.isValidOption(content)){
			//the content must be a valid option
			setEditorContent(popup.format(content));			
		}
	}
	
	/** Sets the content, updating the filter -and propagating any changes- */
	private void setEditorContent(Object content) {
		editor.setContent(content);
		filter.checkChanges();
	}
	
	/** Enabled/Disables the editor, and the associate filter */
	@Override public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (filter!=null){
			//popup.setEnabled(enabled);
			downButton.setEnabled(enabled);
			editor.setEnabled(enabled);
			filter.update();
		}
	}
	
	@Override public void setBackground(Color bg) {
		super.setBackground(bg);
		if (editor!=null){
	    	editor.getComponent().setBackground(bg);
	    	downButton.setBackground(bg);
	        popup.setBackground(bg);
	        repaint();
		}
	}
	
    /** Sets the color used to show filter's errors (invalid syntax) */
    public void setErrorForeground(Color fg) {
    	editor.setErrorForeground(fg);
    }

    /** Returns the color used to show filter's errors */
    public Color getErrorForeground() {
    	return editor.getErrorForeground();
    }

    /** Sets the color used to represent disabled state */
    public void setDisabledForeground(Color fg){
    	editor.setDisabledForeground(fg);
    	downButton.setDisabledColor(fg);
    	popup.setDisabledColor(fg);
    	border.setColor(fg);
    }

    /** Returns the color used to represent disabled state */
    public Color getDisabledForeground(){
    	return editor.getDisabledForeground();
    }
    
	@Override public void setForeground(Color fg) {
		super.setForeground(fg);
		if (editor!=null){
	    	editor.setForeground(fg);
	    	downButton.setForeground(fg);
	        popup.setForeground(fg);
		}
	}
	
	@Override public void setFont(Font font) {
		super.setFont(font);
		if (editor!=null){
			editor.getComponent().setFont(font);
	        popup.setFont(font);
		}
	}
	
	/** Diposes the editor, not to be used again */
	public void detach() {
		unsetAutoOptions();
	}
	
	/**
	 * Resets the filter, which implies:<ul>
	 * <li>Content set to empty</li>
	 * <li>If it has autooptions, they are recreated</li>
	 * <li>Without autooptions, if there is a renderer, nothing else is done</li>
	 * <li>Without autoptions, and with no renderer:<ul>
	 * <li>History is lost<li>
	 * <li>Options are removed</li>
	 * <li>It becomes editable</li></ul></li>
	 * </ul>
	 */
	public void resetFilter() {
		setEditorContent(EditorComponent.EMPTY_FILTER);
		if (autoOptionsHandler!=null){
			TableModel model = autoOptionsHandler.tableModel;
			setAutoOptions(model);
		} else if (getListCellRenderer()==null){
			clearOptions();
			setEditable(true);
		}
	}
	
	/** 
	 * Sets the {@link IFilterTextParser}; if the editor does not have a
	 * {@link ListCellRenderer}, a parser is mandatory 
	 */
    public void setTextParser(IFilterTextParser parser){
    	releaseTextParser();
    	parser.addPropertyChangeListener(textParserListener);
		popup.setIgnoreCase(parser.isIgnoreCase());
    	editor.setTextParser(parser);
    	filter.checkChanges();
    }
    
    private void releaseTextParser(){
    	IFilterTextParser parser = editor.getTextParser();
    	if (parser!=null){
    		parser.removePropertyChangeListener(textParserListener);
    	}
    }

    /**Returns the associated {@link IFilterTextParser} */
    public IFilterTextParser getTextParser(){
    	return editor.getTextParser();
    }
    
    /** 
     * Defines the filter position associated to this editor.<br> 
     * It corresponds to the table's model column
     **/
	public void setFilterPosition(int filterPosition) {
		editor.setPosition(filterPosition);
		filter.checkChanges();
	}

    /** Returns the filter position associated to this editor*/
	public int getFilterPosition() {
		return editor.getPosition();
	}
	
	/** 
	 * Defines the format, used in the options list to convert content 
	 * into strings (if / when needed)
	 **/
	public void setFormat(Format format){
		popup.setFormat(format);
	}

	/** Sets the available options, shown on the popup menu */
	public void setOptions(Collection<?> options) {
		popup.clear();
		addOptions(options);
	}

	/** 
	 * Adds the options to the current set, removing any duplicates.<br>
	 * If there is no {@link ListCellRenderer} defined, the content is 
	 * stringfied and sorted.<br>
	 */
	public void addOptions(Collection<?> options) {
		popup.addOptions(options);
		downButton.setCanPopup(popup.hasContent());
	}

	/** Clears any options currently defined, including the current history */
	public void clearOptions() {
		popup.clear();
		downButton.setCanPopup(popup.hasContent());
	}

	/**
	 * Sets the {@link ListCellRenderer} for the options / history.<p>
	 * It also affectes to how the content is rendered<br>
	 * If not null, the content cannot be text-edited anymore
	 * @param renderer
	 */
	public void setListCellRenderer(ListCellRenderer renderer){
		popup.setListCellRenderer(renderer);
		setupEditorComponent(renderer);
		editor.getComponent().setBackground(getBackground());
		editor.setForeground(getForeground());
		editor.getComponent().setFont(getFont());
	}

	/** Returns the associated {@link ListCellRenderer} */
	public ListCellRenderer getListCellRenderer(){
		return popup.getListCellRenderer();
	}

	/**
	 * Defines the editor, if text based -i.e., without associated 
	 * {@link ListCellRenderer}, as editable: this flag means that the user 
	 * can enter any text, not being limited to the existing options
	 */
	public void setEditable(boolean enable) {
		if (!popup.hasOptions()){
			enable=true;
		}
		if (enable != isEditable()) {
			editor.setEditable(enable);
		}
	}

	/** 
	 * Returns the editable flag
	 * @see #setEditable(boolean)
	 */
	public boolean isEditable() {
		return editor.isEditable();
	}
	
	/**
	 * Using autoOptions, the options displayed on the popup menu are 
	 * automatically extracted from the associated {@link TableModel}.
	 * @param tableModel can be set to null unset the flag
	 */
	public void setAutoOptions(TableModel tableModel){
		unsetAutoOptions();
		if (tableModel!=null){
			autoOptionsHandler=new AutoOptionsHandler(tableModel);
		}
	}

	/** Returns true if the editor is using autoOptions */
	public boolean isAutoOptions(){
		return autoOptionsHandler!=null;
	}
	
	/** Unsets the autoOptions flag */
	public void unsetAutoOptions(){
		if (autoOptionsHandler!=null){
			autoOptionsHandler.detach();
			autoOptionsHandler=null;
		}
	}
	
	private void setupEditorComponent(ListCellRenderer renderer){
		EditorComponent newComponent=null;
		if (renderer==null){
			if (!(editor instanceof EditorComponent.Text)){
				newComponent = new EditorComponent.Text(popup);
			}
		} else if (!(editor instanceof EditorComponent.Rendered)){
			newComponent = popup.createRenderedEditorComponent();
			//trigger popup when the user clicks on the componnet itself
			newComponent.getComponent().addMouseListener(new MouseAdapter() {
            	@Override public void mouseClicked(MouseEvent e) {
            		if (isEnabled()){
            			triggerPopup(editor.getComponent());
            		}
            	}
			});
			newComponent.getComponent().setPreferredSize(
					editor.getComponent().getPreferredSize());
		}
		if (newComponent!=null){
			if (editor!=null){
				remove(editor.getComponent());
				newComponent.setTextParser(editor.getTextParser());
				newComponent.setPosition(editor.getPosition());
				newComponent.getComponent().setBackground(editor.getComponent().getBackground());
				newComponent.setForeground(editor.getForeground());
				newComponent.setErrorForeground(editor.getErrorForeground());
				newComponent.getComponent().setFont(editor.getComponent().getFont());
				newComponent.setErrorForeground(editor.getErrorForeground());
				newComponent.setDisabledForeground(editor.getDisabledForeground());
			}
			editor = newComponent;
			setupComponent(editor.getComponent());
			add(editor.getComponent(), BorderLayout.CENTER);
			invalidate();
		}		
		setEnabled(isEnabled());
	}
	
	private void setupComponent(JComponent component){
		component.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				editor.focusGained(false);
				popup.hide();
				filter.checkChanges();
			}

			@Override
			public void focusGained(FocusEvent e) {
				if (isEnabled() && editor.focusGained(true)){
					showOptions();
					popup.setPopupFocused(true);
				}
			}
		});

		component.setBorder(null);
		component.setFocusable(true);
		component.setEnabled(isEnabled());
		
		setupEnterKey(component);
		setupEscKey(component);
		setupHomeKey(component);
		setupHomeCtrlKey(component);
		setupEndKey(component);
		setupEndCtrlKey(component);
		setupUpKey(component);
		setupUpCtrlKey(component);
		setupUpPageKey(component);
		setupDownPageKey(component);
		setupDownKey(component);
		setupDownCtrlKey(component);
	}

	/** Method called when an element in the options menu is selected */
	void popupSelection(Object selection) {
		if (selection != null) {
			setEditorContent(selection);
		}
	}

	/** Shows the popup menu, preselecting the best match */
	void showOptions() {
		if (!popup.isVisible()) {
			popup.display(editor.getComponent());
			popup.selectBestMatch(editor.getContent(), false);
		}
	}
	
	/** triggers the popup for an operation starting on the source component */
	void triggerPopup(Object source){
		if (!popup.isMenuCanceledForMouseEvent(source)){
			editor.getComponent().requestFocus();
			showOptions();
			popup.setPopupFocused(true);
		}		
	}
	
	// LISTENERS for KEY EVENTS

	/**
	 * Change action for pressing enter key: on a popup, select the current item
	 * and close it.<br>
	 * Without popup, unselect any possible selection
	 */
	private void setupEnterKey(JComponent component) {

		String actionName = "FCB_ENTER"; 
		Action action = new AbstractAction(actionName) {

			private static final long serialVersionUID = 6926912268574067920L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (popup.isPopupFocused()) {
					popupSelection(popup.getSelection());
				} else {
					filter.checkChanges();
				}
				popup.hide();
			}
		};
		component.getActionMap().put(actionName, action);
		component.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), actionName);
	}

	/**
	 * Change action for pressing enter key: on a popup, hide it<br>
	 * And unselect any possible selection
	 */
	private void setupEscKey(JComponent component) {

		String actionName = "FCB_ESC"; 
		Action action = new AbstractAction(actionName) {

			private static final long serialVersionUID = -4351240441578952476L;

			@Override
			public void actionPerformed(ActionEvent e) {
				popup.hide();
				if (e.getSource() instanceof JTextField){
					JTextField textField=(JTextField)e.getSource();
					textField.setCaretPosition(textField.getCaretPosition());
				}
			}
		};
		component.getActionMap().put(actionName, action);
		component.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), actionName);
	}

	private void setupEndKey(JComponent component) {
		String actionName = "FCB_END"; 
		Action action = new AbstractAction(actionName) {

			private static final long serialVersionUID = -2777729244353281164L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!popup.isPopupFocused() || !popup.selectLast(false)) {
					if (e.getSource() instanceof JTextField){
						JTextField textField=(JTextField)e.getSource();
						textField.setCaretPosition(textField.getText().length());
					}
				}
			}
		};
		component.getActionMap().put(actionName, action);
		component.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), actionName);
	}

	private void setupEndCtrlKey(JComponent component) {
		String actionName = "FCB_END_CTRL"; 
		Action action = new AbstractAction(actionName) {

			private static final long serialVersionUID = 1945871436968682881L;

			@Override
			public void actionPerformed(ActionEvent e) {
				//if focus is on the popup: select the very last item on the 
				//popup, changing probably from the history list to the option 
				//list;  if the item is already on the very last element, or the 
				//focus is on the text field, move the caret to the end
				if (!popup.isPopupFocused() || !popup.selectLast(true)){
					if (e.getSource() instanceof JTextField){
						JTextField textField=(JTextField)e.getSource();
						textField.setCaretPosition(textField.getText().length());
					}
				}
			}
		};
		component.getActionMap().put(actionName, action);
		component.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_END, Event.CTRL_MASK), 
				actionName);
	}

	private void setupHomeCtrlKey(JComponent component) {
		String actionName = "FCB_HOME_CTRL"; 
		Action action = new AbstractAction(actionName) {

			private static final long serialVersionUID = 3916227645612863334L;

			@Override
			public void actionPerformed(ActionEvent e) {
				//if focus is on the popup: select the very first item on the 
				//popup, changing probably from the option list to the history 
				//list;  if the item is already on the very first element, or 
				//the focus is on the text field, move the caret home
				if (!popup.isPopupFocused() || !popup.selectFirst(true)){
					if (e.getSource() instanceof JTextField){
						JTextField textField=(JTextField)e.getSource();
						textField.setCaretPosition(0);
					}
				}
			}
		};
		component.getActionMap().put(actionName, action);
		component.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_HOME, Event.CTRL_MASK), 
				actionName);
	}

	/**
	 * If the focus is on the popup, Home moves the selected item to the first
	 * in the list -if it is the first on the options list jumps to the first on
	 * the history list. Otherwise, just moves the caret position to the origin.
	 * Exceptionally, if the focus is on the popup and the selected item is
	 * already the very first shown, it also moves the caret position to the
	 * origin.
	 */
	private void setupHomeKey(JComponent component) {
		String actionName = "FCB_HOME"; 
		Action action = new AbstractAction(actionName) {

			private static final long serialVersionUID = -1583258893221830664L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!popup.isPopupFocused() || !popup.selectFirst(false)) {
					if (e.getSource() instanceof JTextField){
						JTextField textField=(JTextField)e.getSource();
						textField.setCaretPosition(0);
					}
				}
			}
		};
		component.getActionMap().put(actionName, action);
		component.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), actionName);
	}

	private void setupDownPageKey(JComponent component) {
		String actionName = "FCB_PAGE_DOWN"; 
		Action action = new AbstractAction(actionName) {

			private static final long serialVersionUID = -1187830005921916553L;

			@Override
			public void actionPerformed(ActionEvent e) {
				//without moving the focus, move down one page on the popup menu, 
				//probably jumping to options list
				if (popup.isVisible()) {
					boolean focusPopup = popup.isPopupFocused();
					popup.selectDownPage();
					popup.setPopupFocused(focusPopup);
				} else {
					showOptions();
				}
			}
		};
		component.getActionMap().put(actionName, action);
		component.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), actionName);
	}

	private void setupUpPageKey(JComponent component) {
		String actionName = "FCB_PAGE_UP"; 
		Action action = new AbstractAction(actionName) {

			private static final long serialVersionUID = 6590487133211390977L;

			@Override
			public void actionPerformed(ActionEvent e) {
				//without moving the focus, move up one page on the popup menu, 
				//probably jumping to history list
				if (popup.isVisible()) {
					boolean focusPopup = popup.isPopupFocused();
					popup.selectUpPage();
					popup.setPopupFocused(focusPopup);
				}
			}
		};
		component.getActionMap().put(actionName, action);
		component.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), actionName);
	}

	private void setupUpCtrlKey(JComponent component) {
		String actionName = "FCB_UP_CTRL"; 
		Action action = new AbstractAction(actionName) {

			private static final long serialVersionUID = 746565926592574009L;

			@Override
			public void actionPerformed(ActionEvent e) {
				//if focus is on the popup: move from options to history, and, 
				//being already on history, up to text field.
				if (popup.isPopupFocused()) {
					if (!popup.selectUp(true)){
						popup.setPopupFocused(false);
					}
				}
			}
		};
		component.getActionMap().put(actionName, action);
		component.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.CTRL_MASK), 
				actionName);
	}

	private void setupUpKey(JComponent component) {
		String actionName = "FCB_UP"; 
		Action action = new AbstractAction(actionName) {

			private static final long serialVersionUID = 4555560696351340571L;

			@Override
			public void actionPerformed(ActionEvent e) {
				//if popup is not visible, just make it visible.
				//if popup has not the focus, pass it the focus
				//else: move up!
				if (popup.isVisible()) {
					if (popup.isPopupFocused()){
						popup.selectUp(false);
					} else {
						popup.setPopupFocused(true);
					}
				} else {
					showOptions();
				}
			}
		};
		component.getActionMap().put(actionName, action);
		component.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), actionName);
	}

	private void setupDownCtrlKey(JComponent component) {
		String actionName = "FCB_DOWN_CTRL"; 
		Action action = new AbstractAction(actionName) {

			private static final long serialVersionUID = -8075976293862885060L;

			@Override
			public void actionPerformed(ActionEvent e) {
				//if popup is not visible, make it visible
				//if popup has not the focus, pass it the focus
				//else: move to the first visible element in the options
				if (popup.isVisible()) {
					if (popup.isPopupFocused()){
						popup.selectDown(true);
					} else {
						popup.setPopupFocused(true);
					}
				} else {
					showOptions();
				}
			}
		};
		component.getActionMap().put(actionName, action);
		component.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.CTRL_MASK), 
				actionName);
	}

	private void setupDownKey(JComponent component) {
		String actionName = "FCB_DOWN"; 
		Action action = new AbstractAction(actionName) {

			private static final long serialVersionUID = -4133513199725709434L;

			@Override
			public void actionPerformed(ActionEvent e) {
				//if popup is not visible, just make it visible.
				//if popup has not the focus, pass it the focus
				//else: move down!
				if (popup.isVisible()) {
					if (popup.isPopupFocused()){
						popup.selectDown(false);
					} else {
						popup.setPopupFocused(true);
					}
				} else {
					showOptions();
				}
			}
		};
		component.getActionMap().put(actionName, action);
		component.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), actionName);
	}

	/**
	 * Custom implementation of the arrow used to display the popup menu.<br>
	 * It can have three states:<ul>
	 * <li>Disabled: shown  with disabled, color</li> 
	 * <li>Enabled, full: shown with normal color</li> 
	 * <li>Enabled, not full: means that the popup cannot be shown, because has 
	 * no content;  it is displayed as disabled</li> 
	 */
	final static class FilterArrowButton extends JButton {
		private static final long serialVersionUID = -777416843479142582L;
		private final static int FILL_X[] = { 0, 3, 6 };
		private final static int FILL_Y[] = { 0, 5, 0 };
		private final static int MIN_X = 6;
		private final static int MIN_Y = 6;
		
		private boolean canPopup=true;
		private boolean enabled=true;
		private Color disabledColor;
		
		public void setCanPopup(boolean full){
			this.canPopup=full;
			super.setEnabled(full && enabled);
		}
		
		public void setDisabledColor(Color color){
			disabledColor=color;
			if (!isEnabled()){
				repaint();
			}
		}
		
		@Override public void setEnabled(boolean b) {
			this.enabled=b;
			super.setEnabled(canPopup && enabled);
		}
		
		@Override public void paint(Graphics g) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			int height = getHeight();
			int width = getWidth();

			g.setColor(getBackground());
			g.fillRect(0, 0, width, height);

			width = (width - MIN_X) / 2;
			height = Math.min(height / 2, height - MIN_Y);
			g.translate(width, height);
			if (enabled && canPopup){
				g.setColor(getForeground());
			} else {
				g.setColor(disabledColor);
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

    /**
     * Wrapper of the filter associated to the {@link EditorComponent}, 
     * ensuring some added functionality (like auto-adding to the 
     * history list when the filter changes) 
     */
    final class Filter extends BaseFilter {
		RowFilter delegateFilter;
    	@Override
    	public boolean include(RowFilter.Entry entry) {
    		return delegateFilter==null? true : delegateFilter.include(entry);
    	}
    	public void checkChanges(){
    		if (isEnabled()){
	    		checkChanges(false);
	    		Object content = editor.getContent();
	    		if (!EditorComponent.EMPTY_FILTER.equals(content)){
	    			popup.addHistory(content);
	    			downButton.setCanPopup(popup.hasContent());
	    		}
    		}
    	}
    	public void update(){
    		if (isEnabled()){
        		checkChanges(true);    			
    		} else {
				delegateFilter=null;
    			reportFilterUpdatedToObservers();    			
    		}
    	}
    	private void checkChanges(boolean forceUpdate){
    		if (editor.checkFilterUpdate(forceUpdate)){
    			delegateFilter = editor.getFilter();
    			reportFilterUpdatedToObservers();
    		}
    	}
    }

    /**
     * Implementation of the {@link Border} associated to each filter editor
     */
	final class EditorBorder implements Border {
		
		private Color borderColor;
		
		public void setColor(Color color){
			borderColor = color;
			repaint();
		}
		
		@Override public void paintBorder(Component c, Graphics g, int x, int y, 
				int width, int height) {
			g.setColor(borderColor);
			g.drawLine(0, height-1, width-1, height-1);
			g.drawLine(width-1, 0, width-1, height-1);
		}
		
		@Override public boolean isBorderOpaque() {
			return true;
		}
		
		@Override public Insets getBorderInsets(Component c) {
			return new Insets(0, 1, 1, 1);
		}
	}

	/**
	 * Class to automatically extract the content from the table model, 
	 * used when the filter editor is defined with autoOptions flag.
	 */
	final class AutoOptionsHandler implements TableModelListener{
		TableModel tableModel;
		public AutoOptionsHandler(TableModel tableModel) {
			this.tableModel=tableModel;
			extractFilterContentsFromModel();
			tableModel.addTableModelListener(this);
		}
		
		public void detach(){
			tableModel.removeTableModelListener(this);			
		}
		
		@Override public void tableChanged(TableModelEvent e) {
			int r = e.getFirstRow();			
			if (r == TableModelEvent.HEADER_ROW){				
				extractFilterContentsFromModel();
			} else if (e.getType()!=TableModelEvent.DELETE){				
				int c = e.getColumn(); 				
				if (c == TableModelEvent.ALL_COLUMNS || c == getFilterPosition()){					
					extendFilterContentsFromModel(r, e.getLastRow());
				}
			}
		}
		
	    private void extractFilterContentsFromModel() {
	    	clearOptions();
	    	extendFilterContentsFromModel(0, tableModel.getRowCount()-1);
	    }
	    
	    private void extendFilterContentsFromModel(int firstRow, int lastRow){
	    	List<Object> all = new ArrayList<Object>();
            int column = getFilterPosition();

            lastRow = Math.min(tableModel.getRowCount() - 1, lastRow);

            while (lastRow >= firstRow) {
                all.add(tableModel.getValueAt(firstRow++, column));
            }
            addOptions(all);
	    }
	    
	}
}