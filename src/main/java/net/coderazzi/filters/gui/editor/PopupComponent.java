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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Format;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import net.coderazzi.filters.gui.CustomChoice;
import net.coderazzi.filters.gui.FilterSettings;
import net.coderazzi.filters.gui.IFilterEditor;


/**
 * Internal editor component, responsible to handle the popup menu,
 * which contains the history and the options list.
 */
abstract class PopupComponent implements PopupMenuListener{

	/** Minimum number of visible options -if there are options- */
	private static final int MIN_VISIBLE_OPTIONS = 4; 
	private JPopupMenu popup;
	private FilterListCellRenderer listRenderer;
	private JScrollPane optionsScrollPane;
	private JScrollPane historyScrollPane;
	private JSeparator separator;	

	private OptionsListModel optionsModel;
	private HistoryListModel historyModel;
	/** 
	 * cancelReason contains the source of the event that 
	 * canceled last time the popup menu 
	 **/
	private Object cancelReason;

	/** This is the total max number of visble rows (history PLUS options) */
	private int maxVisibleRows = FilterSettings.maxVisiblePopupRows;
	/** 
	 * Max history is the maximum number of elements in the history list 
	 * when there are NO options 
	 **/
	private int maxHistory = FilterSettings.maxPopupHistory; 

	/** focusedList always refer to one of optionsList or historyList */
	JList focusedList;
	JList optionsList;
	JList historyList;


	public PopupComponent() {
		optionsModel = new OptionsListModel();
		historyModel = new HistoryListModel();
		setMaxHistory(maxHistory);
		createGui();
	}

	/** Invoked when the user select an element in the option or history lists*/
	protected abstract void optionSelected(Object selection);
	
	/** 
	 * Creates an EditorComponent that can display content with 
	 * the same renderer used to display the options
	 */
	public EditorComponent createRenderedEditorComponent(FilterEditor editor) {
		return new EditorComponent.Rendered(editor, listRenderer);
	}

	/** Returns the current selection -can be history or and option-*/
	public Object getSelection() {
		return focusedList.getSelectedValue();
	}
	
	/** Returns true if the current selection belongs to the history*/
	public boolean isHistorySelection(){
		return focusedList==historyList;
	}

	/** Returns true if the popup is currently visible */
	public boolean isVisible() {
		return popup.isVisible();
	}
	
	/**
	 * Displays the popup, if there is content (history or options), 
	 * and is not yet visible<br>
	 * It uses the passed component as guide to set the location 
	 * and the size of the popup.
	 */
	public boolean display(Component editor) {
		if (isVisible() || !hasContent()) {
			return false;
		}
		setPopupFocused(false);
		int width = editor.getParent().getWidth()-1;
		configurePaneSize(optionsScrollPane, width);
		configurePaneSize(historyScrollPane, width);
		if (editor.isValid()){
			popup.show(editor, -editor.getLocation().x-1, editor.getHeight());
		}
		// Not yet knowing the focus, but the call to select (immediately after,
		// always), takes care of it
		focusedList = historyList;
		return true;
	}

	/** Hides the popup, returning false it was already hidden */
	public boolean hide() {
		if (isVisible()) {
			popup.setVisible(false);
			return true;
		}
		return false;
	}

	/**
	 * Finds -and selects- the best match to a given content, 
	 * using the existing history and options.<br>
	 * It always favor content belonging to the options list, 
	 * rather than to the history list.
	 * @param hint an object used to select the match. 
	 *  If the content is text-based (there is no {@link ListCellRenderer} 
	 *  defined), the hint is considered the start of the string, and the best
	 *  match should start with the given hint). 
	 *  If the content is not text-based, only exact matches are returned,
	 *  no matter the value of the parameter perfectMatch
	 * @param perfectMatch when the content is text-based, if no option/history 
	 *  starts with the given hint string, smaller hint substrings are used, 
	 *  unless perfectMatch is true
	 */
	public String selectBestMatch(Object hint, boolean perfectMatch) {
		Match historyMatch = historyModel.getClosestMatch(hint, perfectMatch);
		if (optionsModel.getSize() > 0) {
			Match match = optionsModel.getClosestMatch(hint, 
					perfectMatch || historyMatch.exact);
			if (isVisible() && match.index >= 0) {
				optionsList.ensureIndexIsVisible(match.index);
			}
			if (match.exact || (!historyMatch.exact 
					&& (match.len >= historyMatch.len))) {
				if (match.index >= 0) {
					if (isVisible()) {
						focusOptions();
						select(match.index);
					}
					return optionsModel.getElementAt(match.index).toString();
				}
				return null;
			}
		}
		if (historyMatch.index != -1) {
			if (isVisible()) {
				focusHistory();
				select(historyMatch.index);
			}
			return historyModel.getElementAt(historyMatch.index).toString();
		}
		return null;
	}

	/** Specifies that the content is to be handled as strings */
	public void setStringContent(Format format, Comparator stringComparator) {
		listRenderer.setUserRenderer(null);
		if (optionsModel.setStringContent(format, stringComparator)){
			historyModel.setStringContent(stringComparator);
			reconfigureGui();
		}
	}

	/** Specifies that the content requires no conversion to strings */
	public void setRenderedContent(ListCellRenderer renderer, Comparator classComparator) {
		listRenderer.setUserRenderer(renderer);
		if (optionsModel.setRenderedContent(classComparator)){
			historyModel.setStringContent(null);
			reconfigureGui();
		}
	}

	/** Returns the current {@link ListCellRenderer} */
	public ListCellRenderer getListCellRenderer() {
		return listRenderer.getUserRenderer();
	}

	/** 
	 * Informs that the focus is on the popup: 
	 * this affects how the selected elements are displayed
	 **/
	public void setPopupFocused(boolean set) {
		if (set != listRenderer.isFocusOnList()) {
			listRenderer.setFocusOnList(set);
			focusedList.repaint();
		}
	}

	/** Returns true if the focus is currently on the popup */
	public boolean isPopupFocused() {
		return isVisible() && listRenderer.isFocusOnList();
	}

	/** Sets the list's background color */
	public void setBackground(Color color){
		optionsList.setBackground(color);
		historyList.setBackground(color);
	}
	
	/** Sets the list's foreground color */
	public void setForeground(Color color){
		optionsList.setForeground(color);
		historyList.setForeground(color);
	}
	
	/** Sets the list's selected background color */
	public void setSelectionBackground(Color color){
		optionsList.setSelectionBackground(color);
		historyList.setSelectionBackground(color);
	}
	
	/** Sets the list's selected foreground color */
	public void setSelectionForeground(Color color){
		optionsList.setSelectionForeground(color);
		historyList.setSelectionForeground(color);
	}
	
	/** Gets the list's selected background color */
	public Color getSelectionBackground(){
		return optionsList.getSelectionBackground();
	}
	
	/** Gets the list's selected foreground color */
	public Color getSelectionForeground(){
		return optionsList.getSelectionForeground();
	}
	
	/** Sets the disabled color, used on the CustomChoices' text */
	public void setDisabledColor(Color color){
		listRenderer.setDisabledColor(color);
	}
	
	/** Sets the disabled color, used for many things, like border, separator */
	public void setGridColor(Color color){
		popup.setBorder(BorderFactory.createLineBorder(color, 1));
		separator.setForeground(color);
	}
	
	/** Return the grid color*/
	public Color getGridColor(){
		return separator.getForeground();
	}
	
	/** Sets the list's font color */
	public void setFont(Font font){
		optionsList.setFont(font);
		historyList.setFont(font);
		ensureListRowsHeight();
	}
	
	/** Returns true if the passed object matches an existing option */
	public boolean isValidOption(Object object){
		return optionsModel.isValidOption(object);		
	}
	
	/** Returns true if the popup has any content to display */
	public boolean hasContent(){
		return !optionsModel.isEmpty() || !historyModel.isEmpty();
	}

	/** Returns true if the popup contains options (history notwithstanding) */
	public boolean hasOptions() {
		return !optionsModel.isEmpty();
	}

	/** @see IFilterEditor#setMaxVisibleRows(int) */
	public void setMaxVisibleRows(int maxVisibleRows) {
		this.maxVisibleRows = Math.max(MIN_VISIBLE_OPTIONS, maxVisibleRows);
		fixMaxHistory();
		reconfigureGui();
	}

	/** @see IFilterEditor#getMaxVisibleRows() */
	public int getMaxVisibleRows() {
		return maxVisibleRows;
	}

	/** @see IFilterEditor#setMaxHistory(int) */
	public void setMaxHistory(int size) {
		this.maxHistory = size;
		if (fixMaxHistory()) {
			reconfigureGui();
		}
	}

	/** @see IFilterEditor#getMaxHistory() */
	public int getMaxHistory() {
		return maxHistory;
	}
	
	/** Internal method to calculate the real history size used */
	private boolean fixMaxHistory(){
		int now = historyModel.getMaxHistory();
		int optionsSize = optionsModel.getSize();
		int finalSize = optionsSize==0? maxVisibleRows 
				: Math.min(maxHistory, maxVisibleRows 
						- Math.min(optionsSize, MIN_VISIBLE_OPTIONS));
		return finalSize==now? false : historyModel.setMaxHistory(finalSize);
	}

	/** Clears both the history and the options lists */
	public void clear() {
		optionsModel.clearContent();
		historyModel.clear();
		fixMaxHistory();
		reconfigureGui();
	}

	/** Adds content to the history list */
	public void addHistory(Object st) {
		if (historyModel.add(st)) {
			reconfigureGui();
		}
	}

	/** 
	 * Adds content to the options list.<br>
	 * If there is no {@link ListCellRenderer} defined,
	 * the content is stringfied and sorted -so duplicates are removed-
	 * @returns true if the operation implies a change
	 */
	public boolean addOptions(Collection<?> options) {
		if (optionsModel.addContent(options)){
			fixMaxHistory();
			reconfigureGui();
			return true;
		}
		return false;
	}

	/** Returns the current options */
	public Collection<?> getOptions(){
		return optionsModel.getOptions();
	}
	
	/** Returns the custom choice matching the given text */
	public CustomChoice getCustomChoice(String s){
		return optionsModel.getCustomChoice(s);
	}

	/**
	 * Selects the first element in the focused list. If it is already on the
	 * first element, or forceJump is true, selects the first element on the
	 * history list.<br>
	 * Returns true if there is indeed a change (or forceJump is true)
	 */
	public boolean selectFirst(boolean forceJump) {
		boolean ret = canSwitchToHistory() 
			&& (forceJump || optionsList.getSelectedIndex() == 0);
		if (ret) {
			focusHistory();
		}
		return select(0) || ret;
	}

	/**
	 * Selects the last element in the focused list. If it is already on the
	 * last element, or forceJump is true, selects the last element on the
	 * options list.<br>
	 * Returns true if there is indeed a change (or forceJump is true)
	 */
	public boolean selectLast(boolean forceJump) {
		boolean ret = canSwitchToOptions() && (forceJump 
				|| historyList.getSelectedIndex() == historyModel.getSize() - 1);
		if (ret) {
			focusOptions();
		}
		return select(focusedList.getModel().getSize() - 1) || ret;
	}

	/**
	 * If jumpRequired is true, or cannot move up on the focused list and the
	 * focused list is the option list, then move to the last element on the
	 * history list.<br>
	 * Otherwise, it just returns false
	 */
	public boolean selectUp(boolean jumpRequired) {
		if (jumpRequired || !select(focusedList.getSelectedIndex() - 1)) {
			if (!canSwitchToHistory()) {
				return false;
			}
			focusHistory();
			select(historyModel.getSize() - 1);
		}
		return true;
	}

	/**
	 * If jumpRequired is true, or cannot move down on the focused list and the
	 * focused list is the history list, then move to the first visible element 
	 * on the options list.<br>
	 * Otherwise, it just returns false
	 */
	public void selectDown(boolean jumpRequired) {
		if (jumpRequired || !select(focusedList.getSelectedIndex() + 1)) {
			if (canSwitchToOptions()) {
				focusOptions();
				select(optionsList.getFirstVisibleIndex());
			}
		}
	}

	/** Moves down a page, or to the last element in the option list, if needed*/
	public void selectDownPage() {
		if (isFocusInHistory()) {
			if (canSwitchToOptions()) {
				focusOptions();
			}
			select(focusedList.getLastVisibleIndex());
		} else {
			int last = optionsList.getLastVisibleIndex();
			if (last == optionsList.getSelectedIndex()) {
				last = Math.min(last + last - optionsList.getFirstVisibleIndex(),
						optionsModel.getSize() - 1);
			}
			select(last);
		}
	}

	/** Moves up a page, or to the first element in the history list, if needed*/
	public void selectUpPage() {
		int select = 0;
		if (!isFocusInHistory()) {
			int selected = optionsList.getSelectedIndex();
			if (canSwitchToHistory() && selected == 0) {
				focusHistory();
			} else {
				select = optionsList.getFirstVisibleIndex();
				if (select == selected) {
					select = Math.max(0, 
							select + select - optionsList.getLastVisibleIndex());
				}
			}
		}
		select(select);
	}

	/**
	 * Selects the given row in the focused list.<br>
	 * Returns true if there is a selection change
	 */
	private boolean select(int n) {
		int current = focusedList.getSelectedIndex();
		setPopupFocused(true);
		if (n >= 0) {
			focusedList.setSelectedIndex(n);
			focusedList.ensureIndexIsVisible(n);
		}
		return current != focusedList.getSelectedIndex();
	}

	/** 
	 * Returns true if the focused list is the options list 
	 * and there is history content 
	 **/
	private boolean canSwitchToHistory() {
		return focusedList == optionsList && !historyModel.isEmpty();
	}

	/** 
	 * Returns true if the focused list is the history list 
	 * and there is options content 
	 **/
	private boolean canSwitchToOptions() {
		return focusedList == historyList && optionsScrollPane.isVisible();
	}

	/** Moves the focus to the history list */
	private void focusHistory() {
		optionsList.clearSelection();
		focusedList = historyList;
	}

	/** Moves the focus to the options list */
	private void focusOptions() {
		historyList.clearSelection();
		focusedList = optionsList;
	}

	/** Returns true if the focused list is the history list*/
	private boolean isFocusInHistory() {
		return focusedList == historyList;
	}

	/** Configures the passed pane to have the given preferred width */
	private void configurePaneSize(JScrollPane pane, int width) {
		Dimension size = pane.getPreferredSize();
		size.width = width;
		pane.setPreferredSize(size);
	}

	/** Ensures that the height of the rows in the lists have the required size*/
	private void ensureListRowsHeight(){
		Object prototype;
		if (listRenderer!=null && listRenderer.getUserRenderer()==null){
			prototype = optionsList.getPrototypeCellValue();
			//we need to change the prototype. The jlist will not update its
			//cell height if the prototype does not change
			prototype = "X".equals(prototype) ? "Z" : "X";
		} else {
			prototype=null;
		}
		optionsList.setPrototypeCellValue(prototype);
		historyList.setPrototypeCellValue(prototype); 		
	}

	/** Creation of the popup's gui */
	private void createGui() {
		MouseHandler mouseHandler = new MouseHandler();
		optionsList = new JList(optionsModel);
		optionsList.addMouseMotionListener(mouseHandler);
		optionsList.addMouseListener(mouseHandler);

		optionsScrollPane = createScrollPane(optionsList);

		historyList = new JList(historyModel);
		historyList.addMouseMotionListener(mouseHandler);
		historyList.addMouseListener(mouseHandler);

		optionsList.setBorder(null);
		optionsList.setFocusable(false);
		optionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		historyList.setBorder(null);
		historyList.setFocusable(false);
		historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		ensureListRowsHeight();

		separator = new JSeparator();

		popup = new JPopupMenu();
		popup.setLayout(new BorderLayout());
		popup.setBorderPainted(true);
		popup.setOpaque(false);		
		popup.addPopupMenuListener(this);

		historyScrollPane = createScrollPane(historyList);
		popup.add(historyScrollPane, BorderLayout.NORTH);
		popup.add(separator, BorderLayout.CENTER);
		popup.add(optionsScrollPane, BorderLayout.SOUTH);
		popup.setDoubleBuffered(true);
		popup.setFocusable(false);

		listRenderer = new FilterListCellRenderer(optionsList);
		optionsList.setCellRenderer(listRenderer);
		historyList.setCellRenderer(listRenderer);
		reconfigureGui();
	}

	/** 
	 * Reconfigures the gui, ensuring the correct size of the history 
	 * and option lists 
	 **/
	private void reconfigureGui() {
		// if there is no history and no options, show still the history
		int historySize = historyModel.getSize();
		boolean showOptions = optionsModel.getSize() > 0;
		boolean showHistory = historySize > 0 || !showOptions;
		optionsScrollPane.setVisible(showOptions);
		historyScrollPane.setVisible(showHistory);
		if (showHistory) {
			historyList.setVisibleRowCount(Math.max(1, historySize));
			historyScrollPane.setPreferredSize(null);
		}
		if (showOptions) {
			optionsList.setVisibleRowCount(Math.min(optionsModel.getSize(), 
					maxVisibleRows - historySize));
			optionsScrollPane.setPreferredSize(null);
		}
		separator.setVisible(showHistory && showOptions);
	}

	
	private JScrollPane createScrollPane(JList list) {
		JScrollPane ret = new JScrollPane(list, 
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		ret.setHorizontalScrollBar(null);
		ret.setFocusable(false);
		ret.getVerticalScrollBar().setFocusable(false);
		ret.setBorder(null);
		return ret;
	}

	@Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		cancelReason=null;
	}
	
	@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		//no need to react to this event
	}
	
	@Override public void popupMenuCanceled(PopupMenuEvent e) {
		AWTEvent ev = EventQueue.getCurrentEvent();
		if (ev instanceof MouseEvent){
			cancelReason = ev.getSource();
		}
	}
	
	/**
	 * Returns the source of the event than canceled last time the popup menu 
	 */
	public boolean isMenuCanceledForMouseEvent(Object source){
		boolean ret = !popup.isVisible() && cancelReason==source;
		cancelReason=null;
		return ret;
	}

	/**
	 * The mouse handler will select automatically the option under the mouse,
	 * and passes directly the focus to the popup under the mouse
	 */
	final class MouseHandler extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			setPopupFocused(true);
			listSelection(focusedList.getSelectedValue());
		}

		@Override public void mouseMoved(MouseEvent e) {
			setPopupFocused(true);
			JList focus = (JList) e.getSource();
			JList other = (focus == optionsList) ? historyList : optionsList;
			focus.setSelectedIndex(focus.locationToIndex(e.getPoint()));
			if (other.getModel().getSize() > 0) {
				other.setSelectedIndex(0); // silly, but needed
				other.clearSelection();
			}
			focusedList = focus;
		}

		private void listSelection(Object object) {
			optionSelected(object);
			hide();
		}
	}

	/**
	 * Class to find matches in the lists (history / options)
	 */
	static class Match {
		// index in the list. Will be -1 if the content is empty or a fullMatch
		// is required and cannot be found
		int index;
		// length of the string that is matched. On fullMatch calls, this length
		// is the passed string's length
		int len;
		// exact is true if the given index corresponds to an string that fully
		// matches the passed string
		boolean exact;

		public Match() {
			//default constructor, required
		}

		public Match(int index) {
			this.index = index;
			exact = index != -1;
		}

		public static Match findOnUnsortedContent(List content, int len, 
				Comparator strComparator, String strStart, boolean fullMatch) 
		{
			Match ret = new Match(-1);
			while (--len>0){
				int matchLen = getMatchingLength(strStart, content.get(len).toString(), strComparator);
				if (matchLen>ret.len){
					ret.index=len;
					ret.len=matchLen;
					if (matchLen==strStart.length()){
						ret.exact=true;
						return ret;
					}
				}
			}
			if (fullMatch){
				ret.index=-1;
				ret.len=0;
			}
			return ret;
		}

		/** Returns the number of characters matching between two strings */
		public static int getMatchingLength(String a, String b, Comparator stringComparator){
			int max = Math.min(a.length(), b.length());
			for (int i=0; i<max; i++){
				if (0!=stringComparator.compare(a.substring(i, i+1), b.substring(i, i+1))){
					return i;
				}
			}
			return max;
		}
		
	}
}