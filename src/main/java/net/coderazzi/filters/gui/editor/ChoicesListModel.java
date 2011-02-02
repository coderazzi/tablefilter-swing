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

import java.text.Collator;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListCellRenderer;

import net.coderazzi.filters.gui.CustomChoice;


/**
 * List model to handle the choices in the popup menu.<br>
 * When the user specifies a {@link ListCellRenderer}, choices are 
 * considered non-text; otherwise, content is converted, if needed, 
 * to Strings, and sorted.<br>
 * This class includes functionality to find the best match for a given
 * string, returning the choice that more closely matches the input. This
 * functionality works only for text input (i.e., no {@link ListCellRenderer}
 * specified)<br>
 * The model handles also specifically {@link CustomChoice} instances, 
 * which are placed at the beginning of the list, sorted by their own
 * precedence.<br>
 * By design, it is not expected to have many {@link CustomChoice} instances
 * associated to an editor; searching for a custom choice from a given string
 * is done therefore linearly -with obvious performance drawbacks in cases
 * with many many custom choices-.
 */
public class ChoicesListModel extends AbstractListModel {

	private static final long serialVersionUID = 3523952153693100563L;
	private List content;
	private Format format;
	private int customChoices;
	boolean useFormatter;
	Comparator comparator;
	
	public ChoicesListModel() {
		this.content = new ArrayList();
		setStringContent(null, Collator.getInstance());
	}
	
	@Override public int getSize() {
		return content.size();
	}

	@Override public Object getElementAt(int i) {
		return content.get(i);
	}

	public void clearContent() {
		int size = getSize();
		if (size > 0) {
			customChoices = 0;
			content.clear();
			fireIntervalRemoved(this, 0, size);
		}
	}
	
	public boolean isEmpty(){
		return content.isEmpty();
	}

	/** Returns true if the object is a valid choice (as object, or string) */
	public boolean isValidChoice(Object o){
		return content.contains(o);
	}
	
	/** Returns the current choices */
	public Collection<?> getChoices(){
		return content;
	}

	/** Returns the CustomChoice matching the given text, if any */ 
	public CustomChoice getCustomChoice(String s){
		for (int i=0; i<customChoices;i++){
			CustomChoice cc=(CustomChoice) content.get(i);
			if (0==comparator.compare(s, cc.toString())){
				return cc;
			}
		}
		return null;
	}

	/** @see PopupComponent#selectBestMatch(Object, boolean) */
	public PopupComponent.Match getClosestMatch(Object hint, boolean exact) {
		return (useFormatter && (hint instanceof String))? 
				findOnSortedContent((String)hint, exact) : 
					new PopupComponent.Match(content.indexOf(hint));
	}

	/** Specifies that the content is to be handled as strings */
	public boolean setStringContent(Format format, Comparator stringComparator) {
		boolean ret = !useFormatter || format!=this.format || comparator!=stringComparator;
		if (ret){
			useFormatter=true;
			this.format=format;
			this.comparator=stringComparator;
			clearContent();
		}
		return ret;
	}

	/** Specifies that the content requires no conversion to strings */
	public boolean setRenderedContent(Comparator classComparator) {
		boolean ret = useFormatter || comparator!=classComparator;
		if (ret){
			useFormatter=false;
			this.format=null;
			this.comparator=classComparator;
			clearContent();
		}
		return ret;
	}

	/** 
	 * Adds additional choices.<br>
	 * If the content is text-based, the choices are converted into Strings, 
	 * and sorted.<br>
	 * Otherwise, no sorting is performed, although duplicates are still
	 * discarded
	 * @return true if there are any changes after the operation
	 */
	public boolean addContent(Collection addedContent) {
		boolean changed=false;
		for (Object o : addedContent){
			if (!(o instanceof CustomChoice)){
				if (o==null){
					o=CustomChoice.MATCH_EMPTY;
				} else if (useFormatter){
					String s = format==null? o.toString() : format.format(o);	
					o = s.length()==0? CustomChoice.MATCH_EMPTY : s;					
				}
			}
			changed=addContent(o) || changed;
		}
		if (changed){
			addContent(CustomChoice.MATCH_ALL);
			fireContentsChanged(this, 0, getSize());
		}
		return changed;
	}
	
	private boolean addContent(Object o){
		//using the wrapper comparator to handle also CustomChoices
		int pos = Collections.binarySearch(content, o, wrapperComparator);
		if (pos<0){
			content.add(-1-pos, o);
			if (o instanceof CustomChoice){
				customChoices++;
			}
			return true;
		}
		return false;
	}
	
	/** Creation of the Match, for text based, sorted content */
	private PopupComponent.Match findOnSortedContent(String strStart, 
			                                         boolean fullMatch) {
		PopupComponent.Match ret;
		if (content.isEmpty()) {
			ret = new PopupComponent.Match(-1);
		} else {
			ret = PopupComponent.Match.findOnUnsortedContent(
				content, customChoices, comparator, strStart, fullMatch);
			if (!ret.exact){
				int pos = Collections.binarySearch(
						content.subList(customChoices, content.size()), 
						strStart, 
						comparator);
				if (pos>=0){
					//found it, do nothing else
					ret.exact=true;
					ret.index=customChoices + pos;
				} else if (!fullMatch){
					//try the two positions around
					int suggested=customChoices-pos-1;
					if (suggested<content.size()){
						int len = getMatchingLength(strStart, suggested);
						if (len>ret.len || ret.len==0){
							ret.index=suggested;
							ret.len=len;
						}
					}
					if (--suggested>=customChoices){
						int len = getMatchingLength(strStart, suggested);
						if (len>ret.len || ret.len==0){
							ret.index=suggested;
							ret.len=len;
						}
					}
					if (ret.index>=customChoices){
						ret.exact = ret.len==content.get(ret.index).toString().length();
					}
				}
			}
		}
		return ret;
	}
	
	/**
	 * Returns the number of characters matching between 
	 * content[contentPosition] and target
	 */
	private int getMatchingLength(String target, int contentPosition){
		return PopupComponent.Match.getMatchingLength(target, 
				content.get(contentPosition).toString(),
				comparator);
	}
	
	private Comparator wrapperComparator = new Comparator() {
		@Override public int compare(Object o1, Object o2) {
			if (o1 instanceof CustomChoice){
				if (o2 instanceof CustomChoice){
					CustomChoice c1=(CustomChoice)o1;
					CustomChoice c2=(CustomChoice)o2;
					int ret=c1.getPrecedence() - c2.getPrecedence();
					if (ret==0){
						if (useFormatter){
							//in this case, the comparator is string comparator
							ret=comparator.compare(c1.toString(), c2.toString());
						} else {
							ret = o1.hashCode()-o2.hashCode();
						}
					}
					return ret;
				}
				return -1;
			} else if (o2 instanceof CustomChoice){
				return 1;
			}
			return comparator.compare(o1, o2);
		}
	};

}
