package net.coderazzi.filters.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.JTableHeader;

import net.coderazzi.filters.gui.TableFilterHeader.Position;

/**
 * <p>Helper class to locate the filter header on the expected place by the table header</p>
 * 
 * @author  Luis M Pena - dr.lu@coderazzi.net
 */
class PositionHelper implements PropertyChangeListener{

    /** This variable defines how to handle the position of the header */
    Position location;

    /**
     * The viewport associated to this header. It is null if the location is not
     * automatically managed
     */
    JViewport headerViewport;
    
    /**
     * The previous viewport of the associated table
     */
    Component previousTableViewport;
    
    /**
     * The handled filter header
     */
    TableFilterHeader filterHeader;
    
    public PositionHelper(TableFilterHeader filterHeader) {
    	this.filterHeader = filterHeader;
	}
    
    
    /**
     * <p>Defines the behaviour of the header concerning its position related to the table.</p>
     */
    public void setPosition(Position location) {
        this.location = location;
    	placeTableHeader(removeTableHeader());
    }


    /**
     * Returns the mode currently associated to the TableHeader
     */
    public Position getPosition() {
        return location;
    }

    /**
     * The filter header reports that the table being handled is going to change
     */
    public void changeTable(JTable oldTable, JTable newTable){
        if (oldTable!=null){
        	oldTable.removePropertyChangeListener("ancestor", this);
        }
    	cleanUp();
    	if (newTable!=null){
    		newTable.addPropertyChangeListener("ancestor", this);
   			trySetUp(newTable);
    	}
    }

    /**
     * Method automatically invoked when the class ancestor changes
     */
    public void filterHeaderContainmentUpdate() {
    	if (!canHeaderLocationBeManaged()){
    		cleanUp();
    	}
    }
    
    /**
     * PropertyChangeListener interface
     */
	public void propertyChange(PropertyChangeEvent evt) {
		//the table has changed containment. clean up status and prepare again, if possible
		//However, do nothing if the current setup is fine
		if (previousTableViewport!=evt.getNewValue() || evt.getSource()!=filterHeader.getTable()){
			previousTableViewport=null;
			cleanUp();
			trySetUp(filterHeader.getTable());
		}
	}

    /**
     * Returns true if the header location can be automatically controlled
     * @return false if the component has been explicitly included in a container
     */
    private boolean canHeaderLocationBeManaged(){
    	Container parent = filterHeader.getParent();
    	return parent==null || parent==headerViewport;
    }
    
    
    /**
     * Tries to setup the filter header automatically for the given table
     */
    private void trySetUp(JTable table){
    	if (table!=null && canHeaderLocationBeManaged()){
	        Container p = table.getParent();
	        if (p instanceof JViewport) {
	            Container gp = p.getParent();
	            if (gp instanceof JScrollPane) {
	                JScrollPane scrollPane = (JScrollPane)gp;
	                JViewport viewport = scrollPane.getViewport();
	                if (viewport != null && viewport.getView() == table) {
	                	setUp(scrollPane);
	                	previousTableViewport=p;
	                }
	            }
	        }
    	}    	
    }
    
    /**
     * Sets up the header, placing it on a new viewport for the given Scrollpane
     */
    private void setUp(JScrollPane scrollPane){
        headerViewport=new JViewport(){

        	private static final long serialVersionUID = 7109623726722227105L;

			@Override
        	public void setView(Component view) {
        		if (view instanceof JTableHeader){
        			//if the view is not a table header, somebody is doing
        			//funny stuff. not much to do!
	        		removeTableHeader();
	    			placeTableHeader(view);
	        		super.setView(filterHeader);
        		} 
        	}
        };
        JViewport currentColumnHeader=scrollPane.getColumnHeader();
        if (currentColumnHeader!=null){
        	//this happens if the table has not been yet added to the scrollPane
        	Component view = currentColumnHeader.getView();
        	if (view!=null){
        		headerViewport.setView(view);
        	}
        }
        scrollPane.setColumnHeader(headerViewport);
    }
    
    /**
     * Removes the current viewport, setting it up to clean status
     */
    private void cleanUp(){
    	JViewport currentViewport=headerViewport;
    	headerViewport=null;
    	if (currentViewport!=null){
    		currentViewport.remove(filterHeader);
	    	Container parent=currentViewport.getParent();
	    	if (parent instanceof JScrollPane){
	    		JScrollPane scrollPane=(JScrollPane)parent;
	    		if (scrollPane.getColumnHeader()==currentViewport){
			    	Component tableHeader=getTableHeader();
	    			JViewport newView = tableHeader==null? null : createCleanViewport(tableHeader);
	    			scrollPane.setColumnHeader(newView);
	    		}
	    	}
    	}
    }
    
    /**
     * Creates a simple JViewport with the given component as view
     */
    private JViewport createCleanViewport(Component tableHeader){
    	JViewport ret = new JViewport();
		ret.setView(tableHeader);
		return ret;
    }
    
    /**
     * Locates the passed component in the header, with the position depending on the current header location
     * @param header the table header, added to the current filter header
     */
    private void placeTableHeader(Component header){
    	if (header!=null){
    		filterHeader.add(header, location==Position.TOP? BorderLayout.SOUTH : BorderLayout.NORTH);
    		filterHeader.revalidate();
    	}
	}
    
    /**
     * Removes the current JTableHeader in the filterHeader, returning it.
     * it does nothing if there is no such JTableHeader
     */
    private Component removeTableHeader(){
    	Component tableHeader=getTableHeader();
    	if (tableHeader!=null){
    		filterHeader.remove(tableHeader);
    	}
    	return tableHeader;
	}
    
    /**
     * Returns the JTableHeader in the filterHeader, if any
     */
    private Component getTableHeader(){
    	for (Component component : filterHeader.getComponents()){
    		//there should be just one (the header's controller) 
    		//or two components (with the table header)
    		if (component instanceof JTableHeader){
    			return component;
    		}
    	}    	
    	return null;
    }

}
