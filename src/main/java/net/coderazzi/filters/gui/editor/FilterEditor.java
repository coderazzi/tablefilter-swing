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
import java.text.Format;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.RowFilter;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import net.coderazzi.filters.Filter;
import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.gui.AutoOptions;
import net.coderazzi.filters.gui.CustomChoice;
import net.coderazzi.filters.gui.FilterSettings;
import net.coderazzi.filters.gui.FiltersHandler;
import net.coderazzi.filters.gui.IFilterEditor;
import net.coderazzi.filters.gui.IParserModel;
import net.coderazzi.filters.parser.DateComparator;

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
public class FilterEditor extends JComponent implements IFilterEditor {

	private static final long serialVersionUID = 6908400421021655278L;
	private EditorBorder border = new EditorBorder();
	private Set<CustomChoice> customChoices;
	private AutoOptions autoOptions;
	private Format format;
	private Comparator comparator;
	private boolean ignoreCase;
	private int modelIndex;
	private Class modelClass;
	
	FiltersHandler filtersHandler;
	FilterArrowButton downButton = new FilterArrowButton();
    EditorFilter filter = new EditorFilter();
	EditorComponent editor;
	PopupComponent popup;

	public FilterEditor(FiltersHandler filtersHandler, int modelIndex,
			Class<?> modelClass) {
		this.filtersHandler = filtersHandler;
		this.modelIndex = modelIndex;	
		this.modelClass = modelClass;
		
		setLayout(new BorderLayout());
		setBorder(border);
		
		popup = new PopupComponent() {

			@Override
			protected void optionSelected(Object selection) {
				popupSelection(selection);
			}
		};
		downButton.addActionListener(new ActionListener() {

			@Override public void actionPerformed(ActionEvent e) {
				triggerPopup(downButton);
			}
		});	
		downButton.setCanPopup(false);
		
		add(downButton, BorderLayout.EAST);
		setupEditorComponent(null);
		
		setFormat(getParserModel().getFormat(modelClass));
		setComparator(getParserModel().getComparator(modelClass));
		setIgnoreCase(getParserModel().isIgnoreCase());
	}
	
	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setMaxVisibleRows(int)
	 */
	@Override public void setMaxVisibleRows(int maxVisibleRows) {
		popup.setMaxVisibleRows(maxVisibleRows);
	}

	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#getMaxVisibleRows()
	 */
	@Override public int getMaxVisibleRows() {
		return popup.getMaxVisibleRows();
	}

	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setMaxHistory(int)
	 */
	@Override public void setMaxHistory(int size) {
		popup.setMaxHistory(size);
	}

	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#getMaxHistory()
	 */
	@Override public int getMaxHistory() {
		return popup.getMaxHistory();
	}
	
	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#getFilter()
	 */
	@Override public IFilter getFilter() {
		return filter;
	}	
	
	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#getContent()
	 */
	@Override public Object getContent() {
		return editor.getContent();
	}
	
	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setContent(java.lang.Object)
	 */
	@Override public void setContent(Object content){
		if (content==null){
			setEditorContent(CustomChoice.MATCH_ALL, false);
		} else if (isEditable()){
			//we need to use, eventually, the provided formatter
			setEditorContent(format(content), true);
		} else if (popup.isValidOption(content)){
			//the content must be a valid option
			setEditorContent(format(content), true);			
		}
	}
	
	/** formats an object using the current class's format */
	private String format(Object o){
		return format==null? o==null? "" : o.toString() : format.format(o);
	}

	/** 
	 * Sets the content, updating the filter -and propagating any changes-<br>
	 * The parameters escapeIt must be true if the content could require
	 * escaping (otherwise, it will be always treated literally). <br>
	 * Escaping only applies, anyway, to editable columns (Strings) 
	 */
	private void setEditorContent(Object content, boolean escapeIt) {
		editor.setContent(content, escapeIt);
		filter.checkChanges(false);
	}
	
	void setEditorEnabled(boolean enabled) {
		super.setEnabled(enabled);
		downButton.setEnabled(enabled);
		editor.setEnabled(enabled);
	}
	
	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setBackground(java.awt.Color)
	 */
	@Override public void setBackground(Color bg) {
		super.setBackground(bg);
		if (editor!=null){
	    	editor.getComponent().setBackground(bg);
	    	downButton.setBackground(bg);
	        popup.setBackground(bg);
	        repaint();
		}
	}
	
    /* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setGridColor(java.awt.Color)
	 */
	@Override public void setGridColor(Color c) {
    	popup.setGridColor(c);
    	border.setColor(c);
    }
	
	@Override public Color getGridColor() {
		return border.getColor();
	}

    /* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setErrorForeground(java.awt.Color)
	 */
    @Override public void setErrorForeground(Color fg) {
    	editor.setErrorForeground(fg);
    }

    /* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#getErrorForeground()
	 */
    @Override public Color getErrorForeground() {
    	return editor.getErrorForeground();
    }

    /* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setDisabledForeground(java.awt.Color)
	 */
    @Override public void setDisabledForeground(Color fg){
    	editor.setDisabledForeground(fg);
    	downButton.setDisabledColor(fg);
    	popup.setDisabledColor(fg);
    }

    /* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#getDisabledForeground()
	 */
    @Override public Color getDisabledForeground(){
    	return editor.getDisabledForeground();
    }
    
    /* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setSelectionForeground(java.awt.Color)
	 */
    @Override public void setSelectionForeground(Color fg){
    	editor.setSelectionForeground(fg);
    	downButton.setSelectionForeground(fg);
    	popup.setSelectionForeground(fg);
    }

    /* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#getSelectionForeground()
	 */
    @Override public Color getSelectionForeground(){
    	return popup.getSelectionForeground();
    }
    
    /* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setSelectionBackground(java.awt.Color)
	 */
    @Override public void setSelectionBackground(Color bg){
    	editor.setSelectionBackground(bg);
    	downButton.setSelectionBackground(bg);
    	popup.setSelectionBackground(bg);
    }

    /* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#getSelectionBackground()
	 */
    @Override public Color getSelectionBackground(){
    	return popup.getSelectionBackground();
    }
    
	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setForeground(java.awt.Color)
	 */
	@Override public void setForeground(Color fg) {
		super.setForeground(fg);
		if (editor!=null){
	    	editor.setForeground(fg);
	    	downButton.setForeground(fg);
	        popup.setForeground(fg);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setFont(java.awt.Font)
	 */
	@Override public void setFont(Font font) {
		super.setFont(font);
		if (editor!=null){
			editor.getComponent().setFont(font);
	        popup.setFont(font);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#resetFilter()
	 */
	@Override public void resetFilter() {
		setEditorContent(null, false);
		requestOptions();		
	}
	
    void requestOptions(){
    	if (isEnabled()){
    		filtersHandler.updatedEditorOptions(this);
    	}
    }
    
    @Override public boolean isIgnoreCase() {
    	return ignoreCase;
    }
    
    @Override public void setIgnoreCase(boolean set) {
    	if (ignoreCase!=set){
    		ignoreCase=set;
    		formatUpdated();
    	}
    }
    
    @Override public Format getFormat(){
    	return format;
    }
    
    @Override public void setFormat(Format format){
    	if (this.format!=format){
    		this.format = format;
    		//as bonus, override default Comparator for dates
    		//if the instance is not a DateComparator, the user has set
    		//its own comparator (perhaps has done it as well and it is 
    		//a DateComparator, but seems unlikely)
    		if (format!=null && 
    				(comparator instanceof DateComparator) &&
    				Date.class.isAssignableFrom(modelClass))
    		{
   				setComparator(DateComparator.getDateComparator(format));
    		}
    		formatUpdated();
    	}
    }
    
    private void formatUpdated(){
		if (getListCellRenderer()==null){
			popup.setStringContent(format, getStringComparator());
    		updateTextParser();
			filter.checkChanges(false);
			requestOptions();
		}    	
    }
    
    private void updateTextParser(){
    	editor.setParser(getParserModel().createParser(this));    	
    }
    
    @Override public void setComparator(Comparator comparator){
    	//the comparator is only used for rendered content, and also 
    	//included on the table sorter
    	if (comparator!=this.comparator && comparator!=null){
    		updateTextParser();
    		this.comparator=comparator;
        	JTable table = filtersHandler.getTable();
        	if (table!=null && (table.getRowSorter() instanceof DefaultRowSorter)){
        		((DefaultRowSorter) table.getRowSorter()).setComparator(getModelIndex(), comparator);
        	}
    		ListCellRenderer lcr = getListCellRenderer();
    		if (lcr==null){
    			filter.checkChanges(true);
    		} else {
    			popup.setRenderedContent(lcr, comparator);
    			requestOptions();
    		}
    	}
    }
    
    @Override public Comparator getComparator(){
    	return comparator;
    }
    
    private IParserModel getParserModel(){
    	return filtersHandler.getParserModel();
    }
    
    private Comparator getStringComparator(){
    	return getParserModel().getStringComparator(ignoreCase);
    }
    
    @Override public Class getModelClass(){
    	return modelClass;
    }
    
    /* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#getModelIndex()
	 */
	@Override public int getModelIndex() {
		return modelIndex;
	}
	
	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setCustomChoices(java.util.Set)
	 */
	@Override public void setCustomChoices(Set<CustomChoice> options) {
		if (options==null || options.isEmpty()){
			this.customChoices=null;
		} else {
			this.customChoices = new HashSet<CustomChoice>(options);
		}
		requestOptions();
	}
	
	public void setOptions(Collection<?> options){
		popup.clear();
		addOptions(options);		
	}
	
	public Collection<?> getOptions(){
		return popup.getOptions();
	}

	public void addOptions(Collection<?> options) {
		System.out.println("Editor "+getModelIndex()+": add "+options.size()+" on "+getOptions().size());
		if (popup.addOptions(options)){
			downButton.setCanPopup(popup.hasContent());
		}
	}
	
	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#getCustomChoices()
	 */
	@Override public Set<CustomChoice> getCustomChoices(){
		return customChoices==null? new HashSet<CustomChoice>() : new HashSet<CustomChoice>(customChoices); 
	}

	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setListCellRenderer(javax.swing.ListCellRenderer)
	 */
	@Override public void setListCellRenderer(ListCellRenderer renderer){
		popup.setRenderedContent(renderer, comparator);
		setupEditorComponent(renderer);
		editor.getComponent().setBackground(getBackground());
		editor.setForeground(getForeground());
		editor.getComponent().setFont(getFont());
		filter.checkChanges(true);
		requestOptions();
	}

    /* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setListCellRenderer(javax.swing.table.TableCellRenderer)
	 */
    @Override public void setListCellRenderer(final TableCellRenderer renderer) {
    	setListCellRenderer(renderer==null? null :
    			new DefaultListCellRenderer() {

    		private static final long serialVersionUID = -5990815893475331934L;
    		private JTable table = filtersHandler.getTable();
    		private int position = getModelIndex();

			@Override public Component getListCellRendererComponent(JList list, 
					Object value, int index, boolean isSelected, 
					boolean cellHasFocus) {
				Component ret =  renderer.getTableCellRendererComponent(table, 
						value, isSelected, cellHasFocus, 1, position);
				if (isSelected){
					ret.setBackground(list.getSelectionBackground());
					ret.setForeground(list.getSelectionForeground());
				}else {
					ret.setBackground(list.getBackground());
					ret.setForeground(list.getForeground());
				}
				return ret;
            }
        });
    }

	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#getListCellRenderer()
	 */
	@Override public ListCellRenderer getListCellRenderer(){
		return popup.getListCellRenderer();
	}

	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setEditable(boolean)
	 */
	@Override public void setEditable(boolean enable) {
		if (enable != isEditable()) {
			editor.setEditable(enable);
		}
	}

	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#isEditable()
	 */
	@Override public boolean isEditable() {
		return editor.isEditable();
	}
	
	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#setAutoOptions(AutoOptions)
	 */
	@Override public void setAutoOptions(AutoOptions autoOptions){
		if (autoOptions!=null && autoOptions!=this.autoOptions){
			this.autoOptions=autoOptions;
			Object enums[]=modelClass.getEnumConstants();
			if (Boolean.class==modelClass || (enums!=null && enums.length<=8)){
				setEditable(autoOptions==AutoOptions.DISABLED);
				setMaxHistory(autoOptions==AutoOptions.DISABLED?
						FilterSettings.maxPopupHistory : 0);
			}
			requestOptions();
		}
	}

	/* (non-Javadoc)
	 * @see net.coderazzi.filters.gui.editor.IFilterEditor#getAutoOptions()
	 */
	@Override public AutoOptions getAutoOptions(){
		return autoOptions;
	}
	
	private void setupEditorComponent(ListCellRenderer renderer){
		EditorComponent newComponent=null;
		if (renderer==null){
			if (!(editor instanceof EditorComponent.Text)){
				newComponent = new EditorComponent.Text(this, popup);
			}
		} else if (!(editor instanceof EditorComponent.Rendered)){
			newComponent = popup.createRenderedEditorComponent(this);
			//trigger popup when the user clicks on the component itself
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
			revalidate();
		}		
		setEditorEnabled(filter.isEnabled());
	}
	
	private void setupComponent(JComponent component){
		component.addFocusListener(new FocusListener() {

			@Override public void focusLost(FocusEvent e) {
				popup.hide();
				filter.checkChanges(false);
				//important: call focusMoved AFTER checking changes, to
				//ensure that any changes on decoration (custom choice)
				//are not lost
				editor.focusMoved(false);
				downButton.setFocused(false);
			}

			@Override public void focusGained(FocusEvent e) {
				downButton.setFocused(true);
				if (isEnabled()){
					editor.focusMoved(true);
				}
			}
		});

		component.setFocusable(true);
		
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
			//the selection must be escaped if it is a String which does
			//not belong to the history.
			setEditorContent(selection, !popup.isHistorySelection());
		}
	}

	/** Shows the popup menu, preselecting the best match */
	boolean showOptions() {
		if (!popup.isVisible()) {
			if (!popup.display(editor.getComponent())){
				return false;
			}
			popup.selectBestMatch(editor.getContent(), false);
		}
		return true;
	}
	
	/** triggers the popup for an operation starting on the source component */
	void triggerPopup(Object source){
		if (!popup.isMenuCanceledForMouseEvent(source)){
			editor.getComponent().requestFocus();
			if (showOptions()){
				popup.setPopupFocused(true);
			}
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

			@Override public void actionPerformed(ActionEvent e) {
				if (popup.isPopupFocused()) {
					popupSelection(popup.getSelection());
				} else {
					//use update instead of checkChanges, in case it
					//is needed to reset the icon of a CustomChoice
					filter.checkChanges(true);
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

			@Override public void actionPerformed(ActionEvent e) {
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

			@Override public void actionPerformed(ActionEvent e) {
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

			@Override public void actionPerformed(ActionEvent e) {
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

			@Override public void actionPerformed(ActionEvent e) {
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

			@Override public void actionPerformed(ActionEvent e) {
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

			@Override public void actionPerformed(ActionEvent e) {
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

			@Override public void actionPerformed(ActionEvent e) {
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

			@Override public void actionPerformed(ActionEvent e) {
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

			@Override public void actionPerformed(ActionEvent e) {
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

			@Override public void actionPerformed(ActionEvent e) {
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

			@Override public void actionPerformed(ActionEvent e) {
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
		
		private boolean focus;
		private boolean canPopup=true;
		private boolean enabled=true;
		private Color disabledColor;
		private Color selectionForeground, selectionBackground;
		
		public void setCanPopup(boolean full){
			this.canPopup=full;
			super.setEnabled(full && enabled);
		}
		
		public void setFocused(boolean focus){
			this.focus=focus;
			repaint();
		}
		
		public void setDisabledColor(Color color){
			disabledColor=color;
			if (!isEnabled()){
				repaint();
			}
		}
		
		public void setSelectionForeground(Color color){
			selectionForeground=color;
			if (focus){
				repaint();
			}
		}
		
		public void setSelectionBackground(Color color){
			selectionBackground=color;
			if (focus){
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

			g.setColor(enabled && focus? selectionBackground: getBackground());
			g.fillRect(0, 0, width, height);

			width = (width - MIN_X) / 2;
			height = Math.min(height / 2, height - MIN_Y);
			g.translate(width, height);
			if (enabled && canPopup){
				g.setColor(focus? selectionForeground : getForeground());
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
    final class EditorFilter extends Filter {
		RowFilter delegateFilter;
    	@Override
    	public boolean include(RowFilter.Entry entry) {
    		return delegateFilter==null? true : delegateFilter.include(entry);
    	}
    	@Override public void setEnabled(boolean enable) {
    		if (enable!=isEnabled()){
    			setEditorEnabled(enable);
        		if (enable){
        			delegateFilter = editor.checkFilterUpdate(true);
        		} else {
        			delegateFilter = null;
        		}
        		super.setEnabled(enable);
    		}
    	}
    	public void checkChanges(boolean forceUpdate){
    		if (isEnabled()){
	    		RowFilter oldFilter = editor.getFilter();
	    		RowFilter newFilter = editor.checkFilterUpdate(forceUpdate);
	    		if (newFilter != delegateFilter || oldFilter !=delegateFilter){
	    			delegateFilter = newFilter;
	    			reportFilterUpdatedToObservers();
	    		}
	    		Object content = editor.getContent();
	    		if (!(content instanceof CustomChoice)){
	    			popup.addHistory(content);
	    			downButton.setCanPopup(popup.hasContent());
	    		}
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
		
		public Color getColor(){
			return borderColor;
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
	
}