package com.byteslooser.filters.gui;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class TableFilterHeaderBeanInfo extends SimpleBeanInfo{
    
    @Override
	public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor desc = new BeanDescriptor(TableFilterHeader.class);
        desc.setValue("isContainer", Boolean.FALSE);
        return desc;
    }
    
    @Override
	public java.awt.Image getIcon(int iconKind) {
        return loadImage("/com/byteslooser/filters/resources/tableFilterHeader.png");
    }

}
