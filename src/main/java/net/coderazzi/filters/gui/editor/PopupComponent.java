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
 * which contains the history and the choices list.
 */
abstract class PopupComponent implements PopupMenuListener{

	/** Minimum number of visible choices -if there are choices- */
	private static final int MIN_VISIBLE_CHOICES = 4; 
	private JPopupMenu popup;
	private FilterListCellRenderer listRenderer;
	private JScrollPane choicesScrollPane;
	private JScrollPane historyScrollPane;
	private JSeparator separator;	

	private ChoicesListModel choicesModel;
	private HistoryListModel historyModel;
	/** 
	 * cancelReason contains the source of the event that 
	 * canceled last time the popup menu 
	 **/
	private Object cancelReason;

	/** This is the total max number of visble rows (history PLUS choices) */
	private int maxVisibleRows = FilterSettings.maxVisiblePopupRows;
	/** 
	 * Max history is the maximum number of elements in the history list 
	 * when there are NO choices 
	 **/
	private int maxHistory = FilterSettings.maxPopupHistory; 

	/** focusedList always refer to one of choicesList or historyList */
	JList focusedList;
	JList choicesList;
	JList historyList;


	public PopupComponent() {
		choicesModel = new ChoicesListModel();
		historyModel = new HistoryListModel();
		setMaxHistory(maxHistory);
		createGui();
	}

	/** Invoked when the user select an element in the choices or history lists*/
	protected abstract void choiceSelected(Object selection);
	
	/** Returns the current selection -can be history or and choices-*/
	public Object getSelection() {
		return focusedList.getSelectedValue();
	}
	
	/** Returns true if the current selection belongs to the history*/
	public boolean isHistorySelection(){
		return focusedList==historyList;
	}

	/** Returns true if the passed object matches an existing choice */
	public boolean isValidChoice(Object object){
		return choicesModel.isValidChoice(object);		
	}
	
	/** Returns true if the popup has any content to display */
	public boolean hasContent(){
		return !choicesModel.isEmpty() || !historyModel.isEmpty();
	}

	/** Returns true if the popup contains choices (history notwithstanding) */
	public boolean hasChoices() {
		return !choicesModel.isEmpty();
	}

	/** Returns the custom choice matching the given text */
	public CustomChoice getCustomChoice(String s){
		return choicesModel.getCustomChoice(s);
	}

	/** Returns the current choices */
	public Collection<?> getChoices(){
		return choicesModel.getChoices();
	}
	
	/** 
	 * Adds content to the choices list.<br>
	 * If there is no {@link ListCellRenderer} defined,
	 * the content is stringfied and sorted -so duplicates are removed-
	 * @returns true if the operation implies a change
	 */
	public boolean addChoices(Collection<?> choices) {
		if (choicesModel.addContent(choices)){
			fixMaxHistory();
			reconfigureGui();
			return true;
		}
		return false;
	}

	/** Adds content to the history list */
	public void addHistory(Object st) {
		if (historyModel.add(st)) {
			reconfigureGui();
		}
	}

	/** Clears both the history and the choices lists */
	public void clear() {
		choicesModel.clearContent();
		historyModel.clear();
		fixMaxHistory();
		reconfigureGui();
	}

	/** Returns true if the popup is currently visible */
	public boolean isVisible() {
		return popup.isVisible();
	}
	
	/**
	 * Displays the popup, if there is content (history or choices), 
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
		configurePaneSize(choicesScrollPane, width);
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
	 * Creates an EditorComponent that can display content with 
	 * the same renderer used to display the choices
	 */
	public EditorComponent createRenderedEditorComponent(FilterEditor editor) {
		return new EditorComponent.Rendered(editor, listRenderer);
	}

	/** Returns the current {@link ListCellRenderer} */
	public ListCellRenderer getListCellRenderer() {
		return listRenderer.getUserRenderer();
	}

	/** Specifies that the content requires no conversion to strings */
	public void setRenderedContent(ListCellRenderer renderer, Comparator classComparator) {
		listRenderer.setUserRenderer(renderer);
		if (choicesModel.setRenderedContent(classComparator)){
			historyModel.setStringContent(null);
			reconfigureGui();
		}
	}

	/** Specifies that the content is to be handled as strings */
	public void setStringContent(Format format, Comparator stringComparator) {
		listRenderer.setUserRenderer(null);
		if (choicesModel.setStringContent(format, stringComparator)){
			historyModel.setStringContent(stringComparator);
			reconfigureGui();
		}
	}
	
	/**  
	 * Returns the string comparator<br> 
	 * it will be invalid (null or not string comparator) for rendered content
	 */
	public Comparator<String> getStringComparator(){
		return historyModel.getStringComparator();
	}

	/**
	 * Finds -and selects- the best match to a given content, 
	 * using the existing history and choices.<br>
	 * It always favor content belonging to the choices list, 
	 * rather than to the history list.
	 * @param hint an object used to select the match. 
	 *  If the content is text-based (there is no {@link ListCellRenderer} 
	 *  defined), the hint is considered the start of the string, and the best
	 *  match should start with the given hint). 
	 *  If the content is not text-based, only exact matches are returned,
	 *  no matter the value of the parameter perfectMatch
	 * @param perfectMatch when the content is text-based, if no choice/history 
	 *  starts with the given hint string, smaller hint substrings are used, 
	 *  unless perfectMatch is true
	 */
	public String selectBestMatch(Object hint, boolean perfectMatch) {
		Match historyMatch = historyModel.getClosestMatch(hint, perfectMatch);
		if (choicesModel.getSize() > 0) {
			Match match = choicesModel.getClosestMatch(hint, 
					perfectMatch || historyMatch.exact);
			if (isVisible() && match.index >= 0) {
				choicesList.ensureIndexIsVisible(match.index);
			}
			if (match.exact || (!historyMatch.exact 
					&& (match.len >= historyMatch.len))) {
				if (match.index >= 0) {
					if (isVisible()) {
						focusChoices();
						select(match.index);
					}
					return choicesModel.getElementAt(match.index).toString();
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

	/** @see IFilterEditor#setMaxVisibleRows(int) */
	public void setMaxVisibleRows(int maxVisibleRows) {
		this.maxVisibleRows = Math.max(MIN_VISIBLE_CHOICES, maxVisibleRows);
		fixMaxHistory();
		reconfigureGui();
	}

	/** @see IFilterEditor#getMaxVisibleRows() */
	public int getMaxVisibleRows() {
		return maxVisibleRows;
	}

	/** @see IFilterEditor#setMaxHistory(int) */
	public void setMaxHistory(Integer size) {
		this.maxHistory = size==null? FilterSettings.maxPopupHistory : size;
		if (fixMaxHistory()) {
			reconfigureGui();
		}
	}

	/** @see IFilterEditor#getMaxHistory() */
	public int getMaxHistory() {
		return maxHistory;
	}
	
	/**
	 * Selects the first element in the focused list. If it is already on the
	 * first element, or forceJump is true, selects the first element on the
	 * history list.<br>
	 * Returns true if there is indeed a change (or forceJump is true)
	 */
	public boolean selectFirst(boolean forceJump) {
		boolean ret = canSwitchToHistory() 
			&& (forceJump || choicesList.getSelectedIndex() == 0);
		if (ret) {
			focusHistory();
		}
		return select(0) || ret;
	}

	/**
	 * Selects the last element in the focused list. If it is already on the
	 * last element, or forceJump is true, selects the last element on the
	 * choices list.<br>
	 * Returns true if there is indeed a change (or forceJump is true)
	 */
	public boolean selectLast(boolean forceJump) {
		boolean ret = canSwitchToChoices() && (forceJump 
				|| historyList.getSelectedIndex() == historyModel.getSize() - 1);
		if (ret) {
			focusChoices();
		}
		return select(focusedList.getModel().getSize() - 1) || ret;
	}

	/**
	 * If jumpRequired is true, or cannot move up on the focused list and the
	 * focused list is the choices list, then move to the last element on the
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
	 * on the choices list.<br>
	 * Otherwise, it just returns false
	 */
	public void selectDown(boolean jumpRequired) {
		if (jumpRequired || !select(focusedList.getSelectedIndex() + 1)) {
			if (canSwitchToChoices()) {
				focusChoices();
				select(choicesList.getFirstVisibleIndex());
			}
		}
	}

	/** Moves down a page, or to the last element in the choices list, if needed*/
	public void selectDownPage() {
		if (isFocusInHistory()) {
			if (canSwitchToChoices()) {
				focusChoices();
			}
			select(focusedList.getLastVisibleIndex());
		} else {
			int last = choicesList.getLastVisibleIndex();
			if (last == choicesList.getSelectedIndex()) {
				last = Math.min(last + last - choicesList.getFirstVisibleIndex(),
						choicesModel.getSize() - 1);
			}
			select(last);
		}
	}

	/** Moves up a page, or to the first element in the history list, if needed*/
	public void selectUpPage() {
		int select = 0;
		if (!isFocusInHistory()) {
			int selected = choicesList.getSelectedIndex();
			if (canSwitchToHistory() && selected == 0) {
				focusHistory();
			} else {
				select = choicesList.getFirstVisibleIndex();
				if (select == selected) {
					select = Math.max(0, 
							select + select - choicesList.getLastVisibleIndex());
				}
			}
		}
		select(select);
	}

	/** Sets the list's background color */
	public void setBackground(Color color){
		choicesList.setBackground(color);
		historyList.setBackground(color);
	}
	
	/** Sets the list's foreground color */
	public void setForeground(Color color){
		choicesList.setForeground(color);
		historyList.setForeground(color);
	}
	
	/** Sets the list's selected background color */
	public void setSelectionBackground(Color color){
		choicesList.setSelectionBackground(color);
		historyList.setSelectionBackground(color);
	}
	
	/** Sets the list's selected foreground color */
	public void setSelectionForeground(Color color){
		choicesList.setSelectionForeground(color);
		historyList.setSelectionForeground(color);
	}
	
	/** Gets the list's selected background color */
	public Color getSelectionBackground(){
		return choicesList.getSelectionBackground();
	}
	
	/** Gets the list's selected foreground color */
	public Color getSelectionForeground(){
		return choicesList.getSelectionForeground();
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
		choicesList.setFont(font);
		historyList.setFont(font);
		ensureListRowsHeight();
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
	 * Returns true if the focused list is the choices list 
	 * and there is history content 
	 **/
	private boolean canSwitchToHistory() {
		return focusedList == choicesList && !historyModel.isEmpty();
	}

	/** 
	 * Returns true if the focused list is the history list 
	 * and there is choices content 
	 **/
	private boolean canSwitchToChoices() {
		return focusedList == historyList && choicesScrollPane.isVisible();
	}

	/** Internal method to calculate the real history size used */
	private boolean fixMaxHistory(){
		int now = historyModel.getMaxHistory();
		int choicesSize = choicesModel.getSize();
		int finalSize = choicesSize==0? maxVisibleRows 
				: Math.min(maxHistory, maxVisibleRows 
						- Math.min(choicesSize, MIN_VISIBLE_CHOICES));
		return finalSize==now? false : historyModel.setMaxHistory(finalSize);
	}

	/** Moves the focus to the history list */
	private void focusHistory() {
		choicesList.clearSelection();
		focusedList = historyList;
	}

	/** Moves the focus to the choices list */
	private void focusChoices() {
		historyList.clearSelection();
		focusedList = choicesList;
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
			prototype = choicesList.getPrototypeCellValue();
			//we need to change the prototype. The jlist will not update its
			//cell height if the prototype does not change
			prototype = "X".equals(prototype) ? "Z" : "X";
		} else {
			prototype=null;
		}
		choicesList.setPrototypeCellValue(prototype);
		historyList.setPrototypeCellValue(prototype); 		
	}

	/** Creation of the popup's gui */
	private void createGui() {
		MouseHandler mouseHandler = new MouseHandler();
		choicesList = new JList(choicesModel);
		choicesList.addMouseMotionListener(mouseHandler);
		choicesList.addMouseListener(mouseHandler);

		choicesScrollPane = createScrollPane(choicesList);

		historyList = new JList(historyModel);
		historyList.addMouseMotionListener(mouseHandler);
		historyList.addMouseListener(mouseHandler);

		choicesList.setBorder(null);
		choicesList.setFocusable(false);
		choicesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
		popup.add(choicesScrollPane, BorderLayout.SOUTH);
		popup.setDoubleBuffered(true);
		popup.setFocusable(false);

		listRenderer = new FilterListCellRenderer(choicesList);
		choicesList.setCellRenderer(listRenderer);
		historyList.setCellRenderer(listRenderer);
		reconfigureGui();
	}

	/** 
	 * Reconfigures the gui, ensuring the correct size of the history 
	 * and choices lists 
	 **/
	private void reconfigureGui() {
		// if there is no history and no choices, show still the history
		int historySize = historyModel.getSize();
		boolean showChoices = choicesModel.getSize() > 0;
		boolean showHistory = historySize > 0 || !showChoices;
		choicesScrollPane.setVisible(showChoices);
		historyScrollPane.setVisible(showHistory);
		if (showHistory) {
			historyList.setVisibleRowCount(Math.max(1, historySize));
			historyScrollPane.setPreferredSize(null);
		}
		if (showChoices) {
			choicesList.setVisibleRowCount(Math.min(choicesModel.getSize(), 
					maxVisibleRows - historySize));
			choicesScrollPane.setPreferredSize(null);
		}
		separator.setVisible(showHistory && showChoices);
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
	 * The mouse handler will select automatically the choice under the mouse,
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
			JList other = (focus == choicesList) ? historyList : choicesList;
			focus.setSelectedIndex(focus.locationToIndex(e.getPoint()));
			if (other.getModel().getSize() > 0) {
				other.setSelectedIndex(0); // silly, but needed
				other.clearSelection();
			}
			focusedList = focus;
		}

		private void listSelection(Object object) {
			choiceSelected(object);
			hide();
		}
	}

	/**
	 * Class to find matches in the lists (history / choices)
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
			int strLen=strStart.length();
			Match ret = new Match(-1);
			while (len-->0){
				String use=content.get(len).toString();
				int matchLen = getMatchingLength(strStart, use, strComparator);
				if (matchLen>ret.len || ret.len==0){
					ret.index=len;
					ret.len=matchLen;
					if (matchLen==strLen && use.length()==strLen){
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
            	char f = a.charAt(i);
            	char s = b.charAt(i);
                if (f!=s && stringComparator.compare(String.valueOf(f), String.valueOf(s))!=0) {
                    return i;
                }
			}
			return max;
		}
		
	}
}