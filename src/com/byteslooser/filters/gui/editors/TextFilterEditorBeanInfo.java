package com.byteslooser.filters.gui.editors;

import java.beans.SimpleBeanInfo;

public class TextFilterEditorBeanInfo extends SimpleBeanInfo{
    
    public java.awt.Image getIcon(int iconKind) {
        return loadImage("/com/byteslooser/filters/resources/textEditor.png");
    }

}
