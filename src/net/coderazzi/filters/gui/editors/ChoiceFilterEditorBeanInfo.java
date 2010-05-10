package net.coderazzi.filters.gui.editors;

import java.beans.SimpleBeanInfo;

public class ChoiceFilterEditorBeanInfo extends SimpleBeanInfo{
    
    @Override
	public java.awt.Image getIcon(int iconKind) {
        return loadImage("/net/coderazzi/filters/resources/choiceEditor.png");
    }

}
