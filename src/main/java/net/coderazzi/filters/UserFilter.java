package net.coderazzi.filters;

import net.coderazzi.filters.gui.TableFilterHeader;


/**
 * Commodity class to implement a custom user filter. 
 * @author  Luis M Pena - lu@coderazzi.net
 */
public abstract class UserFilter extends BaseFilter{
	
	private TableFilter filter;

	/** A UserFilter is expected to be associated to a {@link TableFilter}*/
	public UserFilter(TableFilterHeader filterHeader){
		setTableFilter(filterHeader);
	}
	
	/** A UserFilter is expected to be associated to a {@link TableFilter}*/
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
			filter.removeFilter(this);
			if (enable){
				filter.addFilter(this);			
			}
		}
	}

}
