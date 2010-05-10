package net.coderazzi.filters.gui.editors;

import java.util.HashMap;
import java.util.Map;

import javax.swing.RowFilter;

import net.coderazzi.filters.IFilterObservable;
import net.coderazzi.filters.IFilterObserver;
import net.coderazzi.filters.gui.ITableFilterEditor;
import net.coderazzi.filters.gui.ITableFilterEditorObserver;

/**
 * Helper class to handle Observers in the table editors
 *
 * @author  Luis M Pena - sen@coderazzi.net
 */
class ObserverHelper {

	private ITableFilterEditor owner;
	private Map<ITableFilterEditorObserver, IFilterObserver> observers;
	
	public ObserverHelper(ITableFilterEditor owner) {
		this.owner = owner;
		this.observers = new HashMap<ITableFilterEditorObserver, IFilterObserver>();
	}

    public void addTableFilterObserver(final ITableFilterEditorObserver observer) {
    	if (!observers.containsKey(observer)){
	    	IFilterObserver filterObserver = new IFilterObserver() {
				
				@Override
				public void filterUpdated(IFilterObservable obs, RowFilter newValue) {
					observer.tableFilterUpdated(owner, owner.getFilter());
				}
			};
			
			observers.put(observer, filterObserver);
	
	    	owner.getFilterObservable().addFilterObserver(filterObserver);
    	}
    }
    
    public void removeTableFilterObserver(ITableFilterEditorObserver observer) {
    	IFilterObserver filterObserver = observers.remove(observer);
    	if (filterObserver!=null){
    		owner.getFilterObservable().removeFilterObserver(filterObserver);
    	}
    }    

}
