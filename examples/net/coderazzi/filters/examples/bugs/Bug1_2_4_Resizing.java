package net.coderazzi.filters.examples.bugs;

import java.awt.BorderLayout;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.UIManager;

import net.coderazzi.filters.examples.utils.TestTableModel;

/**
 * Test to verify some resizing problems happening on version 1.2.4
 * ->maximizing the internal frame would not render correctly the filter header
 */
public class Bug1_2_4_Resizing extends JFrame{
	
	private static final long serialVersionUID = 851460277109186791L;

	Bug1_2_4_Resizing(){
		super("Bug 1_2_4");
		createGui();
		setSize(1000, 800);
	}
	
	
	void createGui()
	{
		JDesktopPane desktopPane = new JDesktopPane();
		setContentPane(desktopPane);
		JInternalFrame frame = new JInternalFrame("Bug", true, true, true);
		frame.setIconifiable(true);
		desktopPane.add(frame);

		Bug1_2_4_ResizingView view = new Bug1_2_4_ResizingView();
		frame.getContentPane().add(view, BorderLayout.CENTER);
		view.getTable().setModel(TestTableModel.createTestTableModel(100));
		view.getTableFilterHeader().setTable(view.getTable());

		frame.setSize(800, 600);
		frame.setLocation(50, 50);
		frame.setVisible(true);		
	}
		



	
    public final static void main(String[] args) {

        try {
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Bug1_2_4_Resizing frame = new Bug1_2_4_Resizing();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
	
}
