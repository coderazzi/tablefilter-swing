package net.coderazzi.filters;

import net.coderazzi.filters.gui.TableFilterHeader;

/**
 * Commodity class to implement an added user filter. 
 * @author  Luis M Pena - dr.lu@coderazzi.net
 */
public abstract class UserFilter extends AbstractObservableRowFilter{
	
	private TableFilter filter;
	
	public UserFilter(TableFilterHeader filterHeader){
		setTableFilter(filterHeader);
	}
	
	public UserFilter(TableFilter filter){
		setTableFilter(filter);
	}
	
	public void setTableFilter(TableFilter filter){
		this.filter=filter;
	}
	
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
