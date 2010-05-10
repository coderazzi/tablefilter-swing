package net.coderazzi.filters;

import net.coderazzi.filters.gui.TableFilterHeader;

/**
 * Commodity class to implement an added user filter. 
 * @author  Luis M Pena - lu@coderazzi.net
 * @since version 1.5.0
 */
public abstract class UserFilter extends AbstractObservableRowFilter{
	
	private TableFilter filter;
	
	public UserFilter(TableFilterHeader filterHeader){
		setTableFilter(filterHeader);
	}
	
	public UserFilter(TableFilter filter){
		setTableFilter(filter);
	}
	
	/**
	 * Sets the associated filter, enabling automatically this user filter
	 */
	public void setTableFilter(TableFilter filter){
		setEnabled(false);
		this.filter=filter;
		setEnabled(true);
	}
	
	/**
	 * Sets the associated filter, enabling automatically this user filter
	 */
	public void setTableFilter(TableFilterHeader filterHeader){
		setTableFilter(filterHeader.getTableFilter());
	}
	
	/**
	 * Enables/disables the current filter
	 */
	public void setEnabled(boolean enable){
		if (filter!=null){
			filter.removeFilterObservable(this);
			if (enable){
				filter.addFilterObservable(this);			
			}
		}
	}

}
